/**
 * @file RutinasViewModel.kt
 * @brief ViewModel de la pantalla de Rutinas de la aplicación GYM.
 * Observa las rutinas configuradas, permite construir automáticamente una rutina
 * PPL para el día elegido mediante [ConstruirRutinaCasoUso] y calcula la
 * estimación del 1RM con [CalcularUnRMCasoUso] (fórmulas de Epley y Brzycki).
 */
package com.gym.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.app.data.repository.RepositorioEjercicioFake
import com.gym.app.data.repository.RepositorioGimnasioFake
import com.gym.app.data.repository.RepositorioRutinaFake
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.CalculoUnRM
import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.model.Gimnasio
import com.gym.app.domain.model.Rutina
import com.gym.app.domain.repository.RepositorioEjercicio
import com.gym.app.domain.repository.RepositorioGimnasio
import com.gym.app.domain.repository.RepositorioRutina
import com.gym.app.domain.usecase.entrenamiento.CalcularUnRMCasoUso
import com.gym.app.domain.usecase.entrenamiento.ConstruirRutinaCasoUso
import com.gym.app.domain.usecase.gimnasio.AlternativasMaquinaCasoUso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @data class EstadoRutinas
 * @brief Estado inmutable de la pantalla de Rutinas.
 * @property rutinas Rutinas de entrenamiento configuradas por el usuario.
 * @property gimnasio Gimnasio del usuario (para conocer la maquinaria disponible).
 * @property ejercicios Catálogo de ejercicios (para resolver nombres de bloques).
 * @property calculoUnRM Estimación del 1RM calculada (null si aún no se calculó).
 * @property construyendo Indica si la construcción de una rutina está en curso.
 * @property cargando Indica si los datos están en proceso de carga.
 * @property error Mensaje de error si alguna operación falló (null en caso normal).
 */
data class EstadoRutinas(
    val rutinas: List<Rutina> = emptyList(),
    val gimnasio: Gimnasio? = null,
    val ejercicios: List<Ejercicio> = emptyList(),
    val calculoUnRM: Double? = null,
    val construyendo: Boolean = false,
    val cargando: Boolean = true,
    val error: String? = null
)

/**
 * @class RutinasViewModel
 * @brief Gestiona el estado de la pantalla de Rutinas.
 *
 * Observa en paralelo las rutinas configuradas, el gimnasio (para conocer las
 * máquinas disponibles) y el catálogo de ejercicios. La construcción de una
 * rutina PPL delega en [ConstruirRutinaCasoUso] y persiste el resultado con
 * [RepositorioRutina.guardarRutina]. El cálculo de 1RM delega en
 * [CalcularUnRMCasoUso] y materializa el valor con [CalculoUnRM.calcular].
 *
 * Constructores según el patrón del proyecto: primario inyectable (fakes por
 * defecto para pruebas) y secundario que resuelve las dependencias desde el
 * [ContenedorDependencias].
 */
class RutinasViewModel(
    private val repositorioRutina: RepositorioRutina = RepositorioRutinaFake(),
    private val repositorioGimnasio: RepositorioGimnasio = RepositorioGimnasioFake(),
    private val repositorioEjercicio: RepositorioEjercicio = RepositorioEjercicioFake(),
    private val construirRutinaCasoUso: ConstruirRutinaCasoUso = ConstruirRutinaCasoUso(
        AlternativasMaquinaCasoUso(RepositorioGimnasioFake())
    ),
    private val calcularUnRMCasoUso: CalcularUnRMCasoUso = CalcularUnRMCasoUso()
) : ViewModel() {

    /**
     * @brief Constructor secundario que resuelve las dependencias reales desde
     * el [ContenedorDependencias] (inyección manual).
     * @param contenedor Contenedor de dependencias de la aplicación.
     */
    constructor(contenedor: ContenedorDependencias) : this(
        repositorioRutina = contenedor.repositorioRutina,
        repositorioGimnasio = contenedor.repositorioGimnasio,
        repositorioEjercicio = contenedor.repositorioEjercicio,
        construirRutinaCasoUso = contenedor.construirRutinaCasoUso,
        calcularUnRMCasoUso = contenedor.calcularUnRMCasoUso
    )

    private val _estado = MutableStateFlow(EstadoRutinas())
    val estado: StateFlow<EstadoRutinas> = _estado.asStateFlow()

    init {
        iniciarObservacion()
    }

    /**
     * @brief Inicia la observación reactiva de las rutinas, del gimnasio y del
     * catálogo de ejercicios. Cada emisión de cualquiera de los flujos actualiza
     * el estado combinado.
     */
    fun iniciarObservacion() {
        viewModelScope.launch {
            repositorioRutina.observarRutinas()
                .combine(repositorioGimnasio.observarGimnasio()) { rutinas, gimnasio ->
                    rutinas to gimnasio
                }
                .combine(repositorioEjercicio.observarEjercicios()) { par, ejercicios ->
                    Triple(par.first, par.second, ejercicios)
                }
                .collect { (rutinas, gimnasio, ejercicios) ->
                    _estado.update {
                        it.copy(
                            rutinas = rutinas,
                            gimnasio = gimnasio,
                            ejercicios = ejercicios,
                            cargando = false
                        )
                    }
                }
        }
    }

    /**
     * @brief Construye automáticamente la rutina PPL para el día indicado y la
     * persiste en el repositorio de rutinas.
     *
     * Necesita que el gimnasio esté configurado (para conocer las máquinas
     * disponibles) y el catálogo de ejercicios cargado. Si el caso de uso devuelve
     * `null` (día de descanso o sin ejercicios suficientes) se informa del motivo
     * mediante el campo de error del estado.
     * @param diaSemana Día de la semana (1 = lunes ... 7 = domingo).
     */
    fun construirRutina(diaSemana: Int) {
        val gimnasio = _estado.value.gimnasio
        val ejercicios = _estado.value.ejercicios
        if (gimnasio == null) {
            _estado.update {
                it.copy(error = "Configura primero tu gimnasio y su maquinaria.")
            }
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(construyendo = true, error = null) }
            construirRutinaCasoUso.ejecutar(diaSemana, gimnasio.maquinas, ejercicios)
                .onSuccess { rutina ->
                    if (rutina != null) {
                        repositorioRutina.guardarRutina(rutina)
                        _estado.update { it.copy(construyendo = false) }
                    } else {
                        _estado.update {
                            it.copy(
                                construyendo = false,
                                error = "No se pudo construir la rutina para ese día " +
                                    "(descanso o ejercicios insuficientes)."
                            )
                        }
                    }
                }
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(
                            construyendo = false,
                            error = excepcion.message ?: "No se pudo construir la rutina."
                        )
                    }
                }
        }
    }

    /**
     * @brief Calcula la estimación del 1RM a partir del peso y las repeticiones.
     * @param pesoKg Carga levantada en kilogramos (debe ser mayor que 0).
     * @param repeticiones Repeticiones ejecutadas (debe estar en 1..35).
     */
    fun calcularUnRM(pesoKg: Double, repeticiones: Int) {
        viewModelScope.launch {
            calcularUnRMCasoUso.ejecutar(pesoKg, repeticiones)
                .onSuccess { calculo ->
                    val valorEstimado = CalculoUnRM.calcular(calculo.pesoKg, calculo.repeticiones)
                    _estado.update { it.copy(calculoUnRM = valorEstimado, error = null) }
                }
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(
                            calculoUnRM = null,
                            error = excepcion.message ?: "Datos de 1RM no válidos."
                        )
                    }
                }
        }
    }

    /**
     * @brief Descarta el mensaje de error mostrado en la pantalla.
     */
    fun limpiarError() {
        _estado.update { it.copy(error = null) }
    }
}