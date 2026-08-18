/**
 * @file MappersNuevos.kt
 * @brief Funciones de extensión para transformar los modelos nuevos entre Room y dominio.
 */
package com.gym.app.data.mapper

import com.gym.app.data.local.entidad.EntidadBloqueRutina
import com.gym.app.data.local.entidad.EntidadEjercicio
import com.gym.app.data.local.entidad.EntidadGimnasio
import com.gym.app.data.local.entidad.EntidadIngestaRegistrada
import com.gym.app.data.local.entidad.EntidadIngredienteToma
import com.gym.app.data.local.entidad.EntidadItemListaCompra
import com.gym.app.data.local.entidad.EntidadListaCompra
import com.gym.app.data.local.entidad.EntidadMaquina
import com.gym.app.data.local.entidad.EntidadMapeoAprendido
import com.gym.app.data.local.entidad.EntidadPerfilUsuario
import com.gym.app.data.local.entidad.EntidadPlanComida
import com.gym.app.data.local.entidad.EntidadRutina
import com.gym.app.data.local.entidad.EntidadSerieRealizada
import com.gym.app.data.local.entidad.EntidadSesionEntrenamiento
import com.gym.app.data.local.entidad.EntidadToma
import com.gym.app.data.remote.dto.DtoSerieRealizadaRemoto
import com.gym.app.domain.model.Alimento
import com.gym.app.domain.model.BloqueRutina
import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.model.Gimnasio
import com.gym.app.domain.model.IngestaRegistrada
import com.gym.app.domain.model.IngredienteToma
import com.gym.app.domain.model.ItemListaCompra
import com.gym.app.domain.model.ListaCompra
import com.gym.app.domain.model.Maquina
import com.gym.app.domain.model.MapeoAprendido
import com.gym.app.domain.model.PerfilUsuario
import com.gym.app.domain.model.PlanComida
import com.gym.app.domain.model.Rutina
import com.gym.app.domain.model.SerieRealizada
import com.gym.app.domain.model.SesionEntrenamiento
import com.gym.app.domain.model.Toma
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/** Separa una lista de strings guardada como texto unido por comas. */
private fun String?.aLista(): List<String> =
    this?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

/** Une una lista de strings en texto separado por comas para Room. */
private fun List<String>.aTexto(): String = joinToString(",")

/** Une una lista de enteros en texto separado por comas para Room. */
private fun List<Int>.aTextoEnteros(): String = joinToString(",")

/** Convierte una fecha epoch millis a LocalDate en la zona horaria del sistema. */
private fun Long.aLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

/** Convierte un LocalDate a epoch millis al inicio del día. */
private fun LocalDate.aEpochMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

// ─── PerfilUsuario ────────────────────────────────────────────────────────────

/** Convierte [EntidadPerfilUsuario] a [PerfilUsuario] de dominio. */
fun EntidadPerfilUsuario.aDominio(): PerfilUsuario = PerfilUsuario(
    id = id,
    email = email,
    nombre = nombre,
    alias = alias,
    pesoObjetivoKg = pesoObjetivoKg,
    alturaCm = alturaCm,
    edad = edad,
    sexo = sexo,
    factorActividad = factorActividad,
    objetivo = objetivo,
    fechaNacimiento = fechaNacimiento
)

/** Convierte [PerfilUsuario] de dominio a [EntidadPerfilUsuario] para Room. */
fun PerfilUsuario.aEntidad(): EntidadPerfilUsuario = EntidadPerfilUsuario(
    id = id,
    email = email,
    nombre = nombre,
    alias = alias,
    pesoObjetivoKg = pesoObjetivoKg,
    alturaCm = alturaCm,
    edad = edad,
    sexo = sexo,
    factorActividad = factorActividad,
    objetivo = objetivo,
    fechaNacimiento = fechaNacimiento
)

// ─── PlanComida / Toma / IngredienteToma ──────────────────────────────────────

/** Convierte un [EntidadPlanComida] con sus tomas e ingredientes a [PlanComida]. */
fun EntidadPlanComida.aDominio(
    tomas: List<EntidadToma>,
    ingredientesPorToma: Map<String, List<EntidadIngredienteToma>>,
    alimentos: Map<String, Alimento> = emptyMap()
): PlanComida = PlanComida(
    id = id,
    nombre = nombre,
    fecha = fecha.aLocalDate(),
    tomas = tomas
        .sortedBy { it.orden }
        .map { toma ->
            Toma(
                id = toma.id,
                tipoIngesta = toma.tipoIngesta,
                orden = toma.orden,
                horaSugerida = toma.horaSugerida,
                ingredientes = (ingredientesPorToma[toma.id] ?: emptyList()).map { ing ->
                    IngredienteToma(
                        id = ing.id,
                        alimentoId = ing.alimentoId,
                        nombre = ing.nombre,
                        cantidadGramos = ing.cantidadGramos,
                        pesaje = ing.pesaje,
                        origenPlan = ing.origenPlan,
                        alimentoResuelto = ing.alimentoId?.let { alimentos[it] }
                    )
                }
            )
        },
    origenImportacion = origenImportacion
)

/** Convierte [PlanComida] de dominio a entidades de cabecera para Room. */
fun PlanComida.aEntidad(userId: String): EntidadPlanComida = EntidadPlanComida(
    id = id,
    userId = userId,
    nombre = nombre,
    fecha = fecha.aEpochMillis(),
    origenImportacion = origenImportacion
)

/** Convierte una [Toma] de dominio a [EntidadToma]. */
fun Toma.aEntidad(planComidaId: String): EntidadToma = EntidadToma(
    id = id,
    planComidaId = planComidaId,
    tipoIngesta = tipoIngesta,
    orden = orden,
    horaSugerida = horaSugerida
)

/** Convierte un [IngredienteToma] de dominio a [EntidadIngredienteToma]. */
fun IngredienteToma.aEntidad(tomaId: String): EntidadIngredienteToma = EntidadIngredienteToma(
    id = id,
    tomaId = tomaId,
    alimentoId = alimentoId,
    nombre = nombre,
    cantidadGramos = cantidadGramos,
    pesaje = pesaje,
    origenPlan = origenPlan
)

// ─── IngestaRegistrada ────────────────────────────────────────────────────────

/** Convierte [EntidadIngestaRegistrada] a [IngestaRegistrada] de dominio. */
fun EntidadIngestaRegistrada.aDominio(): IngestaRegistrada = IngestaRegistrada(
    id = id,
    userId = userId,
    nombre = nombre,
    kcal = kcal,
    proteinasG = proteinasG,
    carbohidratosG = carbohidratosG,
    grasasG = grasasG,
    tipoIngesta = tipoIngesta,
    fecha = fecha.aLocalDate(),
    momentoDia = momentoDia,
    origen = origen
)

/** Convierte [IngestaRegistrada] de dominio a [EntidadIngestaRegistrada] para Room. */
fun IngestaRegistrada.aEntidad(): EntidadIngestaRegistrada = EntidadIngestaRegistrada(
    id = id,
    userId = userId,
    nombre = nombre,
    kcal = kcal,
    proteinasG = proteinasG,
    carbohidratosG = carbohidratosG,
    grasasG = grasasG,
    tipoIngesta = tipoIngesta,
    fecha = fecha.aEpochMillis(),
    momentoDia = momentoDia,
    origen = origen,
    sincronizado = false
)

// ─── ListaCompra ──────────────────────────────────────────────────────────────

/** Convierte una [EntidadListaCompra] con sus items a [ListaCompra]. */
fun EntidadListaCompra.aDominio(items: List<EntidadItemListaCompra>): ListaCompra = ListaCompra(
    id = id,
    semanaInicio = semanaInicio.aLocalDate(),
    items = items.map { it.aDominio() },
    supermercados = items.mapNotNull { it.supermercado }.distinct()
)

/** Convierte [EntidadItemListaCompra] a [ItemListaCompra] de dominio. */
fun EntidadItemListaCompra.aDominio(): ItemListaCompra = ItemListaCompra(
    id = id,
    nombreAlimento = nombreAlimento,
    cantidadGramos = cantidadGramos,
    unidadComercial = unidadComercial,
    cantidadPaquetes = cantidadPaquetes,
    supermercado = supermercado,
    tipoIngestaOrigen = tipoIngestaOrigen.aLista(),
    comprado = comprado
)

/** Convierte [ListaCompra] de dominio a [EntidadListaCompra] para Room. */
fun ListaCompra.aEntidad(userId: String): EntidadListaCompra = EntidadListaCompra(
    id = id,
    userId = userId,
    semanaInicio = semanaInicio.aEpochMillis()
)

/** Convierte [ItemListaCompra] de dominio a [EntidadItemListaCompra]. */
fun ItemListaCompra.aEntidad(listaId: String): EntidadItemListaCompra = EntidadItemListaCompra(
    id = id,
    listaId = listaId,
    nombreAlimento = nombreAlimento,
    cantidadGramos = cantidadGramos,
    unidadComercial = unidadComercial,
    cantidadPaquetes = cantidadPaquetes,
    supermercado = supermercado,
    tipoIngestaOrigen = tipoIngestaOrigen.aTexto(),
    comprado = comprado
)

// ─── Gimnasio / Maquina ───────────────────────────────────────────────────────

/** Convierte un [EntidadGimnasio] con sus máquinas a [Gimnasio]. */
fun EntidadGimnasio.aDominio(maquinas: List<EntidadMaquina>): Gimnasio = Gimnasio(
    id = id,
    nombre = nombre,
    direccion = direccion,
    maquinas = maquinas.map { it.aDominio() }
)

/** Convierte [EntidadMaquina] a [Maquina] de dominio. */
fun EntidadMaquina.aDominio(): Maquina = Maquina(
    id = id,
    nombre = nombre,
    grupoMuscular = grupoMuscular.aLista(),
    tipoEquipamiento = tipoEquipamiento,
    disponible = disponible,
    marca = marca,
    modelo = modelo,
    ejerciciosPosibles = ejerciciosPosibles.aLista(),
    sinonimos = sinonimos.aLista()
)

/** Convierte [Gimnasio] de dominio a [EntidadGimnasio] para Room. */
fun Gimnasio.aEntidad(userId: String): EntidadGimnasio = EntidadGimnasio(
    id = id,
    userId = userId,
    nombre = nombre,
    direccion = direccion
)

/** Convierte [Maquina] de dominio a [EntidadMaquina]. */
fun Maquina.aEntidad(gimnasioId: String): EntidadMaquina = EntidadMaquina(
    id = id,
    gimnasioId = gimnasioId,
    nombre = nombre,
    grupoMuscular = grupoMuscular.aTexto(),
    tipoEquipamiento = tipoEquipamiento,
    disponible = disponible,
    marca = marca,
    modelo = modelo,
    ejerciciosPosibles = ejerciciosPosibles.aTexto(),
    sinonimos = sinonimos.aTexto()
)

// ─── Ejercicio ────────────────────────────────────────────────────────────────

/** Convierte [EntidadEjercicio] a [Ejercicio] de dominio. */
fun EntidadEjercicio.aDominio(): Ejercicio = Ejercicio(
    id = id,
    nombre = nombre,
    grupoMuscularPrincipal = grupoMuscularPrincipal,
    grupoMuscularSecundario = grupoMuscularSecundario,
    maquinaId = maquinaId,
    equipamiento = equipamiento,
    instrucciones = instrucciones
)

/** Convierte [Ejercicio] de dominio a [EntidadEjercicio] para Room. */
fun Ejercicio.aEntidad(userId: String): EntidadEjercicio = EntidadEjercicio(
    id = id,
    userId = userId,
    nombre = nombre,
    grupoMuscularPrincipal = grupoMuscularPrincipal,
    grupoMuscularSecundario = grupoMuscularSecundario,
    maquinaId = maquinaId,
    equipamiento = equipamiento,
    instrucciones = instrucciones
)

// ─── Rutina / BloqueRutina ────────────────────────────────────────────────────

/** Convierte una [EntidadRutina] con sus bloques a [Rutina]. */
fun EntidadRutina.aDominio(bloques: List<EntidadBloqueRutina>): Rutina = Rutina(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    diasSemana = diasSemana.aLista().map { it.toIntOrNull() ?: 0 },
    bloques = bloques.sortedBy { it.serie }.map { it.aDominio() }
)

/** Convierte [EntidadBloqueRutina] a [BloqueRutina] de dominio. */
fun EntidadBloqueRutina.aDominio(): BloqueRutina = BloqueRutina(
    id = id,
    ejercicioId = ejercicioId,
    serie = serie,
    repeticiones = repeticiones,
    pesoKg = pesoKg,
    descansoSegundos = descansoSegundos
)

/** Convierte [Rutina] de dominio a [EntidadRutina] para Room. */
fun Rutina.aEntidad(userId: String): EntidadRutina = EntidadRutina(
    id = id,
    userId = userId,
    nombre = nombre,
    descripcion = descripcion,
    diasSemana = diasSemana.aTextoEnteros()
)

/** Convierte [BloqueRutina] de dominio a [EntidadBloqueRutina]. */
fun BloqueRutina.aEntidad(rutinaId: String): EntidadBloqueRutina = EntidadBloqueRutina(
    id = id,
    rutinaId = rutinaId,
    ejercicioId = ejercicioId,
    serie = serie,
    repeticiones = repeticiones,
    pesoKg = pesoKg,
    descansoSegundos = descansoSegundos
)

// ─── SesionEntrenamiento ──────────────────────────────────────────────────────

/** Convierte [EntidadSesionEntrenamiento] a [SesionEntrenamiento] de dominio. */
fun EntidadSesionEntrenamiento.aDominio(): SesionEntrenamiento = SesionEntrenamiento(
    id = id,
    userId = userId,
    fecha = fecha,
    nombreRutina = nombreRutina,
    ejerciciosCompletados = ejerciciosCompletados.aLista(),
    serieRealizadas = serieRealizadas,
    duracionMinutos = duracionMinutos,
    completo = completo
)

/** Convierte [SesionEntrenamiento] de dominio a [EntidadSesionEntrenamiento] para Room. */
fun SesionEntrenamiento.aEntidad(): EntidadSesionEntrenamiento = EntidadSesionEntrenamiento(
    id = id,
    userId = userId,
    fecha = fecha,
    nombreRutina = nombreRutina,
    ejerciciosCompletados = ejerciciosCompletados.aTexto(),
    serieRealizadas = serieRealizadas,
    duracionMinutos = duracionMinutos,
    completo = completo,
    sincronizado = false
)

// ─── MapeoAprendido ────────────────────────────────────────────────────────────

/** Convierte [EntidadMapeoAprendido] a [MapeoAprendido] de dominio. */
fun EntidadMapeoAprendido.aDominio(): MapeoAprendido = MapeoAprendido(
    nombreNormalizado = nombreNormalizado,
    maquinaId = maquinaId,
    fecha = fecha
)

/** Convierte [MapeoAprendido] de dominio a [EntidadMapeoAprendido] para Room. */
fun MapeoAprendido.aEntidad(): EntidadMapeoAprendido = EntidadMapeoAprendido(
    nombreNormalizado = nombreNormalizado,
    maquinaId = maquinaId,
    fecha = fecha
)

// ─── SerieRealizada ───────────────────────────────────────────────────────────

/** Convierte [EntidadSerieRealizada] a [SerieRealizada] de dominio. */
fun EntidadSerieRealizada.aDominio(): SerieRealizada = SerieRealizada(
    id = id,
    sesionId = sesionId,
    ejercicioId = ejercicioId,
    numeroSerie = numeroSerie,
    pesoKg = pesoKg,
    repeticiones = repeticiones,
    fecha = fecha
)

/** Convierte [SerieRealizada] de dominio a [EntidadSerieRealizada] para Room. */
fun SerieRealizada.aEntidad(): EntidadSerieRealizada = EntidadSerieRealizada(
    id = id,
    sesionId = sesionId,
    ejercicioId = ejercicioId,
    numeroSerie = numeroSerie,
    pesoKg = pesoKg,
    repeticiones = repeticiones,
    fecha = fecha,
    sincronizado = false
)

/** Convierte [EntidadSerieRealizada] a [DtoSerieRealizadaRemoto] para Supabase. */
fun EntidadSerieRealizada.aDtoRemoto(): DtoSerieRealizadaRemoto = DtoSerieRealizadaRemoto(
    id = id,
    sesionId = sesionId,
    ejercicioId = ejercicioId,
    numeroSerie = numeroSerie,
    pesoKg = pesoKg,
    repeticiones = repeticiones,
    fecha = fecha
)
