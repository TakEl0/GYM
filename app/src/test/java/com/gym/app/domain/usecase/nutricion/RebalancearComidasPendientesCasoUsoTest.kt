/**
 * @file RebalancearComidasPendientesCasoUsoTest.kt
 * @brief Pruebas unitarias del motor de rebalanceo intra-día del método Naturvitia.
 */
package com.gym.app.domain.usecase.nutricion

import com.gym.app.domain.model.Alimento
import com.gym.app.domain.model.IngestaRegistrada
import com.gym.app.domain.model.IngredienteToma
import com.gym.app.domain.model.PlanComida
import com.gym.app.domain.model.Toma
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class RebalancearComidasPendientesCasoUsoTest
 * @brief Verifica el cálculo del desvío nutricional y la generación de ajustes
 * sobre las tomas pendientes del día según las reglas del método Naturvitia.
 */
class RebalancearComidasPendientesCasoUsoTest {

    private val casoUso = RebalancearComidasPendientesCasoUso()

    /** Fecha fija de los datos de prueba (determinista). */
    private val fecha = LocalDate.of(2026, 8, 16)

    /**
     * @brief Construye un alimento de referencia con los macros indicados por 100 g.
     */
    private fun crearAlimento(
        proteinas: Double = 0.0,
        carbohidratos: Double = 0.0,
        grasas: Double = 0.0,
        kcal: Double = 0.0
    ): Alimento = Alimento(
        id = "alimento-${UUID.randomUUID()}",
        nombre = "Alimento de prueba",
        kcalPor100g = kcal,
        proteinasPor100g = proteinas,
        carbohidratosPor100g = carbohidratos,
        grasasPor100g = grasas
    )

    /**
     * @brief Construye un ingrediente con nombre, gramaje y alimento de referencia.
     */
    private fun crearIngrediente(
        nombre: String,
        gramos: Double,
        alimento: Alimento
    ): IngredienteToma = IngredienteToma(
        id = "ingrediente-${UUID.randomUUID()}",
        alimentoId = alimento.id,
        nombre = nombre,
        cantidadGramos = gramos,
        pesaje = IngredienteToma.PESAJE_CRUDO,
        origenPlan = true,
        alimentoResuelto = alimento
    )

    /**
     * @brief Construye un plan de cuatro tomas con macros conocidos:
     * - DESAYUNO: 25 g de proteína (pechuga).
     * - COMIDA: 25 g de proteína (pechuga).
     * - MERIENDA: 50 g de carbohidratos (panecillo).
     * - CENA: 25 g de carbohidratos (arroz blanco).
     * Total: 50 g proteína, 75 g CH, 0 g grasa y 550 kcal.
     */
    private fun crearPlan(): PlanComida {
        val pechuga = crearAlimento(proteinas = 25.0, kcal = 120.0)
        val panecillo = crearAlimento(carbohidratos = 50.0, kcal = 200.0)
        val arroz = crearAlimento(carbohidratos = 25.0, kcal = 110.0)

        val desayuno = Toma(
            id = "toma-desayuno",
            tipoIngesta = Toma.TIPO_DESAYUNO,
            orden = 1,
            ingredientes = listOf(
                crearIngrediente("Pechuga de pollo", 100.0, pechuga)
            )
        )
        val comida = Toma(
            id = "toma-comida",
            tipoIngesta = Toma.TIPO_COMIDA,
            orden = 2,
            ingredientes = listOf(
                crearIngrediente("Pechuga de pollo", 100.0, pechuga)
            )
        )
        val merienda = Toma(
            id = "toma-merienda",
            tipoIngesta = Toma.TIPO_MERIENDA,
            orden = 3,
            ingredientes = listOf(
                crearIngrediente("Panecillo integral", 100.0, panecillo)
            )
        )
        val cena = Toma(
            id = "toma-cena",
            tipoIngesta = Toma.TIPO_CENA,
            orden = 4,
            ingredientes = listOf(
                crearIngrediente("Arroz blanco", 100.0, arroz)
            )
        )
        return PlanComida(
            id = "plan-rebalanceo",
            nombre = "Plan de prueba",
            fecha = fecha,
            tomas = listOf(desayuno, comida, merienda, cena)
        )
    }

    /**
     * @brief Construye una ingesta registrada con los macros indicados.
     */
    private fun crearIngesta(
        tipoIngesta: String,
        proteinas: Double,
        carbohidratos: Double,
        grasas: Double,
        kcal: Double
    ): IngestaRegistrada = IngestaRegistrada(
        id = "ingesta-${UUID.randomUUID()}",
        userId = "usuario-1",
        nombre = "Ingesta de prueba",
        kcal = kcal,
        proteinasG = proteinas,
        carbohidratosG = carbohidratos,
        grasasG = grasas,
        tipoIngesta = tipoIngesta,
        fecha = fecha,
        momentoDia = "TARDE",
        origen = IngestaRegistrada.ORIGEN_MANUAL
    )

    @Test
    fun `sin desvio entre plan y consumo devuelve lista vacia`() = runTest {
        val plan = crearPlan()
        // El consumo coincide exactamente con lo planificado (desvío cero).
        val ingestas = listOf(
            crearIngesta(Toma.TIPO_COMIDA, 50.0, 75.0, 0.0, 550.0)
        )

        val resultado = casoUso.ejecutar(plan, ingestas)

        assertTrue(resultado.isSuccess)
        assertTrue(resultado.getOrThrow().isEmpty())
    }

    @Test
    fun `desvio dentro de la tolerancia devuelve lista vacia`() = runTest {
        val plan = crearPlan()
        // Desvío de 5 g de proteína y 5 g de CH: dentro de la tolerancia (±10 g o 5 %).
        val ingestas = listOf(
            crearIngesta(Toma.TIPO_COMIDA, 45.0, 70.0, 0.0, 550.0)
        )

        val resultado = casoUso.ejecutar(plan, ingestas)

        assertTrue(resultado.isSuccess)
        assertTrue(resultado.getOrThrow().isEmpty())
    }

    @Test
    fun `desvio grande propone ajustes solo sobre las tomas pendientes`() = runTest {
        val plan = crearPlan()
        // Exceso claro de carbohidratos y grasas frente a lo planificado.
        val ingestas = listOf(
            crearIngesta(Toma.TIPO_COMIDA, 60.0, 120.0, 20.0, 950.0)
        )

        val resultado = casoUso.ejecutar(plan, ingestas)

        assertTrue(resultado.isSuccess)
        val ajustes = resultado.getOrThrow()
        assertEquals(1, ajustes.size)
        assertEquals(Toma.TIPO_DESAYUNO, ajustes[0].tipoIngesta)
        // La corrección de CH elimina el panecillo de la merienda y el arroz de la cena.
        assertTrue(
            ajustes[0].cambios.any { it.contains("panecillo") && it.contains("merienda") }
        )
        assertTrue(
            ajustes[0].cambios.any { it.contains("arroz") && it.contains("cena") }
        )
        // Se informa al usuario de los cambios en castellano.
        assertTrue(ajustes[0].cambios.all { it.isNotBlank() })
    }

    @Test
    fun `no modifica las tomas ya consumidas`() = runTest {
        val plan = crearPlan()
        val ingestas = listOf(
            crearIngesta(Toma.TIPO_COMIDA, 60.0, 120.0, 20.0, 950.0)
        )

        val ajustes = casoUso.ejecutar(plan, ingestas).getOrThrow()

        val tomasRevisadas = ajustes[0].tomasRevisadas
        // La toma de COMIDA (consumida) conserva intactos sus ingredientes.
        val comidaRevisada = tomasRevisadas.first { it.tipoIngesta == Toma.TIPO_COMIDA }
        val comidaOriginal = plan.tomas.first { it.tipoIngesta == Toma.TIPO_COMIDA }
        assertEquals(comidaOriginal.ingredientes, comidaRevisada.ingredientes)

        // El desayuno (pendiente pero sin ingredientes ricos en CH) tampoco se toca.
        val desayunoRevisado = tomasRevisadas.first { it.tipoIngesta == Toma.TIPO_DESAYUNO }
        val desayunoOriginal = plan.tomas.first { it.tipoIngesta == Toma.TIPO_DESAYUNO }
        assertEquals(desayunoOriginal.ingredientes, desayunoRevisado.ingredientes)
    }

    @Test
    fun `deficit de proteina anade atun a la merienda pendiente`() = runTest {
        // Plan con más proteína total: 3 tomas de pechuga (75 g) y panecillo en la merienda.
        val pechuga = crearAlimento(proteinas = 25.0, kcal = 120.0)
        val panecillo = crearAlimento(carbohidratos = 50.0, kcal = 200.0)
        val plan = PlanComida(
            id = "plan-proteinas",
            nombre = "Plan proteico",
            fecha = fecha,
            tomas = listOf(
                Toma(
                    id = "toma-desayuno",
                    tipoIngesta = Toma.TIPO_DESAYUNO,
                    orden = 1,
                    ingredientes = listOf(crearIngrediente("Pechuga de pollo", 100.0, pechuga))
                ),
                Toma(
                    id = "toma-comida",
                    tipoIngesta = Toma.TIPO_COMIDA,
                    orden = 2,
                    ingredientes = listOf(crearIngrediente("Pechuga de pollo", 100.0, pechuga))
                ),
                Toma(
                    id = "toma-merienda",
                    tipoIngesta = Toma.TIPO_MERIENDA,
                    orden = 3,
                    ingredientes = listOf(crearIngrediente("Panecillo integral", 100.0, panecillo))
                ),
                Toma(
                    id = "toma-cena",
                    tipoIngesta = Toma.TIPO_CENA,
                    orden = 4,
                    ingredientes = listOf(crearIngrediente("Pechuga de pollo", 100.0, pechuga))
                )
            )
        )
        // Consumo con exceso de proteína frente al planificado.
        val ingestas = listOf(
            crearIngesta(Toma.TIPO_COMIDA, 100.0, 0.0, 0.0, 400.0)
        )

        val ajustes = casoUso.ejecutar(plan, ingestas).getOrThrow()

        assertEquals(1, ajustes.size)
        // Se añade atún al natural (100 g) a la merienda pendiente.
        assertTrue(ajustes[0].cambios.any { it.contains("Atún al natural") && it.contains("merienda") })

        val meriendaRevisada = ajustes[0].tomasRevisadas
            .first { it.tipoIngesta == Toma.TIPO_MERIENDA }
        assertEquals(2, meriendaRevisada.ingredientes.size)
        assertTrue(meriendaRevisada.ingredientes.any { it.nombre.contains("Atún", ignoreCase = true) })
        assertEquals(100.0, meriendaRevisada.ingredientes.first { it.nombre.contains("Atún") }.cantidadGramos, 0.001)
    }

    @Test
    fun `si todas las tomas estan consumidas no se generan ajustes`() = runTest {
        val plan = crearPlan()
        // Se han consumido el desayuno y la comida (todas las tomas del día).
        val ingestas = listOf(
            crearIngesta(Toma.TIPO_DESAYUNO, 25.0, 0.0, 0.0, 120.0),
            crearIngesta(Toma.TIPO_COMIDA, 25.0, 0.0, 0.0, 120.0),
            crearIngesta(Toma.TIPO_MERIENDA, 0.0, 50.0, 0.0, 200.0),
            crearIngesta(Toma.TIPO_CENA, 0.0, 25.0, 0.0, 110.0)
        )

        val resultado = casoUso.ejecutar(plan, ingestas)

        assertTrue(resultado.isSuccess)
        assertTrue(resultado.getOrThrow().isEmpty())
    }
}