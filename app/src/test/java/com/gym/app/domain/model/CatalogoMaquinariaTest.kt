/**
 * @file CatalogoMaquinariaTest.kt
 * @brief Pruebas unitarias del catálogo real de maquinaria Fitness Park España.
 */
package com.gym.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class CatalogoMaquinariaTest
 * @brief Verifica la integridad del [CatalogoMaquinaria] real de Fitness Park:
 * contenido mínimo, unicidad de IDs, marcas principales, resolución de los 29
 * ejercicios del plan nutricional, familias musculares y propagación de atributos.
 */
class CatalogoMaquinariaTest {

    @Test
    fun `el catalogo no esta vacio y contiene al menos 45 maquinas reales`() {
        val catalogo = CatalogoMaquinaria.maquinas

        assertTrue("El catálogo no debe estar vacío.", catalogo.isNotEmpty())
        assertTrue(
            "El catálogo debe contener al menos 45 máquinas de Fitness Park, pero contiene ${catalogo.size}.",
            catalogo.size >= 45
        )
    }

    @Test
    fun `todos los identificadores son unicos y no vacios`() {
        val ids = CatalogoMaquinaria.maquinas.map { it.id }

        assertEquals(
            "El catálogo contiene identificadores duplicados.",
            ids.size,
            ids.toSet().size
        )
        assertTrue(
            "Todos los identificadores deben ser no vacíos.",
            ids.all { it.isNotBlank() }
        )
    }

    @Test
    fun `cada maquina tiene nombre no vacio y al menos un grupo muscular`() {
        val maquinas = CatalogoMaquinaria.maquinas

        assertTrue(
            "Todas las máquinas deben tener un nombre no vacío.",
            maquinas.all { it.nombre.isNotBlank() }
        )
        assertTrue(
            "Todas las máquinas deben trabajar al menos un grupo muscular.",
            maquinas.all { it.grupoMuscular.isNotEmpty() }
        )
        assertEquals(
            "Los nombres comerciales de las máquinas deben ser únicos.",
            maquinas.size,
            maquinas.map { it.nombre }.toSet().size
        )
    }

    @Test
    fun `el catalogo incluye maquinas de las marcas principales de Fitness Park`() {
        val marcas = CatalogoMaquinaria.maquinas.mapNotNull { it.marca }.toSet()

        assertTrue("Debe incluir Technogym.", marcas.any { it.contains("Technogym", ignoreCase = true) })
        assertTrue("Debe incluir Hammer Strength.", marcas.any { it.contains("Hammer Strength", ignoreCase = true) })
        assertTrue("Debe incluir gym80.", marcas.any { it.contains("gym80", ignoreCase = true) })
        assertTrue("Debe incluir Eleiko.", marcas.any { it.contains("Eleiko", ignoreCase = true) })
        assertTrue("Debe incluir Nike Strength o Rogue.", marcas.any { it.contains("Nike", ignoreCase = true) || it.contains("Rogue", ignoreCase = true) })
    }

    @Test
    fun `las entradas con marca declaran ejercicios posibles y sinonimos`() {
        val conMarca = CatalogoMaquinaria.maquinas.filter { it.marca != null }
        assertTrue(
            "Las entradas con marca deben declarar ejercicios posibles y sinónimos para el mapeo.",
            conMarca.all { it.ejerciciosPosibles.isNotEmpty() || it.sinonimos.isNotEmpty() }
        )
    }

    @Test
    fun `los 29 ejercicios del plan real del nutricionista son resolubles`() {
        val ejerciciosNutricionista = listOf(
            "Femoral tumbado",
            "Prensa a 45º",
            "Adductor",
            "Patada de glúteo en máquina",
            "Extensiones",
            "Hip thrust en banco",
            "Press horizontal en máquina",
            "Cruces en polea",
            "Press vertical en máquina peso libre",
            "Peck deck",
            "Curl con mancuernas en banco 45º",
            "Curl con barra",
            "Peso muerto con barra",
            "Dominadas asistidas",
            "Jalones en V",
            "Remo en polea baja",
            "Elevación de piernas en paralelas",
            "Rueda abdominal",
            "Elevaciones posteriores con mancuerna",
            "Elevaciones laterales con mancuerna",
            "Press militar en multipower",
            "Deltoide posterior en máquina",
            "Extensiones de tríceps en polea",
            "Press francés con barra",
            "Press banca inclinado en multipower",
            "Aperturas en máquina",
            "Remo hammer",
            "Jalones en máquina",
            "Hiperextensiones"
        )

        val todasEntradas = CatalogoMaquinaria.maquinas

        for (ejercicio in ejerciciosNutricionista) {
            val resolible = todasEntradas.any { entrada ->
                entrada.sinonimos.any { it.equals(ejercicio, ignoreCase = true) } ||
                entrada.ejerciciosPosibles.any { it.equals(ejercicio, ignoreCase = true) } ||
                entrada.nombre.contains(ejercicio, ignoreCase = true)
            }
            assertTrue("El ejercicio del plan Naturvitia '$ejercicio' debe resolverse contra el catálogo de Fitness Park.", resolible)
        }
    }

    @Test
    fun `agruparPorFamilia devuelve las familias esperadas con sus maquinas`() {
        val familias = CatalogoMaquinaria.agruparPorFamilia()
        val familiasEsperadas = setOf(
            "Pierna",
            "Espalda",
            "Pecho",
            "Hombro",
            "Bíceps",
            "Tríceps",
            "Abdomen",
            "Equipamiento libre"
        )

        assertEquals("Deben existir las 8 familias musculares del catálogo.", familiasEsperadas, familias.keys)

        // Cada familia contiene al menos una máquina.
        assertTrue(
            "Cada familia debe contener al menos una máquina.",
            familias.values.all { it.isNotEmpty() }
        )

        // Todas las máquinas del catálogo quedan repartidas sin pérdidas.
        val totalRepartidas = familias.values.sumOf { it.size }
        assertEquals(CatalogoMaquinaria.maquinas.size, totalRepartidas)
    }

    @Test
    fun `aMaquina convierte una entrada propagando marca, modelo, ejerciciosPosibles y sinonimos`() {
        val entrada = CatalogoMaquinaria.EntradaCatalogo(
            id = "prensa-45",
            nombre = "Prensa de piernas 45º Technogym Artis",
            grupoMuscular = listOf("CUADRICEPS", "GLUTEO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Leg Press",
            ejerciciosPosibles = listOf("prensa-45", "prensa"),
            sinonimos = listOf("Prensa a 45º", "prensa 45")
        )

        val maquina = CatalogoMaquinaria.aMaquina(entrada)

        assertEquals("prensa-45", maquina.id)
        assertEquals("Prensa de piernas 45º Technogym Artis", maquina.nombre)
        assertEquals(listOf("CUADRICEPS", "GLUTEO"), maquina.grupoMuscular)
        assertEquals(Maquina.TIPO_MAQUINA_GUIADA, maquina.tipoEquipamiento)
        assertTrue(maquina.disponible)
        assertEquals("Technogym", maquina.marca)
        assertEquals("Artis Leg Press", maquina.modelo)
        assertEquals(listOf("prensa-45", "prensa"), maquina.ejerciciosPosibles)
        assertEquals(listOf("Prensa a 45º", "prensa 45"), maquina.sinonimos)
    }
}
