/**
 * @file CatalogoMaquinariaTest.kt
 * @brief Pruebas unitarias del catálogo estándar de maquinaria de gimnasio.
 */
package com.gym.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class CatalogoMaquinariaTest
 * @brief Verifica la integridad del [CatalogoMaquinaria]: contenido mínimo,
 * unicidad de identificadores, agrupación por familias musculares y la
 * conversión de entradas del catálogo a [Maquina] de dominio.
 */
class CatalogoMaquinariaTest {

    @Test
    fun `el catalogo no esta vacio y contiene al menos 30 maquinas`() {
        val catalogo = CatalogoMaquinaria.maquinas

        assertTrue("El catálogo no debe estar vacío.", catalogo.isNotEmpty())
        assertTrue(
            "El catálogo debe contener al menos 30 máquinas, pero contiene ${catalogo.size}.",
            catalogo.size >= 30
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
    fun `aMaquina convierte una entrada en una maquina con id estable y disponible`() {
        val entrada = CatalogoMaquinaria.EntradaCatalogo(
            id = "prensa-45",
            nombre = "Prensa de piernas 45º",
            grupoMuscular = listOf("CUADRICEPS", "GLUTEO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA
        )

        val maquina = CatalogoMaquinaria.aMaquina(entrada)

        assertEquals("El identificador del catálogo debe conservarse (estable).", "prensa-45", maquina.id)
        assertEquals("Prensa de piernas 45º", maquina.nombre)
        assertEquals(listOf("CUADRICEPS", "GLUTEO"), maquina.grupoMuscular)
        assertEquals(Maquina.TIPO_MAQUINA_GUIADA, maquina.tipoEquipamiento)
        assertTrue("La máquina importada debe estar disponible por defecto.", maquina.disponible)
    }
}