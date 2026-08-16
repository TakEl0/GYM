/**
 * @file ListaCompraViewModel.kt
 * @brief ViewModel de la pantalla de Lista de la Compra de la aplicación GYM.
 * Observa de forma reactiva las listas semanales, genera una nueva lista
 * consolidada a partir de los planes de comidas de la semana mediante
 * [GenerarListaCompraSemanalCasoUso] y permite marcar cada ítem como comprado.
 */
package com.gym.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.app.data.repository.RepositorioListaCompraFake
import com.gym.app.data.repository.RepositorioPlanComidaFake
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.ListaCompra
import com.gym.app.domain.repository.RepositorioListaCompra
import com.gym.app.domain.repository.RepositorioPlanComida
import com.gym.app.domain.usecase.compra.GenerarListaCompraSemanalCasoUso
import com.gym.app.domain.usecase.compra.MarcarItemCompradoCasoUso
import java.time.LocalDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @data class EstadoListaCompra
 * @brief Estado inmutable de la pantalla de Lista de la Compra.
 * @property listas Listas de la compra consolidadas por semana.
 * @property generando Indica si la generación de la lista semanal está en curso.
 * @property cargando Indica si los datos están en proceso de carga.
 * @property error Mensaje de error si alguna operación falló (null en caso normal).
 */
data class EstadoListaCompra(
    val listas: List<ListaCompra> = emptyList(),
    val generando: Boolean = false,
    val cargando: Boolean = true,
    val error: String? = null
) {
    /**
     * @brief Lista de la compra más reciente (la de la última semana generada).
     * Se emplea como lista visible por defecto en la pantalla.
     */
    val listaMasReciente: ListaCompra?
        get() = listas.maxByOrNull { it.semanaInicio }
}

/**
 * @class ListaCompraViewModel
 * @brief Gestiona el estado de la pantalla de Lista de la Compra.
 *
 * Observa las listas semanales del [RepositorioListaCompra]. La generación de la
 * lista semanal consulta los planes de comidas de la semana actual (lunes a
 * domingo) mediante [RepositorioPlanComida.observarPlanesEntre], los consolida con
 * [GenerarListaCompraSemanalCasoUso] y persiste el resultado con
 * [RepositorioListaCompra.guardarLista]. El marcado de ítems delega en
 * [MarcarItemCompradoCasoUso], construido internamente sobre el mismo repositorio
 * inyectado para mantener la coherencia de la fuente de datos.
 *
 * Constructores según el patrón del proyecto: primario inyectable (fakes por
 * defecto para pruebas) y secundario que resuelve las dependencias desde el
 * [ContenedorDependencias].
 */
class ListaCompraViewModel(
    private val repositorioListaCompra: RepositorioListaCompra = RepositorioListaCompraFake(),
    private val repositorioPlanComida: RepositorioPlanComida = RepositorioPlanComidaFake(),
    private val generarListaCompraSemanalCasoUso: GenerarListaCompraSemanalCasoUso =
        GenerarListaCompraSemanalCasoUso(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    /** Caso de uso de marcado de ítems, construido sobre el repositorio inyectado. */
    private val marcarItemCompradoCasoUso = MarcarItemCompradoCasoUso(repositorioListaCompra, dispatcher)

    /**
     * @brief Constructor secundario que resuelve las dependencias reales desde
     * el [ContenedorDependencias] (inyección manual).
     * @param contenedor Contenedor de dependencias de la aplicación.
     */
    constructor(contenedor: ContenedorDependencias) : this(
        repositorioListaCompra = contenedor.repositorioListaCompra,
        repositorioPlanComida = contenedor.repositorioPlanComida,
        generarListaCompraSemanalCasoUso = contenedor.generarListaCompraSemanalCasoUso
    )

    private val _estado = MutableStateFlow(EstadoListaCompra())
    val estado: StateFlow<EstadoListaCompra> = _estado.asStateFlow()

    init {
        iniciarObservacion()
    }

    /**
     * @brief Inicia la observación reactiva de las listas de la compra del usuario.
     * Cada emisión del repositorio actualiza el estado con las listas disponibles.
     */
    fun iniciarObservacion() {
        viewModelScope.launch {
            repositorioListaCompra.observarListas().collect { listas ->
                _estado.update { it.copy(listas = listas, cargando = false) }
            }
        }
    }

    /**
     * @brief Genera la lista de la compra de la semana actual.
     *
     * Consulta los planes de comidas comprendidos entre el lunes y el domingo de
     * la semana en curso, los consolida con [GenerarListaCompraSemanalCasoUso] y
     * persiste la lista resultante. Si no existen planes en la semana, el caso de
     * uso devuelve una lista vacía que igualmente se guarda.
     */
    fun generarListaSemanal() {
        viewModelScope.launch {
            _estado.update { it.copy(generando = true, error = null) }
            try {
                val hoy = LocalDate.now()
                val inicioSemana = hoy.minusDays((hoy.dayOfWeek.value - 1).toLong())
                val finSemana = inicioSemana.plusDays(6)
                val planesSemana = repositorioPlanComida
                    .observarPlanesEntre(inicioSemana, finSemana)
                    .first()

                generarListaCompraSemanalCasoUso.ejecutar(planesSemana)
                    .onSuccess { lista ->
                        repositorioListaCompra.guardarLista(lista)
                        _estado.update { it.copy(generando = false) }
                    }
                    .onFailure { excepcion ->
                        _estado.update {
                            it.copy(
                                generando = false,
                                error = excepcion.message ?: "No se pudo generar la lista de la compra."
                            )
                        }
                    }
            } catch (excepcion: Exception) {
                _estado.update {
                    it.copy(
                        generando = false,
                        error = "No se pudo generar la lista de la compra: ${excepcion.message}"
                    )
                }
            }
        }
    }

    /**
     * @brief Marca o desmarca un ítem de una lista como comprado.
     * @param listaId Identificador de la lista que contiene el ítem.
     * @param itemId Identificador del ítem a actualizar.
     * @param comprado Nuevo estado de compra (`true` = comprado).
     */
    fun marcarItem(listaId: String, itemId: String, comprado: Boolean) {
        viewModelScope.launch {
            marcarItemCompradoCasoUso.ejecutar(listaId, itemId, comprado)
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(error = excepcion.message ?: "No se pudo actualizar el ítem.")
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