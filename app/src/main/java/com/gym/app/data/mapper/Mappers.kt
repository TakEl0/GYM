/**
 * @file Mappers.kt
 * @brief Funciones de extensión para transformar modelos entre base de datos (Room), DTOs remotos y dominio.
 */
package com.gym.app.data.mapper

import com.gym.app.data.local.entidad.EntidadComida
import com.gym.app.data.local.entidad.EntidadEntrenamiento
import com.gym.app.data.local.entidad.EntidadRegistroPeso
import com.gym.app.data.remote.dto.DtoComidaRemoto
import com.gym.app.data.remote.dto.DtoEntrenamientoRemoto
import com.gym.app.data.remote.dto.DtoRegistroPesoRemoto
import com.gym.app.domain.model.Comida
import com.gym.app.domain.model.Entrenamiento
import com.gym.app.domain.model.RegistroPeso
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/**
 * @brief Convierte [EntidadRegistroPeso] a [RegistroPeso] de dominio.
 */
fun EntidadRegistroPeso.aDominio(): RegistroPeso = RegistroPeso(
    fecha = Instant.ofEpochMilli(fecha).atZone(ZoneId.systemDefault()).toLocalDate(),
    pesoKg = pesoKg,
    grasaCorporalPorcentaje = grasaCorporal
)

/**
 * @brief Convierte [RegistroPeso] de dominio a [EntidadRegistroPeso] para Room.
 */
fun RegistroPeso.aEntidad(userId: String, id: String = UUID.randomUUID().toString()): EntidadRegistroPeso = EntidadRegistroPeso(
    id = id,
    userId = userId,
    pesoKg = pesoKg,
    grasaCorporal = grasaCorporalPorcentaje,
    fecha = fecha.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    sincronizado = false
)

/**
 * @brief Convierte [EntidadRegistroPeso] a [DtoRegistroPesoRemoto].
 */
fun EntidadRegistroPeso.aDtoRemoto(): DtoRegistroPesoRemoto = DtoRegistroPesoRemoto(
    id = id,
    userId = userId,
    pesoKg = pesoKg,
    grasaCorporal = grasaCorporal,
    fecha = fecha
)

/**
 * @brief Convierte [DtoRegistroPesoRemoto] a [EntidadRegistroPeso].
 */
fun DtoRegistroPesoRemoto.aEntidad(sincronizado: Boolean = true): EntidadRegistroPeso = EntidadRegistroPeso(
    id = id,
    userId = userId,
    pesoKg = pesoKg,
    grasaCorporal = grasaCorporal,
    fecha = fecha,
    sincronizado = sincronizado
)

/**
 * @brief Convierte [EntidadEntrenamiento] a [Entrenamiento] de dominio.
 */
fun EntidadEntrenamiento.aDominio(): Entrenamiento = Entrenamiento(
    id = id,
    nombre = nombre,
    grupoMuscular = grupoMuscular,
    seriesTotales = seriesTotales,
    ejerciciosRealizados = ejerciciosRealizados,
    totalEjercicios = totalEjercicios,
    duracionMinutos = duracionMinutos,
    completo = completo,
    fecha = fecha
)

/**
 * @brief Convierte [Entrenamiento] de dominio a [EntidadEntrenamiento] para Room.
 */
fun Entrenamiento.aEntidad(userId: String, sincronizado: Boolean = false): EntidadEntrenamiento = EntidadEntrenamiento(
    id = id,
    userId = userId,
    nombre = nombre,
    grupoMuscular = grupoMuscular,
    seriesTotales = seriesTotales,
    ejerciciosRealizados = ejerciciosRealizados,
    totalEjercicios = totalEjercicios,
    duracionMinutos = duracionMinutos,
    completo = completo,
    fecha = fecha,
    sincronizado = sincronizado
)

/**
 * @brief Convierte [EntidadEntrenamiento] a [DtoEntrenamientoRemoto].
 */
fun EntidadEntrenamiento.aDtoRemoto(): DtoEntrenamientoRemoto = DtoEntrenamientoRemoto(
    id = id,
    userId = userId,
    nombre = nombre,
    grupoMuscular = grupoMuscular,
    seriesTotales = seriesTotales,
    ejerciciosRealizados = ejerciciosRealizados,
    totalEjercicios = totalEjercicios,
    duracionMinutos = duracionMinutos,
    completo = completo,
    fecha = fecha
)

/**
 * @brief Convierte [DtoEntrenamientoRemoto] a [EntidadEntrenamiento].
 */
fun DtoEntrenamientoRemoto.aEntidad(sincronizado: Boolean = true): EntidadEntrenamiento = EntidadEntrenamiento(
    id = id,
    userId = userId,
    nombre = nombre,
    grupoMuscular = grupoMuscular,
    seriesTotales = seriesTotales,
    ejerciciosRealizados = ejerciciosRealizados,
    totalEjercicios = totalEjercicios,
    duracionMinutos = duracionMinutos,
    completo = completo,
    fecha = fecha,
    sincronizado = sincronizado
)

/**
 * @brief Convierte [EntidadComida] a [Comida] de dominio.
 */
fun EntidadComida.aDominio(): Comida = Comida(
    id = id,
    nombre = nombre,
    kcal = kcal,
    proteinasG = proteinasG,
    carbohidratosG = carbohidratosG,
    grasasG = grasasG,
    tipoIngesta = tipoIngesta,
    fecha = Instant.ofEpochMilli(fecha).atZone(ZoneId.systemDefault()).toLocalDate()
)

/**
 * @brief Convierte [Comida] de dominio a [EntidadComida] para Room.
 */
fun Comida.aEntidad(userId: String, sincronizado: Boolean = false): EntidadComida = EntidadComida(
    id = id,
    userId = userId,
    nombre = nombre,
    kcal = kcal,
    proteinasG = proteinasG,
    carbohidratosG = carbohidratosG,
    grasasG = grasasG,
    tipoIngesta = tipoIngesta,
    fecha = fecha.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    sincronizado = sincronizado
)

/**
 * @brief Convierte [EntidadComida] a [DtoComidaRemoto].
 */
fun EntidadComida.aDtoRemoto(): DtoComidaRemoto = DtoComidaRemoto(
    id = id,
    userId = userId,
    nombre = nombre,
    kcal = kcal,
    proteinasG = proteinasG,
    carbohidratosG = carbohidratosG,
    grasasG = grasasG,
    tipoIngesta = tipoIngesta,
    fecha = fecha
)

/**
 * @brief Convierte [DtoComidaRemoto] a [EntidadComida].
 */
fun DtoComidaRemoto.aEntidad(sincronizado: Boolean = true): EntidadComida = EntidadComida(
    id = id,
    userId = userId,
    nombre = nombre,
    kcal = kcal,
    proteinasG = proteinasG,
    carbohidratosG = carbohidratosG,
    grasasG = grasasG,
    tipoIngesta = tipoIngesta,
    fecha = fecha,
    sincronizado = sincronizado
)
