/**
 * @file ImportarRutinaNaturvitiaCasoUsoTest.kt
 * @brief Pruebas unitarias del caso de uso de importación del plan de entrenamiento
 * Naturvitia a rutinas persistentes.
 *
 * Se utilizan los repositorios mockeados con MockK y el motor de mapeo REAL
 * ([MotorMapeoEjercicioAMaquina]) contra el catálogo real de Fitness Park para
 * verificar la resolución de los 29 ejercicios del plan del nutricionista.
 */
package com.gym.app.domain.usecase.importacion

import com.gym.app.domain.model.CatalogoMaquinaria
import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.model.Gimnasio
import com.gym.app.domain.model.Maquina
import com.gym.app.domain.repository.RepositorioEjercicio
import com.gym.app.domain.repository.RepositorioGimnasio
import com.gym.app.domain.repository.RepositorioRutina
import com.gym.app.domain.usecase.gimnasio.MotorMapeoEjercicioAMaquina
import com.gym.app.domain.usecase.gimnasio.ResolverEjercicioAMaquinaCasoUso
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class ImportarRutinaNaturvitiaCasoUsoTest
 * @brief Verifica la importación del plan real de 5 días: construcción de rutinas y
 * bloques, resolución de los ejercicios contra el catálogo real, error cuando el
 * gimnasio no tiene maquinaria, y reporte de ejercicios sin mapear sin abortar.
 */
class ImportarRutinaNaturvitiaCasoUsoTest {

    /** Repositorio de rutinas mockeado (relajado: solo registra llamadas). */
    private val repositorioRutina = mockk<RepositorioRutina>(relaxed = true)

    /** Repositorio de ejercicios mockeado (relajado: solo registra llamadas). */
    private val repositorioEjercicio = mockk<RepositorioEjercicio>(relaxed = true)

    /** Repositorio del gimnasio mockeado (el flujo se configura por test). */
    private val repositorioGimnasio = mockk<RepositorioGimnasio>(relaxed = true)

    /** Resolver real (motor local) contra el catálogo de Fitness Park. */
    private val resolverReal = ResolverEjercicioAMaquinaCasoUso(motor = MotorMapeoEjercicioAMaquina)

    /** Gimnasio precargado con las 47 máquinas del catálogo real de Fitness Park. */
    private val gimnasioConMaquinas: Gimnasio = Gimnasio(
        id = "fitness-park",
        nombre = "Fitness Park",
        direccion = "Centro comercial",
        maquinas = CatalogoMaquinaria.maquinas.map { CatalogoMaquinaria.aMaquina(it) }
    )

    /** Texto REAL del plan de entrenamiento Naturvitia (5 días, formato S R V T). */
    private val textoPlanReal = """
        --- PAGINA 1 ---
        Manu Miralles Granados
        14/01/2026
        Día 1
        Femoral tumbado
        S R V T
         4 12 1 y 1 60
        Prensa a 45º
        S R V T
         4 12 1 y 1 60
        Adductor
        S R V T
         4 12 1 y 1 60
        Patada de glúteo en máquina
        S R V T
         4 12 1 y 1 60
        Extensiones
        S R V T
         4 12 1 y 1 60
        Hip thrust en banco
        S R V T
         4 12 1 y 1 60
        Día 2
        Press horizontal en máquina
        S R V T
         4 12 1 y 1 60
        Cruces en polea
        S R V T
         4 12 1 y 1 60
        Press vertical en máquina peso libre
        S R V T
         4 12 1 y 1 60
        Peck deck
        S R V T
         4 12 1 y 1 60

        --- PAGINA 2 ---
        Manu Miralles Granados
        14/01/2026
        Día 2
        Curl mancuernas banco 45º
        S R V T
         4 12 1 y 1 60
        Curl con barra
        S R V T
         4 12 1 y 1 60
        Día 3
        Peso muerto para espalda con barra
        S R V T
         4 12 1 y 1 60
        Dominadas en máquina asistida
        S R V T
         4 12 1 y 1 60
        Jalones al pecho agarre cerrado en "V"
        S R V T
         4 12 1 y 1 60
        Remo en polea baja
        S R V T
         4 12 1 y 1 60
        Elevación de piernas en paralelas
        S R V T
         4 10 1 y 1 30
        Rueda abdominal
        S R V T
         4 10 1 y 1 30

        --- PAGINA 3 ---
        Manu Miralles Granados
        14/01/2026
        Día 4
        Elevaciones posteriores con mancuerna
        S R V T
         4 12 1 y 1 60
        Elevaciones laterales con mancuerna
        S R V T
         4 12 1 y 1 60
        Press militar en multipower
        S R V T
         4 12 1 y 1 60
        Deltoide posterior en máquina
        S R V T
         4 12 1 y 1 60
        Extensiones en polea
        S R V T
         4 12 1 y 1 60
        Press francés con barra
        S R V T
         4 12 1 y 1 60
        Día 5
        Press banca inclinado en multipower
        S R V T
         4 12 1 y 1 60
        Aperturas en máquina
        S R V T
         4 12 1 y 1 60
        Remo hammer
        S R V T
         4 12 1 y 1 60
        Jalones en máquina
        S R V T
         4 12 1 y 1 60

        --- PAGINA 4 ---
        Manu Miralles Granados
        14/01/2026
        Día 5
        Hiperextensiones
        S R V T
         4 12 1 y 1 60

        --- PAGINA 5 ---
        Manu Miralles Granados
        14/01/2026
        Explicación del entrenamiento
        Realizamos 4 series por ejercicio. El descanso entre series es de 60seg.
    """.trimIndent()

    /**
     * @brief Construye el caso de uso bajo prueba con los mocks y el resolver real.
     */
    private fun crearCasoUso(): ImportarRutinaNaturvitiaCasoUso = ImportarRutinaNaturvitiaCasoUso(
        repositorioRutina = repositorioRutina,
        repositorioEjercicio = repositorioEjercicio,
        repositorioGimnasio = repositorioGimnasio,
        resolverEjercicioAMaquina = resolverReal,
        dispatcher = Dispatchers.Unconfined
    )

    @Test
    fun `importa la rutina completa con 5 dias y mapea los ejercicios`() = runTest {
        coEvery { repositorioGimnasio.observarGimnasio() } returns flowOf(gimnasioConMaquinas)

        val resultado = crearCasoUso().ejecutar(textoPlanReal)

        assertTrue("La importación debe tener éxito.", resultado.isSuccess)
        val resumen = resultado.getOrThrow()

        // 5 rutinas, una por día, con días de la semana secuenciales desde el lunes.
        assertEquals(5, resumen.rutinasCreadas.size)
        assertEquals(
            listOf(listOf(1), listOf(2), listOf(3), listOf(4), listOf(5)),
            resumen.rutinasCreadas.map { it.diasSemana }
        )

        // Nombres compuestos con el grupo muscular deducido por el parser.
        assertEquals("Día 1 - Pierna", resumen.rutinasCreadas[0].nombre)
        assertEquals("Día 2 - Pecho", resumen.rutinasCreadas[1].nombre)
        assertEquals("Día 3 - Espalda", resumen.rutinasCreadas[2].nombre)
        assertEquals("Día 4 - Hombro", resumen.rutinasCreadas[3].nombre)
        assertEquals("Día 5 - Pecho", resumen.rutinasCreadas[4].nombre)

        // La descripción incluye la técnica general del plan (TUT y carga).
        assertTrue(resumen.rutinasCreadas[0].descripcion.orEmpty().contains("TUT 1-1"))

        // Bloques: Día 1 (6 ejercicios) y Día 3 (6 ejercicios) completos y en orden.
        assertEquals(6, resumen.rutinasCreadas[0].bloques.size)
        assertEquals("ejercicio-femoral-tumbado", resumen.rutinasCreadas[0].bloques[0].ejercicioId)
        assertEquals("bloque-1-0", resumen.rutinasCreadas[0].bloques[0].id)
        assertEquals(4, resumen.rutinasCreadas[0].bloques[0].serie)
        assertEquals(12, resumen.rutinasCreadas[0].bloques[0].repeticiones)
        assertEquals(60, resumen.rutinasCreadas[0].bloques[0].descansoSegundos)
        assertEquals(6, resumen.rutinasCreadas[2].bloques.size)

        // Los 29 ejercicios del plan real se mapean contra el catálogo de Fitness Park.
        assertTrue(
            "Deben mapearse al menos 25 ejercicios, pero se mapearon ${resumen.ejerciciosMapeados}.",
            resumen.ejerciciosMapeados >= 25
        )
        assertEquals(
            "El plan real contiene 29 ejercicios y el catálogo ampliado debe resolverlos todos.",
            29,
            resumen.ejerciciosMapeados
        )
        assertTrue(
            "Como máximo deben quedar 1-2 ejercicios sin mapear, pero quedaron: ${resumen.ejerciciosSinMapear}.",
            resumen.ejerciciosSinMapear.size <= 2
        )

        // Persistencia: 5 rutinas y una sola pasada de guardado masivo de ejercicios,
        // deduplicados (29 únicos) y todos con máquina resuelta y grupo muscular.
        coVerify(exactly = 5) { repositorioRutina.guardarRutina(any()) }
        coVerify(exactly = 1) {
            repositorioEjercicio.guardarVarios(
                match<List<Ejercicio>> { ejercicios ->
                    ejercicios.size == 29 &&
                        ejercicios.all { it.maquinaId != null } &&
                        ejercicios.all { it.grupoMuscularPrincipal.isNotBlank() }
                }
            )
        }
    }

    @Test
    fun `falla si el gimnasio es null`() = runTest {
        coEvery { repositorioGimnasio.observarGimnasio() } returns flowOf(null)

        val resultado = crearCasoUso().ejecutar(textoPlanReal)

        assertTrue(resultado.isFailure)
        assertEquals(
            ImportarRutinaNaturvitiaCasoUso.MENSAJE_GIMNASIO_NO_CONFIGURADO,
            resultado.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { repositorioRutina.guardarRutina(any()) }
        coVerify(exactly = 0) { repositorioEjercicio.guardarVarios(any()) }
    }

    @Test
    fun `falla si el gimnasio esta configurado pero no tiene maquinas`() = runTest {
        val gimnasioVacio = Gimnasio(id = "gimnasio-vacio", nombre = "Sin maquinaria")
        coEvery { repositorioGimnasio.observarGimnasio() } returns flowOf(gimnasioVacio)

        val resultado = crearCasoUso().ejecutar(textoPlanReal)

        assertTrue(resultado.isFailure)
        assertEquals(
            ImportarRutinaNaturvitiaCasoUso.MENSAJE_GIMNASIO_NO_CONFIGURADO,
            resultado.exceptionOrNull()?.message
        )
    }

    @Test
    fun `reporta ejercicios sin mapear sin fallar`() = runTest {
        coEvery { repositorioGimnasio.observarGimnasio() } returns flowOf(gimnasioConMaquinas)

        // Un ejercicio real (mapeable) y un ejercicio inventado que el motor no resuelve.
        val textoConDesconocido = """
            Día 1
            Rueda abdominal
            S R V T
             4 12 1 y 1 60
            Zumba galáctica
            S R V T
             4 12 1 y 1 60
        """.trimIndent()

        val resultado = crearCasoUso().ejecutar(textoConDesconocido)

        assertTrue("La importación no debe fallar por ejercicios sin mapear.", resultado.isSuccess)
        val resumen = resultado.getOrThrow()

        // La rutina se crea omitiendo el ejercicio desconocido.
        assertEquals(1, resumen.rutinasCreadas.size)
        assertEquals(1, resumen.rutinasCreadas[0].bloques.size)
        assertEquals("ejercicio-rueda-abdominal", resumen.rutinasCreadas[0].bloques[0].ejercicioId)

        // El ejercicio desconocido se reporta para revisión manual / IA.
        assertEquals(listOf("Zumba galáctica"), resumen.ejerciciosSinMapear)
        assertEquals(1, resumen.ejerciciosMapeados)
    }

    @Test
    fun `construye bloques con series repeticiones y descanso del pdf`() = runTest {
        coEvery { repositorioGimnasio.observarGimnasio() } returns flowOf(gimnasioConMaquinas)

        // Solo el día 3: incluye las excepciones de abdomen (4x10 y 30 s).
        val textoDia3 = """
            Día 3
            Peso muerto para espalda con barra
            S R V T
             4 12 1 y 1 60
            Dominadas en máquina asistida
            S R V T
             4 12 1 y 1 60
            Jalones al pecho agarre cerrado en "V"
            S R V T
             4 12 1 y 1 60
            Remo en polea baja
            S R V T
             4 12 1 y 1 60
            Elevación de piernas en paralelas
            S R V T
             4 10 1 y 1 30
            Rueda abdominal
            S R V T
             4 10 1 y 1 30
        """.trimIndent()

        val resultado = crearCasoUso().ejecutar(textoDia3)

        assertTrue(resultado.isSuccess)
        val rutina = resultado.getOrThrow().rutinasCreadas.single()
        assertEquals(6, rutina.bloques.size)

        // Excepción del plan: Rueda abdominal con 4x10 y 30 s.
        val bloqueRueda = rutina.bloques.first { it.ejercicioId == "ejercicio-rueda-abdominal" }
        assertEquals(4, bloqueRueda.serie)
        assertEquals(10, bloqueRueda.repeticiones)
        assertEquals(30, bloqueRueda.descansoSegundos)

        // Excepción del plan: Elevación de piernas en paralelas con 4x10 y 30 s.
        val bloquePiernas = rutina.bloques.first { it.ejercicioId == "ejercicio-elevacion-de-pierna-en-paralela" }
        assertEquals(4, bloquePiernas.serie)
        assertEquals(10, bloquePiernas.repeticiones)
        assertEquals(30, bloquePiernas.descansoSegundos)

        // El resto de ejercicios del día mantiene 12 repeticiones y 60 s de descanso.
        val bloquesEstandar = rutina.bloques.filter {
            it.ejercicioId != "ejercicio-rueda-abdominal" &&
                it.ejercicioId != "ejercicio-elevacion-de-pierna-en-paralela"
        }
        assertEquals(4, bloquesEstandar.size)
        assertTrue(bloquesEstandar.all { it.serie == 4 })
        assertTrue(bloquesEstandar.all { it.repeticiones == 12 })
        assertTrue(bloquesEstandar.all { it.descansoSegundos == 60 })
        // El peso se deja sin rellenar: se carga en el entrenamiento en vivo.
        assertTrue(rutina.bloques.all { it.pesoKg == null })
    }
}