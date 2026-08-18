/**
 * @file SesionActivaViewModel.kt
 * @brief ViewModel de la sesión de entrenamiento en vivo de la aplicación GYM.
 * Gestiona el estado de la sesión activa: rutina cargada, ejercicios resueltos
 * a su máquina real, series registradas en vivo, cronómetro total de la sesión,
 * cronómetro de descanso con avisos y finalización con resumen estadístico.
 */
package com.gym.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.EjercicioConMaquina
import com.gym.app.domain.model.Rutina
import com.gym.app.domain.model.SerieRealizada
import com.gym.app.domain.repository.RepositorioSerieRealizada
import com.gym.app.domain.usecase.entrenamiento.CalcularCargaSugeridaCasoUso
import com.gym.app.domain.usecase.entrenamiento.CalcularResumenSesionCasoUso
import com.gym.app.domain.usecase.entrenamiento.EditarSerieCasoUso
import com.gym.app.domain.usecase.entrenamiento.EliminarSerieCasoUso
import com.gym.app.domain.usecase.entrenamiento.FinalizarSesionActivaCasoUso
import com.gym.app.domain.usecase.entrenamiento.ObtenerRutinaPorIdCasoUso
import com.gym.app.domain.usecase.entrenamiento.PrepararSesionActivaCasoUso
import com.gym.app.domain.usecase.entrenamiento.RegistrarSerieCasoUso
import com.gym.app.domain.usecase.entrenamiento.ResumenSesion
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * @data class EstadoSesionActiva
 * @brief Estado inmutable de la sesión de entrenamiento en vivo.
 *
 * @property rutina Rutina que se está ejecutando (null mientras carga o si falla).
 * @property ejercicios Ejercicios de la sesión resueltos con su máquina real.
 * @property series Series registradas en vivo durante la sesión (flujo reactivo).
 * @property sesionId Identificador único de la sesión activa (generado al iniciar).
 * @property segundosTranscurridos Cronómetro total de la sesión en segundos.
 * @property descansoRestante Segundos restantes del descanso (0 = sin descanso activo).
 * @property descansoTotal Duración configurada del descanso actual en segundos.
 * @property descansoActivo Indica si el cronómetro de descanso está corriendo.
 * @property descansoTerminado Indica si el descanso llegó a 0 (aviso sonoro/vibración).
 * @property ejercicioActualId Ejercicio sobre el que se registró la última serie.
 * @property cargando Indica si la sesión se está preparando.
 * @property finalizada Indica si la sesión se finalizó y se calculó el resumen.
 * @property resumen Resumen estadístico de la sesión (volumen, series y 1RM).
 * @property cargasSugeridas Carga sugerida por ejercicio para el diálogo de serie.
 * @property error Mensaje de error si alguna operación falló (null en caso normal).
 */
data class EstadoSesionActiva(
    val rutina: Rutina? = null,
    val ejercicios: List<EjercicioConMaquina> = emptyList(),
    val series: List<SerieRealizada> = emptyList(),
    val sesionId: String? = null,
    val segundosTranscurridos: Long = 0L,
    val descansoRestante: Int = 0,
    val descansoTotal: Int = 0,
    val descansoActivo: Boolean = false,
    val descansoTerminado: Boolean = false,
    val ejercicioActualId: String? = null,
    val cargando: Boolean = true,
    val finalizada: Boolean = false,
    val resumen: ResumenSesion? = null,
    val cargasSugeridas: Map<String, Double> = emptyMap(),
    val error: String? = null
) {

    /** Número de series completadas (una por cada serie registrada). */
    val seriesCompletadas: Int get() = series.size

    /** Número total de series planificadas (Σ series de los bloques de la rutina). */
    val seriesTotales: Int get() = ejercicios.sumOf { it.bloque.serie }

    /** Número de ejercicios que ya han completado todas sus series prescritas. */
    val ejerciciosCompletados: Int get() = ejercicios.count { ejercicio ->
        series.count { it.ejercicioId == ejercicio.bloque.ejercicioId } >= ejercicio.bloque.serie
    }

    /** Número total de ejercicios de la sesión. */
    val totalEjercicios: Int get() = ejercicios.size
}

/**
 * @class SesionActivaViewModel
 * @brief Gestiona el estado de la sesión de entrenamiento en vivo.
 *
 * # Responsabilidades
 * - **Iniciar sesión**: carga la rutina por id, prepara los ejercicios (resolviendo
 *   cada bloque a su máquina real), genera el id de sesión y arranca el cronómetro
 *   total junto con la observación reactiva de las series.
 * - **Registrar series**: persiste la serie con [RegistrarSerieCasoUso], arranca el
 *   descanso con el tiempo prescrito del bloque y mantiene las series en vivo vía
 *   [RepositorioSerieRealizada.observarPorSesion].
 * - **Editar y eliminar series**: delegan en sus casos de uso; el flujo reactivo
 *   actualiza la lista de series automáticamente.
 * - **Descanso**: cronómetro de cuenta atrás con saltar, ajustar (+/− segundos),
 *   pausar/reanudar y aviso cuando llega a cero (la interfaz reproduce el aviso).
 * - **Finalizar sesión**: persiste la sesión resumida con [FinalizarSesionActivaCasoUso],
 *   calcula el [ResumenSesion] y marca el estado como finalizado.
 *
 * Constructores según el patrón del proyecto: primario inyectable (casos de uso,
 * repositorio, identificador de usuario y dispatcher para pruebas) y secundario
 * que resuelve las dependencias reales desde el [ContenedorDependencias].
 *
 * @property obtenerRutinaPorIdCasoUso Consulta de la rutina por su identificador.
 * @property prepararSesionActivaCasoUso Preparación de los ejercicios de la sesión.
 * @property registrarSerieCasoUso Registro de una serie realizada.
 * @property editarSerieCasoUso Edición de la carga/reps de una serie.
 * @property eliminarSerieCasoUso Eliminación de una serie con renumeración.
 * @property finalizarSesionActivaCasoUso Finalización y persistencia de la sesión.
 * @property calcularCargaSugeridaCasoUso Consulta del último kg usado por ejercicio.
 * @property calcularResumenSesionCasoUso Cálculo del resumen estadístico final.
 * @property repositorioSerieRealizada Puerto para observar las series en vivo.
 * @property userId Identificador del usuario autenticado (null en modo desarrollo).
 * @property dispatcher Dispatcher para las operaciones suspendidas (por defecto IO).
 */
class SesionActivaViewModel(
    private val obtenerRutinaPorIdCasoUso: ObtenerRutinaPorIdCasoUso,
    private val prepararSesionActivaCasoUso: PrepararSesionActivaCasoUso,
    private val registrarSerieCasoUso: RegistrarSerieCasoUso,
    private val editarSerieCasoUso: EditarSerieCasoUso,
    private val eliminarSerieCasoUso: EliminarSerieCasoUso,
    private val finalizarSesionActivaCasoUso: FinalizarSesionActivaCasoUso,
    private val calcularCargaSugeridaCasoUso: CalcularCargaSugeridaCasoUso,
    private val calcularResumenSesionCasoUso: CalcularResumenSesionCasoUso,
    private val repositorioSerieRealizada: RepositorioSerieRealizada,
    private val userId: String? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    /**
     * @brief Constructor secundario que resuelve las dependencias reales desde el
     * [ContenedorDependencias] (inyección manual). El identificador de usuario se
     * obtiene de la sesión activa de Supabase; si no hay sesión, queda `null` y se
     * usará el valor por defecto "usuario_sin_sesion" al finalizar.
     * @param contenedor Contenedor de dependencias de la aplicación.
     */
    constructor(contenedor: ContenedorDependencias) : this(
        obtenerRutinaPorIdCasoUso = contenedor.obtenerRutinaPorIdCasoUso,
        prepararSesionActivaCasoUso = contenedor.prepararSesionActivaCasoUso,
        registrarSerieCasoUso = contenedor.registrarSerieCasoUso,
        editarSerieCasoUso = contenedor.editarSerieCasoUso,
        eliminarSerieCasoUso = contenedor.eliminarSerieCasoUso,
        finalizarSesionActivaCasoUso = contenedor.finalizarSesionActivaCasoUso,
        calcularCargaSugeridaCasoUso = contenedor.calcularCargaSugeridaCasoUso,
        calcularResumenSesionCasoUso = contenedor.calcularResumenSesionCasoUso,
        repositorioSerieRealizada = contenedor.repositorioSerieRealizada,
        userId = contenedor.obtenerSesionActualCasoUso.ejecutar()?.user?.id
    )

    private val _estado = MutableStateFlow(EstadoSesionActiva())
    val estado: StateFlow<EstadoSesionActiva> = _estado.asStateFlow()

    /** Job del ticker del cronómetro total de la sesión. */
    private var tickerTotalJob: Job? = null

    /** Job del ticker de cuenta atrás del descanso. */
    private var tickerDescansoJob: Job? = null

    /** Job de observación reactiva de las series de la sesión. */
    private var jobObservacionSeries: Job? = null

    /**
     * @brief Inicia la sesión de entrenamiento en vivo para la rutina indicada.
     *
     * Carga la rutina, prepara los ejercicios (resolviendo cada bloque a su máquina
     * real), genera el id de sesión con UUID, arranca el cronómetro total, observa
     * las series de la sesión en vivo y calcula las cargas sugeridas por ejercicio.
     * Si la rutina no existe o la preparación falla, informa del error en el estado.
     *
     * @param rutinaId Identificador de la rutina a ejecutar.
     */
    fun iniciarSesion(rutinaId: String) {
        // Protección: si la sesión ya está en curso no se reinicia.
        if (_estado.value.sesionId != null) return
        _estado.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch(dispatcher) {
            val rutina = obtenerRutinaPorIdCasoUso.ejecutar(rutinaId)
            if (rutina == null) {
                _estado.update {
                    it.copy(cargando = false, error = MENSAJE_RUTINA_NO_ENCONTRADA)
                }
                return@launch
            }
            prepararSesionActivaCasoUso.ejecutar(rutina)
                .onSuccess { preparacion ->
                    val sesionId = UUID.randomUUID().toString()
                    _estado.update {
                        it.copy(
                            rutina = rutina,
                            ejercicios = preparacion.ejercicios,
                            sesionId = sesionId,
                            cargando = false,
                            error = null
                        )
                    }
                    arrancarTickerTotal()
                    observarSeriesDeLaSesion(sesionId)
                    cargarCargasSugeridas()
                }
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(
                            cargando = false,
                            error = excepcion.message ?: MENSAJE_PREPARACION_FALLIDA
                        )
                    }
                }
        }
    }

    /**
     * @brief Registra una serie realizada en el ejercicio indicado.
     *
     * Delega en [RegistrarSerieCasoUso]; si el registro tiene éxito, arranca el
     * descanso con el tiempo prescrito del bloque de ese ejercicio y recuerda el
     * ejercicio actual para la barra inferior de descanso. El flujo reactivo de
     * series actualizará la lista en vivo al persistirse.
     *
     * @param ejercicioId Identificador del ejercicio ejecutado.
     * @param pesoKg Carga levantada en kilogramos (debe ser mayor que 0).
     * @param repeticiones Repeticiones ejecutadas (deben ser al menos 1).
     */
    fun registrarSerie(ejercicioId: String, pesoKg: Double, repeticiones: Int) {
        val sesionId = _estado.value.sesionId
        if (sesionId == null) {
            _estado.update { it.copy(error = MENSAJE_SIN_SESION) }
            return
        }
        viewModelScope.launch(dispatcher) {
            registrarSerieCasoUso.ejecutar(sesionId, ejercicioId, pesoKg, repeticiones)
                .onSuccess {
                    val bloque = _estado.value.ejercicios
                        .firstOrNull { it.bloque.ejercicioId == ejercicioId }
                        ?.bloque
                    val descansoSegundos = bloque?.descansoSegundos ?: 0
                    _estado.update {
                        it.copy(
                            descansoActivo = descansoSegundos > 0,
                            descansoRestante = descansoSegundos,
                            descansoTotal = descansoSegundos,
                            descansoTerminado = false,
                            ejercicioActualId = ejercicioId,
                            error = null
                        )
                    }
                    if (descansoSegundos > 0) {
                        arrancarTickerDescanso()
                    }
                }
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(error = excepcion.message ?: MENSAJE_REGISTRO_FALLIDO)
                    }
                }
        }
    }

    /**
     * @brief Edita la carga y las repeticiones de una serie ya registrada.
     * Delega en [EditarSerieCasoUso]; el flujo reactivo actualiza la lista en vivo.
     * @param serieId Identificador de la serie a editar.
     * @param pesoKg Nueva carga en kilogramos.
     * @param repeticiones Nuevas repeticiones.
     */
    fun editarSerie(serieId: String, pesoKg: Double, repeticiones: Int) {
        viewModelScope.launch(dispatcher) {
            editarSerieCasoUso.ejecutar(serieId, pesoKg, repeticiones)
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(error = excepcion.message ?: MENSAJE_EDICION_FALLIDA)
                    }
                }
        }
    }

    /**
     * @brief Elimina una serie registrada y renumera las restantes de la sesión.
     * Delega en [EliminarSerieCasoUso]; el flujo reactivo actualiza la lista en vivo.
     * @param serieId Identificador de la serie a eliminar.
     */
    fun eliminarSerie(serieId: String) {
        viewModelScope.launch(dispatcher) {
            eliminarSerieCasoUso.ejecutar(serieId)
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(error = excepcion.message ?: MENSAJE_ELIMINACION_FALLIDA)
                    }
                }
        }
    }

    /**
     * @brief Salta el descanso actual: detiene el ticker y limpia el descanso.
     */
    fun saltarDescanso() {
        tickerDescansoJob?.cancel()
        _estado.update {
            it.copy(
                descansoActivo = false,
                descansoRestante = 0,
                descansoTerminado = false
            )
        }
    }

    /**
     * @brief Ajusta el descanso en el delta indicado (positivo añade tiempo,
     * negativo lo resta), sin permitir valores negativos. Afecta tanto al tiempo
     * restante como a la duración total configurada.
     * @param deltaSegundos Variación en segundos (+15, −15, ...).
     */
    fun ajustarDescanso(deltaSegundos: Int) {
        _estado.update {
            it.copy(
                descansoRestante = (it.descansoRestante + deltaSegundos).coerceAtLeast(0),
                descansoTotal = (it.descansoTotal + deltaSegundos).coerceAtLeast(0)
            )
        }
    }

    /**
     * @brief Pausa o reanuda el cronómetro de descanso según su estado actual:
     * - Si está corriendo y queda tiempo, lo pausa (detiene el ticker).
     * - Si está pausado y queda tiempo, lo reanuda (relanza el ticker).
     * - Si no queda tiempo, no hace nada.
     */
    fun pausarReanudarDescanso() {
        val estadoActual = _estado.value
        if (estadoActual.descansoRestante <= 0) return
        if (estadoActual.descansoActivo) {
            tickerDescansoJob?.cancel()
            _estado.update { it.copy(descansoActivo = false) }
        } else {
            _estado.update { it.copy(descansoActivo = true) }
            arrancarTickerDescanso()
        }
    }

    /**
     * @brief Marca el aviso de descanso terminado como visto. Lo invoca la interfaz
     * tras reproducir el aviso sonoro y la vibración para poder ocultarlo.
     */
    fun marcarDescansoVisto() {
        _estado.update { it.copy(descansoTerminado = false) }
    }

    /**
     * @brief Finaliza la sesión activa.
     *
     * Calcula la duración en minutos a partir del cronómetro total (mínimo 1 minuto
     * si la sesión duró más de 0 segundos), persiste la sesión resumida con
     * [FinalizarSesionActivaCasoUso], calcula el [ResumenSesion] con
     * [CalcularResumenSesionCasoUso] y marca el estado como finalizado. Los
     * cronómetros se cancelan al finalizar.
     */
    fun finalizarSesion() {
        val sesionId = _estado.value.sesionId
        val rutina = _estado.value.rutina
        if (sesionId == null || rutina == null) {
            _estado.update { it.copy(error = MENSAJE_SIN_SESION) }
            return
        }
        val segundos = _estado.value.segundosTranscurridos
        val duracionMinutos: Int = if (segundos > 0) {
            (segundos / SEGUNDOS_POR_MINUTO).coerceAtLeast(1L).toInt()
        } else {
            0
        }
        viewModelScope.launch(dispatcher) {
            finalizarSesionActivaCasoUso.ejecutar(
                sesionId = sesionId,
                nombreRutina = rutina.nombre,
                userId = userId ?: USUARIO_SIN_SESION,
                duracionMinutos = duracionMinutos
            ).onSuccess {
                calcularResumenSesionCasoUso.ejecutar(sesionId)
                    .onSuccess { resumen ->
                        tickerTotalJob?.cancel()
                        tickerDescansoJob?.cancel()
                        _estado.update {
                            it.copy(finalizada = true, resumen = resumen, error = null)
                        }
                    }
                    .onFailure { excepcion ->
                        _estado.update {
                            it.copy(error = excepcion.message ?: MENSAJE_RESUMEN_FALLIDO)
                        }
                    }
            }.onFailure { excepcion ->
                _estado.update {
                    it.copy(error = excepcion.message ?: MENSAJE_FINALIZACION_FALLIDA)
                }
            }
        }
    }

    /**
     * @brief Devuelve la carga sugerida (en kg) para el diálogo de registro de serie
     * de un ejercicio: el último peso usado si ya se calculó, el peso del bloque de
     * la rutina si está definido o un valor por defecto (20 kg) en caso contrario.
     * @param ejercicioId Identificador del ejercicio.
     * @return Carga sugerida en kilogramos.
     */
    fun cargaSugeridaDe(ejercicioId: String): Double? {
        val estadoActual = _estado.value
        return estadoActual.cargasSugeridas[ejercicioId]
            ?: estadoActual.ejercicios
                .firstOrNull { it.bloque.ejercicioId == ejercicioId }
                ?.bloque?.pesoKg
            ?: CARGA_SUGERIDA_DEFECTO
    }

    /**
     * @brief Descarta el mensaje de error mostrado en la pantalla.
     */
    fun limpiarError() {
        _estado.update { it.copy(error = null) }
    }

    /**
     * @brief Cancela los cronómetros y la observación de series al destruirse el
     * ViewModel, evitando fugas de corrutinas.
     */
    override fun onCleared() {
        tickerTotalJob?.cancel()
        tickerDescansoJob?.cancel()
        jobObservacionSeries?.cancel()
        super.onCleared()
    }

    /**
     * @brief Arranca el ticker del cronómetro total de la sesión: una corrutina que
     * incrementa [EstadoSesionActiva.segundosTranscurridos] cada segundo.
     *
     * Se lanza sobre el [dispatcher] inyectado (por defecto IO) en lugar de sobre
     * Main: el ticker solo actualiza un [StateFlow] (seguro entre hilos) y, en
     * pruebas, esa elección evita que el cronómetro quede enlazado al scheduler
     * virtual de `runTest` (que avanzaría el tiempo de forma indefinida).
     */
    private fun arrancarTickerTotal() {
        tickerTotalJob?.cancel()
        tickerTotalJob = viewModelScope.launch(dispatcher) {
            while (isActive) {
                delay(TICKER_INTERVALO_MS)
                _estado.update {
                    it.copy(segundosTranscurridos = it.segundosTranscurridos + 1)
                }
            }
        }
    }

    /**
     * @brief Arranca el ticker de cuenta atrás del descanso: decrementa
     * [EstadoSesionActiva.descansoRestante] cada segundo. Al llegar a 0 detiene el
     * cronómetro y marca [EstadoSesionActiva.descansoTerminado] para que la interfaz
     * reproduzca el aviso sonoro y la vibración.
     *
     * Al igual que el ticker total, se lanza sobre el [dispatcher] inyectado para
     * mantener los cronómetros desacoplados del scheduler de pruebas.
     */
    private fun arrancarTickerDescanso() {
        tickerDescansoJob?.cancel()
        tickerDescansoJob = viewModelScope.launch(dispatcher) {
            while (isActive) {
                delay(TICKER_INTERVALO_MS)
                val estadoActual = _estado.value
                if (!estadoActual.descansoActivo || estadoActual.descansoRestante <= 0) break
                val restante = estadoActual.descansoRestante - 1
                if (restante <= 0) {
                    _estado.update {
                        it.copy(
                            descansoRestante = 0,
                            descansoActivo = false,
                            descansoTerminado = true
                        )
                    }
                    break
                }
                _estado.update { it.copy(descansoRestante = restante) }
            }
        }
    }

    /**
     * @brief Observa las series de la sesión en vivo mediante el flujo reactivo del
     * repositorio. Cada emisión actualiza [EstadoSesionActiva.series].
     * @param sesionId Identificador de la sesión activa.
     */
    private fun observarSeriesDeLaSesion(sesionId: String) {
        jobObservacionSeries?.cancel()
        jobObservacionSeries = viewModelScope.launch(dispatcher) {
            repositorioSerieRealizada.observarPorSesion(sesionId).collect { series ->
                _estado.update { it.copy(series = series) }
            }
        }
    }

    /**
     * @brief Calcula y almacena la carga sugerida de cada ejercicio de la sesión
     * consultando el historial de series anteriores. Si no hay historial (o la
     * consulta falla), se usa el peso del bloque de la rutina o el valor por defecto.
     */
    private fun cargarCargasSugeridas() {
        viewModelScope.launch(dispatcher) {
            val cargas = _estado.value.ejercicios.associate { ejercicio ->
                val sugerida = runCatching {
                    calcularCargaSugeridaCasoUso.ejecutar(ejercicio.bloque.ejercicioId)
                }.getOrNull() ?: ejercicio.bloque.pesoKg ?: CARGA_SUGERIDA_DEFECTO
                ejercicio.bloque.ejercicioId to sugerida
            }
            _estado.update { it.copy(cargasSugeridas = cargas) }
        }
    }

    companion object {
        /** Intervalo del ticker de los cronómetros en milisegundos (1 segundo). */
        private const val TICKER_INTERVALO_MS: Long = 1_000L

        /** Segundos que tiene un minuto (para calcular la duración al finalizar). */
        private const val SEGUNDOS_POR_MINUTO: Long = 60L

        /** Carga sugerida por defecto cuando no hay historial ni peso en el bloque. */
        private const val CARGA_SUGERIDA_DEFECTO: Double = 20.0

        /** Identificador genérico usado cuando no existe una sesión de usuario activa. */
        private const val USUARIO_SIN_SESION: String = "usuario_sin_sesion"

        /** Mensaje de error cuando la rutina solicitada no existe. */
        private const val MENSAJE_RUTINA_NO_ENCONTRADA: String =
            "No se encontró la rutina solicitada."

        /** Mensaje de error cuando la preparación de la sesión falla. */
        private const val MENSAJE_PREPARACION_FALLIDA: String =
            "No se pudo preparar la sesión de entrenamiento."

        /** Mensaje de error cuando se opera sin una sesión activa. */
        private const val MENSAJE_SIN_SESION: String =
            "No hay una sesión activa en este momento."

        /** Mensaje de error cuando el registro de una serie falla. */
        private const val MENSAJE_REGISTRO_FALLIDO: String =
            "No se pudo registrar la serie."

        /** Mensaje de error cuando la edición de una serie falla. */
        private const val MENSAJE_EDICION_FALLIDA: String =
            "No se pudo editar la serie."

        /** Mensaje de error cuando la eliminación de una serie falla. */
        private const val MENSAJE_ELIMINACION_FALLIDA: String =
            "No se pudo eliminar la serie."

        /** Mensaje de error cuando la finalización de la sesión falla. */
        private const val MENSAJE_FINALIZACION_FALLIDA: String =
            "No se pudo finalizar la sesión."

        /** Mensaje de error cuando el cálculo del resumen falla. */
        private const val MENSAJE_RESUMEN_FALLIDO: String =
            "No se pudo calcular el resumen de la sesión."
    }
}