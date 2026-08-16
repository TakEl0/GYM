/**
 * @file CalcularResumenNutricionalAvanzadoCasoUsoTest.kt
 * @brief Pruebas unitarias del resumen nutricional avanzado a partir de ingestas.
 */
package com.gym.app.domain.usecase.nutricion

import com.gym.app.domain.model.IngestaRegistrada
import com.gym.app.domain.model.ResumenNutricional
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class CalcularResumenNutricionalAvanzadoCasoUsoTest
 * @brief Verifica la suma de macros desde las ingestas registradas y el cálculo
 * de restantes partiendo del resumen de objetivos del día.
 */
class CalcularResumenNutricionalAvanzadoCasoUsoTest {

    private val casoUso = CalcularResumenNutricionalAvanzadoCasoUso()

    /** Fecha fija de las ingestas de prueba (determinista). */
    private val fecha = LocalDate.of(2026, 8, 16)

    /**
     * @brief Construye un resumen de objetivos con los valores objetivo indicados.
     */
    private fun crearObjetivos(
        kcal: Double = 2500.0,
        proteinas: Double = 150.0,
        carbohidratos: Double = 250.0,
        grasas: Double = 60.0
    ): ResumenNutricional = ResumenNutricional(
        kcalConsumidas = 0.0,
        kcalObjetivo = kcal,
        kcalRestantes = kcal,
        proteinasConsumidasG = 0.0,
        proteinasObjetivoG = proteinas,
        carbohidratosConsumidosG = 0.0,
        carbohidratosObjetivoG = carbohidratos,
        grasasConsumidasG = 0.0,
        grasasObjetivoG = grasas
    )

    /**
     * @brief Construye una ingesta registrada con los macros indicados.
     */
    private fun crearIngesta(
        kcal: Double,
        proteinas: Double,
        carbohidratos: Double,
        grasas: Double
    ): IngestaRegistrada = IngestaRegistrada(
        id = "ingesta-${kcal}",
        userId = "usuario-1",
        nombre = "Ingesta de prueba",
        kcal = kcal,
        proteinasG = proteinas,
        carbohidratosG = carbohidratos,
        grasasG = grasas,
        tipoIngesta = "COMIDA",
        fecha = fecha,
        momentoDia = "TARDE",
        origen = IngestaRegistrada.ORIGEN_MANUAL
    )

    @Test
    fun `suma los macros consumidos de todas las ingestas`() = runTest {
        val objetivos = crearObjetivos()
        val ingestas = listOf(
            crearIngesta(kcal = 400.0, proteinas = 30.0, carbohidratos = 45.0, grasas = 10.0),
            crearIngesta(kcal = 600.0, proteinas = 50.0, carbohidratos = 70.0, grasas = 15.0)
        )

        val resumen = casoUso.ejecutar(objetivos, ingestas).getOrThrow()

        assertEquals(1000.0, resumen.kcalConsumidas, 0.001)
        assertEquals(80.0, resumen.proteinasConsumidasG, 0.001)
        assertEquals(115.0, resumen.carbohidratosConsumidosG, 0.001)
        assertEquals(25.0, resumen.grasasConsumidasG, 0.001)
    }

    @Test
    fun `suma macros con decimales y redondeo natural del dominio`() = runTest {
        val objetivos = crearObjetivos()
        val ingestas = listOf(
            crearIngesta(kcal = 100.5, proteinas = 12.25, carbohidratos = 10.4, grasas = 3.1),
            crearIngesta(kcal = 50.25, proteinas = 8.75, carbohidratos = 20.6, grasas = 2.9)
        )

        val resumen = casoUso.ejecutar(objetivos, ingestas).getOrThrow()

        assertEquals(150.75, resumen.kcalConsumidas, 0.001)
        assertEquals(21.0, resumen.proteinasConsumidasG, 0.001)
        assertEquals(31.0, resumen.carbohidratosConsumidosG, 0.001)
        assertEquals(6.0, resumen.grasasConsumidasG, 0.001)
    }

    @Test
    fun `calcula los restantes como objetivo menos consumido`() = runTest {
        val objetivos = crearObjetivos(kcal = 2500.0)
        val ingestas = listOf(
            crearIngesta(kcal = 800.0, proteinas = 60.0, carbohidratos = 90.0, grasas = 20.0)
        )

        val resumen = casoUso.ejecutar(objetivos, ingestas).getOrThrow()

        assertEquals(1700.0, resumen.kcalRestantes, 0.001)
        assertEquals(90.0, resumen.proteinasObjetivoG - resumen.proteinasConsumidasG, 0.001)
    }

    @Test
    fun `los restantes pueden ser negativos al superar el objetivo`() = runTest {
        val objetivos = crearObjetivos(kcal = 2500.0)
        val ingestas = listOf(
            crearIngesta(kcal = 3000.0, proteinas = 180.0, carbohidratos = 300.0, grasas = 80.0)
        )

        val resumen = casoUso.ejecutar(objetivos, ingestas).getOrThrow()

        assertEquals(-500.0, resumen.kcalRestantes, 0.001)
        // El progreso porcentual queda limitado a 100 por el propio modelo.
        assertEquals(100, resumen.progresoPorcentaje)
    }

    @Test
    fun `sin ingestas el resumen conserva los objetivos`() = runTest {
        val objetivos = crearObjetivos()

        val resumen = casoUso.ejecutar(objetivos, emptyList()).getOrThrow()

        assertEquals(0.0, resumen.kcalConsumidas, 0.001)
        assertEquals(2500.0, resumen.kcalRestantes, 0.001)
        assertEquals(0, resumen.progresoPorcentaje)
        assertTrue(resumen.kcalObjetivo > 0.0)
    }
}