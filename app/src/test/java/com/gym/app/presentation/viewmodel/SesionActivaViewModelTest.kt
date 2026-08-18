/**
 * @file SesionActivaViewModelTest.kt
 * @brief Pruebas unitarias del ViewModel de la sesión de entrenamiento en vivo.
 * Cubren el inicio de la sesión, el registro de series con el arranque del descanso,
 * la validación de pesos inválidos, la eliminación de series con el flujo reactivo y
 * la finalización con el cálculo del resumen.
 */
package com.gym.app.presentation.viewmodel

import com.gym.app.domain.model.BloqueRutina
import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.model.EjercicioConMaquina
import com.gym.app.domain.model.Rutina
import com.gym.app.domain.model.SerieRealizada
import com.gym.app.domain.repository.RepositorioSerieRealizada
import com.gym.app.domain.usecase.entrenamiento.CalcularCargaSugeridaCasoUso
import com.gym.app.domain.usecase.entrenamiento.CalcularResumenSesionCasoUso
import com.gym.app.domain.usecase.entrenamiento.EditarSerieCasoUso
import com.gym.app.domain.usecase.entrenamiento.EjercicioUnRM
import com.gym.app.domain.usecase.entrenamiento.EliminarSerieCasoUso
import com.gym.app.domain.usecase.entrenamiento.FinalizarSesionActivaCasoUso
import com.gym.app.domain.usecase.entrenamiento.ObtenerRutinaPorIdCasoUso
import com.gym.app.domain.usecase.entrenamiento.PrepararSesionActivaCasoUso
import com.gym.app.domain.usecase.entrenamiento.RegistrarSerieCasoUso
import com.gym.app.domain.usecase.entrenamiento.ResumenSesion
import com.gym.app.domain.usecase.entrenamiento.ResultadoPreparacionSesion
import com.gym.app.test.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * @class SesionActivaViewModelTest
 * @brief Verifica el ciclo de vida de la sesión en vivo.
 *
 * Sigue el estilo de los tests de ViewModels del proyecto: `MainDispatcherRule` con
 * `UnconfinedTestDispatcher` (ejecución síncrona) + `runTest` + MockK. Se inyecta un
 * `UnconfinedTestDispatcher` con un `TestCoroutineScheduler` independiente al ViewModel
 * para que las corrutinas de las operaciones suspendidas se ejecuten de forma inmediata;
 * los cronómetros internos (lanzados sobre ese mismo dispatcher) quedan suspendidos en
 * su primer delay en un scheduler propio que `runTest` nunca avanza, y por tanto no
 * interfieren con las aserciones ni provocan bucles de tiempo virtual infinitos.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SesionActivaViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val obtenerRutinaPorIdCasoUso = mockk<ObtenerRutinaPorIdCasoUso>()
    private val prepararSesionActivaCasoUso = mockk<PrepararSesionActivaCasoUso>()
    private val registrarSerieCasoUso = mockk<RegistrarSerieCasoUso>()
    private val editarSerieCasoUso = mockk<EditarSerieCasoUso>()
    private val eliminarSerieCasoUso = mockk<EliminarSerieCasoUso>()
    private val finalizarSesionActivaCasoUso = mockk<FinalizarSesionActivaCasoUso>()
    private val calcularCargaSugeridaCasoUso = mockk<CalcularCargaSugeridaCasoUso>()
    private val calcularResumenSesionCasoUso = mockk<CalcularResumenSesionCasoUso>()
    private val repositorioSerieRealizada = mockk<RepositorioSerieRealizada>()

    /**
     * @brief Construye el ViewModel con todos los mocks y un dispatcher Unconfined
     * para que las operaciones suspendidas se ejecuten de forma síncrona.
     *
     * Se crea el dispatcher con un [TestCoroutineScheduler] propio (y no el que
     * `runTest` expone como "actual"): los cronómetros internos del ViewModel usan
     * ese scheduler independiente, de modo que `advanceUntilIdleOr` de `runTest` no
     * puede avanzar su tiempo virtual indefinidamente (evita el bucle infinito del
     * ticker del cronómetro total al finalizar cada prueba).
     */
    private fun crearViewModel(): SesionActivaViewModel = SesionActivaViewModel(
        obtenerRutinaPorIdCasoUso = obtenerRutinaPorIdCasoUso,
        prepararSesionActivaCasoUso = prepararSesionActivaCasoUso,
        registrarSerieCasoUso = registrarSerieCasoUso,
        editarSerieCasoUso = editarSerieCasoUso,
        eliminarSerieCasoUso = eliminarSerieCasoUso,
        finalizarSesionActivaCasoUso = finalizarSesionActivaCasoUso,
        calcularCargaSugeridaCasoUso = calcularCargaSugeridaCasoUso,
        calcularResumenSesionCasoUso = calcularResumenSesionCasoUso,
        repositorioSerieRealizada = repositorioSerieRealizada,
        userId = "usuario-1",
        dispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
    )

    /**
     * @brief Rutina de prueba con dos bloques (60 s y 30 s de descanso).
     */
    private fun crearRutina(): Rutina = Rutina(
        id = "rutina-1",
        nombre = "PPL - Pecho",
        descripcion = "Rutina de empuje",
        diasSemana = listOf(1, 3),
        bloques = listOf(
            BloqueRutina(
                id = "bloque-1",
                ejercicioId = "ej-1",
                serie = 3,
                repeticiones = 12,
                pesoKg = 60.0,
                descansoSegundos = 60
            ),
            BloqueRutina(
                id = "bloque-2",
                ejercicioId = "ej-2",
                serie = 2,
                repeticiones = 10,
                pesoKg = null,
                descansoSegundos = 30
            )
        )
    )

    /**
     * @brief Ejercicios resueltos de la sesión (uno por bloque de la rutina).
     */
    private fun crearEjercicios(): List<EjercicioConMaquina> = listOf(
        EjercicioConMaquina(
            bloque = BloqueRutina(
                id = "bloque-1",
                ejercicioId = "ej-1",
                serie = 3,
                repeticiones = 12,
                pesoKg = 60.0,
                descansoSegundos = 60
            ),
            ejercicio = Ejercicio(
                id = "ej-1",
                nombre = "Press de banca",
                grupoMuscularPrincipal = "PECHO"
            ),
            maquina = null
        ),
        EjercicioConMaquina(
            bloque = BloqueRutina(
                id = "bloque-2",
                ejercicioId = "ej-2",
                serie = 2,
                repeticiones = 10,
                pesoKg = null,
                descansoSegundos = 30
            ),
            ejercicio = Ejercicio(
                id = "ej-2",
                nombre = "Aperturas",
                grupoMuscularPrincipal = "PECHO"
            ),
            maquina = null
        )
    )

    /**
     * @brief Resultado de preparación de la sesión (3 + 2 series planificadas).
     */
    private fun crearPreparacion(): ResultadoPreparacionSesion = ResultadoPreparacionSesion(
        ejercicios = crearEjercicios(),
        seriesTotales = 5
    )

    /**
     * @brief Configura el flujo vacío de series y los casos de uso de inicio.
     */
    private fun configurarInicioDeSesion() {
        coEvery { obtenerRutinaPorIdCasoUso.ejecutar("rutina-1") } returns crearRutina()
        coEvery { prepararSesionActivaCasoUso.ejecutar(any()) } returns Result.success(crearPreparacion())
        coEvery { repositorioSerieRealizada.observarPorSesion(any()) } returns flowOf(emptyList())
    }

    @Test
    fun `iniciarSesion carga la rutina y prepara los ejercicios`() = runTest {
        configurarInicioDeSesion()

        val viewModel = crearViewModel()
        viewModel.iniciarSesion("rutina-1")

        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        assertNotNull(estado.rutina)
        assertEquals("PPL - Pecho", estado.rutina?.nombre)
        assertEquals(2, estado.ejercicios.size)
        assertEquals(5, estado.seriesTotales)
        assertEquals(2, estado.totalEjercicios)
        assertNotNull(estado.sesionId)
        assertNull(estado.error)
    }

    @Test
    fun `registrarSerie arranca el descanso con el tiempo del bloque`() = runTest {
        configurarInicioDeSesion()
        coEvery {
            registrarSerieCasoUso.ejecutar(any(), "ej-1", 80.0, 12)
        } returns Result.success(
            SerieRealizada(
                id = "serie-1",
                sesionId = "sesion-1",
                ejercicioId = "ej-1",
                numeroSerie = 1,
                pesoKg = 80.0,
                repeticiones = 12,
                fecha = 0L
            )
        )

        val viewModel = crearViewModel()
        viewModel.iniciarSesion("rutina-1")
        viewModel.registrarSerie("ej-1", 80.0, 12)

        val estado = viewModel.estado.value
        assertTrue(estado.descansoActivo)
        assertEquals(60, estado.descansoTotal)
        assertEquals(60, estado.descansoRestante)
        assertEquals("ej-1", estado.ejercicioActualId)
        assertNull(estado.error)
    }

    @Test
    fun `registrarSerie con peso invalido no arranca descanso y pone error`() = runTest {
        configurarInicioDeSesion()
        coEvery {
            registrarSerieCasoUso.ejecutar(any(), "ej-1", 0.0, 12)
        } returns Result.failure(
            IllegalArgumentException("El peso debe ser mayor que 0 kg.")
        )

        val viewModel = crearViewModel()
        viewModel.iniciarSesion("rutina-1")
        viewModel.registrarSerie("ej-1", 0.0, 12)

        val estado = viewModel.estado.value
        assertFalse(estado.descansoActivo)
        assertEquals(0, estado.descansoRestante)
        assertEquals(0, estado.descansoTotal)
        assertEquals("El peso debe ser mayor que 0 kg.", estado.error)
    }

    @Test
    fun `eliminarSerie actualiza la lista de series`() = runTest {
        val serie1 = SerieRealizada(
            id = "serie-1",
            sesionId = "sesion-1",
            ejercicioId = "ej-1",
            numeroSerie = 1,
            pesoKg = 80.0,
            repeticiones = 12,
            fecha = 0L
        )
        val serie2 = SerieRealizada(
            id = "serie-2",
            sesionId = "sesion-1",
            ejercicioId = "ej-1",
            numeroSerie = 2,
            pesoKg = 80.0,
            repeticiones = 10,
            fecha = 1L
        )
        // Flujo reactivo vivo: el repositorio real emitiría la lista sin la serie
        // eliminada tras la renumeración del caso de uso.
        val flujoSeries = MutableStateFlow(listOf(serie1, serie2))

        coEvery { obtenerRutinaPorIdCasoUso.ejecutar("rutina-1") } returns crearRutina()
        coEvery { prepararSesionActivaCasoUso.ejecutar(any()) } returns Result.success(crearPreparacion())
        coEvery { repositorioSerieRealizada.observarPorSesion(any()) } returns flujoSeries
        coEvery { eliminarSerieCasoUso.ejecutar("serie-2") } returns Result.success(Unit)

        val viewModel = crearViewModel()
        viewModel.iniciarSesion("rutina-1")
        assertEquals(2, viewModel.estado.value.series.size)

        viewModel.eliminarSerie("serie-2")

        // El flujo reactivo emite la lista actualizada tras la eliminación.
        flujoSeries.value = listOf(serie1)

        assertEquals(listOf("serie-1"), viewModel.estado.value.series.map { it.id })
        assertEquals(1, viewModel.estado.value.seriesCompletadas)
        assertNull(viewModel.estado.value.error)
    }

    @Test
    fun `finalizarSesion persiste la sesion y calcula el resumen`() = runTest {
        configurarInicioDeSesion()
        coEvery {
            finalizarSesionActivaCasoUso.ejecutar(any(), "PPL - Pecho", "usuario-1", 0)
        } returns Result.success(Unit)
        val resumen = ResumenSesion(
            volumenTotalKg = 2880.0,
            seriesTotales = 5,
            ejerciciosConUnRM = listOf(
                EjercicioUnRM(
                    ejercicioId = "ej-1",
                    nombre = "Press de banca",
                    mejorSeriePesoKg = 80.0,
                    mejorSerieReps = 12,
                    estimacionUnRM = 112.0
                )
            )
        )
        coEvery { calcularResumenSesionCasoUso.ejecutar(any()) } returns Result.success(resumen)

        val viewModel = crearViewModel()
        viewModel.iniciarSesion("rutina-1")
        viewModel.finalizarSesion()

        val estado = viewModel.estado.value
        assertTrue(estado.finalizada)
        assertNotNull(estado.resumen)
        assertEquals(2880.0, estado.resumen?.volumenTotalKg ?: 0.0, 0.001)
        assertEquals(5, estado.resumen?.seriesTotales)
        assertEquals(1, estado.resumen?.ejerciciosConUnRM?.size)
        assertNull(estado.error)
        coVerify(exactly = 1) {
            finalizarSesionActivaCasoUso.ejecutar(any(), "PPL - Pecho", "usuario-1", 0)
        }
    }
}