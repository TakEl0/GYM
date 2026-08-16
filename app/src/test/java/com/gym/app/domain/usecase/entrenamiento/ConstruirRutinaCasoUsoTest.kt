/**
 * @file ConstruirRutinaCasoUsoTest.kt
 * @brief Pruebas unitarias de la construcción automática de la rutina PPL del día.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.model.Maquina
import com.gym.app.domain.usecase.gimnasio.AlternativasMaquinaCasoUso
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class ConstruirRutinaCasoUsoTest
 * @brief Verifica la selección de ejercicios por grupo muscular del día PPL, la
 * sustitución de máquinas no disponibles mediante [AlternativasMaquinaCasoUso]
 * y la generación de los bloques de serie prescritos.
 */
class ConstruirRutinaCasoUsoTest {

    private val alternativasMaquinaCasoUso = mockk<AlternativasMaquinaCasoUso>(relaxed = true)

    private val casoUso = ConstruirRutinaCasoUso(alternativasMaquinaCasoUso)

    /**
     * @brief Construye un ejercicio del catálogo con el grupo muscular indicado.
     */
    private fun crearEjercicio(
        id: String,
        nombre: String,
        grupo: String,
        maquinaId: String? = null
    ): Ejercicio = Ejercicio(
        id = id,
        nombre = nombre,
        grupoMuscularPrincipal = grupo,
        maquinaId = maquinaId
    )

    /**
     * @brief Construye una máquina disponible del gimnasio.
     */
    private fun crearMaquina(id: String, nombre: String): Maquina = Maquina(
        id = id,
        nombre = nombre,
        grupoMuscular = listOf("PECHO"),
        disponible = true
    )

    @Test
    fun `construye la rutina de pecho del lunes con tres ejercicios`() = runTest {
        val ejercicios = listOf(
            crearEjercicio("ej-1", "Press de banca", "Pecho"),
            crearEjercicio("ej-2", "Aperturas en máquina", "Pecho"),
            crearEjercicio("ej-3", "Press inclinado", "Pecho")
        )

        val rutina = casoUso.ejecutar(
            diaSemana = 1,
            maquinasDisponibles = emptyList(),
            ejercicios = ejercicios
        ).getOrThrow()

        assertEquals("PPL - Pecho", rutina!!.nombre)
        assertEquals(listOf(1), rutina.diasSemana)
        // 3 ejercicios x 4 series = 12 bloques.
        assertEquals(12, rutina.bloques.size)
        assertEquals(10, rutina.bloques[0].repeticiones)
        assertEquals(90, rutina.bloques[0].descansoSegundos)
        assertEquals(1, rutina.bloques[0].serie)
        assertEquals(4, rutina.bloques[3].serie)
    }

    @Test
    fun `domingo es dia de descanso y devuelve null`() = runTest {
        val rutina = casoUso.ejecutar(
            diaSemana = 7,
            maquinasDisponibles = emptyList(),
            ejercicios = listOf(crearEjercicio("ej-1", "Press de banca", "Pecho"))
        ).getOrThrow()

        assertNull(rutina)
    }

    @Test
    fun `con menos de tres ejercicios del grupo no se construye la rutina`() = runTest {
        val rutina = casoUso.ejecutar(
            diaSemana = 1,
            maquinasDisponibles = emptyList(),
            ejercicios = listOf(
                crearEjercicio("ej-1", "Press de banca", "Pecho"),
                crearEjercicio("ej-2", "Aperturas", "Pecho")
            )
        ).getOrThrow()

        assertNull(rutina)
    }

    @Test
    fun `sustituye la maquina no disponible usando alternativas`() = runTest {
        val ejercicios = listOf(
            crearEjercicio("ej-1", "Press de banca", "Pecho", maquinaId = "m-1"),
            crearEjercicio("ej-2", "Aperturas en máquina", "Pecho", maquinaId = "m-2"),
            crearEjercicio("ej-3", "Press inclinado en máquina", "Pecho", maquinaId = "m-3")
        )
        val maquinas = listOf(crearMaquina("m-2", "Máquina aperturas"), crearMaquina("m-3", "Prensa inclinada"))
        // La máquina m-1 no está disponible: se propone una flexión como sustituto.
        val sustituto = crearEjercicio("ej-4", "Flexiones", "Pecho")
        coEvery { alternativasMaquinaCasoUso.ejecutar("m-1", ejercicios) } returns
            Result.success(listOf(sustituto))

        val rutina = casoUso.ejecutar(
            diaSemana = 1,
            maquinasDisponibles = maquinas,
            ejercicios = ejercicios
        ).getOrThrow()

        assertEquals(12, rutina!!.bloques.size)
        // El bloque del sustituto (flexiones) está presente en la rutina.
        assertTrue(rutina.bloques.any { it.ejercicioId == "ej-4" })
        coVerify(exactly = 1) { alternativasMaquinaCasoUso.ejecutar("m-1", ejercicios) }
    }

    @Test
    fun `si el sustituto no cubre el minimo no se construye la rutina`() = runTest {
        val ejercicios = listOf(
            crearEjercicio("ej-1", "Press de banca", "Pecho", maquinaId = "m-1"),
            crearEjercicio("ej-2", "Aperturas en máquina", "Pecho", maquinaId = "m-2")
        )
        // Solo dos ejercicios originales y el sustituto devuelve lista vacía.
        // Se tipa el Result explícitamente y se cubren todas las llamadas con
        // matcher genérico: ambos ejercicios (m-1 y m-2) tienen máquina no
        // disponible, y la segunda llamada sin stub caería en el default relajado
        // de MockK, que genera un Result<List> mal tipado (ClassCastException).
        val resultadoVacio: Result<List<Ejercicio>> = Result.success(emptyList())
        coEvery { alternativasMaquinaCasoUso.ejecutar(any(), any()) } returns resultadoVacio

        val rutina = casoUso.ejecutar(
            diaSemana = 1,
            maquinasDisponibles = emptyList(),
            ejercicios = ejercicios
        ).getOrThrow()

        assertNull(rutina)
    }
}