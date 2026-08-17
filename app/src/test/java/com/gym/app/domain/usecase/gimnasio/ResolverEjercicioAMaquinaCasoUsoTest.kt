/**
 * @file ResolverEjercicioAMaquinaCasoUsoTest.kt
 * @brief Pruebas unitarias del caso de uso que resuelve un ejercicio contra una máquina.
 */
package com.gym.app.domain.usecase.gimnasio

import com.gym.app.domain.model.CatalogoMaquinaria
import com.gym.app.domain.model.Maquina
import com.gym.app.domain.model.MapeoAprendido
import com.gym.app.domain.repository.RepositorioMapeoAprendido
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class ResolverEjercicioAMaquinaCasoUsoTest
 * @brief Verifica la prioridad del aprendizaje sobre el motor local y la delegación en
 * las reglas cuando no existe una corrección manual del usuario.
 */
class ResolverEjercicioAMaquinaCasoUsoTest {

    /** Catálogo real de Fitness Park convertido a máquinas de dominio. */
    private val maquinas: List<Maquina> =
        CatalogoMaquinaria.maquinas.map { CatalogoMaquinaria.aMaquina(it) }

    @Test
    fun `sin repositorio de aprendizaje delega en el motor local`() = runTest {
        val casoUso = ResolverEjercicioAMaquinaCasoUso(motor = MotorMapeoEjercicioAMaquina)

        val r = requireNotNull(casoUso.ejecutar("Rueda abdominal", maquinas)) {
            "Debe resolverse mediante el motor local."
        }

        assertEquals("rueda-abdominal", r.maquinaId)
        assertEquals(OrigenMapeo.EXACTO, r.origen)
    }

    @Test
    fun `si existe un mapeo aprendido se usa con origen MANUAL aunque el nombre no este en el catalogo`() = runTest {
        val repositorio = mockk<RepositorioMapeoAprendido>(relaxed = true)
        val aprendido = MapeoAprendido(
            nombreNormalizado = "femoral",
            maquinaId = "maquina-femoral-personalizada",
            fecha = 1_700_000_000_000L
        )
        coEvery { repositorio.buscar("femoral") } returns aprendido

        val casoUso = ResolverEjercicioAMaquinaCasoUso(
            motor = MotorMapeoEjercicioAMaquina,
            repositorioAprendizaje = repositorio
        )

        // "femoral" como clave no existe en el catálogo: la resolución debe venir
        // exclusivamente del aprendizaje del usuario.
        val r = requireNotNull(casoUso.ejecutar("Femoral", maquinas)) {
            "El mapeo aprendido debe resolverse con prioridad."
        }

        assertEquals("maquina-femoral-personalizada", r.maquinaId)
        assertEquals(OrigenMapeo.MANUAL, r.origen)
        assertTrue("La confianza del aprendizaje debe ser alta.", r.confianza >= 0.9f)
    }

    @Test
    fun `si el repositorio no tiene aprendizaje se usa el motor local`() = runTest {
        val repositorio = mockk<RepositorioMapeoAprendido>(relaxed = true)
        coEvery { repositorio.buscar(any()) } returns null

        val casoUso = ResolverEjercicioAMaquinaCasoUso(
            motor = MotorMapeoEjercicioAMaquina,
            repositorioAprendizaje = repositorio
        )

        val r = requireNotNull(casoUso.ejecutar("Adductor", maquinas)) {
            "Sin aprendizaje debe resolverse con el motor local."
        }

        assertEquals("adductor", r.maquinaId)
        assertEquals(OrigenMapeo.EXACTO, r.origen)
    }

    @Test
    fun `nombre vacio devuelve null`() = runTest {
        val casoUso = ResolverEjercicioAMaquinaCasoUso(motor = MotorMapeoEjercicioAMaquina)

        val resolucion = casoUso.ejecutar("   ", maquinas)

        assertNull("Un nombre vacío no debe resolverse.", resolucion)
    }
}