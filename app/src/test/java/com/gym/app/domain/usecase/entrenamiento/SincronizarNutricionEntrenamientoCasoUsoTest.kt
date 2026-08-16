/**
 * @file SincronizarNutricionEntrenamientoCasoUsoTest.kt
 * @brief Pruebas unitarias de la sincronización nutrición-entrenamiento.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.Alimento
import com.gym.app.domain.model.IngredienteToma
import com.gym.app.domain.model.PlanComida
import com.gym.app.domain.model.Rutina
import com.gym.app.domain.model.Toma
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class SincronizarNutricionEntrenamientoCasoUsoTest
 * @brief Verifica la heurística de programación semanal: los días de mayor ingesta
 * calórica y de carbohidratos se marcan como alta demanda y los de menor ingesta
 * como baja demanda (cardio, movilidad o descanso).
 *
 * Fechas de la semana de prueba: 10/08/2026 (lunes) a 16/08/2026 (domingo).
 */
class SincronizarNutricionEntrenamientoCasoUsoTest {

    private val casoUso = SincronizarNutricionEntrenamientoCasoUso()

    /** Lunes 10 de agosto de 2026. */
    private val lunes = LocalDate.of(2026, 8, 10)

    /** Martes 11 de agosto de 2026. */
    private val martes = LocalDate.of(2026, 8, 11)

    /** Miércoles 12 de agosto de 2026. */
    private val miercoles = LocalDate.of(2026, 8, 12)

    /** Domingo 16 de agosto de 2026. */
    private val domingo = LocalDate.of(2026, 8, 16)

    /**
     * @brief Construye un plan diario con las kilocalorías y carbohidratos indicados.
     * Se escala el alimento base (100 kcal por 100 g) para que el plan acumule
     * exactamente las kilocalorías y los gramos de CH solicitados.
     */
    private fun crearPlan(fecha: LocalDate, kcal: Double, carbohidratos: Double): PlanComida {
        val gramos = kcal
        val chPor100 = 100.0 * carbohidratos / kcal
        val alimento = Alimento(
            id = "alimento-$fecha",
            nombre = "Alimento base",
            kcalPor100g = 100.0,
            proteinasPor100g = 0.0,
            carbohidratosPor100g = chPor100,
            grasasPor100g = 0.0
        )
        val ingrediente = IngredienteToma(
            id = "ingrediente-$fecha",
            alimentoId = alimento.id,
            nombre = "Base",
            cantidadGramos = gramos,
            pesaje = IngredienteToma.PESAJE_CRUDO,
            alimentoResuelto = alimento
        )
        val toma = Toma(
            id = "toma-$fecha",
            tipoIngesta = Toma.TIPO_COMIDA,
            orden = 1,
            ingredientes = listOf(ingrediente)
        )
        return PlanComida(
            id = "plan-$fecha",
            nombre = "Plan de $fecha",
            fecha = fecha,
            tomas = listOf(toma)
        )
    }

    @Test
    fun `sin planes devuelve lista de sugerencias vacia`() = runTest {
        val resultado = casoUso.ejecutar(planes = emptyList(), rutinas = emptyList())

        assertTrue(resultado.isSuccess)
        assertTrue(resultado.getOrThrow().isEmpty())
    }

    @Test
    fun `marca el dia de maxima ingesta como alta demanda y el de minima como baja`() = runTest {
        // Lunes: máxima ingesta (3000 kcal y 300 g de CH).
        // Miércoles: mínima ingesta (2000 kcal y 200 g de CH).
        // Domingo: ingesta intermedia (2500 kcal y 250 g de CH).
        val planes = listOf(
            crearPlan(lunes, kcal = 3000.0, carbohidratos = 300.0),
            crearPlan(miercoles, kcal = 2000.0, carbohidratos = 200.0),
            crearPlan(domingo, kcal = 2500.0, carbohidratos = 250.0)
        )

        val sugerencias = casoUso.ejecutar(planes, rutinas = emptyList()).getOrThrow()

        assertEquals(2, sugerencias.size)
        assertTrue(
            sugerencias.any {
                it.contains("alta demanda") && it.contains("LUNES")
            }
        )
        assertTrue(
            sugerencias.any {
                it.contains("cardio, movilidad o descanso") && it.contains("MIÉRCOLES")
            }
        )
    }

    @Test
    fun `desempata los dias de maxima ingesta por el de mas carbohidratos`() = runTest {
        // Lunes y martes empatan en kilocalorías (3000), pero el lunes tiene más CH.
        val planes = listOf(
            crearPlan(lunes, kcal = 3000.0, carbohidratos = 300.0),
            crearPlan(martes, kcal = 3000.0, carbohidratos = 250.0),
            crearPlan(miercoles, kcal = 2000.0, carbohidratos = 200.0)
        )

        val sugerencias = casoUso.ejecutar(planes, rutinas = emptyList()).getOrThrow()

        assertTrue(
            sugerencias.any {
                it.contains("alta demanda") && it.contains("LUNES") && !it.contains("MARTES")
            }
        )
        assertTrue(
            sugerencias.any {
                it.contains("cardio, movilidad o descanso") && it.contains("MIÉRCOLES")
            }
        )
    }

    @Test
    fun `la lista de rutinas no altera el resultado actual de la heuristica`() = runTest {
        val planes = listOf(
            crearPlan(lunes, kcal = 3000.0, carbohidratos = 300.0),
            crearPlan(miercoles, kcal = 2000.0, carbohidratos = 200.0)
        )
        val rutinas = listOf(
            Rutina(
                id = "rutina-1",
                nombre = "PPL - Pecho",
                diasSemana = listOf(Rutina.LUNES)
            )
        )

        val sugerencias = casoUso.ejecutar(planes, rutinas).getOrThrow()

        assertTrue(sugerencias.any { it.contains("LUNES") })
        assertTrue(sugerencias.any { it.contains("MIÉRCOLES") })
    }
}