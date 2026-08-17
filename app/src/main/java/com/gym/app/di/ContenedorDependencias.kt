/**
 * @file ContenedorDependencias.kt
 * @brief Contenedor manual de dependencias (manual DI) para la aplicación GYM.
 * Centraliza la creación de repositorios y casos de uso, resolviendo la
 * implementación concreta (Supabase real o Fake de desarrollo) según si las
 * credenciales de Supabase están configuradas.
 */
package com.gym.app.di

import android.content.Context
import com.gym.app.data.remote.ClienteSupabase
import com.gym.app.data.repository.RepositorioAutenticacionFake
import com.gym.app.data.repository.RepositorioAutenticacionSupabase
import com.gym.app.data.repository.RepositorioComidaFake
import com.gym.app.data.repository.RepositorioComidaSupabase
import com.gym.app.data.repository.RepositorioEjercicioFake
import com.gym.app.data.repository.RepositorioEjercicioRoom
import com.gym.app.data.repository.RepositorioEntrenamientoFake
import com.gym.app.data.repository.RepositorioEntrenamientoSupabase
import com.gym.app.data.repository.RepositorioGimnasioFake
import com.gym.app.data.repository.RepositorioGimnasioRoom
import com.gym.app.data.repository.RepositorioIngestaFake
import com.gym.app.data.repository.RepositorioIngestaRoom
import com.gym.app.data.repository.RepositorioListaCompraFake
import com.gym.app.data.repository.RepositorioListaCompraRoom
import com.gym.app.data.repository.RepositorioPerfilFake
import com.gym.app.data.repository.RepositorioPerfilRoom
import com.gym.app.data.repository.RepositorioPesoFake
import com.gym.app.data.repository.RepositorioPesoSupabase
import com.gym.app.data.repository.RepositorioPlanComidaFake
import com.gym.app.data.repository.RepositorioPlanComidaRoom
import com.gym.app.data.repository.RepositorioRutinaFake
import com.gym.app.data.repository.RepositorioRutinaRoom
import com.gym.app.data.repository.RepositorioSesionEntrenamientoFake
import com.gym.app.data.repository.RepositorioSesionEntrenamientoRoom
import com.gym.app.domain.repository.RepositorioAutenticacion
import com.gym.app.domain.repository.RepositorioComida
import com.gym.app.domain.repository.RepositorioEjercicio
import com.gym.app.domain.repository.RepositorioEntrenamiento
import com.gym.app.domain.repository.RepositorioGimnasio
import com.gym.app.domain.repository.RepositorioIngesta
import com.gym.app.domain.repository.RepositorioListaCompra
import com.gym.app.domain.repository.RepositorioPerfil
import com.gym.app.domain.repository.RepositorioPeso
import com.gym.app.domain.repository.RepositorioPlanComida
import com.gym.app.domain.repository.RepositorioRutina
import com.gym.app.domain.repository.RepositorioSesionEntrenamiento
import com.gym.app.domain.usecase.autenticacion.CerrarSesionCasoUso
import com.gym.app.domain.usecase.autenticacion.IniciarSesionCasoUso
import com.gym.app.domain.usecase.autenticacion.ObservarEstadoSesionCasoUso
import com.gym.app.domain.usecase.autenticacion.ObtenerSesionActualCasoUso
import com.gym.app.domain.usecase.autenticacion.RegistrarUsuarioCasoUso
import com.gym.app.domain.usecase.autenticacion.SincronizarPerfilCasoUso
import com.gym.app.domain.usecase.compra.GenerarListaCompraSemanalCasoUso
import com.gym.app.domain.usecase.compra.MarcarItemCompradoCasoUso
import com.gym.app.domain.usecase.entrenamiento.CalcularUnRMCasoUso
import com.gym.app.domain.usecase.entrenamiento.ConstruirRutinaCasoUso
import com.gym.app.domain.usecase.entrenamiento.ObservarEntrenamientosCalendarioCasoUso
import com.gym.app.domain.usecase.entrenamiento.ObservarEntrenamientosCasoUso
import com.gym.app.domain.usecase.entrenamiento.ObservarSesionesEntrenamientoCasoUso
import com.gym.app.domain.usecase.entrenamiento.RegistrarProgresoEntrenamientoCasoUso
import com.gym.app.domain.usecase.entrenamiento.RegistrarSesionEntrenamientoCasoUso
import com.gym.app.domain.usecase.entrenamiento.SincronizarNutricionEntrenamientoCasoUso
import com.gym.app.domain.usecase.gimnasio.AlternativasMaquinaCasoUso
import com.gym.app.domain.usecase.gimnasio.GuardarGimnasioCasoUso
import com.gym.app.domain.usecase.gimnasio.RegistrarMaquinaCasoUso
import com.gym.app.domain.usecase.importacion.ImportarDocumentosNaturvitiaCasoUso
import com.gym.app.domain.usecase.nutricion.CalcularObjetivosNutricionalesCasoUso
import com.gym.app.domain.usecase.nutricion.CalcularResumenNutricionalAvanzadoCasoUso
import com.gym.app.domain.usecase.nutricion.CalcularResumenNutricionalCasoUso
import com.gym.app.domain.usecase.nutricion.GenerarPlanComidasCasoUso
import com.gym.app.domain.usecase.nutricion.ObservarComidasDelDiaCasoUso
import com.gym.app.domain.usecase.nutricion.RebalancearComidasPendientesCasoUso
import com.gym.app.domain.usecase.nutricion.RegistrarComidaCasoUso
import com.gym.app.domain.usecase.nutricion.RegistrarIngestaCasoUso
import com.gym.app.domain.usecase.perfil.ActualizarObjetivosPerfilCasoUso
import com.gym.app.domain.usecase.perfil.GuardarPerfilCasoUso
import com.gym.app.domain.usecase.perfil.ObtenerPerfilCasoUso
import com.gym.app.domain.usecase.perfil.SincronizarPerfilConBackendCasoUso
import com.gym.app.domain.usecase.peso.ObservarPesosCasoUso
import com.gym.app.domain.usecase.peso.ObtenerUltimoPesoCasoUso
import com.gym.app.domain.usecase.peso.RegistrarPesoCasoUso

/**
 * @class ContenedorDependencias
 * @brief Construye y expone todas las dependencias de la aplicación mediante
 * inyección manual, siguiendo los principios de Clean Architecture.
 *
 * Si [ClienteSupabase.estaConfigurado] es verdadero se utilizan los repositorios
 * reales (Room + Supabase); en caso contrario se emplean los Fakes de memoria
 * para permitir el desarrollo y la previsualización sin backend.
 *
 * @param context Contexto de aplicación usado por los repositorios reales.
 */
class ContenedorDependencias(private val context: Context) {

    // ---------------------------------------------------------------------
    // Repositorios
    // ---------------------------------------------------------------------

    /**
     * Repositorio de autenticación (privado: la capa de presentación solo
     * consume los casos de uso de autenticación expuestos más abajo).
     */
    private val repositorioAutenticacion: RepositorioAutenticacion by lazy {
        if (ClienteSupabase.estaConfigurado) {
            RepositorioAutenticacionSupabase(context)
        } else {
            RepositorioAutenticacionFake()
        }
    }

    /**
     * Repositorio de peso corporal. Se expone públicamente para que los
     * ViewModels de presentación puedan inyectarlo mediante constructor
     * secundario cuando se dispone del contenedor.
     */
    val repositorioPeso: RepositorioPeso by lazy {
        if (ClienteSupabase.estaConfigurado) {
            RepositorioPesoSupabase(context)
        } else {
            RepositorioPesoFake()
        }
    }

    /**
     * Repositorio de entrenamientos. Se expone públicamente para que los
     * ViewModels de presentación puedan inyectarlo mediante constructor
     * secundario (p. ej. DashboardViewModel) cuando se dispone del contenedor.
     */
    val repositorioEntrenamiento: RepositorioEntrenamiento by lazy {
        if (ClienteSupabase.estaConfigurado) {
            RepositorioEntrenamientoSupabase(context)
        } else {
            RepositorioEntrenamientoFake()
        }
    }

    private val repositorioComida: RepositorioComida by lazy {
        if (ClienteSupabase.estaConfigurado) {
            RepositorioComidaSupabase(context)
        } else {
            RepositorioComidaFake()
        }
    }

    /** Repositorio del perfil y objetivos nutricionales del usuario. */
    val repositorioPerfil: RepositorioPerfil by lazy {
        if (ClienteSupabase.estaConfigurado) {
            RepositorioPerfilRoom(context)
        } else {
            RepositorioPerfilFake()
        }
    }

    /** Repositorio de planes de comidas diarios. */
    val repositorioPlanComida: RepositorioPlanComida by lazy {
        if (ClienteSupabase.estaConfigurado) {
            RepositorioPlanComidaRoom(context)
        } else {
            RepositorioPlanComidaFake()
        }
    }

    /** Repositorio de ingestas reales consumidas. */
    val repositorioIngesta: RepositorioIngesta by lazy {
        if (ClienteSupabase.estaConfigurado) {
            RepositorioIngestaRoom(context)
        } else {
            RepositorioIngestaFake()
        }
    }

    /** Repositorio de listas de la compra semanales. */
    val repositorioListaCompra: RepositorioListaCompra by lazy {
        if (ClienteSupabase.estaConfigurado) {
            RepositorioListaCompraRoom(context)
        } else {
            RepositorioListaCompraFake()
        }
    }

    /** Repositorio del gimnasio y su maquinaria. */
    val repositorioGimnasio: RepositorioGimnasio by lazy {
        if (ClienteSupabase.estaConfigurado) {
            RepositorioGimnasioRoom(context)
        } else {
            RepositorioGimnasioFake()
        }
    }

    /** Repositorio del catálogo de ejercicios. */
    val repositorioEjercicio: RepositorioEjercicio by lazy {
        if (ClienteSupabase.estaConfigurado) {
            RepositorioEjercicioRoom(context)
        } else {
            RepositorioEjercicioFake()
        }
    }

    /** Repositorio de rutinas de entrenamiento. */
    val repositorioRutina: RepositorioRutina by lazy {
        if (ClienteSupabase.estaConfigurado) {
            RepositorioRutinaRoom(context)
        } else {
            RepositorioRutinaFake()
        }
    }

    /** Repositorio de sesiones de entrenamiento realizadas. */
    val repositorioSesionEntrenamiento: RepositorioSesionEntrenamiento by lazy {
        if (ClienteSupabase.estaConfigurado) {
            RepositorioSesionEntrenamientoRoom(context)
        } else {
            RepositorioSesionEntrenamientoFake()
        }
    }

    // ---------------------------------------------------------------------
    // Casos de uso de autenticación
    // ---------------------------------------------------------------------

    val iniciarSesionCasoUso: IniciarSesionCasoUso by lazy {
        IniciarSesionCasoUso(repositorioAutenticacion)
    }

    val registrarUsuarioCasoUso: RegistrarUsuarioCasoUso by lazy {
        RegistrarUsuarioCasoUso(repositorioAutenticacion)
    }

    val cerrarSesionCasoUso: CerrarSesionCasoUso by lazy {
        CerrarSesionCasoUso(repositorioAutenticacion)
    }

    val observarEstadoSesionCasoUso: ObservarEstadoSesionCasoUso by lazy {
        ObservarEstadoSesionCasoUso(repositorioAutenticacion)
    }

    val obtenerSesionActualCasoUso: ObtenerSesionActualCasoUso by lazy {
        ObtenerSesionActualCasoUso(repositorioAutenticacion)
    }

    val sincronizarPerfilCasoUso: SincronizarPerfilCasoUso by lazy {
        SincronizarPerfilCasoUso(repositorioAutenticacion)
    }

    // ---------------------------------------------------------------------
    // Casos de uso de perfil
    // ---------------------------------------------------------------------

    val obtenerPerfilCasoUso: ObtenerPerfilCasoUso by lazy {
        ObtenerPerfilCasoUso(repositorioPerfil)
    }

    val actualizarObjetivosPerfilCasoUso: ActualizarObjetivosPerfilCasoUso by lazy {
        ActualizarObjetivosPerfilCasoUso(repositorioPerfil)
    }

    val guardarPerfilCasoUso: GuardarPerfilCasoUso by lazy {
        GuardarPerfilCasoUso(repositorioPerfil)
    }

    val sincronizarPerfilConBackendCasoUso: SincronizarPerfilConBackendCasoUso by lazy {
        SincronizarPerfilConBackendCasoUso(repositorioAutenticacion, repositorioPerfil)
    }

    // ---------------------------------------------------------------------
    // Casos de uso de peso
    // ---------------------------------------------------------------------

    val registrarPesoCasoUso: RegistrarPesoCasoUso by lazy {
        RegistrarPesoCasoUso(repositorioPeso)
    }

    val observarPesosCasoUso: ObservarPesosCasoUso by lazy {
        ObservarPesosCasoUso(repositorioPeso)
    }

    val obtenerUltimoPesoCasoUso: ObtenerUltimoPesoCasoUso by lazy {
        ObtenerUltimoPesoCasoUso(repositorioPeso)
    }

    // ---------------------------------------------------------------------
    // Casos de uso de entrenamiento
    // ---------------------------------------------------------------------

    val observarEntrenamientosCasoUso: ObservarEntrenamientosCasoUso by lazy {
        ObservarEntrenamientosCasoUso(repositorioEntrenamiento)
    }

    val observarEntrenamientosCalendarioCasoUso: ObservarEntrenamientosCalendarioCasoUso by lazy {
        ObservarEntrenamientosCalendarioCasoUso(repositorioEntrenamiento)
    }

    val registrarProgresoEntrenamientoCasoUso: RegistrarProgresoEntrenamientoCasoUso by lazy {
        RegistrarProgresoEntrenamientoCasoUso(repositorioEntrenamiento)
    }

    val registrarSesionEntrenamientoCasoUso: RegistrarSesionEntrenamientoCasoUso by lazy {
        RegistrarSesionEntrenamientoCasoUso(repositorioSesionEntrenamiento)
    }

    val observarSesionesEntrenamientoCasoUso: ObservarSesionesEntrenamientoCasoUso by lazy {
        ObservarSesionesEntrenamientoCasoUso(repositorioSesionEntrenamiento)
    }

    val alternativasMaquinaCasoUso: AlternativasMaquinaCasoUso by lazy {
        AlternativasMaquinaCasoUso(repositorioGimnasio)
    }

    val construirRutinaCasoUso: ConstruirRutinaCasoUso by lazy {
        ConstruirRutinaCasoUso(alternativasMaquinaCasoUso)
    }

    val calcularUnRMCasoUso: CalcularUnRMCasoUso by lazy {
        CalcularUnRMCasoUso()
    }

    val sincronizarNutricionEntrenamientoCasoUso: SincronizarNutricionEntrenamientoCasoUso by lazy {
        SincronizarNutricionEntrenamientoCasoUso()
    }

    // ---------------------------------------------------------------------
    // Casos de uso de gimnasio
    // ---------------------------------------------------------------------

    val guardarGimnasioCasoUso: GuardarGimnasioCasoUso by lazy {
        GuardarGimnasioCasoUso(repositorioGimnasio)
    }

    val registrarMaquinaCasoUso: RegistrarMaquinaCasoUso by lazy {
        RegistrarMaquinaCasoUso(repositorioGimnasio)
    }

    // ---------------------------------------------------------------------
    // Casos de uso de nutrición
    // ---------------------------------------------------------------------

    val observarComidasDelDiaCasoUso: ObservarComidasDelDiaCasoUso by lazy {
        ObservarComidasDelDiaCasoUso(repositorioComida)
    }

    val registrarComidaCasoUso: RegistrarComidaCasoUso by lazy {
        RegistrarComidaCasoUso(repositorioComida)
    }

    val registrarIngestaCasoUso: RegistrarIngestaCasoUso by lazy {
        RegistrarIngestaCasoUso(repositorioIngesta)
    }

    val calcularResumenNutricionalCasoUso: CalcularResumenNutricionalCasoUso by lazy {
        CalcularResumenNutricionalCasoUso()
    }

    val calcularResumenNutricionalAvanzadoCasoUso: CalcularResumenNutricionalAvanzadoCasoUso by lazy {
        CalcularResumenNutricionalAvanzadoCasoUso()
    }

    val calcularObjetivosNutricionalesCasoUso: CalcularObjetivosNutricionalesCasoUso by lazy {
        CalcularObjetivosNutricionalesCasoUso()
    }

    val generarPlanComidasCasoUso: GenerarPlanComidasCasoUso by lazy {
        GenerarPlanComidasCasoUso()
    }

    val rebalancearComidasPendientesCasoUso: RebalancearComidasPendientesCasoUso by lazy {
        RebalancearComidasPendientesCasoUso()
    }

    val importarDocumentosNaturvitiaCasoUso: ImportarDocumentosNaturvitiaCasoUso by lazy {
        ImportarDocumentosNaturvitiaCasoUso(
            repositorioPeso = repositorioPeso,
            repositorioComida = repositorioComida,
            repositorioEntrenamiento = repositorioEntrenamiento
        )
    }

    // ---------------------------------------------------------------------
    // Casos de uso de lista de la compra
    // ---------------------------------------------------------------------

    val generarListaCompraSemanalCasoUso: GenerarListaCompraSemanalCasoUso by lazy {
        GenerarListaCompraSemanalCasoUso()
    }

    val marcarItemCompradoCasoUso: MarcarItemCompradoCasoUso by lazy {
        MarcarItemCompradoCasoUso(repositorioListaCompra)
    }
}