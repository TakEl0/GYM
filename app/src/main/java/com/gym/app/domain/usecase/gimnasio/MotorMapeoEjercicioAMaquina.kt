/**
 * @file MotorMapeoEjercicioAMaquina.kt
 * @brief Motor local de mapeo de ejercicios del plan PDF a máquinas reales del gimnasio.
 *
 * Implementa la primera fase de la estrategia en cascada definida en el ADR 0004:
 * reglas locales primero (normalización + sinónimos + familia muscular) y, solo si
 * no hay coincidencia con confianza suficiente, delegación en IA.
 */
package com.gym.app.domain.usecase.gimnasio

import com.gym.app.domain.model.Maquina
import java.text.Normalizer
import java.util.Locale

/**
 * @enum OrigenMapeo
 * @brief Procedencia de una resolución de mapeo ejercicio → máquina.
 *
 * Ordenado de mayor a menor confianza intrínseca: una coincidencia exacta es más
 * fiable que un sinónimo parcial, y este más que la familia muscular.
 */
enum class OrigenMapeo {
    /** Coincidencia exacta normalizada con el nombre, un ejercicio posible o un sinónimo. */
    EXACTO,

    /** Coincidencia parcial (substring) con un nombre, ejercicio posible o sinónimo. */
    SINONIMO,

    /** Coincidencia por familia muscular y/o tipo de equipamiento sugerido. */
    FAMILIA,

    /** Corrección manual del usuario persistida como aprendizaje (MapeoAprendido). */
    MANUAL
}

/**
 * @data class ResolucionMapeo
 * @brief Resultado del mapeo de un nombre de ejercicio del plan PDF a una máquina del gimnasio.
 *
 * @property nombrePdf Nombre original del ejercicio tal y como aparece en el plan PDF.
 * @property maquinaId Identificador de la máquina resuelta en el catálogo o en el gimnasio.
 * @property confianza Nivel de confianza de la resolución en el intervalo 0..1.
 * @property origen Procedencia de la resolución según [OrigenMapeo].
 */
data class ResolucionMapeo(
    val nombrePdf: String,
    val maquinaId: String,
    val confianza: Float,
    val origen: OrigenMapeo
)

/**
 * @object MotorMapeoEjercicioAMaquina
 * @brief Resuelve localmente (sin IA) un nombre de ejercicio del plan del nutricionista
 * contra el parque de maquinaria del gimnasio.
 *
 * # Algoritmo de resolución (por orden de prioridad)
 *
 * 1. **Normalización** de ambos lados con [normalizar]: minúsculas, sin diacríticos,
 *    expansión de "45º"/"45°" → "45", "multipower" → "smith", plural → singular básico
 *    y colapso de espacios/guiones.
 * 2. **EXACTO**: el nombre normalizado del PDF coincide con el nombre de la máquina
 *    (confianza 1.0), con alguno de sus `ejerciciosPosibles` (0.95) o con alguno de
 *    sus `sinonimos` (0.95).
 * 3. **SINONIMO parcial**: coincidencia por substring entre el nombre del PDF y un
 *    nombre, ejercicio posible o sinónimo normalizado (confianza 0.85-0.9).
 * 4. **FAMILIA**: si sigue sin resolverse, filtra por el grupo muscular y el tipo de
 *    equipamiento que sugiere el nombre del PDF (confianza 0.6-0.7).
 * 5. Devuelve `null` si no encuentra nada; en ese caso intervendrá la IA
 *    (Gemini vía Edge Function) en fases posteriores.
 *
 * La clase es un objeto sin estado: todas las funciones son puras y por tanto
 * directamente testeables.
 */
object MotorMapeoEjercicioAMaquina {

    /**
     * @brief Resuelve un nombre de ejercicio del plan PDF contra una lista de máquinas.
     * @param nombreEjercicio Nombre del ejercicio tal y como aparece en el PDF
     * (p. ej. "Femoral tumbado").
     * @param maquinas Parque de máquinas candidatas (catálogo real o gimnasio del usuario).
     * @return [ResolucionMapeo] con la mejor coincidencia local, o `null` si no se
     * encuentra ninguna (ahí intervendrá la IA posteriormente).
     */
    fun resolver(nombreEjercicio: String, maquinas: List<Maquina>): ResolucionMapeo? {
        val nombreNormalizado = normalizar(nombreEjercicio)
        if (nombreNormalizado.isBlank()) return null

        // 1) Emparejamiento EXACTO (nombre, ejercicios posibles o sinónimos).
        resolverExacto(nombreEjercicio, nombreNormalizado, maquinas)?.let { return it }

        // 2) SINONIMO parcial por substring.
        resolverParcial(nombreEjercicio, nombreNormalizado, maquinas)?.let { return it }

        // 3) FAMILIA muscular + tipo de equipamiento sugerido.
        resolverPorFamilia(nombreEjercicio, nombreNormalizado, maquinas)?.let { return it }

        // 4) Sin resolución local: null para que la IA intervenga.
        return null
    }

    /**
     * @brief Normaliza un texto para comparaciones robustas entre el PDF y el catálogo.
     *
     * Transformaciones aplicadas, en orden:
     * - Minúsculas con [Locale.ROOT] (determinista en cualquier dispositivo).
     * - Eliminación de diacríticos mediante [Normalizer] forma NFD + borrado de marcas
     *   combinables (regex `\p{Mn}`); así "Fémoral" → "femoral".
     * - Expansión del símbolo de grados "45º"/"45°" → "45" ("Prensa a 45º" → "prensa a 45").
     * - Expansión de "multipower" → "smith" ("Press militar en multipower" →
     *   "press militar en smith").
     * - Colapso de espacios, guiones, barras y guiones bajos a un único espacio.
     * - Conversión básica de plural a singular por token (p. ej. "mancuernas" → "mancuerna",
     *   "extensiones" → "extension", "hombros" → "hombro"), siempre de forma consistente
     *   en ambos lados. Se respetan los anglicismos invariantes ("press", "biceps", "triceps").
     *
     * @param texto Texto original (nombre de ejercicio, máquina o sinónimo).
     * @return Texto normalizado listo para comparación.
     */
    fun normalizar(texto: String): String {
        var resultado = texto.lowercase(Locale.ROOT)

        // Elimina diacríticos (acentos, diéresis, tildes de la ñ...).
        resultado = Normalizer.normalize(resultado, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")

        // Expande el símbolo de grados (masculino ordinal "º" o signo de grado "°").
        resultado = resultado.replace("45º", "45").replace("45°", "45")

        // Expande "multipower" al término equivalente "smith".
        resultado = resultado.replace("multipower", "smith")

        // Colapsa separadores (espacios, guiones, guiones bajos, barras).
        resultado = resultado.replace(Regex("[\\s\\-_/]+"), " ")

        // Plural → singular básico por token.
        resultado = resultado.split(" ")
            .joinToString(" ") { convertirPluralASingular(it) }
            .trim()

        return resultado
    }

    /**
     * @brief Busca coincidencias exactas del nombre normalizado contra el nombre de la
     * máquina, sus ejercicios posibles y sus sinónimos (todos normalizados).
     * @param nombreOriginal Nombre del ejercicio tal y como llegó del PDF (para el resultado).
     * @param nombre Nombre del ejercicio ya normalizado.
     * @param maquinas Lista de máquinas candidatas.
     * @return [ResolucionMapeo] con origen EXACTO, o `null` si no hay coincidencia exacta.
     */
    private fun resolverExacto(
        nombreOriginal: String,
        nombre: String,
        maquinas: List<Maquina>
    ): ResolucionMapeo? {
        for (maquina in maquinas) {
            // Coincidencia con el nombre comercial de la máquina.
            if (nombre == normalizar(maquina.nombre)) {
                return ResolucionMapeo(nombreOriginal, maquina.id, CONFIANZA_NOMBRE, OrigenMapeo.EXACTO)
            }
            // Coincidencia con alguno de los ejercicios que resuelve la máquina.
            if (maquina.ejerciciosPosibles.any { nombre == normalizar(it) }) {
                return ResolucionMapeo(nombreOriginal, maquina.id, CONFIANZA_SINONIMO_EXACTO, OrigenMapeo.EXACTO)
            }
            // Coincidencia con alguno de los sinónimos con los que se la conoce.
            if (maquina.sinonimos.any { nombre == normalizar(it) }) {
                return ResolucionMapeo(nombreOriginal, maquina.id, CONFIANZA_SINONIMO_EXACTO, OrigenMapeo.EXACTO)
            }
        }
        return null
    }

    /**
     * @brief Busca coincidencias parciales por substring: que el nombre del PDF contenga
     * el candidato (nombre, ejercicio posible o sinónimo) o que el candidato contenga
     * el nombre del PDF.
     * @param nombreOriginal Nombre del ejercicio tal y como llegó del PDF (para el resultado).
     * @param nombre Nombre del ejercicio ya normalizado.
     * @param maquinas Lista de máquinas candidatas.
     * @return [ResolucionMapeo] con origen SINONIMO, o `null` si no hay substring válido.
     */
    private fun resolverParcial(
        nombreOriginal: String,
        nombre: String,
        maquinas: List<Maquina>
    ): ResolucionMapeo? {
        for (maquina in maquinas) {
            // Candidatos: (confianza, texto normalizado) por nombre, ejercicio posible y sinónimo.
            val candidatos = listOf(CONFIANZA_NOMBRE_PARCIAL to normalizar(maquina.nombre)) +
                maquina.ejerciciosPosibles.map { CONFIANZA_SINONIMO_PARCIAL to normalizar(it) } +
                maquina.sinonimos.map { CONFIANZA_SINONIMO_PARCIAL to normalizar(it) }

            for ((confianza, candidato) in candidatos) {
                // Evita emparejamientos triviales con cadenas demasiado cortas.
                if (candidato.length < LONGITUD_MINIMA_SUBSTRING || nombre.length < LONGITUD_MINIMA_SUBSTRING) continue
                if (nombre.contains(candidato) || candidato.contains(nombre)) {
                    return ResolucionMapeo(nombreOriginal, maquina.id, confianza, OrigenMapeo.SINONIMO)
                }
            }
        }
        return null
    }

    /**
     * @brief Resuelve por familia muscular y tipo de equipamiento sugeridos por el nombre
     * del PDF cuando no ha habido coincidencia léxica. Se usa como último recurso local.
     * @param nombreOriginal Nombre del ejercicio tal y como llegó del PDF (para el resultado).
     * @param nombre Nombre del ejercicio ya normalizado.
     * @param maquinas Lista de máquinas candidatas.
     * @return [ResolucionMapeo] con origen FAMILIA, o `null` si no se detecta ni grupo
     * ni equipamiento o no hay máquina que cumpla el filtro.
     */
    private fun resolverPorFamilia(
        nombreOriginal: String,
        nombre: String,
        maquinas: List<Maquina>
    ): ResolucionMapeo? {
        val grupo = clavesGrupoMuscular.firstOrNull { nombre.contains(it.first) }?.second
        val equipo = clavesTipoEquipamiento.firstOrNull { nombre.contains(it.first) }?.second
        if (grupo == null && equipo == null) return null

        val candidatas = maquinas.filter { maquina ->
            val coincideGrupo = grupo == null || maquina.grupoMuscular.any { it.equals(grupo, ignoreCase = true) }
            val coincideEquipo = equipo == null || maquina.tipoEquipamiento.equals(equipo, ignoreCase = true)
            coincideGrupo && coincideEquipo
        }

        val maquina = candidatas.firstOrNull() ?: return null
        val confianza = if (grupo != null && equipo != null) CONFIANZA_FAMILIA_COMPLETA else CONFIANZA_FAMILIA_PARCIAL
        return ResolucionMapeo(nombreOriginal, maquina.id, confianza, OrigenMapeo.FAMILIA)
    }

    /**
     * @brief Convierte una palabra en plural a su forma singular básica en castellano.
     *
     * Se respetan las palabras invariantes del vocabulario de gimnasio (anglicismos que
     * terminan en "s" pero no son plurales), de modo que "press", "biceps" y "triceps"
     * no se mutilan. El resto de términos se reducen con reglas simples y consistentes
     * en ambos lados de la comparación (p. ej. "mancuernas" → "mancuerna",
     * "extensiones" → "extension", "hombros" → "hombro").
     * @param palabra Término a convertir.
     * @return Término en singular básico, o el original si no aplica la regla.
     */
    private fun convertirPluralASingular(palabra: String): String {
        if (palabra in PALABRAS_INVARIANTES) return palabra
        return when {
            palabra.endsWith("es") && palabra.length > 4 -> palabra.dropLast(2)
            palabra.endsWith("s") && !palabra.endsWith("es") && palabra.length > 3 -> palabra.dropLast(1)
            else -> palabra
        }
    }

    /** Palabras que terminan en "s" pero no son plurales (no se singularizan). */
    private val PALABRAS_INVARIANTES: Set<String> = setOf("press", "biceps", "triceps")

    /** Confianza para coincidencia exacta con el nombre comercial de la máquina. */
    const val CONFIANZA_NOMBRE: Float = 1.0f

    /** Confianza para coincidencia exacta con ejercicio posible o sinónimo. */
    const val CONFIANZA_SINONIMO_EXACTO: Float = 0.95f

    /** Confianza para coincidencia parcial con el nombre comercial. */
    const val CONFIANZA_NOMBRE_PARCIAL: Float = 0.9f

    /** Confianza para coincidencia parcial con ejercicio posible o sinónimo. */
    const val CONFIANZA_SINONIMO_PARCIAL: Float = 0.85f

    /** Confianza cuando se detectan grupo muscular y tipo de equipamiento. */
    const val CONFIANZA_FAMILIA_COMPLETA: Float = 0.7f

    /** Confianza cuando solo se detecta grupo muscular o tipo de equipamiento. */
    const val CONFIANZA_FAMILIA_PARCIAL: Float = 0.6f

    /** Longitud mínima de un substring para considerar una coincidencia parcial válida. */
    const val LONGITUD_MINIMA_SUBSTRING: Int = 4

    /**
     * Claves de detección de grupo muscular, en orden de prioridad. Cada entrada
     * asocia una palabra clave (ya normalizada) con el grupo muscular del catálogo.
     */
    private val clavesGrupoMuscular: List<Pair<String, String>> = listOf(
        "femoral" to "FEMORAL",
        "isquio" to "FEMORAL",
        "gluteo" to "GLUTEO",
        "abductor" to "ABDUCTOR",
        "aductor" to "ADUCTOR",
        "adductor" to "ADUCTOR",
        "dorsal" to "DORSAL",
        "jalon" to "DORSAL",
        "jalone" to "DORSAL",
        "dominada" to "DORSAL",
        "remo" to "DORSAL",
        "trapecio" to "TRAPECIO",
        "lumbar" to "LUMBAR",
        "gemelo" to "GEMELO",
        "tricep" to "TRICEPS",
        "bicep" to "BICEPS",
        "abdominal" to "ABDOMEN",
        "rueda" to "ABDOMEN",
        "crunch" to "ABDOMEN",
        "cuadricep" to "CUADRICEPS",
        "extension" to "CUADRICEPS",
        "sentadilla" to "CUADRICEPS",
        "prensa" to "CUADRICEPS",
        "hack" to "CUADRICEPS",
        "hip" to "GLUTEO",
        "pecho" to "PECHO",
        "press" to "PECHO",
        "peck" to "PECHO",
        "apertura" to "PECHO",
        "cruce" to "PECHO",
        "hombro" to "HOMBRO",
        "deltoide" to "HOMBRO",
        "militar" to "HOMBRO",
        "face" to "HOMBRO",
        "hiperextension" to "LUMBAR"
    )

    /**
     * Claves de detección de tipo de equipamiento, en orden de prioridad.
     */
    private val clavesTipoEquipamiento: List<Pair<String, String>> = listOf(
        "polea" to Maquina.TIPO_POLEA,
        "mancuerna" to Maquina.TIPO_MANCUERNAS,
        "barra" to Maquina.TIPO_BARRA,
        "maquina" to Maquina.TIPO_MAQUINA_GUIADA
    )
}
