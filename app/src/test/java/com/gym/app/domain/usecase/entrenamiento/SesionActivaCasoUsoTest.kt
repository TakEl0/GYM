/**
 * @file SesionActivaCasoUsoTest.kt
 * @brief Pruebas unitarias de los casos de uso de la sesión de entrenamiento en vivo.
 *
 * Cubre el ciclo completo de la sesión activa: registro de series (validación y
 * numeración automática), edición, eliminación con renumeración, carga sugerida,
 * resumen estadístico (volumen total y 1RM) y preparación de la sesión con
 * resolución de las máquinas reales del gimnasio.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.data.repository.RepositorioSerieRealizadaFake
import com.gym.app.domain.model.BloqueRutina
import com.gym.app.domain.model.CalculoUnRM
import com.gym.app.domain.model.CatalogoMaquinaria
import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.model.Gimnasio
import com.gym.app.domain.model.Rutina
import com.gym.app.domain.model.SerieRealizada
import com.gym.app.domain.repository.RepositorioEjercicio
import com.gym.app.domain.repository.RepositorioGimnasio
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class SesionActivaCasoUsoTest
 * @brief Verifica el comportamiento de los casos de uso de la sesión en vivo
 * usando repositorios simulados ([RepositorioSerieRealizadaFake]) y mocks de
 * MockK para el catálogo de ejercicios y el gimnasio.
 */
class SesionActivaCasoUsoTest {

    /**
     * @brief Comprueba que el registro valida peso y repeticiones y numera de
     * forma automática y consecutiva las series del mismo ejercicio.
     */
    @Test
    fun `registrar serie valida peso y repeticiones y numera correctamente`() = runTest {
        val repositorio = RepositorioSerieRealizadaFake()
        val casoUso = RegistrarSerieCasoUso(repositorio, Dispatchers.Unconfined)

        // Dos series del mismo ejercicio en la misma sesión: numeración 1 y 2.
        val primera = casoUso.ejecutar("sesion-1", "ej-1", 60.0, 12).getOrThrow()
        val segunda = casoUso.ejecutar("sesion-1", "ej-1", 62.5, 10).getOrThrow()

        assertEquals(1, primera.numeroSerie)
        assertEquals(2, segunda.numeroSerie)
        assertEquals("sesion-1", primera.sesionId)
        assertEquals("ej-1", primera.ejercicioId)

        // Peso no positivo: falla con el mensaje de validación.
        val falloPeso = casoUso.ejecutar("sesion-1", "ej-1", 0.0, 10)
        assertTrue(falloPeso.isFailure)
        assertEquals(
            RegistrarSerieCasoUso.MENSAJE_PESO_INVALIDO,
            falloPeso.exceptionOrNull()?.message
        )

        // Repeticiones menores que 1: falla con el mensaje de validación.
        val falloReps = casoUso.ejecutar("sesion-1", "ej-1", 60.0, 0)
        assertTrue(falloReps.isFailure)
        assertEquals(
            RegistrarSerieCasoUso.MENSAJE_REPETICIONES_INVALIDAS,
            falloReps.exceptionOrNull()?.message
        )

        // Las series inválidas no se persistieron: solo quedan las dos válidas.
        assertEquals(2, repositorio.observarPorSesion("sesion-1").first().size)
    }

    /**
     * @brief Comprueba que la edición actualiza los kg y las repeticiones de la
     * serie manteniendo su identificador.
     */
    @Test
    fun `editar serie actualiza kg y reps`() = runTest {
        val repositorio = RepositorioSerieRealizadaFake()
        val registrar = RegistrarSerieCasoUso(repositorio, Dispatchers.Unconfined)
        val editar = EditarSerieCasoUso(repositorio, Dispatchers.Unconfined)

        val serie = registrar.ejecutar("sesion-1", "ej-1", 60.0, 12).getOrThrow()

        val resultado = editar.ejecutar(serie.id, 70.0, 8)

        assertTrue(resultado.isSuccess)
        val actualizada = repositorio.obtenerPorId(serie.id)
        assertNotNull(actualizada)
        assertEquals(serie.id, actualizada!!.id)
        assertEquals(70.0, actualizada.pesoKg, 0.001)
        assertEquals(8, actualizada.repeticiones)

        // La validación se aplica también en la edición.
        val falloPeso = editar.ejecutar(serie.id, -5.0, 8)
        assertTrue(falloPeso.isFailure)
        assertEquals(
            EditarSerieCasoUso.MENSAJE_PESO_INVALIDO,
            falloPeso.exceptionOrNull()?.message
        )
    }

    /**
     * @brief Comprueba que al eliminar una serie intermedia las restantes quedan
     * renumeradas de forma consecutiva (1..N).
     */
    @Test
    fun `eliminar serie renumera las series restantes`() = runTest {
        val repositorio = RepositorioSerieRealizadaFake()
        val registrar = RegistrarSerieCasoUso(repositorio, Dispatchers.Unconfined)
        val eliminar = EliminarSerieCasoUso(repositorio, Dispatchers.Unconfined)

        val serie1 = registrar.ejecutar("sesion-1", "ej-1", 60.0, 12).getOrThrow()
        val serie2 = registrar.ejecutar("sesion-1", "ej-1", 65.0, 10).getOrThrow()
        registrar.ejecutar("sesion-1", "ej-1", 70.0, 8).getOrThrow()

        // Se elimina la 2ª serie: la antigua 3ª debe pasar a ser la 2ª.
        val resultado = eliminar.ejecutar(serie2.id)

        assertTrue(resultado.isSuccess)
        val restantes = repositorio.observarPorSesion("sesion-1").first()
        assertEquals(2, restantes.size)
        assertEquals(serie1.id, restantes[0].id)
        assertEquals(1, restantes[0].numeroSerie)
        assertEquals(2, restantes[1].numeroSerie)
    }

    /**
     * @brief Comprueba que la carga sugerida devuelve el último peso registrado
     * del ejercicio y `null` si no hay historial.
     */
    @Test
    fun `calcular carga sugerida devuelve el ultimo peso del ejercicio`() = runTest {
        val repositorio = RepositorioSerieRealizadaFake()
        val casoUso = CalcularCargaSugeridaCasoUso(repositorio, Dispatchers.Unconfined)

        // Sin historial previo: null (la UI usa el peso del bloque o 20 kg).
        assertNull(casoUso.ejecutar("ej-1"))

        // Historial: 60.0 en una sesión anterior y 65.0 en la más reciente.
        repositorio.guardarSerie(
            SerieRealizada("s1", "sesion-antigua", "ej-1", 1, 60.0, 12, 1_000L)
        )
        repositorio.guardarSerie(
            SerieRealizada("s2", "sesion-reciente", "ej-1", 1, 65.0, 10, 2_000L)
        )

        val sugerido = casoUso.ejecutar("ej-1")
        assertNotNull(sugerido)
        assertEquals(65.0, sugerido!!, 0.001)
    }

    /**
     * @brief Comprueba que el resumen calcula el volumen total (Σ kg×reps) y el
     * 1RM estimado de la mejor serie de cada ejercicio.
     */
    @Test
    fun `calcular resumen sesion calcula volumen total y 1RM`() = runTest {
        val repositorioSeries = RepositorioSerieRealizadaFake()
        val repositorioEjercicio = mockk<RepositorioEjercicio>(relaxed = true)
        coEvery { repositorioEjercicio.observarEjercicios() } returns flowOf(
            listOf(
                Ejercicio(
                    id = "ej-1",
                    nombre = "Press de banca",
                    grupoMuscularPrincipal = "PECHO"
                )
            )
        )
        val casoUso = CalcularResumenSesionCasoUso(
            repositorioSeries,
            repositorioEjercicio,
            Dispatchers.Unconfined
        )

        // Dos series del mismo ejercicio: 60×10 y 80×8.
        repositorioSeries.guardarSerie(
            SerieRealizada("s1", "sesion-1", "ej-1", 1, 60.0, 10, 1_000L)
        )
        repositorioSeries.guardarSerie(
            SerieRealizada("s2", "sesion-1", "ej-1", 2, 80.0, 8, 2_000L)
        )

        val resumen = casoUso.ejecutar("sesion-1").getOrThrow()

        // Volumen total = 60×10 + 80×8 = 600 + 640 = 1240 kg.
        assertEquals(1240.0, resumen.volumenTotalKg, 0.001)
        assertEquals(2, resumen.seriesTotales)
        assertEquals(1, resumen.ejerciciosConUnRM.size)

        val ejercicio = resumen.ejerciciosConUnRM[0]
        assertEquals("ej-1", ejercicio.ejercicioId)
        assertEquals("Press de banca", ejercicio.nombre)
        // Mejor serie: la de mayor peso (80×8) con desempate por repeticiones.
        assertEquals(80.0, ejercicio.mejorSeriePesoKg, 0.001)
        assertEquals(8, ejercicio.mejorSerieReps)
        // 1RM = promedio de Epley (80×(1+8/30)) y Brzycki (80×36/29) ≈ 100.32.
        // Se verifica contra la definición del modelo con tolerancia de 1.0 kg.
        assertEquals(
            CalculoUnRM.calcular(pesoKg = 80.0, repeticiones = 8),
            ejercicio.estimacionUnRM,
            1.0
        )
    }

    /**
     * @brief Comprueba que la preparación de la sesión resuelve cada bloque a su
     * máquina real del gimnasio (catálogo real de Fitness Park) conservando el
     * orden de la rutina y el total de series planificadas.
     */
    @Test
    fun `preparar sesion activa resuelve las maquinas reales del gimnasio`() = runTest {
        // Gimnasio con las 47 máquinas del catálogo real de Fitness Park.
        val gimnasio = Gimnasio(
            id = "fitness-park",
            nombre = "Fitness Park",
            direccion = "Centro comercial",
            maquinas = CatalogoMaquinaria.maquinas.map { CatalogoMaquinaria.aMaquina(it) }
        )
        val repositorioGimnasio = mockk<RepositorioGimnasio>(relaxed = true)
        coEvery { repositorioGimnasio.observarGimnasio() } returns flowOf(gimnasio)

        // Ejercicios guardados: el primero con máquina explícita y el segundo
        // resoluble solo por el motor local de mapeo (sin maquinaId).
        val ejercicios = listOf(
            Ejercicio(
                id = "ejercicio-prensa",
                nombre = "Prensa a 45º",
                grupoMuscularPrincipal = "CUADRICEPS",
                maquinaId = "prensa-45"
            ),
            Ejercicio(
                id = "ejercicio-femoral",
                nombre = "Femoral tumbado",
                grupoMuscularPrincipal = "FEMORAL",
                maquinaId = null
            )
        )
        val repositorioEjercicio = mockk<RepositorioEjercicio>(relaxed = true)
        coEvery { repositorioEjercicio.observarEjercicios() } returns flowOf(ejercicios)

        // Rutina de 2 bloques con 4 series cada uno.
        val rutina = Rutina(
            id = "rutina-1",
            nombre = "Día 1 - Pierna",
            bloques = listOf(
                BloqueRutina(
                    id = "bloque-1",
                    ejercicioId = "ejercicio-prensa",
                    serie = 4,
                    repeticiones = 12,
                    descansoSegundos = 60
                ),
                BloqueRutina(
                    id = "bloque-2",
                    ejercicioId = "ejercicio-femoral",
                    serie = 4,
                    repeticiones = 12,
                    descansoSegundos = 60
                )
            )
        )

        val casoUso = PrepararSesionActivaCasoUso(
            repositorioEjercicio,
            repositorioGimnasio,
            Dispatchers.Unconfined
        )

        val resultado = casoUso.ejecutar(rutina).getOrThrow()

        // Orden conservado según los bloques de la rutina.
        assertEquals(2, resultado.ejercicios.size)
        assertEquals("ejercicio-prensa", resultado.ejercicios[0].bloque.ejercicioId)
        assertEquals("ejercicio-femoral", resultado.ejercicios[1].bloque.ejercicioId)

        // Total de series planificadas: 4 + 4 = 8.
        assertEquals(8, resultado.seriesTotales)

        // Ambas máquinas se resuelven contra el catálogo real (marca · modelo).
        assertNotNull("La prensa debe resolver su máquina real.", resultado.ejercicios[0].maquina)
        assertEquals("prensa-45", resultado.ejercicios[0].maquina!!.id)
        assertNotNull("El femoral debe resolver su máquina por el motor.", resultado.ejercicios[1].maquina)
        assertEquals("curl-femoral-tumbado", resultado.ejercicios[1].maquina!!.id)

        // El bloque y el ejercicio quedan correctamente vinculados.
        assertEquals(rutina.bloques[0], resultado.ejercicios[0].bloque)
        assertEquals(ejercicios[0], resultado.ejercicios[0].ejercicio)
    }
}