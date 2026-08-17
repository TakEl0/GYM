/**
 * @file CatalogoMaquinaria.kt
 * @brief Catálogo real de maquinaria y equipamiento de los clubes Fitness Park España.
 *
 * Este catálogo incluye el equipamiento oficial verificado instalado en Fitness Park:
 * - Technogym (gama Artis y Selection)
 * - Hammer Strength (Plate Loaded)
 * - gym80 International
 * - Eleiko
 * - Nike Strength
 * - Rogue / Watson
 *
 * Permite resolver automáticamente los ejercicios de los planes nutricionales y de entrenamiento
 * (método Naturvitia) contra equipamiento real.
 */
package com.gym.app.domain.model

/**
 * @object CatalogoMaquinaria
 * @brief Proporciona la lista estándar de máquinas y equipamiento de Fitness Park disponibles para importar.
 */
object CatalogoMaquinaria {

    /**
     * @data class EntradaCatalogo
     * @brief Una máquina candidata del catálogo para importar.
     * @property id Identificador estable (slug) de la máquina en el catálogo.
     * @property nombre Nombre comercial de la máquina.
     * @property grupoMuscular Grupos musculares que trabaja.
     * @property tipoEquipamiento Tipo de equipamiento según [Maquina].
     * @property marca Marca del fabricante (Technogym, Hammer Strength, gym80, Eleiko, Nike Strength, Rogue, Watson).
     * @property modelo Modelo específico de la máquina.
     * @property ejerciciosPosibles Slugs de ejercicios que resuelve.
     * @property sinonimos Nombres alternativos o sinónimos con los que se puede referir al ejercicio.
     */
    data class EntradaCatalogo(
        val id: String,
        val nombre: String,
        val grupoMuscular: List<String>,
        val tipoEquipamiento: String,
        val marca: String? = null,
        val modelo: String? = null,
        val ejerciciosPosibles: List<String> = emptyList(),
        val sinonimos: List<String> = emptyList()
    )

    /**
     * @brief Lista completa de máquinas del catálogo real de Fitness Park España (47 entradas).
     */
    val maquinas: List<EntradaCatalogo> = listOf(
        // ── Zona de Fuerza Guiada — Technogym Artis ─────────────────────────
        EntradaCatalogo(
            id = "prensa-45",
            nombre = "Prensa de piernas 45º Technogym Artis",
            grupoMuscular = listOf("CUADRICEPS", "GLUTEO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Leg Press",
            ejerciciosPosibles = listOf("prensa-45", "prensa"),
            sinonimos = listOf("Prensa a 45º", "prensa de piernas 45", "prensa 45", "Prensa de piernas 45º")
        ),
        EntradaCatalogo(
            id = "extension-cuadriceps",
            nombre = "Extensión de cuádriceps Technogym Artis",
            grupoMuscular = listOf("CUADRICEPS"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Leg Extension",
            ejerciciosPosibles = listOf("extensiones", "extension-cuadriceps"),
            sinonimos = listOf("Extensiones", "Extensiones de cuádriceps", "extensiones cuádriceps", "Extension de cuadriceps")
        ),
        EntradaCatalogo(
            id = "curl-femoral-sentado",
            nombre = "Curl de isquios sentado Technogym Artis",
            grupoMuscular = listOf("FEMORAL"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Seated Leg Curl",
            ejerciciosPosibles = listOf("curl-femoral-sentado"),
            sinonimos = listOf("Curl femoral sentado", "curl de isquios sentado", "isquios sentado")
        ),
        EntradaCatalogo(
            id = "curl-femoral-tumbado",
            nombre = "Curl de isquios tumbado Technogym Artis",
            grupoMuscular = listOf("FEMORAL"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Lying Leg Curl",
            ejerciciosPosibles = listOf("femoral-tumbado", "curl-femoral-tumbado"),
            sinonimos = listOf("Femoral tumbado", "curl femoral tumbado", "curl de isquios tumbado", "Curl de isquios tumbado")
        ),
        EntradaCatalogo(
            id = "sentadilla-guiada",
            nombre = "Sentadilla guiada Technogym Artis",
            grupoMuscular = listOf("CUADRICEPS", "GLUTEO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Hack Squat",
            ejerciciosPosibles = listOf("sentadilla-guiada", "hack-squat"),
            sinonimos = listOf("Sentadilla", "Sentadilla guiada", "hack squat", "Sentadilla guiada (Hack)")
        ),
        EntradaCatalogo(
            id = "adductor",
            nombre = "Máquina de aductores Technogym Artis",
            grupoMuscular = listOf("ADUCTOR"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Adductor",
            ejerciciosPosibles = listOf("adductor"),
            sinonimos = listOf("Adductor", "aductores", "aductor en maquina", "Máquina de aductores")
        ),
        EntradaCatalogo(
            id = "abductor",
            nombre = "Máquina de abductores Technogym Artis",
            grupoMuscular = listOf("GLUTEO", "ABDUCTOR"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Abductor",
            ejerciciosPosibles = listOf("abductor"),
            sinonimos = listOf("Abductor", "abductores", "abductor en maquina", "Máquina de abductores")
        ),
        EntradaCatalogo(
            id = "multi-hip",
            nombre = "Multi Hip Technogym Artis",
            grupoMuscular = listOf("GLUTEO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Multi Hip",
            ejerciciosPosibles = listOf("patada-gluteo-maquina", "gluteo-maquina"),
            sinonimos = listOf("Patada de glúteo en máquina", "Glúteo en máquina", "patada de gluteo", "Multi Hip")
        ),
        EntradaCatalogo(
            id = "press-pecho-convergente",
            nombre = "Press de pecho convergente Technogym Artis",
            grupoMuscular = listOf("PECHO", "TRICEPS"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Chest Press",
            ejerciciosPosibles = listOf("press-horizontal", "press-pecho-convergente"),
            sinonimos = listOf("Press horizontal en máquina", "Press de pecho convergente", "press horizontal")
        ),
        EntradaCatalogo(
            id = "peck-deck",
            nombre = "Aperturas / Pech Deck Technogym Artis",
            grupoMuscular = listOf("PECHO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Peck Deck / Pectoral",
            ejerciciosPosibles = listOf("peck-deck", "aperturas-maquina"),
            sinonimos = listOf("Peck deck", "Aperturas en máquina", "aperturas maquina", "contractor", "Pech Deck")
        ),
        EntradaCatalogo(
            id = "press-inclinado-guiado",
            nombre = "Press inclinado guiado Technogym Artis",
            grupoMuscular = listOf("PECHO", "HOMBRO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Incline Press",
            ejerciciosPosibles = listOf("press-inclinado-maquina"),
            sinonimos = listOf("Press inclinado en máquina", "Press inclinado guiado")
        ),
        EntradaCatalogo(
            id = "jalon-al-pecho",
            nombre = "Jalón al pecho (Vertical Traction) Technogym Artis",
            grupoMuscular = listOf("DORSAL", "BICEPS"),
            tipoEquipamiento = Maquina.TIPO_POLEA,
            marca = "Technogym",
            modelo = "Artis Vertical Traction",
            ejerciciosPosibles = listOf("jalones-maquina", "jalones-v", "jalon-pecho"),
            sinonimos = listOf("Jalones en máquina", "Jalones en V", "jalón al pecho", "Jalón al pecho (polea alta)")
        ),
        EntradaCatalogo(
            id = "remo-sentado",
            nombre = "Remo sentado Technogym Artis",
            grupoMuscular = listOf("DORSAL", "TRAPECIO", "BICEPS"),
            tipoEquipamiento = Maquina.TIPO_POLEA,
            marca = "Technogym",
            modelo = "Artis Low Row",
            ejerciciosPosibles = listOf("remo-polea-baja", "remo-sentado"),
            sinonimos = listOf("Remo en polea baja", "Remo sentado", "Remo sentado en polea")
        ),
        EntradaCatalogo(
            id = "remo-hombro-posterior",
            nombre = "Remo y hombro posterior Technogym Artis",
            grupoMuscular = listOf("DORSAL", "TRAPECIO", "HOMBRO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Rear Delt / Row",
            ejerciciosPosibles = listOf("deltoide-posterior-maquina", "remo"),
            sinonimos = listOf("Deltoide posterior en máquina", "deltoide posterior maquina", "Remo", "contractor inverso")
        ),
        EntradaCatalogo(
            id = "press-hombros",
            nombre = "Press de hombros Technogym Artis",
            grupoMuscular = listOf("HOMBRO", "TRICEPS"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Shoulder Press",
            ejerciciosPosibles = listOf("press-hombro-maquina"),
            sinonimos = listOf("Press de hombro en máquina", "Press de hombros", "Press de hombro guiado")
        ),
        EntradaCatalogo(
            id = "curl-biceps",
            nombre = "Curl de bíceps Technogym Artis",
            grupoMuscular = listOf("BICEPS"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Biceps Curl",
            ejerciciosPosibles = listOf("curl-biceps-maquina"),
            sinonimos = listOf("Curl de bíceps en máquina", "Curl de bíceps")
        ),
        EntradaCatalogo(
            id = "extension-triceps",
            nombre = "Extensión de tríceps Technogym Artis",
            grupoMuscular = listOf("TRICEPS"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Triceps Extension",
            ejerciciosPosibles = listOf("extensiones-triceps-polea", "extensiones-triceps-maquina"),
            sinonimos = listOf("Extensiones de tríceps en polea", "Extensiones de tríceps en máquina", "Extensión de tríceps en polea", "Extensiones en polea")
        ),
        EntradaCatalogo(
            id = "gemelo-sentado",
            nombre = "Elevación de gemelos sentado Technogym Artis",
            grupoMuscular = listOf("GEMELO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Calf Raise",
            ejerciciosPosibles = listOf("gemelo-sentado"),
            sinonimos = listOf("Elevación de gemelos sentado", "gemelo sentado")
        ),
        EntradaCatalogo(
            id = "press-plano-guiado",
            nombre = "Press plano guiado Technogym Selection",
            grupoMuscular = listOf("PECHO", "TRICEPS"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Selection Chest Press",
            ejerciciosPosibles = listOf("press-plano-guiado"),
            sinonimos = listOf("Press plano guiado", "Press horizontal en máquina")
        ),
        EntradaCatalogo(
            id = "curl-arana",
            nombre = "Banco Scott Technogym Artis",
            grupoMuscular = listOf("BICEPS"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Preacher Curl",
            ejerciciosPosibles = listOf("curl-arana"),
            sinonimos = listOf("Banco Scott (curl araña)", "curl araña", "banco scott")
        ),

        // ── Zona de Fuerza Plate-Loaded — Hammer Strength ───────────────────
        EntradaCatalogo(
            id = "press-banca-iso",
            nombre = "Press de banca Iso-Lateral Hammer Strength",
            grupoMuscular = listOf("PECHO", "TRICEPS"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Hammer Strength",
            modelo = "Iso-Lateral Bench Press",
            ejerciciosPosibles = listOf("press-vertical", "press-banca"),
            sinonimos = listOf("Press vertical en máquina peso libre", "Press de banca Iso-Lateral", "Press de banca", "Press de banca (banco plano)")
        ),
        EntradaCatalogo(
            id = "press-inclinado-iso",
            nombre = "Press inclinado Iso-Lateral Hammer Strength",
            grupoMuscular = listOf("PECHO", "HOMBRO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Hammer Strength",
            modelo = "Iso-Lateral Incline Press",
            ejerciciosPosibles = listOf("press-inclinado"),
            sinonimos = listOf("Press inclinado", "Press inclinado Iso-Lateral")
        ),
        EntradaCatalogo(
            id = "jalon-frontal-iso",
            nombre = "Jalón frontal Iso-Lateral Hammer Strength",
            grupoMuscular = listOf("DORSAL", "BICEPS"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Hammer Strength",
            modelo = "Iso-Lateral Wide Pulldown",
            ejerciciosPosibles = listOf("jalones"),
            sinonimos = listOf("Jalones", "Jalón frontal Iso-Lateral")
        ),
        EntradaCatalogo(
            id = "remo-iso",
            nombre = "Remo Iso-Lateral Hammer Strength",
            grupoMuscular = listOf("DORSAL", "TRAPECIO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Hammer Strength",
            modelo = "Iso-Lateral Row",
            ejerciciosPosibles = listOf("remo-hammer", "remo"),
            sinonimos = listOf("Remo hammer", "remo iso-lateral", "Remo Iso-Lateral Hammer Strength")
        ),
        EntradaCatalogo(
            id = "prensa-lineal",
            nombre = "Prensa lineal de piernas Hammer Strength",
            grupoMuscular = listOf("CUADRICEPS", "GLUTEO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Hammer Strength",
            modelo = "Linear Leg Press",
            ejerciciosPosibles = listOf("prensa-lineal", "prensa-45"),
            sinonimos = listOf("Prensa lineal", "Prensa a 45º")
        ),
        EntradaCatalogo(
            id = "hack-squat",
            nombre = "Sentadilla Hack Hammer Strength",
            grupoMuscular = listOf("CUADRICEPS", "GLUTEO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Hammer Strength",
            modelo = "Hack Squat",
            ejerciciosPosibles = listOf("hack-squat", "sentadilla"),
            sinonimos = listOf("Hack squat", "Sentadilla en máquina")
        ),

        // ── Zona de Poleas y Máquinas — gym80 International ─────────────────
        EntradaCatalogo(
            id = "cable-crossover",
            nombre = "Cable Crossover Station gym80",
            grupoMuscular = listOf("PECHO", "HOMBRO", "TRICEPS"),
            tipoEquipamiento = Maquina.TIPO_POLEA,
            marca = "gym80 International",
            modelo = "Pure Kraft Cable Crossover",
            ejerciciosPosibles = listOf("cruces-polea", "extensiones-triceps-polea"),
            sinonimos = listOf("Cruces en polea", "aperturas en polea", "crossover", "Extensiones de tríceps en polea", "Extensiones en polea")
        ),
        EntradaCatalogo(
            id = "innovation-leg-press",
            nombre = "Innovation Leg Press gym80",
            grupoMuscular = listOf("CUADRICEPS", "GLUTEO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "gym80 International",
            modelo = "Innovation Leg Press",
            ejerciciosPosibles = listOf("prensa-45"),
            sinonimos = listOf("Prensa a 45º", "prensa gym80")
        ),
        EntradaCatalogo(
            id = "lying-leg-curl",
            nombre = "Lying Leg Curl gym80",
            grupoMuscular = listOf("FEMORAL"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "gym80 International",
            modelo = "Pure Kraft Lying Leg Curl",
            ejerciciosPosibles = listOf("femoral-tumbado"),
            sinonimos = listOf("Femoral tumbado", "curl femoral tumbado")
        ),
        EntradaCatalogo(
            id = "total-hip",
            nombre = "Total Hip Machine gym80",
            grupoMuscular = listOf("GLUTEO", "ADUCTOR", "ABDUCTOR"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "gym80 International",
            modelo = "Innovation Total Hip",
            ejerciciosPosibles = listOf("patada-gluteo", "hip-thrust"),
            sinonimos = listOf("Patada de glúteo en máquina", "Hip thrust en banco", "hip thrust", "patada de gluteo")
        ),
        EntradaCatalogo(
            id = "crunch-machine",
            nombre = "Crunch Machine gym80",
            grupoMuscular = listOf("ABDOMEN"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "gym80 International",
            modelo = "Innovation Crunch",
            ejerciciosPosibles = listOf("crunch-maquina"),
            sinonimos = listOf("Crunch en máquina", "crunch abdominal", "Máquina de crunch abdominal")
        ),
        EntradaCatalogo(
            id = "face-pull-gym80",
            nombre = "Face Pull / Cable Station gym80",
            grupoMuscular = listOf("HOMBRO", "TRAPECIO"),
            tipoEquipamiento = Maquina.TIPO_POLEA,
            marca = "gym80 International",
            modelo = "Pure Kraft Cable Station",
            ejerciciosPosibles = listOf("face-pull"),
            sinonimos = listOf("Face pull en polea", "face pull")
        ),

        // ── Peso Libre — Eleiko / Nike Strength / Rogue-Watson ──────────────
        EntradaCatalogo(
            id = "rack-sentadillas",
            nombre = "Rack de sentadillas Eleiko",
            grupoMuscular = listOf("CUADRICEPS", "GLUTEO", "LUMBAR", "HOMBRO"),
            tipoEquipamiento = Maquina.TIPO_BARRA,
            marca = "Eleiko",
            modelo = "IPF Competition Rack",
            ejerciciosPosibles = listOf("sentadilla", "peso-muerto", "press-militar"),
            sinonimos = listOf("Sentadilla", "Peso muerto", "Press militar", "peso muerto con barra", "Rack de barras olímpicas")
        ),
        EntradaCatalogo(
            id = "banco-plano",
            nombre = "Banco plano Eleiko",
            grupoMuscular = listOf("PECHO", "TRICEPS"),
            tipoEquipamiento = Maquina.TIPO_BARRA,
            marca = "Eleiko",
            modelo = "Competition Flat Bench",
            ejerciciosPosibles = listOf("press-banca", "press-frances"),
            sinonimos = listOf("Press de banca", "Press francés con barra", "press frances con barra", "Press de banca (banco plano)")
        ),
        EntradaCatalogo(
            id = "banco-ajustable",
            nombre = "Banco ajustable Nike Strength",
            grupoMuscular = listOf("PECHO", "HOMBRO", "BICEPS"),
            tipoEquipamiento = Maquina.TIPO_MANCUERNAS,
            marca = "Nike Strength",
            modelo = "Adjustable Utility Bench",
            ejerciciosPosibles = listOf("banco-ajustable", "curl-mancuernas-45"),
            sinonimos = listOf("Banco ajustable", "Curl con mancuernas en banco 45º", "curl con mancuernas en banco 45")
        ),
        EntradaCatalogo(
            id = "mancuernas",
            nombre = "Mancuernas (rack completo)",
            grupoMuscular = listOf("BICEPS", "TRICEPS", "HOMBRO", "PECHO", "ESPALDA"),
            tipoEquipamiento = Maquina.TIPO_MANCUERNAS,
            marca = "Rogue",
            modelo = "Urethane Dumbbell Set (1-50kg)",
            ejerciciosPosibles = listOf("mancuernas", "elevaciones-laterales", "elevaciones-posteriores", "curl-mancuernas"),
            sinonimos = listOf("Zona de mancuernas", "Elevaciones laterales con mancuerna", "Elevaciones posteriores con mancuerna", "elevaciones laterales", "elevaciones posteriores", "Curl con mancuernas en banco 45º")
        ),
        EntradaCatalogo(
            id = "barras-olimpicas",
            nombre = "Barras olímpicas + discos Eleiko",
            grupoMuscular = listOf("ESPALDA", "PIERNA", "BICEPS", "TRICEPS"),
            tipoEquipamiento = Maquina.TIPO_BARRA,
            marca = "Eleiko",
            modelo = "XF Bar / Open Collar",
            ejerciciosPosibles = listOf("peso-muerto-barra", "curl-barra", "press-frances-barra"),
            sinonimos = listOf("Peso muerto con barra", "Curl con barra", "Press francés con barra", "peso muerto")
        ),
        EntradaCatalogo(
            id = "rack-dominadas",
            nombre = "Jaula / rack de dominadas",
            grupoMuscular = listOf("DORSAL", "BICEPS", "ABDOMEN"),
            tipoEquipamiento = Maquina.TIPO_BARRA,
            marca = "Rogue",
            modelo = "RM-6 Infinity Monster Rack",
            ejerciciosPosibles = listOf("dominadas", "dominadas-asistidas", "elevacion-piernas"),
            sinonimos = listOf("Dominadas asistidas", "Dominadas", "Elevación de piernas en paralelas", "elevación de piernas", "Elevación de piernas (paralelas)")
        ),
        EntradaCatalogo(
            id = "banco-hiperextensiones",
            nombre = "Banco de hiperextensiones",
            grupoMuscular = listOf("LUMBAR", "GLUTEO", "FEMORAL"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Watson",
            modelo = "45 Degree Hyper Extension",
            ejerciciosPosibles = listOf("hiperextensiones"),
            sinonimos = listOf("Hiperextensiones", "extensiones lumbares", "banco lumbares")
        ),
        EntradaCatalogo(
            id = "rueda-abdominal",
            nombre = "Rueda abdominal / banco de abdominales",
            grupoMuscular = listOf("ABDOMEN"),
            tipoEquipamiento = Maquina.TIPO_MANCUERNAS,
            marca = "Nike Strength",
            modelo = "Ab Wheel & Sit-up Bench",
            ejerciciosPosibles = listOf("rueda-abdominal"),
            sinonimos = listOf("Rueda abdominal", "rueda abdominal")
        ),
        EntradaCatalogo(
            id = "multipower-smith",
            nombre = "Multipower / Smith (barra guiada)",
            grupoMuscular = listOf("PECHO", "HOMBRO", "CUADRICEPS"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Selection Smith Machine",
            ejerciciosPosibles = listOf("press-militar-multipower", "press-banca-inclinado-multipower"),
            sinonimos = listOf("Press militar en multipower", "Press banca inclinado en multipower", "Multipower", "sentadilla smith", "Multipower (Sentadilla Smith)")
        ),
        EntradaCatalogo(
            id = "gemelo-de-pie",
            nombre = "Elevación de gemelos de pie Eleiko",
            grupoMuscular = listOf("GEMELO"),
            tipoEquipamiento = Maquina.TIPO_BARRA,
            marca = "Eleiko",
            modelo = "Standing Calf Raise",
            ejerciciosPosibles = listOf("gemelo-de-pie"),
            sinonimos = listOf("Elevación de gemelos de pie", "gemelo de pie")
        ),

        // ── Zona de Cardio — Technogym Artis ───────────────────────────────
        EntradaCatalogo(
            id = "cinta-correr-artis",
            nombre = "Cinta de correr Technogym Artis Run",
            grupoMuscular = listOf("PIERNA", "CARDIO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Run",
            ejerciciosPosibles = listOf("cinta", "cardio"),
            sinonimos = listOf("Cinta de correr", "cardio")
        ),
        EntradaCatalogo(
            id = "bicicleta-estatica-artis",
            nombre = "Bicicleta estática Artis Bike",
            grupoMuscular = listOf("CUADRICEPS", "CARDIO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Bike",
            ejerciciosPosibles = listOf("bicicleta", "cardio"),
            sinonimos = listOf("Bicicleta estática", "bicicleta")
        ),
        EntradaCatalogo(
            id = "bicicleta-reclinada-artis",
            nombre = "Bicicleta reclinada Artis Recline",
            grupoMuscular = listOf("CUADRICEPS", "CARDIO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Recline",
            ejerciciosPosibles = listOf("bicicleta-reclinada", "cardio"),
            sinonimos = listOf("Bicicleta reclinada")
        ),
        EntradaCatalogo(
            id = "eliptica-artis",
            nombre = "Elíptica Artis Synchro",
            grupoMuscular = listOf("PIERNA", "CARDIO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Synchro",
            ejerciciosPosibles = listOf("eliptica", "cardio"),
            sinonimos = listOf("Elíptica", "eliptica")
        ),
        EntradaCatalogo(
            id = "escaleras-artis",
            nombre = "Escaleras Artis Climb",
            grupoMuscular = listOf("PIERNA", "GLUTEO", "CARDIO"),
            tipoEquipamiento = Maquina.TIPO_MAQUINA_GUIADA,
            marca = "Technogym",
            modelo = "Artis Climb",
            ejerciciosPosibles = listOf("escaleras", "cardio-gluteo"),
            sinonimos = listOf("Escaleras", "escalera")
        )
    )

    /**
     * @brief Identificadores estables de las entradas de equipamiento libre.
     * Estas máquinas (bancos, zona de mancuernas, barras y racks) se agrupan
     * bajo la familia "Equipamiento libre".
     */
    private val idsEquipamientoLibre: Set<String> = setOf(
        "banco-ajustable",
        "mancuernas",
        "barras-olimpicas",
        "rack-dominadas",
        "banco-plano",
        "rack-sentadillas",
        "gemelo-de-pie"
    )

    /**
     * @brief Devuelve las máquinas del catálogo agrupadas por familia.
     * @return Mapa con el nombre legible de la familia como clave y sus máquinas
     * como valor, en el orden estable del catálogo.
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
    const val NOMBRE_FAMILIA_EQUIPAMIENTO_LIBRE: String = "Equipamiento libre"

    /**
     * @brief Convierte una [EntradaCatalogo] en una [Maquina] de dominio, propagando
     * marca, modelo, ejercicios posibles y sinónimos.
     * @param entrada Entrada del catálogo a convertir.
     * @return Máquina de dominio con identificador estable del catálogo.
     */
    fun aMaquina(entrada: EntradaCatalogo): Maquina = Maquina(
        id = entrada.id,
        nombre = entrada.nombre,
        grupoMuscular = entrada.grupoMuscular,
        tipoEquipamiento = entrada.tipoEquipamiento,
        disponible = true,
        marca = entrada.marca,
        modelo = entrada.modelo,
        ejerciciosPosibles = entrada.ejerciciosPosibles,
        sinonimos = entrada.sinonimos
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
