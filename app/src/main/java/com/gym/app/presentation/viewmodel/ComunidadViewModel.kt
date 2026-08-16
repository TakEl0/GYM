package com.gym.app.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.app.data.repository.RepositorioComunidadSupabase
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

data class EstadoComunidad(
    val publicaciones: List<Publicacion> = emptyList(),
    val eventos: List<EventoCalendario> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null,
    val mensajeExito: String? = null
)

class ComunidadViewModel(
    context: Context
) : ViewModel() {

    private val repositorio: RepositorioComunidad = RepositorioComunidadSupabase(context)

    private val _estado = MutableStateFlow(EstadoComunidad())
    val estado: StateFlow<EstadoComunidad> = _estado.asStateFlow()

    init {
        cargarDatos()
    }

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

    fun crearPublicacion(userId: String, autorNombre: String, contenido: String, urlImagen: String?, tipo: String = "ENTRENAMIENTO") {
        viewModelScope.launch {
            val pub = Publicacion(
                id = UUID.randomUUID().toString(),
                userId = userId,
                autorNombre = autorNombre,
                contenido = contenido,
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

    fun reaccionar(publicacionId: String, userId: String, tipoReaccion: String) {
        viewModelScope.launch {
            val reaccion = Reaccion(
                id = UUID.randomUUID().toString(),
                publicacionId = publicacionId,
                userId = userId,
                tipoReaccion = tipoReaccion
            )
            repositorio.reaccionar(reaccion)
            cargarDatos()
        }
    }

    fun crearEvento(userId: String, titulo: String, descripcion: String, fechaInicio: Long, tipo: String = "ENTRENAMIENTO_GRUPAL") {
        viewModelScope.launch {
            val evento = EventoCalendario(
                id = UUID.randomUUID().toString(),
                userId = userId,
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
}
