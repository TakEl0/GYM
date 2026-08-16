/**
 * @file ImportarDocumentosNaturvitiaCasoUso.kt
 * @brief Caso de uso para importar y procesar los 3 documentos PDF de Naturvitia (Dieta, Entrenamiento y Báscula).
 */
package com.gym.app.domain.usecase.importacion

import android.content.Context
import android.net.Uri
import com.gym.app.data.parser.ParserDocumentosNaturvitia
import com.gym.app.domain.repository.RepositorioComida
import com.gym.app.domain.repository.RepositorioEntrenamiento
import com.gym.app.domain.repository.RepositorioPeso
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class ImportarDocumentosNaturvitiaCasoUso
 * @brief Orquesta la extracción de datos desde los archivos PDF mediante PDFBox
 * y los persiste en los repositorios locales y de Supabase.
 */
class ImportarDocumentosNaturvitiaCasoUso(
    private val repositorioPeso: RepositorioPeso,
    private val repositorioComida: RepositorioComida,
    private val repositorioEntrenamiento: RepositorioEntrenamiento,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Procesa los URIs de los documentos PDF proporcionados y registra la información extraída.
     * @param context Contexto de Android para resolver los URIs.
     * @param uriInBody URI del PDF de la báscula / InBody (opcional).
     * @param uriDieta URI del PDF de la dieta (opcional).
     * @param uriEntrenamiento URI del PDF del plan de entrenamiento (opcional).
     * @return [Result] con éxito o error de importación.
     */
    suspend fun ejecutar(
        context: Context,
        uriInBody: Uri?,
        uriDieta: Uri?,
        uriEntrenamiento: Uri?
    ): Result<Unit> = withContext(dispatcher) {
        try {
            // 1. Procesar InBody / Báscula
            if (uriInBody != null) {
                val textoInBody = ParserDocumentosNaturvitia.extraerTextoPdf(context, uriInBody)
                val registroPeso = ParserDocumentosNaturvitia.parsearInBody(textoInBody)
                repositorioPeso.guardarRegistro(registroPeso)
            }

            // 2. Procesar Dieta
            if (uriDieta != null) {
                val textoDieta = ParserDocumentosNaturvitia.extraerTextoPdf(context, uriDieta)
                val listaComidas = ParserDocumentosNaturvitia.parsearDieta(textoDieta)
                for (comida in listaComidas) {
                    repositorioComida.guardarComida(comida)
                }
            }

            // 3. Procesar Entrenamiento
            if (uriEntrenamiento != null) {
                val textoEntreno = ParserDocumentosNaturvitia.extraerTextoPdf(context, uriEntrenamiento)
                val listaEntrenamientos = ParserDocumentosNaturvitia.parsearEntrenamiento(textoEntreno)
                for (entreno in listaEntrenamientos) {
                    repositorioEntrenamiento.guardarEntrenamiento(entreno)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
