/**
 * @file EntidadMapeoAprendido.kt
 * @brief Entidad Room que representa un mapeo ejercicio → máquina aprendido.
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @class EntidadMapeoAprendido
 * @brief Almacena en la base de datos local las correcciones manuales del usuario sobre
 * el mapeo de ejercicios del plan PDF a máquinas del gimnasio.
 *
 * La clave primaria es el nombre del ejercicio ya normalizado: si el usuario corrige el
 * mismo ejercicio varias veces, la última corrección reemplaza a la anterior
 * (OnConflictStrategy.REPLACE en el DAO).
 *
 * @property nombreNormalizado Nombre del ejercicio normalizado (clave primaria).
 * @property maquinaId Identificador de la máquina elegida por el usuario.
 * @property fecha Fecha de la corrección en formato epoch millis.
 */
@Entity(tableName = "mapeos_aprendidos")
data class EntidadMapeoAprendido(
    @PrimaryKey
    val nombreNormalizado: String,
    val maquinaId: String,
    val fecha: Long
)