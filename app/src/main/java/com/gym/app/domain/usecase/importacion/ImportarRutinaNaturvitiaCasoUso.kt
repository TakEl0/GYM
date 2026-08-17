/**
 * @file ImportarRutinaNaturvitiaCasoUso.kt
 * @brief Caso de uso que convierte el plan de entrenamiento Naturvitia (texto extraído
 * del PDF del nutricionista) en rutinas de entrenamiento persistentes.
 *
 * Forma parte de la estrategia del ADR 0004: el texto del PDF se parsea con
 * [com.gym.app.data.parser.ParserDocumentosNaturvitia], cada ejercicio se resuelve contra
 * la maquinaria real del gimnasio con [ResolverEjercicioAMaquinaCasoUso] y el resultado
 * se materializa en [com.gym.app.domain.model.Rutina] con sus
 * [com.gym.app.domain.model.BloqueRutina] y en [com.gym.app.domain.model.Ejercicio] del
 * catálogo del usuario.
 */
package com.gym.app.domain.usecase.importacion

import com.gym.app.data.parser.ParserDocumentosNaturvitia
import com.gym.app.domain.model.BloqueRutina
import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.model.Entrenamiento
import com.gym.app.domain.model.Maquina
import com.gym.app.domain.model.Rutina
import com.gym.app.domain.repository.RepositorioEjercicio
import com.gym.app.domain.repository.RepositorioGimnasio
import com.gym.app.domain.repository.RepositorioRutina
import com.gym.app.domain.usecase.gimnasio.MotorMapeoEjercicioAMaquina
import com.gym.app.domain.usecase.gimnasio.ResolucionMapeo
import com.gym.app.domain.usecase.gimnasio.ResolverEjercicioAMaquinaCasoUso
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * @data class ResultadoImportacionRutina
 * @brief Resumen de una importación de plan de entrenamiento Naturvitia.
 *
 * @property rutinasCreadas Rutinas construidas y persistidas, una por cada día del plan.
 * @property ejerciciosMapeados Número de ejercicios distintos del plan que se resolvieron
 * contra una máquina del gimnasio y se guardaron en el catálogo de ejercicios.
 * @property ejerciciosSinMapear Nombres de los ejercicios del plan que el motor no resolvió
 * contra la maquinaria del gimnasio. Se omiten de las rutinas y quedan pendientes de
 * revisión manual o asistida por IA.
 */
data class ResultadoImportacionRutina(
    val rutinasCreadas: List<Rutina>,
    val ejerciciosMapeados: Int,
    val ejerciciosSinMapear: List<String>
)

/**
 * @class ImportarRutinaNaturvitiaCasoUso
 * @brief Orquesta la importación de un plan de entrenamiento Naturvitia de 5 días en
 * rutinas persistentes vinculadas a la maquinaria real del gimnasio del usuario.
 *
 * # Flujo de ejecución
 *
 * 1. **Parseo**: se convierte el texto del PDF en una lista de [Entrenamiento] (uno por
 *    día) mediante [ParserDocumentosNaturvitia.parsearEntrenamiento].
 * 2. **Gimnasio**: se obtiene el gimnasio configurado con
 *    [RepositorioGimnasio.observarGimnasio]. Si no existe o no tiene máquinas, la
 *    importación falla sin crear nada.
 * 3. **Resolución**: por cada [com.gym.app.domain.model.DetalleEjercicio] del día se
 *    invoca [ResolverEjercicioAMaquinaCasoUso.ejecutar] contra el parque de máquinas.
 *    - Si resuelve, se crea un [Ejercicio] nuevo (id slug, grupo muscular deducido,
 *      máquina y equipamiento) y un [BloqueRutina] con las métricas del PDF.
 *    - Si no resuelve, el ejercicio se omite de la rutina y su nombre se reporta en
 *      [ResultadoImportacionRutina.ejerciciosSinMapear] para revisión manual/IA.
 * 4. **Persistencia**: los ejercicios únicos se guardan con
 *    [RepositorioEjercicio.guardarVarios] y cada rutina con
 *    [RepositorioRutina.guardarRutina].
 *
 * Los días se asignan secuencialmente empezando por el lunes (1): día 1 → Lunes,
 * día 2 → Martes, ... y, si hubiera más de 7, la semana vuelve a empezar en ciclo.
 *
 * @property repositorioRutina Puerto de persistencia de las rutinas creadas.
 * @property repositorioEjercicio Puerto de persistencia del catálogo de ejercicios.
 * @property repositorioGimnasio Puerto de acceso al gimnasio y su maquinaria.
 * @property resolverEjercicioAMaquina Resuelve cada ejercicio del plan contra una máquina.
 * @property dispatcher Dispatcher sobre el que se ejecuta la operación (por defecto IO).
 */
class ImportarRutinaNaturvitiaCasoUso(
    private val repositorioRutina: RepositorioRutina,
    private val repositorioEjercicio: RepositorioEjercicio,
    private val repositorioGimnasio: RepositorioGimnasio,
    private val resolverEjercicioAMaquina: ResolverEjercicioAMaquinaCasoUso,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Importa un plan de entrenamiento Naturvitia desde su texto extraído del PDF.
     * @param textoEntrenamiento Texto completo del plan (formato "S R V T" de Naturvitia).
     * @return [Result] con [ResultadoImportacionRutina] en caso de éxito, o el error que
     * se haya producido (p. ej. gimnasio sin maquinaria configurada).
     */
    suspend fun ejecutar(textoEntrenamiento: String): Result<ResultadoImportacionRutina> =
        withContext(dispatcher) {
            try {
                // 1) Parseo del texto del PDF en un entrenamiento por día.
                val entrenamientos = ParserDocumentosNaturvitia.parsearEntrenamiento(textoEntrenamiento)

                // 2) Gimnasio del usuario: sin maquinaria no se puede mapear nada.
                val gimnasio = repositorioGimnasio.observarGimnasio().first()
                val maquinas = gimnasio?.maquinas.orEmpty()
                if (maquinas.isEmpty()) {
                    return@withContext Result.failure(
                        IllegalStateException(MENSAJE_GIMNASIO_NO_CONFIGURADO)
                    )
                }

                val rutinasCreadas = mutableListOf<Rutina>()
                val ejerciciosGuardados = LinkedHashMap<String, Ejercicio>()
                val ejerciciosSinMapear = mutableListOf<String>()

                // 3) Por cada día: resolver ejercicios, construir bloques y guardar rutina.
                entrenamientos.forEachIndexed { indiceDia, entrenamiento ->
                    val dia = indiceDia + 1
                    val bloques = construirBloques(
                        entrenamiento = entrenamiento,
                        dia = dia,
                        maquinas = maquinas,
                        ejerciciosGuardados = ejerciciosGuardados,
                        ejerciciosSinMapear = ejerciciosSinMapear
                    )
                    val rutina = Rutina(
                        id = "$PREFIJO_ID_RUTINA-$dia",
                        nombre = construirNombreRutina(entrenamiento),
                        descripcion = construirDescripcion(entrenamiento),
                        diasSemana = listOf(calcularDiaSemana(indiceDia)),
                        bloques = bloques
                    )
                    repositorioRutina.guardarRutina(rutina)
                    rutinasCreadas += rutina
                }

                // 4) Persistencia de los ejercicios únicos del plan (deduplicados por id).
                repositorioEjercicio.guardarVarios(ejerciciosGuardados.values.toList())

                Result.success(
                    ResultadoImportacionRutina(
                        rutinasCreadas = rutinasCreadas,
                        ejerciciosMapeados = ejerciciosGuardados.size,
                        ejerciciosSinMapear = ejerciciosSinMapear
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * @brief Construye los [BloqueRutina] de un día resolviendo cada ejercicio contra la
     * maquinaria. Los ejercicios sin resolución se registran en `ejerciciosSinMapear` y
     * se omiten del resultado.
     * @param entrenamiento Entrenamiento del día a procesar.
     * @param dia Número del día dentro del plan (1..N), usado en los identificadores.
     * @param maquinas Parque de máquinas candidatas del gimnasio.
     * @param ejerciciosGuardados Acumulador de ejercicios creados (deduplicados por id).
     * @param ejerciciosSinMapear Acumulador de nombres de ejercicios no resueltos.
     * @return Lista de bloques de serie del día, en el orden del plan.
     */
    private suspend fun construirBloques(
        entrenamiento: Entrenamiento,
        dia: Int,
        maquinas: List<Maquina>,
        ejerciciosGuardados: MutableMap<String, Ejercicio>,
        ejerciciosSinMapear: MutableList<String>
    ): List<BloqueRutina> {
        val bloques = mutableListOf<BloqueRutina>()
        entrenamiento.ejercicios.forEachIndexed { indice, detalle ->
            val resolucion = resolverEjercicioAMaquina.ejecutar(detalle.nombre, maquinas)
            if (resolucion == null) {
                ejerciciosSinMapear += detalle.nombre
                return@forEachIndexed
            }
            val maquina = maquinas.firstOrNull { it.id == resolucion.maquinaId }
            val ejercicio = crearEjercicio(detalle.nombre, resolucion, maquina)
            ejerciciosGuardados[ejercicio.id] = ejercicio
            bloques += BloqueRutina(
                id = "$PREFIJO_ID_BLOQUE-$dia-$indice",
                ejercicioId = ejercicio.id,
                serie = detalle.series,
                repeticiones = detalle.repeticiones,
                // El peso se rellena en el entrenamiento en vivo / seguimiento de cargas.
                pesoKg = null,
                descansoSegundos = detalle.descansoSegundos
            )
        }
        return bloques
    }

    /**
     * @brief Crea un [Ejercicio] del catálogo a partir de un detalle del plan y su
     * resolución a una máquina concreta del gimnasio.
     * @param nombre Nombre del ejercicio tal y como aparece en el PDF.
     * @param resolucion Resolución del motor (máquina destino y origen de confianza).
     * @param maquina Máquina resuelta en el parque del gimnasio (puede ser `null` si el
     * identificador resuelto no se encuentra, aunque no debería ocurrir).
     * @return Ejercicio listo para persistir en el catálogo.
     */
    private fun crearEjercicio(nombre: String, resolucion: ResolucionMapeo, maquina: Maquina?): Ejercicio =
        Ejercicio(
            id = crearSlugEjercicio(nombre),
            nombre = nombre,
            grupoMuscularPrincipal = deducirGrupoMuscular(nombre, maquina),
            grupoMuscularSecundario = null,
            maquinaId = resolucion.maquinaId,
            equipamiento = maquina?.tipoEquipamiento ?: Ejercicio.EQUIPAMIENTO_MAQUINA_GUIADA,
            instrucciones = null
        )

    /**
     * @brief Genera el identificador estable (slug) de un ejercicio a partir de su nombre.
     * @param nombre Nombre del ejercicio del plan (p. ej. "Femoral tumbado").
     * @return Slug normalizado prefijado, p. ej. "ejercicio-femoral-tumbado".
     */
    private fun crearSlugEjercicio(nombre: String): String {
        val normalizado = MotorMapeoEjercicioAMaquina.normalizar(nombre)
        val slug = normalizado.replace(Regex("[^a-z0-9]+"), "-").trim('-')
        return "$PREFIJO_ID_EJERCICIO-$slug"
    }

    /**
     * @brief Deduce el grupo muscular principal de un ejercicio.
     *
     * Prioridad: (1) palabra clave específica presente en el nombre del PDF (p. ej.
     * "femoral", "prensa", "jalon", "hip"); (2) primer grupo muscular de la máquina
     * resuelta; (3) grupo genérico [GRUPO_MUSCULAR_GENERICO] como último recurso.
     *
     * @param nombre Nombre del ejercicio del plan.
     * @param maquina Máquina resuelta (puede ser `null`).
     * @return Grupo muscular principal en formato catálogo (p. ej. "CUADRICEPS").
     */
    private fun deducirGrupoMuscular(nombre: String, maquina: Maquina?): String {
        val normalizado = MotorMapeoEjercicioAMaquina.normalizar(nombre)
        return CLAVES_GRUPO_MUSCULAR
            .firstOrNull { (clave, _) -> normalizado.contains(clave) }
            ?.second
            ?: maquina?.grupoMuscular?.firstOrNull()
            ?: GRUPO_MUSCULAR_GENERICO
    }

    /**
     * @brief Compone el nombre legible de la rutina del día.
     * @param entrenamiento Entrenamiento parseado del plan.
     * @return "Día N - Grupo" (p. ej. "Día 1 - Pierna") o el nombre del PDF tal cual
     * si el parser no dedujo grupo muscular.
     */
    private fun construirNombreRutina(entrenamiento: Entrenamiento): String {
        val grupo = entrenamiento.grupoMuscular.firstOrNull().orEmpty()
        return if (grupo.isNotBlank()) "${entrenamiento.nombre} - $grupo" else entrenamiento.nombre
    }

    /**
     * @brief Construye la descripción de la rutina con la técnica general del plan.
     * @param entrenamiento Entrenamiento parseado del plan.
     * @return Descripción base de la importación más la técnica general (TUT y carga)
     * si el plan la declara en sus observaciones.
     */
    private fun construirDescripcion(entrenamiento: Entrenamiento): String {
        val tecnica = entrenamiento.observaciones.trim()
        return if (tecnica.isNotEmpty()) "$DESCRIPCION_BASE. Técnica: $tecnica." else DESCRIPCION_BASE
    }

    /**
     * @brief Asigna el día de la semana (1 = Lunes ... 7 = Domingo) de forma secuencial
     * a partir del índice del día dentro del plan, ciclando si hay más de 7 días.
     * @param indiceDia Índice cero-base del día en la lista parseada.
     * @return Día de la semana en el intervalo 1..7.
     */
    private fun calcularDiaSemana(indiceDia: Int): Int = (indiceDia % 7) + 1

    companion object {
        /** Mensaje de error cuando el gimnasio no tiene maquinaria configurada. */
        const val MENSAJE_GIMNASIO_NO_CONFIGURADO: String =
            "Configura primero tu gimnasio y su maquinaria."

        /** Prefijo de los identificadores de las rutinas importadas. */
        const val PREFIJO_ID_RUTINA: String = "rutina-naturvitia"

        /** Prefijo de los identificadores de los bloques de las rutinas importadas. */
        const val PREFIJO_ID_BLOQUE: String = "bloque"

        /** Prefijo de los identificadores de los ejercicios creados durante la importación. */
        const val PREFIJO_ID_EJERCICIO: String = "ejercicio"

        /** Descripción base de todas las rutinas importadas del plan Naturvitia. */
        private const val DESCRIPCION_BASE: String = "Importada del plan Naturvitia"

        /** Grupo muscular genérico cuando no se deduce ninguno del nombre ni de la máquina. */
        private const val GRUPO_MUSCULAR_GENERICO: String = "GENERAL"

        /**
         * Claves de detección de grupo muscular por palabra clave del nombre del PDF,
         * ordenadas de más específicas a más genéricas. La primera clave que aparezca en
         * el nombre normalizado determina el grupo; si ninguna coincide se usa el grupo
         * de la máquina resuelta.
         */
        private val CLAVES_GRUPO_MUSCULAR: List<Pair<String, String>> = listOf(
            "femoral" to "FEMORAL",
            "isquio" to "FEMORAL",
            "prensa" to "CUADRICEPS",
            "sentadilla" to "CUADRICEPS",
            "gluteo" to "GLUTEO",
            "hip" to "GLUTEO",
            "adductor" to "ADUCTOR",
            "aductor" to "ADUCTOR",
            "abductor" to "ABDUCTOR",
            "frances" to "TRICEPS",
            "tricep" to "TRICEPS",
            "bicep" to "BICEPS",
            "curl" to "BICEPS",
            "deltoide" to "HOMBRO",
            "militar" to "HOMBRO",
            "hombro" to "HOMBRO",
            "abdominal" to "ABDOMEN",
            "rueda" to "ABDOMEN",
            "crunch" to "ABDOMEN",
            "pierna" to "ABDOMEN",
            "elevacion" to "HOMBRO",
            "peso muerto" to "DORSAL",
            "dominada" to "DORSAL",
            "jalon" to "DORSAL",
            "remo" to "DORSAL",
            "peck" to "PECHO",
            "apertura" to "PECHO",
            "cruce" to "PECHO",
            "pecho" to "PECHO",
            "press" to "PECHO",
            "hiperextension" to "LUMBAR",
            "lumbar" to "LUMBAR"
        )
    }
}