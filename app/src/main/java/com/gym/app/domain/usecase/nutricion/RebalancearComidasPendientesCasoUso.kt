/**
 * @file RebalancearComidasPendientesCasoUso.kt
 * @brief Caso de uso del motor de rebalanceo intra-día del método Naturvitia.
 */
package com.gym.app.domain.usecase.nutricion

import com.gym.app.domain.model.AjusteToma
import com.gym.app.domain.model.Alimento
import com.gym.app.domain.model.DesvioNutricional
import com.gym.app.domain.model.IngestaRegistrada
import com.gym.app.domain.model.IngredienteToma
import com.gym.app.domain.model.PlanComida
import com.gym.app.domain.model.Toma
import java.util.UUID

/**
 * @class RebalancearComidasPendientesCasoUso
 * @brief Evalúa en tiempo real el desvío entre lo planificado y lo consumido en
 * el día y, si supera la tolerancia permitida, propone ajustes sobre las tomas
 * que aún no se han consumido para volver a alinear el plan con el objetivo.
 *
 * **Algoritmo** (reglas del método Naturvitia):
 * 1. Calcula el total planificado del día (todas las tomas del plan) y el total
 *    consumido (suma de las ingestas registradas).
 * 2. Calcula el desvío Δ = planificado - consumido por macronutriente mediante
 *    [DesvioNutricional.calcular].
 * 3. Si el desvío está dentro de tolerancia (5 % o ±10 g), devuelve una lista vacía.
 * 4. Si el desvío supera la tolerancia, genera correcciones **solo** sobre las
 *    tomas restantes del día (aquellas cuyo tipo de ingesta no ha sido cubierto
 *    por ninguna ingesta registrada, es decir, todavía pendientes de consumir):
 *    - **Exceso de carbohidratos** (Δ CH < 0): elimina los ingredientes ricos en
 *      CH (boniato, patata, pan, arroz, pasta) de la merienda y la cena, manteniendo
 *      intactos los ingredientes proteicos.
 *    - **Déficit de proteína** (Δ proteína < 0): añade un [IngredienteToma] nuevo
 *      de alta proteína (atún al natural, queso fresco 0 % o EvoWhey) a la merienda
 *      (o a la primera toma pendiente si la merienda ya se consumió).
 *    - **Exceso de grasas** (Δ grasas < 0): elimina el AOVE o los frutos secos y
 *      el aguacate de las tomas pendientes, priorizando la cena.
 *
 * Todas las descripciones de cambio se generan en castellano para que puedan
 * mostrarse directamente al usuario. El caso de uso es de lógica pura: no depende
 * de repositorios ni de frameworks.
 */
class RebalancearComidasPendientesCasoUso {

    /** Palabras clave que identifican ingredientes ricos en carbohidratos. */
    private val ingredientesRicosEnCh = listOf("boniato", "patata", "pan", "arroz", "pasta")

    /** Palabras clave que identifican ingredientes ricos en grasas. */
    private val ingredientesRicosEnGrasa = listOf(
        "aove", "aceite de oliva", "frutos secos", "aguacate"
    )

    /** Ración de atún al natural en gramos usada en la corrección de proteína. */
    private val gramosAtunAlNatural = 100.0

    /**
     * Alimento de referencia del atún al natural en lata, usado como corrección
     * proteica del rebalanceo (tabla nutricional aproximada por 100 g).
     */
    private val alimentoAtunAlNatural = Alimento(
        id = "alimento_rebalanceo_atun_natural",
        nombre = "Atún al natural (lata)",
        kcalPor100g = 116.0,
        proteinasPor100g = 25.5,
        carbohidratosPor100g = 0.0,
        grasasPor100g = 1.0,
        unidadComercial = "lata",
        gramosPorUnidadComercial = 100.0,
        ingredienteFijo = false
    )

    /**
     * @brief Rebalancea las tomas pendientes del día según el desvío nutricional.
     * @param planHoy Plan de comidas del día (fuente de verdad de lo planificado).
     * @param ingestasRegistradas Ingestas realmente consumidas hasta el momento.
     * @return [Result] con la lista de [AjusteToma] propuestos (vacía si el desvío
     * está dentro de tolerancia o si no fue posible generar cambios).
     */
    suspend fun ejecutar(
        planHoy: PlanComida,
        ingestasRegistradas: List<IngestaRegistrada>
    ): Result<List<AjusteToma>> {
        // (a) Cálculo del desvío agregado entre lo planificado y lo consumido.
        val desvio = DesvioNutricional.calcular(planHoy.tomas, ingestasRegistradas)

        // (b) y (c) Si el desvío está dentro de tolerancia, no se propone ningún ajuste.
        if (desvio.dentroTolerancia) return Result.success(emptyList())

        // (d) Tomas restantes: aquellas cuyo tipo de ingesta aún no ha sido consumido.
        val tiposConsumidos = ingestasRegistradas.map { it.tipoIngesta }.toSet()
        val tomasPendientes = planHoy.tomas
            .filter { it.tipoIngesta !in tiposConsumidos }
            .sortedBy { it.orden }

        if (tomasPendientes.isEmpty()) return Result.success(emptyList())

        // Copia de trabajo de las tomas del día que se irá modificando.
        val tomasTrabajo = planHoy.tomas.toMutableList()
        val cambios = mutableListOf<String>()

        // Exceso de carbohidratos: eliminar fuentes de CH de merienda y cena.
        if (desvio.carbohidratosG < 0.0) {
            aplicarCorreccionCarbohidratos(tomasPendientes, tomasTrabajo, cambios)
        }

        // Déficit de proteína: añadir una fuente proteica a una toma pendiente.
        if (desvio.proteinasG < 0.0) {
            aplicarCorreccionProteinas(tomasPendientes, tomasTrabajo, cambios)
        }

        // Exceso de grasas: eliminar AOVE, frutos secos o aguacate.
        if (desvio.grasasG < 0.0) {
            aplicarCorreccionGrasas(tomasPendientes, tomasTrabajo, cambios)
        }

        if (cambios.isEmpty()) return Result.success(emptyList())

        val tipoPrincipal = tomasPendientes.first().tipoIngesta
        return Result.success(
            listOf(
                AjusteToma(
                    tipoIngesta = tipoPrincipal,
                    cambios = cambios,
                    tomasRevisadas = tomasTrabajo.toList()
                )
            )
        )
    }

    /**
     * @brief Corrige el exceso de carbohidratos eliminando los ingredientes ricos
     * en CH de la merienda y de la cena (manteniendo la proteína intacta).
     * @param tomasPendientes Tomas del día aún no consumidas.
     * @param tomasTrabajo Lista de trabajo mutable de las tomas del día.
     * @param cambios Lista acumuladora de descripciones de cambio.
     */
    private fun aplicarCorreccionCarbohidratos(
        tomasPendientes: List<Toma>,
        tomasTrabajo: MutableList<Toma>,
        cambios: MutableList<String>
    ) {
        val tomasObjetivo = tomasPendientes.filter {
            it.tipoIngesta == Toma.TIPO_MERIENDA || it.tipoIngesta == Toma.TIPO_CENA
        }
        for (toma in tomasObjetivo) {
            val ingredientesRicos = toma.ingredientes.filter { esRicoEnCh(it) }
            if (ingredientesRicos.isEmpty()) continue

            val nuevosIngredientes = toma.ingredientes.filterNot { esRicoEnCh(it) }
            cambios += ingredientesRicos.map {
                "Se elimina el ${it.nombre.lowercase()} de la ${tipoIngestaLegible(toma.tipoIngesta)} " +
                    "para compensar el exceso de carbohidratos."
            }
            reemplazarToma(tomasTrabajo, toma.copy(ingredientes = nuevosIngredientes))
        }
    }

    /**
     * @brief Corrige el déficit de proteína añadiendo un ingrediente de alta
     * proteína (atún al natural en lata, 100 g) a la merienda pendiente o, si la
     * merienda ya se consumió, a la primera toma pendiente del día.
     * @param tomasPendientes Tomas del día aún no consumidas.
     * @param tomasTrabajo Lista de trabajo mutable de las tomas del día.
     * @param cambios Lista acumuladora de descripciones de cambio.
     */
    private fun aplicarCorreccionProteinas(
        tomasPendientes: List<Toma>,
        tomasTrabajo: MutableList<Toma>,
        cambios: MutableList<String>
    ) {
        val tomaObjetivo = tomasPendientes.firstOrNull { it.tipoIngesta == Toma.TIPO_MERIENDA }
            ?: tomasPendientes.first()

        val nuevoIngrediente = IngredienteToma(
            id = UUID.randomUUID().toString(),
            alimentoId = alimentoAtunAlNatural.id,
            nombre = alimentoAtunAlNatural.nombre,
            cantidadGramos = gramosAtunAlNatural,
            pesaje = IngredienteToma.PESAJE_CRUDO,
            origenPlan = false,
            alimentoResuelto = alimentoAtunAlNatural
        )

        cambios += "Se añade ${alimentoAtunAlNatural.nombre} " +
            "(${gramosAtunAlNatural.toInt()} g) a la ${tipoIngestaLegible(tomaObjetivo.tipoIngesta)} " +
            "para compensar el déficit de proteína."
        reemplazarToma(
            tomasTrabajo,
            tomaObjetivo.copy(ingredientes = tomaObjetivo.ingredientes + nuevoIngrediente)
        )
    }

    /**
     * @brief Corrige el exceso de grasas eliminando el AOVE, los frutos secos o
     * el aguacate de las tomas pendientes, priorizando la cena.
     * @param tomasPendientes Tomas del día aún no consumidas.
     * @param tomasTrabajo Lista de trabajo mutable de las tomas del día.
     * @param cambios Lista acumuladora de descripciones de cambio.
     */
    private fun aplicarCorreccionGrasas(
        tomasPendientes: List<Toma>,
        tomasTrabajo: MutableList<Toma>,
        cambios: MutableList<String>
    ) {
        val tomasObjetivo = tomasPendientes.sortedBy {
            if (it.tipoIngesta == Toma.TIPO_CENA) 0 else 1
        }
        for (toma in tomasObjetivo) {
            val ingredientesRicos = toma.ingredientes.filter { esRicoEnGrasa(it) }
            if (ingredientesRicos.isEmpty()) continue

            val nuevosIngredientes = toma.ingredientes.filterNot { esRicoEnGrasa(it) }
            cambios += ingredientesRicos.map {
                "Se elimina el ${it.nombre.lowercase()} de la ${tipoIngestaLegible(toma.tipoIngesta)} " +
                    "para compensar el exceso de grasas."
            }
            reemplazarToma(tomasTrabajo, toma.copy(ingredientes = nuevosIngredientes))
        }
    }

    /**
     * @brief Reemplaza en la lista de trabajo la toma que comparte identificador.
     * @param tomasTrabajo Lista de trabajo mutable de las tomas del día.
     * @param tomaNueva Toma modificada que sustituye a la original.
     */
    private fun reemplazarToma(tomasTrabajo: MutableList<Toma>, tomaNueva: Toma) {
        val indice = tomasTrabajo.indexOfFirst { it.id == tomaNueva.id }
        if (indice >= 0) tomasTrabajo[indice] = tomaNueva
    }

    /**
     * @brief Determina si un ingrediente es rico en carbohidratos.
     * @param ingrediente Ingrediente a evaluar.
     * @return `true` si su nombre contiene alguna palabra clave de CH.
     */
    private fun esRicoEnCh(ingrediente: IngredienteToma): Boolean =
        ingredientesRicosEnCh.any { palabra ->
            ingrediente.nombre.contains(palabra, ignoreCase = true)
        }

    /**
     * @brief Determina si un ingrediente es rico en grasas.
     * @param ingrediente Ingrediente a evaluar.
     * @return `true` si su nombre contiene alguna palabra clave de grasa.
     */
    private fun esRicoEnGrasa(ingrediente: IngredienteToma): Boolean =
        ingredientesRicosEnGrasa.any { palabra ->
            ingrediente.nombre.contains(palabra, ignoreCase = true)
        }

    /**
     * @brief Traduce el tipo de ingesta a texto legible en castellano.
     * @param tipo Tipo de ingesta (constante de [Toma]).
     * @return Descripción legible (p. ej. "merienda", "cena").
     */
    private fun tipoIngestaLegible(tipo: String): String = when (tipo) {
        Toma.TIPO_DESAYUNO -> "desayuno"
        Toma.TIPO_MEDIA_MAÑANA -> "media mañana"
        Toma.TIPO_COMIDA -> "comida"
        Toma.TIPO_MERIENDA -> "merienda"
        Toma.TIPO_CENA -> "cena"
        Toma.TIPO_POST_ENTRENO -> "post-entreno"
        else -> tipo.lowercase()
    }
}