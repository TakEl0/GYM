/**
 * @file CalendarioEntrenosViewModel.kt
 * @brief ViewModel de la pantalla de calendario personal de entrenamientos.
 * Gestiona el mes visible, los entrenamientos programados en ese rango y el
 * día seleccionado, exponiendo el estado inmutable mediante StateFlow.
 */
package com.gym.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.Entrenamiento
import com.gym.app.domain.usecase.entrenamiento.ObservarEntrenamientosCalendarioCasoUso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * @data class EstadoCalendarioEntrenos
 * @brief Estado inmutable de la pantalla de calendario de entrenos.
 * @property mesVisible Mes calendario que se está mostrando.
 * @property entrenamientos Entrenamientos comprendidos en el rango del mes visible.
 * @property diaSeleccionado Día del mes seleccionado por el usuario (o null).
 * @property cargando Indica si se están cargando los datos del mes.
 * @property error Mensaje de error si la carga falla (null en caso normal).
 */
data class EstadoCalendarioEntrenos(
    val mesVisible: YearMonth = YearMonth.now(),
    val entrenamientos: List<Entrenamiento> = emptyList(),
    val diaSeleccionado: LocalDate? = null,
    val cargando: Boolean = false,
    val error: String? = null
)

/**
 * @class CalendarioEntrenosViewModel
 * @brief Gestiona el estado del calendario personal de entrenamientos.
 * Observa de forma reactiva los entrenamientos del mes visible y permite
 * navegar entre meses y seleccionar un día concreto para ver su detalle.
 * @param contenedor Contenedor de dependencias de la aplicación.
 */
class CalendarioEntrenosViewModel(
    contenedor: ContenedorDependencias
) : ViewModel() {

    private val observarEntrenamientosCalendario: ObservarEntrenamientosCalendarioCasoUso =
        contenedor.observarEntrenamientosCalendarioCasoUso

    private val _estado = MutableStateFlow(
        EstadoCalendarioEntrenos(diaSeleccionado = LocalDate.now())
    )
    val estado: StateFlow<EstadoCalendarioEntrenos> = _estado.asStateFlow()

    init {
        cargarMes(_estado.value.mesVisible)
    }

    /**
     * @brief Carga y observa los entrenamientos del mes indicado.
     * @param mes Mes calendario cuyos entrenamientos se desean observar.
     */
    fun cargarMes(mes: YearMonth) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            try {
                val inicio = mes.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val fin = mes.atEndOfMonth().atTime(23, 59, 59)
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                observarEntrenamientosCalendario(inicio, fin).collect { lista ->
                    _estado.update {
                        it.copy(
                            mesVisible = mes,
                            entrenamientos = lista,
                            cargando = false
                        )
                    }
                }
            } catch (excepcion: Exception) {
                _estado.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudieron cargar los entrenamientos del mes."
                    )
                }
            }
        }
    }

    /**
     * @brief Navega al mes siguiente o anterior respecto al visible.
     * @param desplazamiento Número de meses a desplazar (positivo = siguiente).
     */
    fun cambiarMes(desplazamiento: Int) {
        val nuevoMes = _estado.value.mesVisible.plusMonths(desplazamiento.toLong())
        cargarMes(nuevoMes)
    }

    /**
     * @brief Selecciona un día concreto del mes para ver sus entrenamientos.
     * @param fecha Día seleccionado por el usuario.
     */
    fun seleccionarDia(fecha: LocalDate) {
        _estado.update { it.copy(diaSeleccionado = fecha) }
    }

    /**
     * @brief Devuelve los entrenamientos programados en un día concreto.
     * @param fecha Día a consultar.
     * @return Lista de entrenamientos de ese día.
     */
    fun entrenamientosDelDia(fecha: LocalDate): List<Entrenamiento> {
        val epochInicio = fecha.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val epochFin = fecha.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return _estado.value.entrenamientos.filter {
            it.fecha in epochInicio..epochFin && it.fecha != 0L
        }
    }
}