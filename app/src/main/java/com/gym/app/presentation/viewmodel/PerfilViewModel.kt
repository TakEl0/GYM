/**
 * @file PerfilViewModel.kt
 * @brief ViewModel de la pantalla de perfil de la aplicación GYM.
 * Observa el perfil del usuario de forma reactiva y expone las operaciones de
 * edición (nombre, datos antropométricos y objetivos) mediante casos de uso
 * de la capa de dominio, siguiendo Clean Architecture.
 */
package com.gym.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.PerfilUsuario
import com.gym.app.domain.usecase.perfil.ActualizarObjetivosPerfilCasoUso
import com.gym.app.domain.usecase.perfil.GuardarPerfilCasoUso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @data class EstadoPerfil
 * @brief Estado inmutable de la pantalla de perfil.
 * @property perfil Perfil observado del usuario (null si aún no existe o se está cargando).
 * @property nombreEditado Nombre en el campo de edición.
 * @property aliasEditado Alias en el campo de edición.
 * @property guardando Indica si hay una operación de guardado en curso.
 * @property mensajeExito Mensaje temporal de éxito tras guardar (null en reposo).
 * @property errorError Mensaje de error de validación o persistencia (null en reposo).
 */
data class EstadoPerfil(
    val perfil: PerfilUsuario? = null,
    val nombreEditado: String = "",
    val aliasEditado: String = "",
    val guardando: Boolean = false,
    val mensajeExito: String? = null,
    val error: String? = null
)

/**
 * @class PerfilViewModel
 * @brief Gestiona el estado de la pantalla de perfil.
 *
 * Expone un [StateFlow] con el perfil observado (se sincroniza automáticamente
 * cuando cambia en la base de datos) y métodos de edición que delegan en los
 * casos de uso [GuardarPerfilCasoUso] (nombre + objetivos completos) y
 * [ActualizarObjetivosPerfilCasoUso] (solo objetivos, para cambios rápidos).
 *
 * @param usuarioId Identificador del usuario autenticado.
 * @param guardarPerfilCasoUso Caso de uso de guardado completo del perfil.
 * @param actualizarObjetivosCasoUso Caso de uso de actualización de objetivos.
 * @param observarPerfil Flujo reactivo del perfil del usuario.
 */
class PerfilViewModel(
    private val usuarioId: String,
    private val guardarPerfilCasoUso: GuardarPerfilCasoUso,
    private val actualizarObjetivosCasoUso: ActualizarObjetivosPerfilCasoUso,
    observarPerfil: kotlinx.coroutines.flow.Flow<PerfilUsuario?>
) : ViewModel() {

    /**
     * @brief Constructor secundario que resuelve las dependencias desde el
     * [ContenedorDependencias] (inyección manual).
     * @param usuarioId Identificador del usuario autenticado.
     * @param contenedor Contenedor de dependencias de la aplicación.
     */
    constructor(usuarioId: String, contenedor: ContenedorDependencias) : this(
        usuarioId = usuarioId,
        guardarPerfilCasoUso = contenedor.guardarPerfilCasoUso,
        actualizarObjetivosCasoUso = contenedor.actualizarObjetivosPerfilCasoUso,
        observarPerfil = contenedor.obtenerPerfilCasoUso.ejecutar(usuarioId)
    )

    private val _estado = MutableStateFlow(EstadoPerfil())
    val estado: StateFlow<EstadoPerfil> = _estado.asStateFlow()

    // Perfil observado: actualiza el estado cuando la fuente de datos emite.
    private val perfilObservado = observarPerfil
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            perfilObservado.collect { perfil ->
                perfil?.let { perfilNoNulo ->
                    _estado.update {
                        it.copy(
                            perfil = perfilNoNulo,
                            nombreEditado = if (it.nombreEditado.isBlank()) perfilNoNulo.nombre else it.nombreEditado,
                            aliasEditado = if (it.aliasEditado.isBlank()) perfilNoNulo.alias.orEmpty() else it.aliasEditado
                        )
                    }
                }
            }
        }
    }

    /**
     * @brief Actualiza el nombre editado en el formulario.
     * @param valor Nuevo texto del campo nombre.
     */
    fun actualizarNombre(valor: String) {
        _estado.update { it.copy(nombreEditado = valor, error = null, mensajeExito = null) }
    }

    /**
     * @brief Actualiza el alias editado en el formulario.
     * @param valor Nuevo texto del campo alias.
     */
    fun actualizarAlias(valor: String) {
        _estado.update { it.copy(aliasEditado = valor, error = null, mensajeExito = null) }
    }

    /**
     * @brief Guarda el perfil completo (nombre, alias y objetivos actuales)
     * usando [GuardarPerfilCasoUso]. Muestra error de validación o éxito al terminar.
     */
    fun guardarPerfilCompleto() {
        val actual = _estado.value.perfil ?: return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, error = null, mensajeExito = null) }
            val resultado = guardarPerfilCasoUso.ejecutar(
                actual.copy(
                    nombre = _estado.value.nombreEditado.trim(),
                    alias = _estado.value.aliasEditado.trim()
                )
            )
            resultado
                .onSuccess { _estado.update { it.copy(guardando = false, mensajeExito = "Perfil guardado correctamente.") } }
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(
                            guardando = false,
                            error = excepcion.message ?: "No se pudo guardar el perfil."
                        )
                    }
                }
        }
    }

    /**
     * @brief Actualiza un objetivo concreto del perfil (peso, altura, edad,
     * sexo, factor de actividad u objetivo nutricional).
     * @param campo Campo a actualizar.
     * @param valor Texto introducido por el usuario (numérico o textual).
     */
    fun actualizarCampo(campo: CampoPerfil, valor: String) {
        val actual = _estado.value.perfil ?: return
        val perfilActualizado = when (campo) {
            CampoPerfil.PESO -> actual.copy(pesoObjetivoKg = valor.toDoubleOrNull())
            CampoPerfil.ALTURA -> actual.copy(alturaCm = valor.toDoubleOrNull())
            CampoPerfil.EDAD -> actual.copy(edad = valor.toIntOrNull())
            CampoPerfil.SEXO -> actual.copy(sexo = valor)
            CampoPerfil.FACTOR_ACTIVIDAD -> actual.copy(factorActividad = valor)
            CampoPerfil.OBJETIVO -> actual.copy(objetivo = valor)
        }
        _estado.update {
            it.copy(perfil = perfilActualizado, error = null, mensajeExito = null)
        }
    }

    /**
     * @brief Guarda únicamente los objetivos y datos antropométricos mediante
     * [ActualizarObjetivosPerfilCasoUso] (no toca el nombre).
     */
    fun guardarObjetivos() {
        val actual = _estado.value.perfil ?: return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, error = null, mensajeExito = null) }
            val resultado = actualizarObjetivosCasoUso.ejecutar(
                id = actual.id,
                pesoObjetivoKg = actual.pesoObjetivoKg,
                alturaCm = actual.alturaCm,
                edad = actual.edad,
                sexo = actual.sexo,
                factorActividad = actual.factorActividad,
                objetivo = actual.objetivo
            )
            resultado
                .onSuccess { _estado.update { it.copy(guardando = false, mensajeExito = "Objetivos guardados correctamente.") } }
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(
                            guardando = false,
                            error = excepcion.message ?: "No se pudieron guardar los objetivos."
                        )
                    }
                }
        }
    }

    /**
     * @brief Descarta los mensajes temporales (éxito/error) del estado.
     */
    fun limpiarMensajes() {
        _estado.update { it.copy(mensajeExito = null, error = null) }
    }
}

/**
 * @enum class CampoPerfil
 * @brief Campos editables del perfil del usuario.
 */
enum class CampoPerfil {
    PESO, ALTURA, EDAD, SEXO, FACTOR_ACTIVIDAD, OBJETIVO
}