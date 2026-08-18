/**
 * @file PrepararSesionActivaCasoUso.kt
 * @brief Caso de uso que prepara los ejercicios de una sesión de entrenamiento en vivo.
 *
 * A partir de la rutina elegida por el usuario, resuelve cada bloque a su
 * [EjercicioConMaquina]: ejercicio del catálogo (o provisional si no existe) y
 * máquina real del gimnasio (marca · modelo) sobre la que se ejecutará.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.BloqueRutina
import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.model.EjercicioConMaquina
import com.gym.app.domain.model.Maquina
import com.gym.app.domain.model.Rutina
import com.gym.app.domain.repository.RepositorioEjercicio
import com.gym.app.domain.repository.RepositorioGimnasio
import com.gym.app.domain.usecase.gimnasio.MotorMapeoEjercicioAMaquina
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * @data class ResultadoPreparacionSesion
 * @brief Resultado de la preparación de una sesión activa.
 *
 * @property ejercicios Ejercicios de la sesión resueltos en el orden de los
 * bloques de la rutina, cada uno con su máquina real (si se pudo resolver).
 * @property seriesTotales Número total de series planificadas
 * (Σ series de todos los bloques de la rutina).
 */
data class ResultadoPreparacionSesion(
    val ejercicios: List<EjercicioConMaquina>,
    val seriesTotales: Int
)

/**
 * @class PrepararSesionActivaCasoUso
 * @brief Construye la lista de [EjercicioConMaquina] que mostrará la sesión en vivo.
 *
 * # Flujo de preparación
 *
 * 1. **Catálogo de ejercicios**: se obtiene con [RepositorioEjercicio.observarEjercicios].
 * 2. **Gimnasio**: se obtiene con [RepositorioGimnasio.observarGimnasio]; si el
 *    gimnasio aún no está configurado, se trata como un parque de máquinas vacío.
 * 3. **Por cada bloque** de la rutina, en su orden:
 *    - Se busca el [Ejercicio] por `bloque.ejercicioId` en el catálogo. Si no
 *      está, se crea un ejercicio **provisional** con identificador y nombre
 *      tomados de `bloque.ejercicioId` y grupo muscular vacío (la preparación
 *      nunca falla por un ejercicio sin catalogar).
 *    - Se resuelve la máquina real: si el ejercicio tiene `maquinaId` explícito
 *      se busca en el parque del gimnasio; si no, se delega en
 *      [MotorMapeoEjercicioAMaquina.resolver] con el nombre del ejercicio.
 * 4. El resultado conserva el **orden de los bloques** de la rutina y el total
 *    de series planificadas para la barra de progreso de la sesión.
 *
 * @property repositorioEjercicio Puerto del catálogo de ejercicios.
 * @property repositorioGimnasio Puerto del gimnasio y su maquinaria.
 * @property dispatcher Dispatcher sobre el que se ejecuta la preparación (por defecto IO).
 */
class PrepararSesionActivaCasoUso(
    private val repositorioEjercicio: RepositorioEjercicio,
    private val repositorioGimnasio: RepositorioGimnasio,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Prepara los ejercicios de la sesión activa a partir de la rutina elegida.
     * @param rutina Rutina que se va a ejecutar en la sesión en vivo.
     * @return [Result] con [ResultadoPreparacionSesion] (ejercicios resueltos y
     * total de series planificadas), o el error de lectura de repositorios.
     */
    suspend fun ejecutar(rutina: Rutina): Result<ResultadoPreparacionSesion> =
        withContext(dispatcher) {
            try {
                val ejerciciosCatalogo = repositorioEjercicio.observarEjercicios().first()
                val gimnasio = repositorioGimnasio.observarGimnasio().first()
                val maquinas = gimnasio?.maquinas.orEmpty()

                val ejerciciosPorId = ejerciciosCatalogo.associateBy { it.id }

                val ejercicios = rutina.bloques.map { bloque ->
                    val ejercicio = ejerciciosPorId[bloque.ejercicioId]
                        ?: crearEjercicioProvisional(bloque)
                    EjercicioConMaquina(
                        bloque = bloque,
                        ejercicio = ejercicio,
                        maquina = resolverMaquina(ejercicio, maquinas)
                    )
                }

                Result.success(
                    ResultadoPreparacionSesion(
                        ejercicios = ejercicios,
                        seriesTotales = rutina.bloques.sumOf { it.serie }
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * @brief Crea un [Ejercicio] provisional para un bloque cuyo ejercicio aún no
     * está catalogado, de modo que la sesión en vivo nunca falle por esa causa.
     *
     * Al carecer [BloqueRutina] de nombre propio, el nombre del ejercicio
     * provisional se toma de `bloque.ejercicioId` (único identificador disponible)
     * y el grupo muscular principal se deja vacío.
     *
     * @param bloque Bloque de la rutina cuyo ejercicio no existe en el catálogo.
     * @return Ejercicio provisional con el id del bloque.
     */
    private fun crearEjercicioProvisional(bloque: BloqueRutina): Ejercicio = Ejercicio(
        id = bloque.ejercicioId,
        nombre = bloque.ejercicioId,
        grupoMuscularPrincipal = GRUPO_MUSCULAR_VACIO
    )

    /**
     * @brief Resuelve la máquina real sobre la que se ejecuta un ejercicio.
     *
     * Estrategia en cascada:
     * 1. Si el ejercicio declara `maquinaId`, se busca directamente en el parque
     *    del gimnasio (vínculo explícito creado durante la importación Naturvitia).
     * 2. Si no, se delega en [MotorMapeoEjercicioAMaquina.resolver] contra el
     *    nombre del ejercicio y, si el motor propone una máquina, se usa.
     * 3. Si no hay resolución, devuelve `null` (peso libre o sin mapear).
     *
     * @param ejercicio Ejercicio a resolver.
     * @param maquinas Parque de máquinas candidatas del gimnasio.
     * @return La [Maquina] real resuelta, o `null` si no se encontró.
     */
    private fun resolverMaquina(ejercicio: Ejercicio, maquinas: List<Maquina>): Maquina? {
        ejercicio.maquinaId?.let { maquinaId ->
            maquinas.firstOrNull { it.id == maquinaId }?.let { return it }
        }
        val resolucion = MotorMapeoEjercicioAMaquina.resolver(ejercicio.nombre, maquinas)
            ?: return null
        return maquinas.firstOrNull { it.id == resolucion.maquinaId }
    }

    companion object {
        /** Grupo muscular vacío para los ejercicios provisionales sin catalogar. */
        const val GRUPO_MUSCULAR_VACIO: String = ""
    }
}