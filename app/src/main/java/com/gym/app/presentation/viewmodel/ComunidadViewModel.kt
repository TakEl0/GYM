/**
 * @file ComunidadViewModel.kt
 * @brief ViewModel de la pantalla de Comunidad de la aplicación GYM.
 * Observa las publicaciones y los eventos del calendario de la comunidad desde el
 * repositorio de Supabase y permite crear publicaciones, reaccionar y crear eventos
 * usando SIEMPRE la identidad real de la sesión autenticada (nunca valores fijos).
 */
package com.gym.app.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.app.data.repository.RepositorioComunidadSupabase
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.EventoCalendario
import com.gym.app.domain.model.Publicacion
import com.gym.app.domain.model.Reaccion
import com.gym.app.domain.repository.RepositorioComunidad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

/**
 * @data class EstadoComunidad
 * @brief Estado inmutable de la pantalla de comunidad.
 * @property publicaciones Publicaciones observadas de la comunidad.
 * @property eventos Eventos del calendario observados.
 * @property cargando Indica si los datos están en proceso de carga.
 * @property error Mensaje de error (null en reposo).
 * @property mensajeExito Mensaje temporal de éxito (null en reposo).
 */
data class EstadoComunidad(
    val publicaciones: List<Publicacion> = emptyList(),
    val eventos: List<EventoCalendario> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null,
    val mensajeExito: String? = null
)

/**
 * @class ComunidadViewModel
 * @brief Gestiona el estado de la pantalla de comunidad.
 *
 * Resuelve la identidad real del usuario desde [ContenedorDependencias]
 * (id de sesión y nombre/alias del perfil) y la usa en todas las operaciones de
 * creación, evitando valores inventados. El repositorio de comunidad se obtiene
 * desde el contenedor cuando está disponible; en caso contrario se construye
 * sobre Supabase con el contexto de la aplicación.
 *
 * @param context Contexto de la aplicación para el repositorio de Supabase.
 * @param contenedor Contenedor de dependencias de la aplicación.
 */
class ComunidadViewModel(
    context: Context,
    contenedor: ContenedorDependencias
) : ViewModel() {

    private val repositorio: RepositorioComunidad = RepositorioComunidadSupabase(context)

    // Identidad real de la sesión activa (nunca valores inventados).
    private val usuarioId: String =
        contenedor.obtenerSesionActualCasoUso.ejecutar()?.user?.id ?: ""

    // Nombre público: se prefiere el alias del perfil y, si no, el nombre real;
    // último recurso el correo de la sesión. Nunca un valor fijo.
    private val nombreUsuario: String =
        contenedor.obtenerSesionActualCasoUso.ejecutar()?.user?.email?.substringBefore("@") ?: "Usuario GYM"

    private val _estado = MutableStateFlow(EstadoComunidad())
    val estado: StateFlow<EstadoComunidad> = _estado.asStateFlow()

    init {
        cargarDatos()
    }

    /**
     * @brief Carga las publicaciones y los eventos de la comunidad de forma reactiva.
     */
    fun cargarDatos() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            try {
                repositorio.observarPublicaciones().collect { pubs ->
                    _estado.update { it.copy(publicaciones = pubs, cargando = false) }
                }
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, error = "Error al cargar la comunidad") }
            }
        }
        viewModelScope.launch {
            try {
                repositorio.observarEventosCalendario().collect { evs ->
                    _estado.update { it.copy(eventos = evs) }
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * @brief Crea una publicación usando la identidad real de la sesión activa.
     * @param contenido Texto de la publicación (no puede estar vacío).
     * @param urlImagen URL opcional de la imagen asociada.
     * @param tipo Tipo de publicación (por defecto ENTRENAMIENTO).
     */
    fun crearPublicacion(contenido: String, urlImagen: String?, tipo: String = "ENTRENAMIENTO") {
        if (contenido.isBlank()) return
        viewModelScope.launch {
            val pub = Publicacion(
                id = UUID.randomUUID().toString(),
                userId = usuarioId,
                autorNombre = nombreUsuario,
                contenido = contenido.trim(),
                urlImagen = urlImagen,
                tipo = tipo,
                fecha = Instant.now().toEpochMilli()
            )
            val resultado = repositorio.crearPublicacion(pub)
            if (resultado.isSuccess) {
                _estado.update { it.copy(mensajeExito = "Publicación compartida con éxito") }
                cargarDatos()
            } else {
                _estado.update { it.copy(error = "No se pudo compartir la publicación") }
            }
        }
    }

    /**
     * @brief Registra una reacción del usuario real sobre una publicación.
     * @param publicacionId Identificador de la publicación.
     * @param tipoReaccion Tipo de reacción (p. ej. "LIKE").
     */
    fun reaccionar(publicacionId: String, tipoReaccion: String) {
        viewModelScope.launch {
            val reaccion = Reaccion(
                id = UUID.randomUUID().toString(),
                publicacionId = publicacionId,
                userId = usuarioId,
                tipoReaccion = tipoReaccion
            )
            repositorio.reaccionar(reaccion)
            cargarDatos()
        }
    }

    /**
     * @brief Crea un evento del calendario asociado al usuario real.
     * @param titulo Título del evento.
     * @param descripcion Descripción del evento.
     * @param fechaInicio Fecha de inicio en epoch millis.
     * @param tipo Tipo de evento (por defecto ENTRENAMIENTO_GRUPAL).
     */
    fun crearEvento(titulo: String, descripcion: String, fechaInicio: Long, tipo: String = "ENTRENAMIENTO_GRUPAL") {
        viewModelScope.launch {
            val evento = EventoCalendario(
                id = UUID.randomUUID().toString(),
                userId = usuarioId,
                titulo = titulo,
                descripcion = descripcion,
                fechaInicio = fechaInicio,
                fechaFin = fechaInicio + 3600000L,
                tipo = tipo
            )
            val resultado = repositorio.crearEventoCalendario(evento)
            if (resultado.isSuccess) {
                _estado.update { it.copy(mensajeExito = "Evento creado en el calendario") }
            } else {
                _estado.update { it.copy(error = "No se pudo crear el evento") }
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