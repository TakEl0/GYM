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
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.usecase.importacion.ImportarDocumentosNaturvitiaCasoUso
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
 */
data class EstadoImportacion(
    val uris: Map<TipoDocumentoNaturvitia, Uri> = emptyMap(),
    val importando: Boolean = false,
    val mensajeExito: String? = null,
    val error: String? = null
)

/**
 * @class ImportacionViewModel
 * @brief Gestiona el estado de la pantalla de importación de documentos.
 *
 * Permite asociar un URI a cada [TipoDocumentoNaturvitia], eliminar selecciones y
 * ejecutar la importación mediante [ImportarDocumentosNaturvitiaCasoUso], que
 * procesa los PDF con PDFBox y persiste los resultados en los repositorios
 * locales y de Supabase.
 *
 * Constructores según el patrón del proyecto: primario inyectable y secundario
 * que resuelve las dependencias desde el [ContenedorDependencias].
 *
 * @param importarDocumentosCasoUso Caso de uso de importación de documentos.
 */
class ImportacionViewModel(
    private val importarDocumentosCasoUso: ImportarDocumentosNaturvitiaCasoUso
) : ViewModel() {

    /**
     * @brief Constructor secundario que resuelve las dependencias desde el
     * [ContenedorDependencias] (inyección manual).
     * @param contenedor Contenedor de dependencias de la aplicación.
     */
    constructor(contenedor: ContenedorDependencias) : this(
        importarDocumentosCasoUso = contenedor.importarDocumentosNaturvitiaCasoUso
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
     * y actualiza el estado con el mensaje de éxito o el error producido. Al
     * terminar correctamente se limpian las selecciones para permitir una nueva
     * importación sin reiniciar la pantalla.
     *
     * @param context Contexto de Android para resolver los URIs.
     */
    fun importar(context: Context) {
        val uris = _estado.value.uris
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _estado.update { it.copy(importando = true, error = null, mensajeExito = null) }
            importarDocumentosCasoUso.ejecutar(
                context = context.applicationContext,
                uriInBody = uris[TipoDocumentoNaturvitia.INBODY],
                uriDieta = uris[TipoDocumentoNaturvitia.DIETA],
                uriEntrenamiento = uris[TipoDocumentoNaturvitia.ENTRENAMIENTO]
            )
                .onSuccess {
                    _estado.update {
                        it.copy(
                            importando = false,
                            uris = emptyMap(),
                            mensajeExito = "Documentos importados correctamente. Ya puedes consultar tu plan del día."
                        )
                    }
                }
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(
                            importando = false,
                            error = excepcion.message ?: "No se pudieron importar los documentos. Revisa que sean PDF válidos de Naturvitia."
                        )
                    }
                }
        }
    }

    /**
     * @brief Descarta los mensajes temporales (éxito/error) del estado.
     */
    fun limpiarMensajes() {
        _estado.update { it.copy(mensajeExito = null, error = null) }
    }
}