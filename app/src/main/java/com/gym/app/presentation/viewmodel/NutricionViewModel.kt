/**
 * @file NutricionViewModel.kt
 * @brief ViewModel de la pantalla de Nutrición de la aplicación GYM.
 * Combina de forma reactiva el plan de comidas del día, las ingestas realmente
 * consumidas y el resumen nutricional (planificado frente a consumido), y expone
 * la acción de rebalanceo intra-día del método Naturvitia mediante
 * [RebalancearComidasPendientesCasoUso].
 */
package com.gym.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.app.data.repository.RepositorioIngestaFake
import com.gym.app.data.repository.RepositorioPlanComidaFake
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.AjusteToma
import com.gym.app.domain.model.IngestaRegistrada
import com.gym.app.domain.model.PlanComida
import com.gym.app.domain.model.ResumenNutricional
import com.gym.app.domain.repository.RepositorioIngesta
import com.gym.app.domain.repository.RepositorioPlanComida
import com.gym.app.domain.usecase.nutricion.CalcularResumenNutricionalAvanzadoCasoUso
import com.gym.app.domain.usecase.nutricion.RebalancearComidasPendientesCasoUso
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @data class EstadoNutricion
 * @brief Estado inmutable de la pantalla de Nutrición.
 * @property planHoy Plan de comidas del día (puede ser null si no existe).
 * @property ingestasHoy Ingestas realmente consumidas hoy.
 * @property resumen Resumen nutricional planificado frente a consumido (null si
 * no hay plan para hoy).
 * @property ajustesRebalanceo Ajustes propuestos por el motor de rebalanceo.
 * @property rebalanceando Indica si el rebalanceo está en curso.
 * @property cargando Indica si los datos están en proceso de carga.
 * @property error Mensaje de error si alguna operación falló (null en caso normal).
 */
data class EstadoNutricion(
    val planHoy: PlanComida? = null,
    val ingestasHoy: List<IngestaRegistrada> = emptyList(),
    val resumen: ResumenNutricional? = null,
    val ajustesRebalanceo: List<AjusteToma> = emptyList(),
    val rebalanceando: Boolean = false,
    val cargando: Boolean = true,
    val error: String? = null
)

/**
 * @class NutricionViewModel
 * @brief Gestiona el estado de la pantalla de Nutrición de la aplicación GYM.
 *
 * Observa en paralelo el [PlanComida] de hoy (fuente de verdad de lo planificado)
 * y las [IngestaRegistrada] del día (lo realmente consumido). Con ambos datos
 * construye el [ResumenNutricional] mediante
 * [CalcularResumenNutricionalAvanzadoCasoUso]; si este caso de uso fallara, se
 * calcula la suma directamente como red de seguridad.
 *
 * La acción "Rebalancear" invoca [RebalancearComidasPendientesCasoUso] con el
 * plan y las ingestas actuales y expone los [AjusteToma] propuestos para que la
 * interfaz los muestre al usuario.
 *
 * Los constructores siguen el patrón del proyecto: un constructor primario con
 * dependencias inyectables (con fakes por defecto para pruebas) y un constructor
 * secundario que resuelve las dependencias reales desde el
 * [ContenedorDependencias].
 */
class NutricionViewModel(
    private val repositorioPlanComida: RepositorioPlanComida = RepositorioPlanComidaFake(),
    private val repositorioIngesta: RepositorioIngesta = RepositorioIngestaFake(),
    private val rebalancearComidasPendientesCasoUso: RebalancearComidasPendientesCasoUso =
        RebalancearComidasPendientesCasoUso(),
    private val calcularResumenNutricionalAvanzadoCasoUso: CalcularResumenNutricionalAvanzadoCasoUso =
        CalcularResumenNutricionalAvanzadoCasoUso()
) : ViewModel() {

    /**
     * @brief Constructor secundario que resuelve las dependencias reales desde
     * el [ContenedorDependencias] (inyección manual).
     * @param contenedor Contenedor de dependencias de la aplicación.
     */
    constructor(contenedor: ContenedorDependencias) : this(
        repositorioPlanComida = contenedor.repositorioPlanComida,
        repositorioIngesta = contenedor.repositorioIngesta,
        rebalancearComidasPendientesCasoUso = contenedor.rebalancearComidasPendientesCasoUso,
        calcularResumenNutricionalAvanzadoCasoUso = contenedor.calcularResumenNutricionalAvanzadoCasoUso
    )

    private val _estado = MutableStateFlow(EstadoNutricion())
    val estado: StateFlow<EstadoNutricion> = _estado.asStateFlow()

    /** Fecha de referencia del día observado (hoy). */
    private val fechaHoy: LocalDate = LocalDate.now()

    init {
        iniciarObservacion()
    }

    /**
     * @brief Inicia la observación reactiva del plan de comidas y de las ingestas
     * del día. Cada vez que cualquiera de los dos flujos emite un nuevo valor, se
     * recalcula el resumen nutricional y se actualiza el estado.
     */
    fun iniciarObservacion() {
        viewModelScope.launch {
            repositorioPlanComida.observarPlanDeHoy(fechaHoy)
                .combine(repositorioIngesta.observarIngestasDelDia(fechaHoy)) { plan, ingestas ->
                    plan to ingestas
                }
                .collect { (plan, ingestas) ->
                    val resumen = plan?.let { construirResumen(it, ingestas) }
                    _estado.update { estado ->
                        estado.copy(
                            planHoy = plan,
                            ingestasHoy = ingestas,
                            resumen = resumen,
                            cargando = false,
                            error = null
                        )
                    }
                }
        }
    }

    /**
     * @brief Invoca el motor de rebalanceo intra-día con el plan de hoy y las
     * ingestas consumidas. Si no existe plan de hoy, la acción no hace nada.
     * Los ajustes propuestos se guardan en [EstadoNutricion.ajustesRebalanceo].
     */
    fun rebalancear() {
        val plan = _estado.value.planHoy ?: return
        val ingestas = _estado.value.ingestasHoy
        viewModelScope.launch {
            _estado.update { it.copy(rebalanceando = true, error = null) }
            rebalancearComidasPendientesCasoUso.ejecutar(plan, ingestas)
                .onSuccess { ajustes ->
                    _estado.update { it.copy(ajustesRebalanceo = ajustes, rebalanceando = false) }
                }
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(
                            rebalanceando = false,
                            error = excepcion.message ?: "No se pudo rebalancear el plan de comidas."
                        )
                    }
                }
        }
    }

    /**
     * @brief Descarta los ajustes de rebalanceo mostrados actualmente en pantalla.
     * Se invoca cuando el usuario cierra el panel de resultados del rebalanceo.
     */
    fun descartarAjustes() {
        _estado.update { it.copy(ajustesRebalanceo = emptyList()) }
    }

    /**
     * @brief Construye el [ResumenNutricional] del día a partir del plan (objetivos)
     * y de las ingestas consumidas.
     *
     * Se delega en [CalcularResumenNutricionalAvanzadoCasoUso] construyendo
     * previamente el resumen de objetivos con los totales planificados del
     * [PlanComida]. Si el caso de uso devuelve error, se calcula la suma directa
     * de las ingestas como red de seguridad para no dejar la pantalla sin datos.
     *
     * @param plan Plan de comidas de hoy (fuente de los objetivos).
     * @param ingestas Ingestas realmente consumidas en el día.
     * @return [ResumenNutricional] con consumidos, objetivos y restantes.
     */
    private suspend fun construirResumen(
        plan: PlanComida,
        ingestas: List<IngestaRegistrada>
    ): ResumenNutricional {
        val objetivos = ResumenNutricional(
            kcalConsumidas = 0.0,
            kcalObjetivo = plan.kcalTotales,
            kcalRestantes = plan.kcalTotales,
            proteinasConsumidasG = 0.0,
            proteinasObjetivoG = plan.proteinasTotalesG,
            carbohidratosConsumidosG = 0.0,
            carbohidratosObjetivoG = plan.carbohidratosTotalesG,
            grasasConsumidasG = 0.0,
            grasasObjetivoG = plan.grasasTotalesG
        )
        return calcularResumenNutricionalAvanzadoCasoUso.ejecutar(objetivos, ingestas)
            .getOrElse {
                // Red de seguridad: suma directa de las ingestas consumidas.
                ResumenNutricional(
                    kcalConsumidas = ingestas.sumOf { ingesta -> ingesta.kcal },
                    kcalObjetivo = plan.kcalTotales,
                    kcalRestantes = plan.kcalTotales - ingestas.sumOf { ingesta -> ingesta.kcal },
                    proteinasConsumidasG = ingestas.sumOf { ingesta -> ingesta.proteinasG },
                    proteinasObjetivoG = plan.proteinasTotalesG,
                    carbohidratosConsumidosG = ingestas.sumOf { ingesta -> ingesta.carbohidratosG },
                    carbohidratosObjetivoG = plan.carbohidratosTotalesG,
                    grasasConsumidasG = ingestas.sumOf { ingesta -> ingesta.grasasG },
                    grasasObjetivoG = plan.grasasTotalesG
                )
            }
    }
}