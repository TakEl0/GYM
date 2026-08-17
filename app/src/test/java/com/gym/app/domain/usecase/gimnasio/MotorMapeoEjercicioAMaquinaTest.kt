/**
 * @file MotorMapeoEjercicioAMaquinaTest.kt
 * @brief Pruebas unitarias del motor local de mapeo de ejercicios a máquinas.
 *
 * Verifica con los nombres EXACTOS del plan del nutricionista (método Naturvitia) contra
 * el catálogo real de Fitness Park (47 máquinas), así como el comportamiento de la
 * normalización y el fallback por familia muscular.
 */
package com.gym.app.domain.usecase.gimnasio

import com.gym.app.domain.model.CatalogoMaquinaria
import com.gym.app.domain.model.Maquina
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class MotorMapeoEjercicioAMaquinaTest
 * @brief Comprueba que el motor resuelve localmente los ejercicios reales del plan del
 * nutricionista contra el catálogo de Fitness Park y que normaliza correctamente.
 */
class MotorMapeoEjercicioAMaquinaTest {

    /** Catálogo real de Fitness Park convertido a máquinas de dominio. */
    private val maquinas: List<Maquina> =
        CatalogoMaquinaria.maquinas.map { CatalogoMaquinaria.aMaquina(it) }

    @Test
    fun `femoral tumbado resuelve con origen EXACTO o SINONIMO y confianza alta contra una maquina FEMORAL`() {
        val r = requireNotNull(MotorMapeoEjercicioAMaquina.resolver("Femoral tumbado", maquinas)) {
            "'Femoral tumbado' debe resolverse contra el catálogo."
        }

        assertTrue(
            "El origen debe ser EXACTO o SINONIMO, pero fue ${r.origen}.",
            r.origen == OrigenMapeo.EXACTO || r.origen == OrigenMapeo.SINONIMO
        )
        assertTrue("La confianza debe ser >= 0.8.", r.confianza >= 0.8f)

        val maquina = maquinas.first { it.id == r.maquinaId }
        assertTrue(
            "La máquina resuelta debe trabajar el grupo FEMORAL.",
            maquina.grupoMuscular.any { it.equals("FEMORAL", ignoreCase = true) }
        )
    }

    @Test
    fun `prensa a 45 grados resuelve contra la prensa de piernas`() {
        val r = requireNotNull(MotorMapeoEjercicioAMaquina.resolver("Prensa a 45º", maquinas)) {
            "'Prensa a 45º' debe resolverse."
        }

        assertEquals("prensa-45", r.maquinaId)
        assertEquals(OrigenMapeo.EXACTO, r.origen)
    }

    @Test
    fun `press militar en multipower resuelve contra la maquina smith`() {
        val r = requireNotNull(MotorMapeoEjercicioAMaquina.resolver("Press militar en multipower", maquinas)) {
            "'Press militar en multipower' debe resolverse."
        }

        assertEquals("multipower-smith", r.maquinaId)
    }

    @Test
    fun `curl con mancuernas en banco 45 resuelve contra el banco ajustable`() {
        val r = requireNotNull(MotorMapeoEjercicioAMaquina.resolver("Curl con mancuernas en banco 45º", maquinas)) {
            "'Curl con mancuernas en banco 45º' debe resolverse."
        }

        assertEquals("banco-ajustable", r.maquinaId)
    }

    @Test
    fun `adductor resuelve contra la maquina de aductores`() {
        val r = requireNotNull(MotorMapeoEjercicioAMaquina.resolver("Adductor", maquinas)) {
            "'Adductor' debe resolverse."
        }

        assertEquals("adductor", r.maquinaId)
    }

    @Test
    fun `patada de gluteo en maquina resuelve contra el multi hip`() {
        val r = requireNotNull(MotorMapeoEjercicioAMaquina.resolver("Patada de glúteo en máquina", maquinas)) {
            "'Patada de glúteo en máquina' debe resolverse."
        }

        assertEquals("multi-hip", r.maquinaId)
    }

    @Test
    fun `elevaciones laterales con mancuerna resuelve con equipamiento MANCUERNAS`() {
        val r = requireNotNull(MotorMapeoEjercicioAMaquina.resolver("Elevaciones laterales con mancuerna", maquinas)) {
            "'Elevaciones laterales con mancuerna' debe resolverse."
        }

        val maquina = maquinas.first { it.id == r.maquinaId }
        assertEquals(Maquina.TIPO_MANCUERNAS, maquina.tipoEquipamiento)
    }

    @Test
    fun `rueda abdominal resuelve contra la rueda de abdominales`() {
        val r = requireNotNull(MotorMapeoEjercicioAMaquina.resolver("Rueda abdominal", maquinas)) {
            "'Rueda abdominal' debe resolverse."
        }

        assertEquals("rueda-abdominal", r.maquinaId)
    }

    @Test
    fun `nombre inventado devuelve null para que despues intervenga la IA`() {
        assertNull(
            "Un nombre inventado no debe resolverse localmente.",
            MotorMapeoEjercicioAMaquina.resolver("Zumba galáctica", maquinas)
        )
    }

    @Test
    fun `fallback por familia muscular resuelve con origen FAMILIA`() {
        val r = requireNotNull(MotorMapeoEjercicioAMaquina.resolver("Desarrollo de hombro con barra", maquinas)) {
            "Debe resolverse por familia muscular y equipamiento."
        }

        assertEquals(OrigenMapeo.FAMILIA, r.origen)
        assertTrue(
            "La confianza de familia debe estar en 0.6..0.7.",
            r.confianza in 0.6f..0.7f
        )
    }

    // ─── Función de normalización ───────────────────────────────────────────────

    @Test
    fun `normalizar quita acentos y pasa a minusculas`() {
        assertEquals("femoral tumbado", MotorMapeoEjercicioAMaquina.normalizar("Fémoral Tumbado"))
    }

    @Test
    fun `normalizar expande 45 grados a 45`() {
        assertEquals("prensa a 45", MotorMapeoEjercicioAMaquina.normalizar("Prensa a 45º"))
    }

    @Test
    fun `normalizar expande multipower a smith`() {
        assertEquals(
            "press militar en smith",
            MotorMapeoEjercicioAMaquina.normalizar("Press militar en multipower")
        )
    }

    @Test
    fun `normalizar colapsa espacios y guiones`() {
        assertEquals(
            "curl femoral tumbado",
            MotorMapeoEjercicioAMaquina.normalizar("  Curl-Femoral   Tumbado  ")
        )
    }

    @Test
    fun `normalizar reduce plurales basicos a singular`() {
        assertEquals(
            "elevacion lateral con mancuerna",
            MotorMapeoEjercicioAMaquina.normalizar("Elevaciones laterales con mancuerna")
        )
    }

    @Test
    fun `normalizar no mutila palabras invariantes que terminan en s`() {
        assertEquals("press", MotorMapeoEjercicioAMaquina.normalizar("Press"))
        assertEquals("biceps", MotorMapeoEjercicioAMaquina.normalizar("Bíceps"))
    }
}