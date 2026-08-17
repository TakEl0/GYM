/**
 * @file ParserEntrenamientoRealTest.kt
 * @brief Pruebas unitarias para verificar el correcto parseo de planes de entrenamiento reales de Naturvitia.
 */
package com.gym.app.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class ParserEntrenamientoRealTest
 * @brief Prueba que el parser extraiga con precisión los 5 días de entrenamiento, sus ejercicios, series, repeticiones, TUT y descansos.
 */
class ParserEntrenamientoRealTest {

    private val textoPdfReal = """
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

    @Test
    fun testParsearEntrenamientoCompleto5Dias() {
        val entrenamientos = ParserDocumentosNaturvitia.parsearEntrenamiento(textoPdfReal)

        // Verificar que se detectan exactamente 5 entrenamientos (Día 1 a Día 5)
        assertEquals(5, entrenamientos.size)
        assertEquals("Día 1", entrenamientos[0].nombre)
        assertEquals("Día 2", entrenamientos[1].nombre)
        assertEquals("Día 3", entrenamientos[2].nombre)
        assertEquals("Día 4", entrenamientos[3].nombre)
        assertEquals("Día 5", entrenamientos[4].nombre)
    }

    @Test
    fun testDia1EjerciciosYMetricas() {
        val entrenamientos = ParserDocumentosNaturvitia.parsearEntrenamiento(textoPdfReal)
        val dia1 = entrenamientos[0]

        // Día 1 tiene 6 ejercicios
        assertEquals(6, dia1.ejercicios.size)
        assertEquals("Femoral tumbado", dia1.ejercicios[0].nombre)
        assertEquals(4, dia1.ejercicios[0].series)
        assertEquals(12, dia1.ejercicios[0].repeticiones)
        assertEquals("1 y 1", dia1.ejercicios[0].velocidad)
        assertEquals(60, dia1.ejercicios[0].descansoSegundos)

        assertEquals("Prensa a 45º", dia1.ejercicios[1].nombre)
    }

    @Test
    fun testDia3Excepciones() {
        val entrenamientos = ParserDocumentosNaturvitia.parsearEntrenamiento(textoPdfReal)
        val dia3 = entrenamientos[2]

        // Verificar excepciones en Día 3: Elevación de piernas en paralelas y Rueda abdominal con 4x10 y 30s
        val elevacionPiernas = dia3.ejercicios.firstOrNull { it.nombre.contains("Elevación de piernas") }
        val ruedaAbdominal = dia3.ejercicios.firstOrNull { it.nombre.contains("Rueda abdominal") }

        assertTrue(elevacionPiernas != null)
        assertEquals(4, elevacionPiernas?.series)
        assertEquals(10, elevacionPiernas?.repeticiones)
        assertEquals(30, elevacionPiernas?.descansoSegundos)

        assertTrue(ruedaAbdominal != null)
        assertEquals(4, ruedaAbdominal?.series)
        assertEquals(10, ruedaAbdominal?.repeticiones)
        assertEquals(30, ruedaAbdominal?.descansoSegundos)
    }
}
