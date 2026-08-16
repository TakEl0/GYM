/**
 * @file RegistroPesoViewModel.kt
 * @brief ViewModel de la pantalla de registro de peso corporal.
 * Expone el estado inmutable del registro de peso mediante StateFlow y
 * delega las operaciones de guardado y consulta en el repositorio de peso.
 */
package com.gym.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.app.data.repository.RepositorioPesoFake
import com.gym.app.domain.model.RegistroPeso
import com.gym.app.domain.repository.RepositorioPeso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * @data class EstadoRegistroPeso
 * @brief Estado inmutable de la pantalla de registro de peso.
 * @property historial Lista de registros de peso (del más reciente al más antiguo).
 * @property ultimoRegistro Último registro de peso disponible.
 * @property pesoActual Peso en kilogramos actualmente escrito en el campo.
 * @property grasaActual Porcentaje de grasa actualmente escrito (puede ser null).
 * @property cargando Indica si los datos están en proceso de carga.
 * @property mensajeGuardado Mensaje informativo tras guardar un registro.
 * @property error Mensaje de error si algo falla (null en caso normal).
 */
data class EstadoRegistroPeso(
    val historial: List<RegistroPeso> = emptyList(),
    val ultimoRegistro: RegistroPeso? = null,
    val pesoActual: String = "",
    val grasaActual: String = "",
    val cargando: Boolean = false,
    val mensajeGuardado: String? = null,
    val error: String? = null
)

/**
 * @class RegistroPesoViewModel
 * @brief Gestiona el estado de la pantalla de registro de peso.
 * Inyecta el repositorio de peso para cargar el historial y guardar nuevas
 * mediciones, actualizando el StateFlow de forma reactiva.
 * Si no se proporciona un repositorio, se utiliza la implementación simulada
 * de la capa de datos para facilitar el desarrollo.
 */
class RegistroPesoViewModel(
    private val repositorioPeso: RepositorioPeso = RepositorioPesoFake()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoRegistroPeso())
    val estado: StateFlow<EstadoRegistroPeso> = _estado.asStateFlow()

    /** Formato de fecha para mostrar en la interfaz (p. ej. 16/08/2026). */
    private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    /**
     * @brief Inicializa la carga del historial de peso.
     * Recupera el historial completo y el último registro desde el repositorio.
     */
    fun cargarHistorial() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            try {
                val historial = repositorioPeso.obtenerHistorial()
                val ultimo = repositorioPeso.obtenerUltimoRegistro()
                _estado.update {
                    it.copy(
                        historial = historial,
                        ultimoRegistro = ultimo,
                        cargando = false
                    )
                }
            } catch (excepcion: Exception) {
                _estado.update {
                    it.copy(cargando = false, error = "No se pudo cargar el historial de peso.")
                }
            }
        }
    }

    /**
     * @brief Actualiza el peso escrito por el usuario en el campo de texto.
     * @param valor Peso en kilogramos como texto sin procesar.
     */
    fun actualizarPeso(valor: String) {
        _estado.update { it.copy(pesoActual = valor, mensajeGuardado = null) }
    }

    /**
     * @brief Actualiza el porcentaje de grasa escrito por el usuario.
     * @param valor Porcentaje de grasa como texto sin procesar.
     */
    fun actualizarGrasa(valor: String) {
        _estado.update { it.copy(grasaActual = valor, mensajeGuardado = null) }
    }

    /**
     * @brief Guarda un nuevo registro de peso con la fecha actual.
     * Valida los campos introducidos y delega el guardado en el repositorio.
     * La coma decimal se normaliza a punto para admitir la notación
     * habitual en castellano (p. ej. "81,5").
     */
    fun guardarRegistro() {
        val estadoActual = _estado.value
        val peso = estadoActual.pesoActual.trim().replace(',', '.').toDoubleOrNull()
        if (peso == null) {
            _estado.update { it.copy(error = "Introduce un peso válido en kilogramos.") }
            return
        }
        if (peso <= 0.0) {
            _estado.update { it.copy(error = "El peso debe ser mayor que cero.") }
            return
        }
        val grasa = estadoActual.grasaActual.trim().replace(',', '.').toDoubleOrNull()

        viewModelScope.launch {
            try {
                repositorioPeso.guardarRegistro(
                    RegistroPeso(
                        fecha = LocalDate.now(),
                        pesoKg = peso,
                        grasaCorporalPorcentaje = grasa,
                        notaComentario = null
                    )
                )
                val historial = repositorioPeso.obtenerHistorial()
                val ultimo = repositorioPeso.obtenerUltimoRegistro()
                _estado.update {
                    it.copy(
                        historial = historial,
                        ultimoRegistro = ultimo,
                        pesoActual = "",
                        grasaActual = "",
                        mensajeGuardado = "Registro guardado correctamente.",
                        error = null
                    )
                }
            } catch (excepcion: Exception) {
                _estado.update { it.copy(error = "No se pudo guardar el registro de peso.") }
            }
        }
    }

    /**
     * @brief Formatea una fecha con el formato dd/MM/yyyy para mostrar en la UI.
     * @param fecha Fecha a formatear.
     * @return Cadena de texto formateada.
     */
    fun formatearFecha(fecha: LocalDate): String = fecha.format(formatoFecha)
}