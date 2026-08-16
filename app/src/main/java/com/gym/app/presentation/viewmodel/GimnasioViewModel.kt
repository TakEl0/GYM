/**
 * @file GimnasioViewModel.kt
 * @brief ViewModel de la pantalla de Gimnasio de la aplicación GYM.
 * Observa el gimnasio del usuario y el catálogo de ejercicios, permite guardar el
 * gimnasio, registrar nuevas máquinas y consultar alternativas cuando una máquina
 * no está disponible.
 */
package com.gym.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.app.data.repository.RepositorioEjercicioFake
import com.gym.app.data.repository.RepositorioGimnasioFake
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.model.Gimnasio
import com.gym.app.domain.model.Maquina
import com.gym.app.domain.repository.RepositorioEjercicio
import com.gym.app.domain.repository.RepositorioGimnasio
import com.gym.app.domain.usecase.gimnasio.AlternativasMaquinaCasoUso
import com.gym.app.domain.usecase.gimnasio.GuardarGimnasioCasoUso
import com.gym.app.domain.usecase.gimnasio.RegistrarMaquinaCasoUso
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @data class EstadoGimnasio
 * @brief Estado inmutable de la pantalla de Gimnasio.
 * @property gimnasio Gimnasio configurado por el usuario (null si aún no existe).
 * @property ejercicios Catálogo de ejercicios disponibles.
 * @property alternativas Ejercicios alternativos sugeridos para la máquina
 * seleccionada (lista vacía si aún no se ha consultado).
 * @property consultandoAlternativas Indica si la consulta de alternativas está en curso.
 * @property cargando Indica si los datos están en proceso de carga.
 * @property error Mensaje de error si alguna operación falló (null en caso normal).
 */
data class EstadoGimnasio(
    val gimnasio: Gimnasio? = null,
    val ejercicios: List<Ejercicio> = emptyList(),
    val alternativas: List<Ejercicio> = emptyList(),
    val consultandoAlternativas: Boolean = false,
    val cargando: Boolean = true,
    val error: String? = null
)

/**
 * @class GimnasioViewModel
 * @brief Gestiona el estado de la pantalla de Gimnasio.
 *
 * Observa en paralelo el [Gimnasio] del usuario y el catálogo de [Ejercicio].
 * Las operaciones de guardado del gimnasio, registro de máquinas y consulta de
 * alternativas delegan en sus respectivos casos de uso, que se construyen
 * internamente sobre el repositorio de gimnasio inyectado para mantener la
 * coherencia de la fuente de datos.
 *
 * Constructores según el patrón del proyecto: primario inyectable (fakes por
 * defecto para pruebas) y secundario que resuelve las dependencias desde el
 * [ContenedorDependencias].
 */
class GimnasioViewModel(
    private val repositorioGimnasio: RepositorioGimnasio = RepositorioGimnasioFake(),
    private val repositorioEjercicio: RepositorioEjercicio = RepositorioEjercicioFake()
) : ViewModel() {

    /** Caso de uso de guardado del gimnasio, sobre el repositorio inyectado. */
    private val guardarGimnasioCasoUso = GuardarGimnasioCasoUso(repositorioGimnasio)

    /** Caso de uso de registro de máquinas, sobre el repositorio inyectado. */
    private val registrarMaquinaCasoUso = RegistrarMaquinaCasoUso(repositorioGimnasio)

    /** Caso de uso de alternativas de máquina, sobre el repositorio inyectado. */
    private val alternativasMaquinaCasoUso = AlternativasMaquinaCasoUso(repositorioGimnasio)

    /**
     * @brief Constructor secundario que resuelve las dependencias reales desde
     * el [ContenedorDependencias] (inyección manual).
     * @param contenedor Contenedor de dependencias de la aplicación.
     */
    constructor(contenedor: ContenedorDependencias) : this(
        repositorioGimnasio = contenedor.repositorioGimnasio,
        repositorioEjercicio = contenedor.repositorioEjercicio
    )

    private val _estado = MutableStateFlow(EstadoGimnasio())
    val estado: StateFlow<EstadoGimnasio> = _estado.asStateFlow()

    init {
        iniciarObservacion()
    }

    /**
     * @brief Inicia la observación reactiva del gimnasio y del catálogo de ejercicios.
     * Cada emisión de cualquiera de los dos flujos actualiza el estado combinado.
     */
    fun iniciarObservacion() {
        viewModelScope.launch {
            repositorioGimnasio.observarGimnasio()
                .combine(repositorioEjercicio.observarEjercicios()) { gimnasio, ejercicios ->
                    gimnasio to ejercicios
                }
                .collect { (gimnasio, ejercicios) ->
                    _estado.update {
                        it.copy(
                            gimnasio = gimnasio,
                            ejercicios = ejercicios,
                            cargando = false
                        )
                    }
                }
        }
    }

    /**
     * @brief Guarda o actualiza el gimnasio con el nombre y la dirección indicados.
     * Si ya existe un gimnasio configurado se conserva su parque de máquinas.
     * @param nombre Nombre comercial del gimnasio (no puede estar vacío).
     * @param direccion Dirección física del gimnasio (puede ser null).
     */
    fun guardarGimnasio(nombre: String, direccion: String?) {
        viewModelScope.launch {
            val gimnasioActual = _estado.value.gimnasio
            val nuevoGimnasio = Gimnasio(
                id = gimnasioActual?.id ?: UUID.randomUUID().toString(),
                nombre = nombre.trim(),
                direccion = direccion?.trim(),
                maquinas = gimnasioActual?.maquinas ?: emptyList()
            )
            guardarGimnasioCasoUso.ejecutar(nuevoGimnasio)
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(error = excepcion.message ?: "No se pudo guardar el gimnasio.")
                    }
                }
        }
    }

    /**
     * @brief Registra una nueva máquina en el gimnasio.
     * Si el gimnasio aún no está configurado, se crea uno por defecto con el nombre
     * genérico "Mi gimnasio" (marcador de configuración pendiente) que el usuario
     * puede renombrar después desde el formulario de guardado.
     * @param nombre Nombre de la máquina (no puede estar vacío).
     * @param gruposMusculares Grupos musculares que trabaja la máquina.
     */
    fun registrarMaquina(nombre: String, gruposMusculares: List<String>) {
        viewModelScope.launch {
            val gimnasioActual = _estado.value.gimnasio
                ?: Gimnasio(
                    id = UUID.randomUUID().toString(),
                    nombre = "Mi gimnasio",
                    maquinas = emptyList()
                )
            val maquina = Maquina(
                id = UUID.randomUUID().toString(),
                nombre = nombre.trim(),
                grupoMuscular = gruposMusculares.map { it.trim() }.filter { it.isNotEmpty() },
                disponible = true
            )
            registrarMaquinaCasoUso.ejecutar(gimnasioActual, maquina)
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(error = excepcion.message ?: "No se pudo registrar la máquina.")
                    }
                }
        }
    }

    /**
     * @brief Consulta los ejercicios alternativos para una máquina concreta.
     * @param maquinaId Identificador de la máquina a sustituir.
     */
    fun consultarAlternativas(maquinaId: String) {
        viewModelScope.launch {
            _estado.update { it.copy(consultandoAlternativas = true, error = null) }
            alternativasMaquinaCasoUso.ejecutar(maquinaId, _estado.value.ejercicios)
                .onSuccess { alternativas ->
                    _estado.update {
                        it.copy(alternativas = alternativas, consultandoAlternativas = false)
                    }
                }
                .onFailure { excepcion ->
                    _estado.update {
                        it.copy(
                            alternativas = emptyList(),
                            consultandoAlternativas = false,
                            error = excepcion.message ?: "No se pudieron consultar las alternativas."
                        )
                    }
                }
        }
    }

    /**
     * @brief Cierra el panel de alternativas de la máquina.
     */
    fun cerrarAlternativas() {
        _estado.update { it.copy(alternativas = emptyList()) }
    }

    /**
     * @brief Descarta el mensaje de error mostrado en la pantalla.
     */
    fun limpiarError() {
        _estado.update { it.copy(error = null) }
    }
}