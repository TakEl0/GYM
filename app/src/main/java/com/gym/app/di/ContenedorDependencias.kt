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
import com.gym.app.data.repository.RepositorioEntrenamientoFake
import com.gym.app.data.repository.RepositorioEntrenamientoSupabase
import com.gym.app.data.repository.RepositorioPesoFake
import com.gym.app.data.repository.RepositorioPesoSupabase
import com.gym.app.domain.repository.RepositorioAutenticacion
import com.gym.app.domain.repository.RepositorioComida
import com.gym.app.domain.repository.RepositorioEntrenamiento
import com.gym.app.domain.repository.RepositorioPeso
import com.gym.app.domain.usecase.autenticacion.CerrarSesionCasoUso
import com.gym.app.domain.usecase.autenticacion.IniciarSesionCasoUso
import com.gym.app.domain.usecase.autenticacion.ObservarEstadoSesionCasoUso
import com.gym.app.domain.usecase.autenticacion.ObtenerSesionActualCasoUso
import com.gym.app.domain.usecase.autenticacion.RegistrarUsuarioCasoUso
import com.gym.app.domain.usecase.autenticacion.SincronizarPerfilCasoUso
import com.gym.app.domain.usecase.entrenamiento.ObservarEntrenamientosCalendarioCasoUso
import com.gym.app.domain.usecase.entrenamiento.ObservarEntrenamientosCasoUso
import com.gym.app.domain.usecase.entrenamiento.RegistrarProgresoEntrenamientoCasoUso
import com.gym.app.domain.usecase.nutricion.CalcularResumenNutricionalCasoUso
import com.gym.app.domain.usecase.nutricion.ObservarComidasDelDiaCasoUso
import com.gym.app.domain.usecase.nutricion.RegistrarComidaCasoUso
import com.gym.app.domain.usecase.importacion.ImportarDocumentosNaturvitiaCasoUso
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
     * secundario (p. ej. RegistroPesoViewModel) cuando se dispone del contenedor.
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

    // ---------------------------------------------------------------------
    // Casos de uso de nutrición
    // ---------------------------------------------------------------------

    val observarComidasDelDiaCasoUso: ObservarComidasDelDiaCasoUso by lazy {
        ObservarComidasDelDiaCasoUso(repositorioComida)
    }

    val registrarComidaCasoUso: RegistrarComidaCasoUso by lazy {
        RegistrarComidaCasoUso(repositorioComida)
    }

    val calcularResumenNutricionalCasoUso: CalcularResumenNutricionalCasoUso by lazy {
        CalcularResumenNutricionalCasoUso()
    }

    val importarDocumentosNaturvitiaCasoUso: ImportarDocumentosNaturvitiaCasoUso by lazy {
        ImportarDocumentosNaturvitiaCasoUso(
            repositorioPeso = repositorioPeso,
            repositorioComida = repositorioComida,
            repositorioEntrenamiento = repositorioEntrenamiento
        )
    }
}