/**
 * @file CatalogoMaquinaria.kt
 * @brief Catálogo estándar de maquinaria y equipamiento de un gimnasio comercial.
 *
 * Este catálogo representa el parque de máquinas típico de un gimnasio profesional.
 * El usuario importa desde este catálogo las máquinas que realmente existen en su
 * centro, de modo que los ejercicios de las rutinas y las sustituciones de maquinaria
 * siempre se resuelven contra equipamiento real y conocido, en lugar de texto libre.
 *
 * Las máquinas se agrupan por familia muscular para facilitar la selección, y cada
 * una declara su tipo de equipamiento ([Maquina.TIPO_MAQUINA_GUIADA], [Maquina.TIPO_POLEA],
 * [Maquina.TIPO_BARRA] o [Maquina.TIPO_MANCUERNAS]).
 */
package com.gym.app.domain.model

/**
 * @object CatalogoMaquinaria
 * @brief Proporciona la lista estándar de máquinas de gimnasio disponibles para importar.
 *
 * Cada entrada define el nombre comercial habitual de la máquina y los grupos
 * musculares que trabaja. El identificador es estable (slug), de modo que una
 * misma máquina importada siempre mantiene coherencia entre sesiones.
 */
object CatalogoMaquinaria {

    /**
     * @data class EntradaCatalogo
     * @brief Una máquina candidata del catálogo para importar.
     * @property id Identificador estable (slug) de la máquina en el catálogo.
     * @property nombre Nombre comercial de la máquina.
     * @property grupoMuscular Grupos musculares que trabaja.
     * @property tipoEquipamiento Tipo de equipamiento según [Maquina].
     */
    data class EntradaCatalogo(
        val id: String,
        val nombre: String,
        val grupoMuscular: List<String>,
        val tipoEquipamiento: String
    )

    /**
     * @brief Lista completa de máquinas del catálogo, ordenadas por familia muscular.
     */
    val maquinas: List<EntradaCatalogo> = listOf(
        // ── Pierna: cuadriceps ─────────────────────────────────────────────
        EntradaCatalogo("prensa-45", "Prensa de piernas 45º", listOf("CUADRICEPS", "GLUTEO"), Maquina.TIPO_MAQUINA_GUIADA),
        EntradaCatalogo("extension-cuadriceps", "Extensión de cuadriceps", listOf("CUADRICEPS"), Maquina.TIPO_MAQUINA_GUIADA),
        EntradaCatalogo("sentadilla-guiada", "Sentadilla guiada (Hack)", listOf("CUADRICEPS", "GLUTEO"), Maquina.TIPO_MAQUINA_GUIADA),
        EntradaCatalogo("sentadilla-smith", "Multipower (Sentadilla Smith)", listOf("CUADRICEPS", "GLUTEO", "FEMORAL"), Maquina.TIPO_MAQUINA_GUIADA),

        // ── Pierna: femoral / isquios ──────────────────────────────────────
        EntradaCatalogo("curl-femoral-sentado", "Curl femoral sentado", listOf("FEMORAL"), Maquina.TIPO_MAQUINA_GUIADA),
        EntradaCatalogo("curl-femoral-tumbado", "Curl femoral tumbado", listOf("FEMORAL"), Maquina.TIPO_MAQUINA_GUIADA),
        EntradaCatalogo("peso-muerto-guiado", "Peso muerto guiado", listOf("FEMORAL", "GLUTEO", "LUMBAR"), Maquina.TIPO_MAQUINA_GUIADA),
        EntradaCatalogo("hip-thrust", "Hip thrust (empuje de cadera)", listOf("GLUTEO", "FEMORAL"), Maquina.TIPO_MAQUINA_GUIADA),

        // ── Pierna: glúteo y aductor ───────────────────────────────────────
        EntradaCatalogo("abductor", "Máquina de abductores", listOf("GLUTEO", "ABDUCTOR"), Maquina.TIPO_MAQUINA_GUIADA),
        EntradaCatalogo("adductor", "Máquina de aductores", listOf("ADUCTOR"), Maquina.TIPO_MAQUINA_GUIADA),
        EntradaCatalogo("gluteo-polea", "Patada de glúteo en polea", listOf("GLUTEO"), Maquina.TIPO_POLEA),

        // ── Pierna: gemelo ─────────────────────────────────────────────────
        EntradaCatalogo("gemelo-sentado", "Elevación de gemelos sentado", listOf("GEMELO"), Maquina.TIPO_MAQUINA_GUIADA),
        EntradaCatalogo("gemelo-de-pie", "Elevación de gemelos de pie", listOf("GEMELO"), Maquina.TIPO_MAQUINA_GUIADA),

        // ── Espalda ────────────────────────────────────────────────────────
        EntradaCatalogo("jalon-al-pecho", "Jalón al pecho (polea alta)", listOf("DORSAL", "BICEPS"), Maquina.TIPO_POLEA),
        EntradaCatalogo("remo-sentado", "Remo sentado en polea", listOf("DORSAL", "TRAPECIO", "BICEPS"), Maquina.TIPO_POLEA),
        EntradaCatalogo("remo-guia", "Remo guiado (pectoral/espalda)", listOf("DORSAL", "TRAPECIO"), Maquina.TIPO_MAQUINA_GUIADA),
        EntradaCatalogo("espalda-guiada", "Máquina de espalda (remo cerrado)", listOf("DORSAL"), Maquina.TIPO_MAQUINA_GUIADA),
        EntradaCatalogo("pull-over", "Pull-over en polea", listOf("DORSAL", "PECHO"), Maquina.TIPO_POLEA),
        EntradaCatalogo("remo-barra", "Remo con barra", listOf("DORSAL", "TRAPECIO", "LUMBAR"), Maquina.TIPO_BARRA),

        // ── Pecho ──────────────────────────────────────────────────────────
        EntradaCatalogo("press-banca", "Press de banca (banco plano)", listOf("PECHO", "TRICEPS", "HOMBRO"), Maquina.TIPO_BARRA),
        EntradaCatalogo("press-inclinado-guiado", "Press inclinado guiado", listOf("PECHO", "HOMBRO"), Maquina.TIPO_MAQUINA_GUIADA),
        EntradaCatalogo("peck-deck", "Contractor de pecho (Peck Deck)", listOf("PECHO"), Maquina.TIPO_MAQUINA_GUIADA),
        EntradaCatalogo("aperturas-polea", "Aperturas en polea", listOf("PECHO"), Maquina.TIPO_POLEA),
        EntradaCatalogo("press-plano-guiado", "Press plano guiado", listOf("PECHO", "TRICEPS"), Maquina.TIPO_MAQUINA_GUIADA),

        // ── Hombro ─────────────────────────────────────────────────────────
        EntradaCatalogo("press-hombro-guiado", "Press de hombro guiado", listOf("HOMBRO", "TRICEPS"), Maquina.TIPO_MAQUINA_GUIADA),
        EntradaCatalogo("elevaciones-laterales", "Elevaciones laterales en polea", listOf("HOMBRO"), Maquina.TIPO_POLEA),
        EntradaCatalogo("face-pull", "Face pull en polea", listOf("HOMBRO", "TRAPECIO"), Maquina.TIPO_POLEA),
        EntradaCatalogo("press-militar", "Press militar con barra", listOf("HOMBRO", "TRICEPS"), Maquina.TIPO_BARRA),

        // ── Brazo: bíceps ──────────────────────────────────────────────────
        EntradaCatalogo("curl-barra", "Curl de bíceps con barra", listOf("BICEPS"), Maquina.TIPO_BARRA),
        EntradaCatalogo("curl-polea", "Curl de bíceps en polea", listOf("BICEPS"), Maquina.TIPO_POLEA),
        EntradaCatalogo("curl-araña", "Banco Scott (curl araña)", listOf("BICEPS"), Maquina.TIPO_MAQUINA_GUIADA),

        // ── Brazo: tríceps ─────────────────────────────────────────────────
        EntradaCatalogo("extension-triceps-polea", "Extensión de tríceps en polea", listOf("TRICEPS"), Maquina.TIPO_POLEA),
        EntradaCatalogo("press-frances", "Press francés con barra", listOf("TRICEPS"), Maquina.TIPO_BARRA),
        EntradaCatalogo("fondo-triceps", "Fondos de tríceps (máquina)", listOf("TRICEPS", "PECHO"), Maquina.TIPO_MAQUINA_GUIADA),

        // ── Abdomen ────────────────────────────────────────────────────────
        EntradaCatalogo("crunch-guiado", "Máquina de crunch abdominal", listOf("ABDOMEN"), Maquina.TIPO_MAQUINA_GUIADA),
        EntradaCatalogo("rueda-abdominal", "Rueda abdominal", listOf("ABDOMEN"), Maquina.TIPO_MANCUERNAS),
        EntradaCatalogo("elevacion-piernas", "Elevación de piernas (paralelas)", listOf("ABDOMEN"), Maquina.TIPO_MAQUINA_GUIADA),

        // ── Equipamiento libre ─────────────────────────────────────────────
        EntradaCatalogo("banco-ajustable", "Banco ajustable", listOf("PECHO", "HOMBRO"), Maquina.TIPO_MANCUERNAS),
        EntradaCatalogo("mancuernas", "Zona de mancuernas", listOf("BICEPS", "TRICEPS", "HOMBRO"), Maquina.TIPO_MANCUERNAS),
        EntradaCatalogo("rack-barras", "Rack de barras olímpicas", listOf("PECHO", "ESPALDA", "PIERNA"), Maquina.TIPO_BARRA),
        EntradaCatalogo("prensa-multicadera", "Prensa multicadera", listOf("CUADRICEPS", "GLUTEO"), Maquina.TIPO_MAQUINA_GUIADA)
    )

    /**
     * @brief Identificadores estables de las entradas de equipamiento libre.
     * Estas máquinas (bancos, zona de mancuernas y racks) no pertenecen a una
     * familia muscular concreta y se agrupan bajo la familia "Equipamiento libre".
     */
    private val idsEquipamientoLibre: Set<String> = setOf(
        "banco-ajustable",
        "mancuernas",
        "rack-barras",
        "prensa-multicadera"
    )

    /**
     * @brief Devuelve las máquinas del catálogo agrupadas por familia.
     * @return Mapa con el nombre legible de la familia como clave y sus máquinas
     * como valor, en el orden estable del catálogo. Las entradas de equipamiento
     * libre se agrupan bajo la familia "Equipamiento libre".
     */
    fun agruparPorFamilia(): Map<String, List<EntradaCatalogo>> = maquinas
        .groupBy { entrada ->
            if (entrada.id in idsEquipamientoLibre) {
                NOMBRE_FAMILIA_EQUIPAMIENTO_LIBRE
            } else {
                familiaDe(entrada.grupoMuscular.firstOrNull().orEmpty())
            }
        }

    /** Nombre legible de la familia de equipamiento libre. */
    private const val NOMBRE_FAMILIA_EQUIPAMIENTO_LIBRE: String = "Equipamiento libre"

    /**
     * @brief Convierte una [EntradaCatalogo] en una [Maquina] de dominio, lista
     * para registrar en el gimnasio.
     * @param entrada Entrada del catálogo a convertir.
     * @return Máquina de dominio con identificador estable del catálogo.
     */
    fun aMaquina(entrada: EntradaCatalogo): Maquina = Maquina(
        id = entrada.id,
        nombre = entrada.nombre,
        grupoMuscular = entrada.grupoMuscular,
        tipoEquipamiento = entrada.tipoEquipamiento,
        disponible = true
    )

    /**
     * @brief Traduce el grupo muscular en mayúsculas a una familia legible.
     * @param grupo Grupo muscular en formato catálogo (p. ej. "CUADRICEPS").
     * @return Nombre de familia legible (p. ej. "Pierna").
     */
    private fun familiaDe(grupo: String): String = when (grupo) {
        "CUADRICEPS", "FEMORAL", "GLUTEO", "ABDUCTOR", "ADUCTOR", "GEMELO", "PIERNA" -> "Pierna"
        "DORSAL", "TRAPECIO", "LUMBAR", "ESPALDA" -> "Espalda"
        "PECHO" -> "Pecho"
        "HOMBRO" -> "Hombro"
        "BICEPS" -> "Bíceps"
        "TRICEPS" -> "Tríceps"
        "ABDOMEN" -> "Abdomen"
        else -> "Equipamiento libre"
    }
}