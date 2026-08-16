/**
 * @file GenerarPlanComidasCasoUsoTest.kt
 * @brief Pruebas unitarias del caso de uso de clonación de planes de comidas.
 */
package com.gym.app.domain.usecase.nutricion

import com.gym.app.domain.model.Alimento
import com.gym.app.domain.model.IngredienteToma
import com.gym.app.domain.model.PlanComida
import com.gym.app.domain.model.Toma
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class GenerarPlanComidasCasoUsoTest
 * @brief Verifica que la clonación de un plan base genera identificadores nuevos
 * para el plan, sus tomas y sus ingredientes, conservando el resto de datos.
 */
class GenerarPlanComidasCasoUsoTest {

    private val casoUso = GenerarPlanComidasCasoUso()

    /** Fecha fija de destino del plan clonado (determinista). */
    private val fechaDestino: LocalDate = LocalDate.of(2026, 8, 16)

    /**
     * @brief Construye un alimento de referencia para los ingredientes de prueba.
     */
    private fun crearAlimento(nombre: String): Alimento = Alimento(
        id = "alimento-$nombre",
        nombre = nombre,
        kcalPor100g = 100.0,
        proteinasPor100g = 10.0,
        carbohidratosPor100g = 20.0,
        grasasPor100g = 5.0
    )

    /**
     * @brief Construye un ingrediente de prueba con identificador y gramaje dados.
     */
    private fun crearIngrediente(id: String, nombre: String, gramos: Double): IngredienteToma =
        IngredienteToma(
            id = id,
            alimentoId = "alimento-$nombre",
            nombre = nombre,
            cantidadGramos = gramos,
            pesaje = IngredienteToma.PESAJE_CRUDO,
            origenPlan = true,
            alimentoResuelto = crearAlimento(nombre)
        )

    /**
     * @brief Construye un plan base de dos tomas con dos ingredientes cada una.
     */
    private fun crearPlanBase(): PlanComida {
        val desayuno = Toma(
            id = "toma-desayuno",
            tipoIngesta = Toma.TIPO_DESAYUNO,
            orden = 1,
            ingredientes = listOf(
                crearIngrediente("ing-pan", "Panecillo", 100.0),
                crearIngrediente("ing-pollo", "Pechuga de pollo", 150.0)
            ),
            horaSugerida = "08:00"
        )
        val cena = Toma(
            id = "toma-cena",
            tipoIngesta = Toma.TIPO_CENA,
            orden = 2,
            ingredientes = listOf(
                crearIngrediente("ing-arroz", "Arroz blanco", 200.0),
                crearIngrediente("ing-atun", "Atún al natural", 100.0)
            ),
            horaSugerida = "21:00"
        )
        return PlanComida(
            id = "plan-base-1",
            nombre = "Plan Naturvitia Lunes",
            fecha = LocalDate.of(2026, 8, 10),
            tomas = listOf(desayuno, cena),
            origenImportacion = true
        )
    }

    @Test
    fun `clona el plan asignando la fecha solicitada`() = runTest {
        val planBase = crearPlanBase()

        val resultado = casoUso.ejecutar(planBase, fechaDestino)

        assertTrue(resultado.isSuccess)
        val planClonado = resultado.getOrThrow()
        assertEquals(fechaDestino, planClonado.fecha)
    }

    @Test
    fun `genera un identificador nuevo para el plan clonado`() = runTest {
        val planBase = crearPlanBase()

        val planClonado = casoUso.ejecutar(planBase, fechaDestino).getOrThrow()

        assertNotNull(planClonado.id)
        assertNotEquals(planBase.id, planClonado.id)
    }

    @Test
    fun `genera identificadores nuevos para las tomas y sus ingredientes`() = runTest {
        val planBase = crearPlanBase()

        val planClonado = casoUso.ejecutar(planBase, fechaDestino).getOrThrow()

        assertEquals(planBase.tomas.size, planClonado.tomas.size)
        planBase.tomas.zip(planClonado.tomas).forEach { (original, clonada) ->
            assertNotEquals(original.id, clonada.id)
            assertEquals(original.ingredientes.size, clonada.ingredientes.size)
            original.ingredientes.zip(clonada.ingredientes).forEach { (ingOriginal, ingClonado) ->
                assertNotEquals(ingOriginal.id, ingClonado.id)
            }
        }
    }

    @Test
    fun `conserva los datos del plan original`() = runTest {
        val planBase = crearPlanBase()

        val planClonado = casoUso.ejecutar(planBase, fechaDestino).getOrThrow()

        assertEquals(planBase.nombre, planClonado.nombre)
        assertEquals(planBase.origenImportacion, planClonado.origenImportacion)
        planBase.tomas.zip(planClonado.tomas).forEach { (original, clonada) ->
            assertEquals(original.tipoIngesta, clonada.tipoIngesta)
            assertEquals(original.orden, clonada.orden)
            assertEquals(original.horaSugerida, clonada.horaSugerida)
            original.ingredientes.zip(clonada.ingredientes).forEach { (ingOriginal, ingClonado) ->
                assertEquals(ingOriginal.nombre, ingClonado.nombre)
                assertEquals(ingOriginal.cantidadGramos, ingClonado.cantidadGramos, 0.001)
                assertEquals(ingOriginal.alimentoId, ingClonado.alimentoId)
                assertEquals(ingOriginal.pesaje, ingClonado.pesaje)
                assertEquals(ingOriginal.origenPlan, ingClonado.origenPlan)
            }
        }
    }

    @Test
    fun `preserva los macros calculados del plan original`() = runTest {
        val planBase = crearPlanBase()

        val planClonado = casoUso.ejecutar(planBase, fechaDestino).getOrThrow()

        assertEquals(planBase.kcalTotales, planClonado.kcalTotales, 0.001)
        assertEquals(planBase.proteinasTotalesG, planClonado.proteinasTotalesG, 0.001)
        assertEquals(planBase.carbohidratosTotalesG, planClonado.carbohidratosTotalesG, 0.001)
        assertEquals(planBase.grasasTotalesG, planClonado.grasasTotalesG, 0.001)
    }
}