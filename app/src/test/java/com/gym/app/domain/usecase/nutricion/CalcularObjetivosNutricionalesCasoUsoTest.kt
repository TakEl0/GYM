/**
 * @file CalcularObjetivosNutricionalesCasoUsoTest.kt
 * @brief Pruebas unitarias del cálculo de objetivos nutricionales a partir del perfil.
 */
package com.gym.app.domain.usecase.nutricion

import com.gym.app.domain.model.PerfilUsuario
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class CalcularObjetivosNutricionalesCasoUsoTest
 * @brief Verifica los cálculos de Mifflin-St Jeor, el factor de actividad y el
 * ajuste por objetivo (volumen +15 %, definición -20 %, mantenimiento sin cambios)
 * tal como los expone el [PerfilUsuario] a través del caso de uso.
 */
class CalcularObjetivosNutricionalesCasoUsoTest {

    private val casoUso = CalcularObjetivosNutricionalesCasoUso()

    /**
     * @brief Construye un perfil completo con los datos antropométricos indicados.
     */
    private fun crearPerfil(
        pesoKg: Double,
        alturaCm: Double,
        edad: Int,
        sexo: String,
        factorActividad: String,
        objetivo: String
    ): PerfilUsuario = PerfilUsuario(
        id = "perfil-1",
        email = "usuario@correo.com",
        nombre = "Usuario de prueba",
        pesoObjetivoKg = pesoKg,
        alturaCm = alturaCm,
        edad = edad,
        sexo = sexo,
        factorActividad = factorActividad,
        objetivo = objetivo
    )

    @Test
    fun `calcula los objetivos de un hombre con Mifflin-St Jeor y volumen`() = runTest {
        // Hombre: TMB = 10*80 + 6,25*180 - 5*30 + 5 = 1780 kcal.
        // TDEE (moderado 1,55) = 2759 kcal. Volumen (+15 %) = 3173 kcal.
        val perfil = crearPerfil(
            pesoKg = 80.0,
            alturaCm = 180.0,
            edad = 30,
            sexo = PerfilUsuario.SEXO_HOMBRE,
            factorActividad = "MODERADO",
            objetivo = PerfilUsuario.OBJETIVO_VOLUMEN
        )

        val resumen = casoUso.ejecutar(perfil).getOrThrow()

        assertEquals(3173.0, resumen.kcalObjetivo, 0.001)
        assertEquals(3173.0, resumen.kcalRestantes, 0.001)
        assertEquals(0.0, resumen.kcalConsumidas, 0.001)
        // Proteína 2,0 g/kg = 160 g; grasa 1,0 g/kg = 80 g; CH = (3173-640-720)/4 = 453 g.
        assertEquals(160.0, resumen.proteinasObjetivoG, 0.001)
        assertEquals(80.0, resumen.grasasObjetivoG, 0.001)
        assertEquals(453.0, resumen.carbohidratosObjetivoG, 0.001)
    }

    @Test
    fun `calcula los objetivos de una mujer con Mifflin-St Jeor y definicion`() = runTest {
        // Mujer: TMB = 10*60 + 6,25*165 - 5*25 - 161 = 1345 kcal.
        // TDEE (ligero 1,375) = 1849 kcal. Definición (-20 %) = 1479 kcal.
        val perfil = crearPerfil(
            pesoKg = 60.0,
            alturaCm = 165.0,
            edad = 25,
            sexo = PerfilUsuario.SEXO_MUJER,
            factorActividad = "LIGERO",
            objetivo = PerfilUsuario.OBJETIVO_DEFINICION
        )

        val resumen = casoUso.ejecutar(perfil).getOrThrow()

        assertEquals(1479.0, resumen.kcalObjetivo, 0.001)
        assertEquals(120.0, resumen.proteinasObjetivoG, 0.001)
        assertEquals(60.0, resumen.grasasObjetivoG, 0.001)
        // CH = (1479 - 480 - 540) / 4 = 114,75 -> 115 g.
        assertEquals(115.0, resumen.carbohidratosObjetivoG, 0.001)
    }

    @Test
    fun `mantenimiento mantiene las calorias del TDEE sin ajuste`() = runTest {
        // TDEE moderado del hombre de 80 kg / 180 cm / 30 años = 2759 kcal.
        val perfil = crearPerfil(
            pesoKg = 80.0,
            alturaCm = 180.0,
            edad = 30,
            sexo = PerfilUsuario.SEXO_HOMBRE,
            factorActividad = "MODERADO",
            objetivo = PerfilUsuario.OBJETIVO_MANTENIMIENTO
        )

        val resumen = casoUso.ejecutar(perfil).getOrThrow()

        assertEquals(2759.0, resumen.kcalObjetivo, 0.001)
    }

    @Test
    fun `el factor de actividad fuerte multiplica la TMB por 1,725`() = runTest {
        // TMB hombre de 80 kg / 180 cm / 30 años = 1780 kcal.
        // TDEE fuerte = 1780 * 1,725 = 3070,5 -> 3071 kcal.
        val perfil = crearPerfil(
            pesoKg = 80.0,
            alturaCm = 180.0,
            edad = 30,
            sexo = PerfilUsuario.SEXO_HOMBRE,
            factorActividad = "FUERTE",
            objetivo = PerfilUsuario.OBJETIVO_MANTENIMIENTO
        )

        val resumen = casoUso.ejecutar(perfil).getOrThrow()

        assertEquals(3071.0, resumen.kcalObjetivo, 0.001)
    }

    @Test
    fun `perfil incompleto devuelve error de validacion`() = runTest {
        val perfilIncompleto = PerfilUsuario(
            id = "perfil-incompleto",
            email = "usuario@correo.com",
            nombre = "Sin datos",
            pesoObjetivoKg = 80.0
            // El resto de campos antropométricos está vacío.
        )

        val resultado = casoUso.ejecutar(perfilIncompleto)

        assertTrue(resultado.isFailure)
        assertEquals(
            "El perfil no está completo para calcular los objetivos nutricionales.",
            resultado.exceptionOrNull()?.message
        )
    }
}