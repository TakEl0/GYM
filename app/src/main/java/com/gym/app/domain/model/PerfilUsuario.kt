/**
 * @file PerfilUsuario.kt
 * @brief Modelo de dominio que representa el perfil antropométrico, de actividad y de
 * objetivos del usuario dentro del método Naturvitia.
 *
 * Esta entidad pertenece a la capa de dominio y es totalmente independiente de Android,
 * de la base de datos y del backend remoto. Concentra los cálculos del metabolismo basal
 * (fórmula de Mifflin-St Jeor), del gasto energético total (TDEE) y de la distribución
 * de macronutrientes objetivo del plan nutricional.
 */
package com.gym.app.domain.model

import kotlin.math.roundToInt

/**
 * @class PerfilUsuario
 * @brief Representa los datos de configuración corporal y de objetivos del usuario.
 *
 * La clase expone propiedades calculadas que materializan las reglas del método Naturvitia:
 * - **TMB** mediante la fórmula de Mifflin-St Jeor.
 * - **TDEE** = TMB × factor de actividad.
 * - **kcalObjetivo** ajustando el TDEE según el objetivo (volumen/definición/mantenimiento).
 * - **Macros objetivo**: proteína 1,6-2,2 g/kg, grasas 0,8-1,2 g/kg y el resto de calorías
 *   como carbohidratos.
 *
 * Si el perfil está incompleto (faltan datos antropométricos), todas las propiedades
 * calculadas devuelven 0 para evitar cálculos inválidos (protección de nulos).
 *
 * @property id Identificador único del perfil (coincide con el id del usuario autenticado).
 * @property email Correo electrónico asociado al perfil.
 * @property nombre Nombre público del usuario.
 * @property pesoObjetivoKg Peso corporal de referencia en kilogramos (objetivo o actual estimado).
 * @property alturaCm Estatura en centímetros.
 * @property edad Edad en años.
 * @property sexo Sexo biológico de referencia (HOMBRE o MUJER), usado en Mifflin-St Jeor.
 * @property factorActividad Nivel de actividad física (SEDENTARIO, LIGERO, MODERADO, FUERTE).
 * @property objetivo Objetivo nutricional (VOLUMEN, DEFINICION o MANTENIMIENTO).
 * @property fechaNacimiento Fecha de nacimiento en formato epoch millis (opcional).
 */
data class PerfilUsuario(
    val id: String,
    val email: String,
    val nombre: String,
    val pesoObjetivoKg: Double? = null,
    val alturaCm: Double? = null,
    val edad: Int? = null,
    val sexo: String? = null,
    val factorActividad: String? = null,
    val objetivo: String? = null,
    val fechaNacimiento: Long? = null
) {

    companion object {
        // ------------------------------------------------------------------
        // Constantes de actividad física (factores multiplicadores del TDEE).
        // ------------------------------------------------------------------

        /** Factor de actividad para un estilo de vida sedentario (1,2). */
        const val FACTOR_ACTIVIDAD_SEDENTARIO: Double = 1.2

        /** Factor de actividad para un nivel de actividad ligero (1,375). */
        const val FACTOR_ACTIVIDAD_LIGERO: Double = 1.375

        /** Factor de actividad para un nivel de actividad moderado (1,55). */
        const val FACTOR_ACTIVIDAD_MODERADO: Double = 1.55

        /** Factor de actividad para un nivel de actividad fuerte (1,725). */
        const val FACTOR_ACTIVIDAD_FUERTE: Double = 1.725

        // ------------------------------------------------------------------
        // Constantes de objetivos nutricionales.
        // ------------------------------------------------------------------

        /** Objetivo de volumen: +15 % de calorías sobre el TDEE. */
        const val OBJETIVO_VOLUMEN: String = "VOLUMEN"

        /** Objetivo de definición: -20 % de calorías sobre el TDEE. */
        const val OBJETIVO_DEFINICION: String = "DEFINICION"

        /** Objetivo de mantenimiento: las calorías objetivo igualan el TDEE. */
        const val OBJETIVO_MANTENIMIENTO: String = "MANTENIMIENTO"

        // ------------------------------------------------------------------
        // Constantes de sexo y de rango de macronutrientes (Naturvitia).
        // ------------------------------------------------------------------

        /** Valor de sexo masculino para la fórmula de Mifflin-St Jeor. */
        const val SEXO_HOMBRE: String = "HOMBRE"

        /** Valor de sexo femenino para la fórmula de Mifflin-St Jeor. */
        const val SEXO_MUJER: String = "MUJER"

        /** Mínimo de proteína recomendado: 1,6 g por kg de peso. */
        const val PROTEINAS_POR_KG_MIN: Double = 1.6

        /** Máximo de proteína recomendado: 2,2 g por kg de peso. */
        const val PROTEINAS_POR_KG_MAX: Double = 2.2

        /** Valor de referencia de proteína usado por defecto: 2,0 g por kg. */
        const val PROTEINAS_POR_KG_REFERENCIA: Double = 2.0

        /** Mínimo de grasa recomendado: 0,8 g por kg de peso. */
        const val GRASAS_POR_KG_MIN: Double = 0.8

        /** Máximo de grasa recomendado: 1,2 g por kg de peso. */
        const val GRASAS_POR_KG_MAX: Double = 1.2

        /** Valor de referencia de grasa usado por defecto: 1,0 g por kg. */
        const val GRASAS_POR_KG_REFERENCIA: Double = 1.0

        /** Kilocalorías aportadas por cada gramo de proteína. */
        const val KCAL_POR_GR_PROTEINA: Double = 4.0

        /** Kilocalorías aportadas por cada gramo de carbohidrato. */
        const val KCAL_POR_GR_CARBOHIDRATO: Double = 4.0

        /** Kilocalorías aportadas por cada gramo de grasa. */
        const val KCAL_POR_GR_GRASA: Double = 9.0
    }

    /**
     * @brief Indica si el perfil dispone de todos los datos necesarios para
     * calcular el metabolismo y los objetivos nutricionales.
     * @return `true` si peso, altura, edad, sexo, factor de actividad y objetivo
     * están informados; `false` en caso contrario.
     */
    val esCompleto: Boolean
        get() = pesoObjetivoKg != null &&
            alturaCm != null &&
            edad != null &&
            sexo != null &&
            factorActividad != null &&
            objetivo != null

    /**
     * @brief Obtiene el valor numérico del factor de actividad configurado.
     *
     * Mapea la cadena textual del factor de actividad a su multiplicador:
     * SEDENTARIO (1,2), LIGERO (1,375), MODERADO (1,55) y FUERTE (1,725).
     * Si el valor no es reconocido se devuelve el factor sedentario como
     * valor conservador por defecto.
     * @return Factor multiplicador del TDEE (Double).
     */
    fun obtenerFactorActividadValor(): Double = when (factorActividad) {
        "LIGERO" -> FACTOR_ACTIVIDAD_LIGERO
        "MODERADO" -> FACTOR_ACTIVIDAD_MODERADO
        "FUERTE" -> FACTOR_ACTIVIDAD_FUERTE
        else -> FACTOR_ACTIVIDAD_SEDENTARIO
    }

    /**
     * @brief Ajusta las kilocalorías totales (TDEE) según el objetivo marcado.
     *
     * Reglas del método Naturvitia:
     * - **VOLUMEN**: TDEE × 1,15 (+15 %).
     * - **DEFINICION**: TDEE × 0,80 (-20 %).
     * - **MANTENIMIENTO**: TDEE (sin cambios).
     * - Cualquier otro valor: TDEE sin cambios.
     * @param tdeeValor Gasto energético total diario en kilocalorías.
     * @return Kilocalorías objetivo ajustadas por el objetivo del usuario.
     */
    fun ajustarCaloriasPorObjetivo(tdeeValor: Double): Double = when (objetivo) {
        OBJETIVO_VOLUMEN -> tdeeValor * 1.15
        OBJETIVO_DEFINICION -> tdeeValor * 0.80
        else -> tdeeValor
    }

    /**
     * @brief Tasa metabólica basal (TMB) calculada con la fórmula de Mifflin-St Jeor.
     *
     * - Hombres: 10×peso(kg) + 6,25×altura(cm) - 5×edad + 5.
     * - Mujeres: 10×peso(kg) + 6,25×altura(cm) - 5×edad - 161.
     *
     * Si el perfil está incompleto devuelve 0,0. El valor se redondea a entero
     * para evitar decimales innecesarios en la planificación calórica.
     * @return TMB en kilocalorías por día.
     */
    val tmb: Double
        get() {
            if (!esCompleto) return 0.0
            val peso = pesoObjetivoKg ?: return 0.0
            val altura = alturaCm ?: return 0.0
            val edadCalculo = edad ?: return 0.0
            val base = 10.0 * peso + 6.25 * altura - 5.0 * edadCalculo
            val ajuste = if (sexo == SEXO_MUJER) -161.0 else 5.0
            return (base + ajuste).roundToInt().toDouble()
        }

    /**
     * @brief Gasto energético total diario (TDEE) del usuario.
     * Resultado de multiplicar la TMB por el factor de actividad
     * (sedentario, ligero, moderado o fuerte).
     * @return TDEE en kilocalorías por día (0,0 si el perfil está incompleto).
     */
    val tdee: Double
        get() {
            if (!esCompleto) return 0.0
            return (tmb * obtenerFactorActividadValor()).roundToInt().toDouble()
        }

    /**
     * @brief Kilocalorías objetivo diarias según el objetivo del usuario.
     * Parte del TDEE y aplica el ajuste de volumen (+15 %), definición (-20 %)
     * o mantenimiento (sin cambios).
     * @return Kilocalorías objetivo por día (0,0 si el perfil está incompleto).
     */
    val kcalObjetivo: Double
        get() {
            if (!esCompleto) return 0.0
            return ajustarCaloriasPorObjetivo(tdee).roundToInt().toDouble()
        }

    /**
     * @brief Gramos de proteína objetivo al día.
     * Calculado con el valor de referencia de 2,0 g/kg (dentro del rango
     * Naturvitia de 1,6-2,2 g/kg) y redondeado a entero.
     * @return Gramos de proteína diarios (0 si el perfil está incompleto).
     */
    val proteinasObjetivoG: Int
        get() {
            if (!esCompleto) return 0
            return (pesoObjetivoKg!! * PROTEINAS_POR_KG_REFERENCIA).roundToInt()
        }

    /**
     * @brief Gramos de grasa objetivo al día.
     * Calculado con el valor de referencia de 1,0 g/kg (dentro del rango
     * Naturvitia de 0,8-1,2 g/kg) y redondeado a entero.
     * @return Gramos de grasa diarios (0 si el perfil está incompleto).
     */
    val grasasObjetivoG: Int
        get() {
            if (!esCompleto) return 0
            return (pesoObjetivoKg!! * GRASAS_POR_KG_REFERENCIA).roundToInt()
        }

    /**
     * @brief Gramos de carbohidratos objetivo al día.
     * Se calculan como el resto calórico tras descontar proteínas y grasas:
     * CH = (kcalObjetivo - P×4 - G×9) / 4.
     * Si el resultado es negativo (objetivos incompatibles) se limita a 0.
     * @return Gramos de carbohidratos diarios (0 si el perfil está incompleto).
     */
    val carbohidratosObjetivoG: Int
        get() {
            if (!esCompleto) return 0
            val kcalProteina = proteinasObjetivoG * KCAL_POR_GR_PROTEINA
            val kcalGrasa = grasasObjetivoG * KCAL_POR_GR_GRASA
            val kcalRestantes = kcalObjetivo - kcalProteina - kcalGrasa
            val gramos = kcalRestantes / KCAL_POR_GR_CARBOHIDRATO
            return gramos.roundToInt().coerceAtLeast(0)
        }
}