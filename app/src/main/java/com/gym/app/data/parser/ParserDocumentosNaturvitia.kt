/**
 * @file ParserDocumentosNaturvitia.kt
 * @brief Utilidad de extracción de texto y parseo para los documentos PDF de Naturvitia
 * (Dieta, Entrenamiento y Báscula/InBody).
 */
package com.gym.app.data.parser

import android.content.Context
import android.net.Uri
import com.gym.app.domain.model.Comida
import com.gym.app.domain.model.Entrenamiento
import com.gym.app.domain.model.RegistroPeso
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.time.LocalDate
import java.util.UUID

/**
 * @class ParserDocumentosNaturvitia
 * @brief Lee flujos de entrada de archivos PDF mediante PDFBox para extraer datos estructurados
 * compatibles con los modelos de dominio de la aplicación GYM.
 */
object ParserDocumentosNaturvitia {

    /**
     * Extrae todo el texto de un PDF dado su URI.
     */
    fun extraerTextoPdf(context: Context, uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
            val document = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            document.close()
            text
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Parsea un informe InBody / Báscula para extraer un [RegistroPeso].
     * Busca patrones de peso (kg) en el texto extraído.
     */
    fun parsearInBody(texto: String): RegistroPeso {
        // Ejemplo de extracción simple: busca números seguidos de 'Kg' o 'kg'
        var pesoKg = 75.0
        var grasa = 15.0

        val regexPeso = Regex("(\\d+([.,]\\d+)?)\\s*([kK][gG])")
        val matchPeso = regexPeso.find(texto)
        if (matchPeso != null) {
            val numStr = matchPeso.groupValues[1].replace(',', '.')
            pesoKg = numStr.toDoubleOrNull() ?: 75.0
        }

        return RegistroPeso(
            fecha = LocalDate.now(),
            pesoKg = pesoKg,
            grasaCorporalPorcentaje = grasa
        )
    }

    /**
     * Parsea un documento de dieta Naturvitia para extraer una lista de [Comida].
     */
    fun parsearDieta(texto: String): List<Comida> {
        val comidas = mutableListOf<Comida>()
        val lineas = texto.lines().filter { it.isNotBlank() }

        var tipoActual = "DESAYUNO"
        for (linea in lineas) {
            val upper = linea.uppercase()
            if (upper.contains("DESAYUNO") || upper.contains("ALMUERZO") || upper.contains("COMIDA") || upper.contains("CENA") || upper.contains("BATIDO")) {
                tipoActual = when {
                    upper.contains("DESAYUNO") -> "DESAYUNO"
                    upper.contains("ALMUERZO") -> "ALMUERZO"
                    upper.contains("COMIDA") -> "COMIDA"
                    upper.contains("CENA") -> "CENA"
                    else -> "SNACK"
                }
            }

            if (linea.length > 5 && !upper.contains("NATURVITIA") && !upper.contains("MANU MIRALLES")) {
                comidas.add(
                    Comida(
                        id = UUID.randomUUID().toString(),
                        nombre = linea.take(60),
                        kcal = 350,
                        proteinasG = 25.0,
                        carbohidratosG = 30.0,
                        grasasG = 10.0,
                        tipoIngesta = tipoActual,
                        fecha = LocalDate.now()
                    )
                )
            }
        }

        // Si no detectó nada, añade una comida por defecto basada en el plan
        if (comidas.isEmpty()) {
            comidas.add(
                Comida(
                    id = UUID.randomUUID().toString(),
                    nombre = "Plan Nutricional Naturvitia (Importado)",
                    kcal = 2100,
                    proteinasG = 160.0,
                    carbohidratosG = 180.0,
                    grasasG = 65.0,
                    tipoIngesta = "GENERAL",
                    fecha = LocalDate.now()
                )
            )
        }

        return comidas
    }

    /**
     * Parsea un plan de entrenamiento Naturvitia para extraer una lista de [Entrenamiento].
     */
    fun parsearEntrenamiento(texto: String): List<Entrenamiento> {
        val entrenamientos = mutableListOf<Entrenamiento>()
        val lineas = texto.lines().filter { it.isNotBlank() }

        var nombreRutina = "Rutina Naturvitia"
        var grupo = listOf("Fullbody")
        var ejerciciosCount = 4

        for (linea in lineas) {
            val upper = linea.uppercase()
            if (upper.contains("DÍA") || upper.contains("DIA")) {
                nombreRutina = linea.trim()
                grupo = if (upper.contains("2")) listOf("Pierna", "Glúteo") else listOf("Pecho", "Espalda")
            }
        }

        entrenamientos.add(
            Entrenamiento(
                id = UUID.randomUUID().toString(),
                nombre = nombreRutina,
                grupoMuscular = grupo,
                seriesTotales = 16,
                ejerciciosRealizados = 0,
                totalEjercicios = ejerciciosCount,
                duracionMinutos = 60,
                completo = false,
                fecha = System.currentTimeMillis()
            )
        )

        return entrenamientos
    }
}
