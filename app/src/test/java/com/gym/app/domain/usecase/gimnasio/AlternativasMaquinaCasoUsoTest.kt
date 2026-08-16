/**
 * @file AlternativasMaquinaCasoUsoTest.kt
 * @brief Pruebas unitarias del motor de sustituciones de maquinaria del gimnasio.
 */
package com.gym.app.domain.usecase.gimnasio

import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.model.Gimnasio
import com.gym.app.domain.model.Maquina
import com.gym.app.domain.repository.RepositorioGimnasio
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class AlternativasMaquinaCasoUsoTest
 * @brief Verifica la tabla de sustitución del método Naturvitia: PRENSA busca
 * sentadillas, JALÓN dominadas o remos, EXTENSIÓN zancadas o sentadilla sissy, y
 * cualquier otra máquina se sustituye por ejercicios del mismo grupo muscular.
 */
class AlternativasMaquinaCasoUsoTest {

    private val repositorio = mockk<RepositorioGimnasio>(relaxed = true)

    private val casoUso = AlternativasMaquinaCasoUso(
        repositorioGimnasio = repositorio,
        dispatcher = Dispatchers.Unconfined
    )

    /** Catálogo de ejercicios de prueba con nombres representativos. */
    private val ejercicios = listOf(
        Ejercicio(id = "ej-sentadilla", nombre = "Sentadilla búlgara", grupoMuscularPrincipal = "PIERNA"),
        Ejercicio(id = "ej-hack", nombre = "Sentadilla hack", grupoMuscularPrincipal = "PIERNA"),
        Ejercicio(id = "ej-dominada", nombre = "Dominada asistida", grupoMuscularPrincipal = "ESPALDA"),
        Ejercicio(id = "ej-remo", nombre = "Remo en polea alta", grupoMuscularPrincipal = "ESPALDA"),
        Ejercicio(id = "ej-zancada", nombre = "Zancadas con mancuernas", grupoMuscularPrincipal = "PIERNA"),
        Ejercicio(id = "ej-sissy", nombre = "Sentadilla sissy", grupoMuscularPrincipal = "PIERNA"),
        Ejercicio(id = "ej-curl", nombre = "Curl de bíceps", grupoMuscularPrincipal = "BRAZO")
    )

    /**
     * @brief Configura el repositorio para devolver el gimnasio con la máquina dada.
     */
    private fun configurarGimnasio(maquina: Maquina) {
        every { repositorio.observarGimnasio() } returns flowOf(
            Gimnasio(id = "gimnasio-1", nombre = "Power House", maquinas = listOf(maquina))
        )
    }

    @Test
    fun `prensa sugiere ejercicios de sentadilla`() = runTest {
        configurarGimnasio(
            Maquina(id = "m-1", nombre = "Prensa de piernas 45º", grupoMuscular = listOf("PIERNA"))
        )

        val sustitutos = casoUso.ejecutar("m-1", ejercicios).getOrThrow()

        // La tabla de sustitución filtra por "sentadilla" en el nombre:
        // "Sentadilla sissy" también contiene la palabra clave, por lo que
        // el sustituto ej-sissy es incluido junto a ej-sentadilla y ej-hack.
        assertEquals(listOf("ej-sentadilla", "ej-hack", "ej-sissy"), sustitutos.map { it.id })
    }

    @Test
    fun `jalon sugiere dominadas o remos`() = runTest {
        configurarGimnasio(
            Maquina(id = "m-2", nombre = "Jalón al pecho", grupoMuscular = listOf("ESPALDA"))
        )

        val sustitutos = casoUso.ejecutar("m-2", ejercicios).getOrThrow()

        assertEquals(listOf("ej-dominada", "ej-remo"), sustitutos.map { it.id })
    }

    @Test
    fun `extension sugiere zancadas o sentadilla sissy`() = runTest {
        configurarGimnasio(
            Maquina(id = "m-3", nombre = "Extensión de cuádriceps", grupoMuscular = listOf("CUADRICEPS"))
        )

        val sustitutos = casoUso.ejecutar("m-3", ejercicios).getOrThrow()

        assertEquals(listOf("ej-zancada", "ej-sissy"), sustitutos.map { it.id })
    }

    @Test
    fun `maquina generica se sustituye por ejercicios del mismo grupo muscular`() = runTest {
        configurarGimnasio(
            Maquina(id = "m-4", nombre = "Banco de curl", grupoMuscular = listOf("BRAZO"))
        )

        val sustitutos = casoUso.ejecutar("m-4", ejercicios).getOrThrow()

        assertEquals(listOf("ej-curl"), sustitutos.map { it.id })
    }

    @Test
    fun `maquina no encontrada devuelve lista vacia`() = runTest {
        configurarGimnasio(
            Maquina(id = "m-1", nombre = "Prensa de piernas", grupoMuscular = listOf("PIERNA"))
        )

        val sustitutos = casoUso.ejecutar("m-inexistente", ejercicios).getOrThrow()

        assertTrue(sustitutos.isEmpty())
    }
}