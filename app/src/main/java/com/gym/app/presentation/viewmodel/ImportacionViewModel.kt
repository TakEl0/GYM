/**
 * @file ImportacionViewModel.kt
 * @brief ViewModel de la pantalla de importación de documentos Naturvitia.
 * Gestiona la selección de los tres documentos PDF (Dieta, Entrenamiento e
 * InBody/Báscula) mediante URIs y ejecuta la importación con el caso de uso
 * [ImportarDocumentosNaturvitiaCasoUso], exponiendo un estado inmutable con
 * el progreso y los mensajes de resultado.
 */
package com.gym.app.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.app.data.parser.ParserDocumentosNaturvitia
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.usecase.importacion.ImportarDocumentosNaturvitiaCasoUso
import com.gym.app.domain.usecase.importacion.ImportarRutinaNaturvitiaCasoUso
import com.gym.app.domain.usecase.importacion.ResultadoImportacionRutina
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @enum class TipoDocumentoNaturvitia
 * @brief Documentos PDF que admite la importación Naturvitia.
 */
enum class TipoDocumentoNaturvitia(val etiqueta: String) {
    /** Informe InBody / báscula con el peso y la composición corporal. */
    INBODY("Informe InBody (báscula)"),

    /** Dieta del nutricionista con los planes de comidas. */
    DIETA("Dieta (plan de comidas)"),

    /** Plan de entrenamiento del preparador. */
    ENTRENAMIENTO("Plan de entrenamiento")
}

/**
 * @data class EstadoImportacion
 * @brief Estado inmutable de la pantalla de importación.
 * @property uris Mapa con el URI seleccionado por [TipoDocumentoNaturvitia] (null si no se ha elegido).
 * @property importando Indica si la importación está en curso.
 * @property mensajeExito Mensaje temporal de éxito tras importar (null en reposo).
 * @property error Mensaje de error (null en reposo).
 * @property resultadoRutina Resumen del mapeo de la rutina de entrenamiento del PDF a la
 * maquinaria real del gimnasio (null si no se importó el plan o aún no terminó).
 * @property avisoRutina Aviso no bloqueante si la rutina no se pudo importar (p. ej. gimnasio
 * sin configurar). La importación del resto de documentos continúa con normalidad.
 */
data class EstadoImportacion(
    val uris: Map<TipoDocumentoNaturvitia, Uri> = emptyMap(),
    val importando: Boolean = false,
    val mensajeExito: String? = null,
    val error: String? = null,
    val resultadoRutina: ResultadoImportacionRutina? = null,
    val avisoRutina: String? = null
)

/**
 * @class ImportacionViewModel
 * @brief Gestiona el estado de la pantalla de importación de documentos.
 *
 * Permite asociar un URI a cada [TipoDocumentoNaturvitia], eliminar selecciones y
 * ejecutar la importación mediante [ImportarDocumentosNaturvitiaCasoUso], que
 * procesa los PDF con PDFBox y persiste los resultados en los repositorios
 * locales y de Supabase. Cuando el documento de entrenamiento está seleccionado,
 * además se extrae el texto del PDF con [ParserDocumentosNaturvitia] y se convierte
 * en rutinas diarias mediante [ImportarRutinaNaturvitiaCasoUso], resolviendo cada
 * ejercicio contra la maquinaria real del gimnasio.
 *
 * Constructores según el patrón del proyecto: primario inyectable y secundario
 * que resuelve las dependencias desde el [ContenedorDependencias].
 *
 * @param importarDocumentosCasoUso Caso de uso de importación de documentos.
 * @param importarRutinaCasoUso Caso de uso de conversión del plan de entrenamiento
 * del PDF en rutinas vinculadas a la maquinaria del gimnasio.
 */
class ImportacionViewModel(
    private val importarDocumentosCasoUso: ImportarDocumentosNaturvitiaCasoUso,
    private val importarRutinaCasoUso: ImportarRutinaNaturvitiaCasoUso
) : ViewModel() {

    /**
     * @brief Constructor secundario que resuelve las dependencias desde el
     * [ContenedorDependencias] (inyección manual).
     * @param contenedor Contenedor de dependencias de la aplicación.
     */
    constructor(contenedor: ContenedorDependencias) : this(
        importarDocumentosCasoUso = contenedor.importarDocumentosNaturvitiaCasoUso,
        importarRutinaCasoUso = contenedor.importarRutinaNaturvitiaCasoUso
    )

    private val _estado = MutableStateFlow(EstadoImportacion())
    val estado: StateFlow<EstadoImportacion> = _estado.asStateFlow()

    /**
     * @brief Asocia un URI a un tipo de documento seleccionado por el usuario.
     * @param tipo Tipo de documento al que pertenece el archivo.
     * @param uri URI del archivo PDF seleccionado (null si se canceló el selector).
     */
    fun seleccionarDocumento(tipo: TipoDocumentoNaturvitia, uri: Uri?) {
        if (uri == null) return
        _estado.update {
            it.copy(
                uris = it.uris + (tipo to uri),
                error = null,
                mensajeExito = null
            )
        }
    }

    /**
     * @brief Elimina la selección de un documento concreto.
     * @param tipo Tipo de documento cuya selección se elimina.
     */
    fun eliminarDocumento(tipo: TipoDocumentoNaturvitia) {
        _estado.update {
            it.copy(
                uris = it.uris - tipo,
                error = null,
                mensajeExito = null
            )
        }
    }

    /**
     * @brief Indica si hay al menos un documento seleccionado para importar.
     * @return `true` si la lista de URIs no está vacía.
     */
    fun hayDocumentosSeleccionados(): Boolean = _estado.value.uris.isNotEmpty()

    /**
     * @brief Ejecuta la importación de los documentos seleccionados.
     *
     * Procesa todos los PDF seleccionados con [ImportarDocumentosNaturvitiaCasoUso]
     * y actualiza el estado con el mensaje de éxito o el error producido. Si el plan
     * de entrenamiento está seleccionado y la importación de documentos tiene éxito,
     * se intenta además la conversión del PDF en rutinas diarias con
     * [importarRutinaCasoUso]:
     * - Si tiene éxito, el resumen del mapeo se expone en [EstadoImportacion.resultadoRutina]
     *   y se amplía el mensaje de éxito con el resumen.
     * - Si falla (p. ej. gimnasio sin configurar), la importación global no se rompe:
     *   se guarda un aviso en [EstadoImportacion.avisoRutina] y los documentos se
     *   consideran importados con normalidad.
     *
     * Al terminar correctamente se limpian las selecciones para permitir una nueva
     * importación sin reiniciar la pantalla.
     *
     * @param context Contexto de Android para resolver los URIs.
     */
    fun importar(context: Context) {
        val uris = _estado.value.uris
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _estado.update {
                it.copy(
                    importando = true,
                    error = null,
                    mensajeExito = null,
                    resultadoRutina = null,
                    avisoRutina = null
                )
            }
            val resultadoDocumentos = importarDocumentosCasoUso.ejecutar(
                context = context.applicationContext,
                uriInBody = uris[TipoDocumentoNaturvitia.INBODY],
                uriDieta = uris[TipoDocumentoNaturvitia.DIETA],
                uriEntrenamiento = uris[TipoDocumentoNaturvitia.ENTRENAMIENTO]
            )
            if (resultadoDocumentos.isSuccess) {
                val uriEntrenamiento = uris[TipoDocumentoNaturvitia.ENTRENAMIENTO]
                if (uriEntrenamiento != null) {
                    importarRutinaDesdePdf(
                        context = context.applicationContext,
                        uriEntrenamiento = uriEntrenamiento
                    )
                } else {
                    _estado.update {
                        it.copy(
                            importando = false,
                            uris = emptyMap(),
                            mensajeExito = MENSAJE_DOCUMENTOS_IMPORTADOS
                        )
                    }
                }
            } else {
                val excepcion = resultadoDocumentos.exceptionOrNull()
                _estado.update {
                    it.copy(
                        importando = false,
                        error = excepcion?.message
                            ?: "No se pudieron importar los documentos. Revisa que sean PDF válidos de Naturvitia."
                    )
                }
            }
        }
    }

    /**
     * @brief Importa la rutina de entrenamiento a partir del PDF del plan seleccionado.
     *
     * Extrae el texto del PDF con [ParserDocumentosNaturvitia] y lo entrega a
     * [importarRutinaCasoUso]. Si la extracción devuelve texto vacío (PDF ilegible)
     * se concluye la importación con el éxito de los documentos, sin tocar la rutina.
     *
     * @param context Contexto de aplicación para resolver el URI del PDF.
     * @param uriEntrenamiento URI del PDF del plan de entrenamiento Naturvitia.
     */
    private suspend fun importarRutinaDesdePdf(context: Context, uriEntrenamiento: Uri) {
        val textoEntrenamiento = ParserDocumentosNaturvitia.extraerTextoPdf(context, uriEntrenamiento)
        if (textoEntrenamiento.isBlank()) {
            _estado.update {
                it.copy(
                    importando = false,
                    uris = emptyMap(),
                    mensajeExito = MENSAJE_DOCUMENTOS_IMPORTADOS
                )
            }
            return
        }
        val resultadoRutina = importarRutinaCasoUso.ejecutar(textoEntrenamiento)
        if (resultadoRutina.isSuccess) {
            val resumen = resultadoRutina.getOrNull() ?: return
            _estado.update {
                it.copy(
                    importando = false,
                    uris = emptyMap(),
                    mensajeExito = construirMensajeExito(resumen),
                    resultadoRutina = resumen
                )
            }
        } else {
            _estado.update {
                it.copy(
                    importando = false,
                    uris = emptyMap(),
                    mensajeExito = MENSAJE_DOCUMENTOS_IMPORTADOS,
                    avisoRutina = resultadoRutina.exceptionOrNull()?.message
                        ?: MENSAJE_ERROR_RUTINA_GENERICO
                )
            }
        }
    }

    /**
     * @brief Compone el mensaje de éxito completo cuando la rutina se importa con éxito.
     * @param resultado Resumen de la importación de la rutina (días y ejercicios mapeados).
     * @return Mensaje legible con el resumen del mapeo y, si procede, los pendientes.
     */
    private fun construirMensajeExito(resultado: ResultadoImportacionRutina): String {
        val resumen = "Rutina importada: ${resultado.rutinasCreadas.size} días, " +
            "${resultado.ejerciciosMapeados} ejercicios mapeados a tu gimnasio."
        val pendientes = resultado.ejerciciosSinMapear.size
        return if (pendientes > 0) {
            "$MENSAJE_DOCUMENTOS_IMPORTADOS $resumen $pendientes ejercicios pendientes de revisar."
        } else {
            "$MENSAJE_DOCUMENTOS_IMPORTADOS $resumen"
        }
    }

    /**
     * @brief Descarta los mensajes temporales (éxito/error/aviso) del estado y el
     * resultado del mapeo de la rutina.
     */
    fun limpiarMensajes() {
        _estado.update {
            it.copy(
                mensajeExito = null,
                error = null,
                resultadoRutina = null,
                avisoRutina = null
            )
        }
    }

    companion object {
        /** Mensaje base de éxito tras importar los documentos. */
        private const val MENSAJE_DOCUMENTOS_IMPORTADOS: String =
            "Documentos importados correctamente. Ya puedes consultar tu plan del día."

        /** Mensaje genérico cuando la rutina no se puede importar sin más detalles. */
        private const val MENSAJE_ERROR_RUTINA_GENERICO: String =
            "No se pudo importar la rutina de entrenamiento."
    }
}