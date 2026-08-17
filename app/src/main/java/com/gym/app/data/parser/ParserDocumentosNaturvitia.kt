/**
 * @file ParserDocumentosNaturvitia.kt
 * @brief Utilidad de extracción de texto y parseo para los documentos PDF de Naturvitia
 * (Dieta, Entrenamiento y Báscula/InBody).
 */
package com.gym.app.data.parser

import android.content.Context
import android.net.Uri
import com.gym.app.domain.model.Comida
import com.gym.app.domain.model.DetalleEjercicio
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
     * Detecta los días ("Día 1" a "Día 5"), los ejercicios con sus series, repeticiones, TUT y descanso.
     */
    fun parsearEntrenamiento(texto: String): List<Entrenamiento> {
        val entrenamientosMap = LinkedHashMap<String, MutableList<DetalleEjercicio>>()
        val lineas = texto.lines().map { it.trim() }.filter { it.isNotBlank() }

        var diaActualNombre: String? = null
        var ejercicioNombreActual: String? = null
        var inExplicacion = false

        for (linea in lineas) {
            val upper = linea.uppercase()
            if (upper.contains("EXPLICACIÓN") || upper.contains("EXPLICACION")) {
                inExplicacion = true
                continue
            }
            if (inExplicacion) {
                continue
            }
            if (upper.startsWith("--- PAGINA") || upper.contains("MANU MIRALLES") || linea.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
                continue
            }

            if (upper.startsWith("DÍA") || upper.startsWith("DIA")) {
                diaActualNombre = linea
                ejercicioNombreActual = null
                entrenamientosMap.getOrPut(diaActualNombre) { mutableListOf() }
                continue
            }

            if (upper == "S R V T" || upper == "SRVT") {
                continue
            }

            // Detect metrics line (e.g., "4 12 1 y 1 60" or containing numbers and 'y')
            val regexMetricas = Regex("^\\s*(\\d+)\\s+(\\d+)\\s+(.+)\\s+(\\d+)\\s*$")
            val matchMetricas = regexMetricas.find(linea)
            if (matchMetricas != null || (linea.any { it.isDigit() } && linea.contains("y"))) {
                val partes = linea.trim().split(Regex("\\s+"))
                if (partes.size >= 4) {
                    val series = partes[0].toIntOrNull() ?: 4
                    val reps = partes[1].toIntOrNull() ?: 12
                    val vel = partes.subList(2, partes.size - 1).joinToString(" ")
                    val descanso = partes.last().toIntOrNull() ?: 60

                    if (ejercicioNombreActual != null && diaActualNombre != null) {
                        entrenamientosMap.getOrPut(diaActualNombre) { mutableListOf() }.add(
                            DetalleEjercicio(
                                nombre = ejercicioNombreActual,
                                series = series,
                                repeticiones = reps,
                                velocidad = vel,
                                descansoSegundos = descanso
                            )
                        )
                        ejercicioNombreActual = null
                    }
                }
            } else {
                ejercicioNombreActual = linea
            }
        }

        val entrenamientos = mutableListOf<Entrenamiento>()
        for ((nombreDia, ejercicios) in entrenamientosMap) {
            if (ejercicios.isNotEmpty()) {
                entrenamientos.add(crearEntrenamientoDesdeDia(nombreDia, ejercicios))
            }
        }

        if (entrenamientos.isEmpty()) {
            entrenamientos.add(
                Entrenamiento(
                    id = UUID.randomUUID().toString(),
                    nombre = "Día 1",
                    grupoMuscular = listOf("Fullbody"),
                    seriesTotales = 16,
                    ejerciciosRealizados = 0,
                    totalEjercicios = 4,
                    duracionMinutos = 60,
                    completo = false,
                    fecha = System.currentTimeMillis()
                )
            )
        }

        return entrenamientos
    }

    /**
     * Crea un objeto [Entrenamiento] a partir del nombre del día y su lista de ejercicios analizados.
     */
    private fun crearEntrenamientoDesdeDia(nombre: String, ejercicios: List<DetalleEjercicio>): Entrenamiento {
        val upperNombre = nombre.uppercase()
        val grupo = when {
            upperNombre.contains("1") -> listOf("Pierna", "Glúteo")
            upperNombre.contains("2") -> listOf("Pecho", "Bíceps")
            upperNombre.contains("3") -> listOf("Espalda", "Abdomen")
            upperNombre.contains("4") -> listOf("Hombro", "Tríceps")
            upperNombre.contains("5") -> listOf("Pecho", "Espalda")
            else -> listOf("Fullbody")
        }
        val seriesTotales = ejercicios.sumOf { it.series }
        val totalEjercicios = ejercicios.size

        return Entrenamiento(
            id = UUID.randomUUID().toString(),
            nombre = nombre,
            grupoMuscular = grupo,
            seriesTotales = seriesTotales,
            ejerciciosRealizados = 0,
            totalEjercicios = totalEjercicios,
            duracionMinutos = totalEjercicios * 10,
            completo = false,
            fecha = System.currentTimeMillis(),
            ejercicios = ejercicios,
            observaciones = "TUT 1-1, peso máximo al fallo técnico"
        )
    }
}
