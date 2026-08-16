/**
 * @file AutenticacionViewModel.kt
 * @brief ViewModel de la pantalla de autenticación de la aplicación GYM.
 * Gestiona el formulario de inicio de sesión y de registro de nuevos usuarios,
 * exponiendo un [StateFlow] inmutable con todo el estado de autenticación y
 * delegando la lógica de negocio en los casos de uso de la capa de dominio.
 */
package com.gym.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.EstadoSesion
import com.gym.app.domain.usecase.autenticacion.CerrarSesionCasoUso
import com.gym.app.domain.usecase.autenticacion.IniciarSesionCasoUso
import com.gym.app.domain.usecase.autenticacion.ObservarEstadoSesionCasoUso
import com.gym.app.domain.usecase.autenticacion.RegistrarUsuarioCasoUso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @enum ModoAutenticacion
 * @brief Modos conmutables de la pantalla de autenticación.
 * @property INICIAR_SESION Formulario de acceso para usuarios existentes.
 * @property REGISTRO Formulario de alta de nuevos usuarios.
 */
enum class ModoAutenticacion {
    INICIAR_SESION,
    REGISTRO
}

/**
 * @data class EstadoAutenticacion
 * @brief Estado inmutable de la pantalla de autenticación.
 * @property email Correo electrónico escrito actualmente en el formulario.
 * @property password Contraseña escrita actualmente en el formulario.
 * @property nombre Nombre completo del usuario (solo visible en modo registro).
 * @property modo Modo activo de la pantalla (inicio de sesión o registro).
 * @property estadoSesion Estado global de la sesión observado desde el dominio.
 * @property cargando Indica si hay una operación de autenticación en curso.
 * @property error Mensaje de error visible (null si no hay errores).
 * @property exitoLogin Indica si la última operación de acceso finalizó con éxito.
 */
data class EstadoAutenticacion(
    val email: String = "",
    val password: String = "",
    val nombre: String = "",
    val modo: ModoAutenticacion = ModoAutenticacion.INICIAR_SESION,
    val estadoSesion: EstadoSesion = EstadoSesion.NO_AUTENTICADO,
    val cargando: Boolean = false,
    val error: String? = null,
    val exitoLogin: Boolean = false
)

/**
 * @class AutenticacionViewModel
 * @brief Gestiona el estado del formulario de autenticación y el flujo de sesión.
 *
 * Expone un [StateFlow] inmutable con los campos del formulario, el modo activo,
 * el estado global de la sesión, el indicador de carga y los errores. Todos los
 * métodos mutan únicamente ese estado; la autenticación real se delega en los
 * casos de uso [IniciarSesionCasoUso], [RegistrarUsuarioCasoUso] y
 * [CerrarSesionCasoUso], y el estado de sesión se observa de forma reactiva a
 * través de [ObservarEstadoSesionCasoUso].
 */
class AutenticacionViewModel(
    private val iniciarSesionCasoUso: IniciarSesionCasoUso,
    private val registrarUsuarioCasoUso: RegistrarUsuarioCasoUso,
    private val cerrarSesionCasoUso: CerrarSesionCasoUso,
    private val observarEstadoSesionCasoUso: ObservarEstadoSesionCasoUso
) : ViewModel() {

    /**
     * @brief Constructor secundario que resuelve los casos de uso desde el
     * [ContenedorDependencias] (inyección manual de dependencias).
     * @param contenedor Contenedor de dependencias de la aplicación.
     */
    constructor(contenedor: ContenedorDependencias) : this(
        iniciarSesionCasoUso = contenedor.iniciarSesionCasoUso,
        registrarUsuarioCasoUso = contenedor.registrarUsuarioCasoUso,
        cerrarSesionCasoUso = contenedor.cerrarSesionCasoUso,
        observarEstadoSesionCasoUso = contenedor.observarEstadoSesionCasoUso
    )

    private val _estado = MutableStateFlow(EstadoAutenticacion())
    val estado: StateFlow<EstadoAutenticacion> = _estado.asStateFlow()

    init {
        // Observa el estado de sesión del dominio para reaccionar a los cambios
        // de autenticación (inicio, cierre o restauración de sesión persistida).
        // Por seguridad, al pasar a NO_AUTENTICADO se limpia la contraseña del
        // formulario para que nunca quede texto sensible tras cerrar la sesión.
        viewModelScope.launch {
            observarEstadoSesionCasoUso.ejecutar().collect { estadoSesion ->
                _estado.update {
                    it.copy(
                        estadoSesion = estadoSesion,
                        cargando = estadoSesion == EstadoSesion.CARGANDO,
                        password = if (estadoSesion == EstadoSesion.NO_AUTENTICADO) "" else it.password,
                        error = if (estadoSesion == EstadoSesion.AUTENTICADO) null else it.error
                    )
                }
            }
        }
    }

    /**
     * @brief Actualiza el correo electrónico escrito por el usuario.
     * @param valor Correo electrónico como texto sin procesar.
     */
    fun actualizarEmail(valor: String) {
        _estado.update { it.copy(email = valor, error = null, exitoLogin = false) }
    }

    /**
     * @brief Actualiza la contraseña escrita por el usuario.
     * @param valor Contraseña como texto sin procesar.
     */
    fun actualizarPassword(valor: String) {
        _estado.update { it.copy(password = valor, error = null, exitoLogin = false) }
    }

    /**
     * @brief Actualiza el nombre completo escrito por el usuario (modo registro).
     * @param valor Nombre completo como texto sin procesar.
     */
    fun actualizarNombre(valor: String) {
        _estado.update { it.copy(nombre = valor, error = null, exitoLogin = false) }
    }

    /**
     * @brief Cambia el modo de la pantalla entre inicio de sesión y registro.
     * @param modo Nuevo modo a activar.
     */
    fun cambiarModo(modo: ModoAutenticacion) {
        _estado.update { it.copy(modo = modo, error = null, exitoLogin = false) }
    }

    /**
     * @brief Inicia sesión con el correo y la contraseña introducidos.
     * Delega en [IniciarSesionCasoUso] dentro de [viewModelScope] y refleja el
     * resultado en el [StateFlow] de forma inmutable.
     */
    fun iniciarSesion() {
        val actual = _estado.value
        if (actual.cargando) return
        _estado.update { it.copy(cargando = true, error = null, exitoLogin = false) }
        viewModelScope.launch {
            iniciarSesionCasoUso.ejecutar(actual.email.trim(), actual.password)
                .onSuccess {
                    // Por higiene de seguridad se limpia la contraseña de la memoria
                    // inmediatamente tras una autenticación exitosa.
                    _estado.update {
                        it.copy(cargando = false, exitoLogin = true, error = null, password = "")
                    }
                }
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(
                            cargando = false,
                            exitoLogin = false,
                            error = excepcion.message ?: "No se pudo iniciar sesión. Inténtalo de nuevo."
                        )
                    }
                }
        }
    }

    /**
     * @brief Registra un nuevo usuario con los datos del formulario.
     * Delega en [RegistrarUsuarioCasoUso] dentro de [viewModelScope] y refleja el
     * resultado en el [StateFlow] de forma inmutable.
     */
    fun registrar() {
        val actual = _estado.value
        if (actual.cargando) return
        _estado.update { it.copy(cargando = true, error = null, exitoLogin = false) }
        viewModelScope.launch {
            registrarUsuarioCasoUso
                .ejecutar(actual.email.trim(), actual.password, actual.nombre.trim())
                .onSuccess {
                    // Se limpia la contraseña de la memoria tras completar el registro.
                    _estado.update {
                        it.copy(cargando = false, exitoLogin = true, error = null, password = "")
                    }
                }
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(
                            cargando = false,
                            exitoLogin = false,
                            error = excepcion.message ?: "No se pudo completar el registro. Inténtalo de nuevo."
                        )
                    }
                }
        }
    }

    /**
     * @brief Cierra la sesión actual del usuario.
     * Delega en [CerrarSesionCasoUso]; el flujo observado de [EstadoSesion]
     * emitirá [EstadoSesion.NO_AUTENTICADO] y la navegación reaccionará.
     */
    fun cerrarSesion() {
        viewModelScope.launch {
            cerrarSesionCasoUso.ejecutar()
        }
    }

    /**
     * @brief Limpia el mensaje de error visible del estado.
     */
    fun limpiarErrores() {
        _estado.update { it.copy(error = null) }
    }
}