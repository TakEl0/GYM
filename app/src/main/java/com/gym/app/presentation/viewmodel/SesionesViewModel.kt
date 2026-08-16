/**
 * @file SesionesViewModel.kt
 * @brief ViewModel de la pantalla de Sesiones (historial semanal) de la aplicación GYM.
 * Observa las sesiones de entrenamiento de la semana actual mediante
 * [ObservarSesionesEntrenamientoCasoUso] y permite registrar una sesión manual con
 * [RegistrarSesionEntrenamientoCasoUso].
 */
package com.gym.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.app.data.repository.RepositorioSesionEntrenamientoFake
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.SesionEntrenamiento
import com.gym.app.domain.repository.RepositorioSesionEntrenamiento
import com.gym.app.domain.usecase.entrenamiento.ObservarSesionesEntrenamientoCasoUso
import com.gym.app.domain.usecase.entrenamiento.RegistrarSesionEntrenamientoCasoUso
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @data class EstadoSesiones
 * @brief Estado inmutable de la pantalla de Sesiones.
 * @property sesiones Sesiones de entrenamiento de la semana actual.
 * @property inicioSemana Fecha de inicio (lunes) de la semana observada.
 * @property finSemana Fecha de fin (domingo) de la semana observada.
 * @property registrando Indica si el registro de una sesión está en curso.
 * @property cargando Indica si los datos están en proceso de carga.
 * @property error Mensaje de error si alguna operación falló (null en caso normal).
 */
data class EstadoSesiones(
    val sesiones: List<SesionEntrenamiento> = emptyList(),
    val inicioSemana: LocalDate = LocalDate.now()
        .minusDays((LocalDate.now().dayOfWeek.value - 1).toLong()),
    val finSemana: LocalDate = LocalDate.now()
        .minusDays((LocalDate.now().dayOfWeek.value - 1).toLong())
        .plusDays(6),
    val registrando: Boolean = false,
    val cargando: Boolean = true,
    val error: String? = null
) {
    /**
     * @brief Número de sesiones completadas en la semana observada.
     */
    val sesionesCompletadas: Int
        get() = sesiones.count { it.completo }
}

/**
 * @class SesionesViewModel
 * @brief Gestiona el estado de la pantalla de Sesiones (historial semanal).
 *
 * Observa las [SesionEntrenamiento] comprendidas entre el lunes y el domingo de
 * la semana actual, convirtiendo las fechas límite a epoch millis para adaptarse
 * a la firma de [ObservarSesionesEntrenamientoCasoUso.ejecutar]. El registro de
 * una sesión manual valida y persiste mediante
 * [RegistrarSesionEntrenamientoCasoUso].
 *
 * Constructores según el patrón del proyecto: primario inyectable (fakes por
 * defecto para pruebas) y secundario que resuelve las dependencias desde el
 * [ContenedorDependencias], incluyendo el identificador del usuario autenticado
 * para asociar las sesiones al perfil correcto.
 */
class SesionesViewModel(
    private val repositorioSesionEntrenamiento: RepositorioSesionEntrenamiento =
        RepositorioSesionEntrenamientoFake(),
    private val userId: String? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    /** Caso de uso de observación de sesiones, sobre el repositorio inyectado. */
    private val observarSesionesEntrenamientoCasoUso =
        ObservarSesionesEntrenamientoCasoUso(repositorioSesionEntrenamiento)

    /** Caso de uso de registro de sesiones, sobre el repositorio inyectado. */
    private val registrarSesionEntrenamientoCasoUso =
        RegistrarSesionEntrenamientoCasoUso(repositorioSesionEntrenamiento, dispatcher)

    /**
     * @brief Constructor secundario que resuelve las dependencias reales desde
     * el [ContenedorDependencias] (inyección manual). El identificador de usuario
     * se obtiene de la sesión activa de Supabase; si no hay sesión, queda null.
     * @param contenedor Contenedor de dependencias de la aplicación.
     */
    constructor(contenedor: ContenedorDependencias) : this(
        repositorioSesionEntrenamiento = contenedor.repositorioSesionEntrenamiento,
        userId = contenedor.obtenerSesionActualCasoUso.ejecutar()?.user?.id
    )

    private val _estado = MutableStateFlow(EstadoSesiones())
    val estado: StateFlow<EstadoSesiones> = _estado.asStateFlow()

    init {
        iniciarObservacion()
    }

    /**
     * @brief Inicia la observación reactiva de las sesiones de la semana actual.
     * Convierte los límites [LocalDate] de la semana a epoch millis y delega en
     * [ObservarSesionesEntrenamientoCasoUso]. Cada emisión actualiza el estado.
     */
    fun iniciarObservacion() {
        viewModelScope.launch {
            val estadoInicial = _estado.value
            val zonaHoraria = ZoneId.systemDefault()
            val inicioEpoch = estadoInicial.inicioSemana
                .atStartOfDay(zonaHoraria)
                .toInstant()
                .toEpochMilli()
            val finEpoch = estadoInicial.finSemana
                .atTime(LocalTime.MAX)
                .atZone(zonaHoraria)
                .toInstant()
                .toEpochMilli()

            observarSesionesEntrenamientoCasoUso.ejecutar(inicioEpoch, finEpoch)
                .collect { sesiones ->
                    _estado.update {
                        it.copy(sesiones = sesiones, cargando = false)
                    }
                }
        }
    }

    /**
     * @brief Registra una sesión de entrenamiento realizada.
     * @param nombreRutina Nombre de la rutina ejecutada.
     * @param serieRealizadas Series totales realizadas en la sesión.
     * @param duracionMinutos Duración de la sesión en minutos.
     * @param completo Indica si la sesión se finalizó por completo.
     */
    fun registrarSesion(
        nombreRutina: String,
        serieRealizadas: Int,
        duracionMinutos: Int,
        completo: Boolean
    ) {
        viewModelScope.launch {
            _estado.update { it.copy(registrando = true, error = null) }
            val sesion = SesionEntrenamiento(
                id = UUID.randomUUID().toString(),
                // Si no hay sesión activa (modo desarrollo con fakes) se usa un
                // identificador genérico para no bloquear el registro.
                userId = userId ?: USUARIO_SIN_SESION,
                fecha = System.currentTimeMillis(),
                nombreRutina = nombreRutina.trim(),
                serieRealizadas = serieRealizadas,
                duracionMinutos = duracionMinutos,
                completo = completo
            )
            registrarSesionEntrenamientoCasoUso.ejecutar(sesion)
                .onSuccess {
                    _estado.update { it.copy(registrando = false) }
                }
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(
                            registrando = false,
                            error = excepcion.message ?: "No se pudo registrar la sesión."
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

    companion object {
        /** Identificador genérico usado cuando no existe una sesión activa. */
        private const val USUARIO_SIN_SESION: String = "usuario_sin_sesion"
    }
}