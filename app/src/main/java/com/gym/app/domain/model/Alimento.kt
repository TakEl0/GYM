/**
 * @file Alimento.kt
 * @brief Modelo de dominio que representa un alimento del catálogo Naturvitia.
 *
 * Esta entidad pertenece a la capa de dominio y describe la composición nutricional
 * de un alimento por cada 100 gramos, así como su información comercial (unidad de
 * venta, supermercado preferido) necesaria para consolidar la lista de la compra.
 */
package com.gym.app.domain.model

/**
 * @class Alimento
 * @brief Representa un alimento con su tabla nutricional por 100 gramos.
 *
 * Los alimentos marcados como `ingredienteFijo` son aquellos definidos de forma
 * inamovible por el plan nutricional (p. ej. "pechuga de pollo", "arroz blanco
 * cocinado") y no pueden eliminarse de una toma al rebalancear el día.
 *
 * @property id Identificador único del alimento.
 * @property nombre Nombre descriptivo del alimento (p. ej. "Pechuga de pollo").
 * @property kcalPor100g Kilocalorías por cada 100 gramos de alimento.
 * @property proteinasPor100g Gramos de proteína por cada 100 gramos.
 * @property carbohidratosPor100g Gramos de carbohidratos por cada 100 gramos.
 * @property grasasPor100g Gramos de grasas por cada 100 gramos.
 * @property unidadComercial Unidad de venta en el supermercado (p. ej. "paquete", "lata", "bandeja").
 * @property gramosPorUnidadComercial Gramos aproximados que contiene cada unidad comercial (opcional).
 * @property supermercadoPreferido Supermercado donde el usuario suele adquirir este alimento (opcional).
 * @property ingredienteFijo Indica si el alimento es fijo dentro del plan (true) o sustituible (false).
 */
data class Alimento(
    val id: String,
    val nombre: String,
    val kcalPor100g: Double,
    val proteinasPor100g: Double,
    val carbohidratosPor100g: Double,
    val grasasPor100g: Double,
    val unidadComercial: String? = null,
    val gramosPorUnidadComercial: Double? = null,
    val supermercadoPreferido: String? = null,
    val ingredienteFijo: Boolean = false
)