/**
 * @file GenerarListaCompraSemanalCasoUso.kt
 * @brief Caso de uso de generación de la lista de la compra semanal consolidada.
 */
package com.gym.app.domain.usecase.compra

import com.gym.app.domain.model.Alimento
import com.gym.app.domain.model.ItemListaCompra
import com.gym.app.domain.model.ListaCompra
import com.gym.app.domain.model.PlanComida
import java.time.LocalDate
import java.util.UUID
import kotlin.math.ceil

/**
 * @class GenerarListaCompraSemanalCasoUso
 * @brief Consolida la lista de la compra de una semana a partir de los planes de
 * comidas diarios, siguiendo las reglas del método Naturvitia:
 *
 * 1. Para cada toma de cada plan y para cada [com.gym.app.domain.model.IngredienteToma]
 *    se acumulan los gramos semanales por nombre de ingrediente.
 * 2. Por ingrediente, si existe una unidad comercial conocida (mapa interno de
 *    alimentos fijos Naturvitia), se calcula el número de paquetes con redondeo
 *    hacia arriba: `cantidadPaquetes = ceil(gramos / gramosPorUnidad)`.
 * 3. El supermercado se resuelve con la prioridad: preferido del usuario (mapa de
 *    entrada) > supermercado del alimento conocido > "Mercadona".
 * 4. La lista devuelve también la lista de supermercados distintos implicados.
 *
 * La semana de la lista toma como fecha de inicio la del primer plan recibido
 * (se asume que corresponde al lunes de la semana). El caso de uso es de lógica
 * pura: no depende de repositorios ni de frameworks.
 */
class GenerarListaCompraSemanalCasoUso {

    /**
     * Acumulador interno de gramos y tipos de ingesta de origen por ingrediente.
     * @property gramos Gramos totales consolidados de la semana.
     * @property tiposIngesta Tipos de ingesta de los que proviene el ingrediente.
     */
    private data class Acumulado(
        var gramos: Double,
        val tiposIngesta: MutableSet<String>
    )

    /**
     * Catálogo interno de alimentos fijos Naturvitia con su unidad comercial,
     * gramos por unidad y supermercado preferido. La coincidencia con el nombre
     * del ingrediente del plan se realiza mediante la clave indicada.
     * @property clave Palabra clave para el emparejamiento (insensible a mayúsculas).
     * @property alimento Alimento del catálogo con sus datos comerciales.
     */
    private data class AlimentoComercial(val clave: String, val alimento: Alimento)

    /** Mapa interno de los alimentos fijos del método Naturvitia. */
    private val alimentosComerciales: List<AlimentoComercial> = listOf(
        AlimentoComercial(
            clave = "panecillo",
            alimento = Alimento(
                id = "alimento_panecillos_integrales",
                nombre = "Panecillos integrales finos",
                kcalPor100g = 270.0,
                proteinasPor100g = 9.0,
                carbohidratosPor100g = 50.0,
                grasasPor100g = 4.0,
                unidadComercial = "paquete",
                gramosPorUnidadComercial = 180.0,
                supermercadoPreferido = "Mercadona"
            )
        ),
        AlimentoComercial(
            clave = "kebab",
            alimento = Alimento(
                id = "alimento_carne_kebab_pollo",
                nombre = "Carne kebab de pollo congelada",
                kcalPor100g = 190.0,
                proteinasPor100g = 12.0,
                carbohidratosPor100g = 5.0,
                grasasPor100g = 13.0,
                unidadComercial = "bolsa",
                gramosPorUnidadComercial = 400.0,
                supermercadoPreferido = "Mercadona"
            )
        ),
        AlimentoComercial(
            clave = "tortilla de maíz",
            alimento = Alimento(
                id = "alimento_tortillas_maiz",
                nombre = "Tortillas de maíz",
                kcalPor100g = 360.0,
                proteinasPor100g = 8.0,
                carbohidratosPor100g = 75.0,
                grasasPor100g = 4.0,
                unidadComercial = "paquete",
                gramosPorUnidadComercial = 250.0,
                supermercadoPreferido = "Mercadona"
            )
        ),
        AlimentoComercial(
            clave = "evowhey",
            alimento = Alimento(
                id = "alimento_evowhey_protein",
                nombre = "EvoWhey Protein",
                kcalPor100g = 380.0,
                proteinasPor100g = 75.0,
                carbohidratosPor100g = 8.0,
                grasasPor100g = 6.0,
                unidadComercial = "tarro",
                gramosPorUnidadComercial = 1000.0,
                supermercadoPreferido = null
            )
        ),
        AlimentoComercial(
            clave = "havarti",
            alimento = Alimento(
                id = "alimento_queso_havarti_light",
                nombre = "Queso Havarti light",
                kcalPor100g = 210.0,
                proteinasPor100g = 25.0,
                carbohidratosPor100g = 3.0,
                grasasPor100g = 11.0,
                unidadComercial = "pack",
                gramosPorUnidadComercial = 300.0,
                supermercadoPreferido = null
            )
        ),
        AlimentoComercial(
            clave = "aove",
            alimento = Alimento(
                id = "alimento_aove",
                nombre = "AOVE",
                kcalPor100g = 900.0,
                proteinasPor100g = 0.0,
                carbohidratosPor100g = 0.0,
                grasasPor100g = 100.0,
                unidadComercial = "botella",
                gramosPorUnidadComercial = 750.0,
                supermercadoPreferido = null
            )
        )
    )

    /**
     * @brief Genera la lista de la compra semanal consolidada.
     * @param planesSemana Planes de comidas de los días de la semana (lunes a domingo).
     * @param supermercadosPreferidos Mapa opcional de supermercados preferidos por
     * el usuario, clave = nombre del ingrediente y valor = supermercado.
     * @return [Result] con la [ListaCompra] consolidada de la semana. Si no se
     * recibe ningún plan, se devuelve una lista vacía con la semana actual.
     */
    suspend fun ejecutar(
        planesSemana: List<PlanComida>,
        supermercadosPreferidos: Map<String, String> = emptyMap()
    ): Result<ListaCompra> {
        val semanaInicio = planesSemana.firstOrNull()?.fecha ?: LocalDate.now()

        if (planesSemana.isEmpty()) {
            return Result.success(
                ListaCompra(
                    id = UUID.randomUUID().toString(),
                    semanaInicio = semanaInicio,
                    items = emptyList(),
                    supermercados = emptyList()
                )
            )
        }

        // Paso 1: acumulación de gramos por nombre de ingrediente.
        val acumulados = LinkedHashMap<String, Acumulado>()
        for (plan in planesSemana) {
            for (toma in plan.tomas) {
                for (ingrediente in toma.ingredientes) {
                    val acumulado = acumulados.getOrPut(ingrediente.nombre) {
                        Acumulado(gramos = 0.0, tiposIngesta = mutableSetOf())
                    }
                    acumulado.gramos += ingrediente.cantidadGramos
                    acumulado.tiposIngesta.add(toma.tipoIngesta)
                }
            }
        }

        // Paso 2 y 3: escalado a paquetes comerciales y resolución de supermercado.
        val items = acumulados.map { (nombre, acumulado) ->
            val alimentoComercial = alimentosComerciales.firstOrNull { comercio ->
                nombre.contains(comercio.clave, ignoreCase = true)
            }
            val alimento = alimentoComercial?.alimento
            val supermercado = supermercadosPreferidos[nombre]
                ?: alimento?.supermercadoPreferido
                ?: "Mercadona"

            if (alimento != null && alimento.gramosPorUnidadComercial != null) {
                val cantidadPaquetes = ceil(
                    acumulado.gramos / alimento.gramosPorUnidadComercial
                ).toInt()
                ItemListaCompra(
                    id = UUID.randomUUID().toString(),
                    nombreAlimento = nombre,
                    cantidadGramos = acumulado.gramos,
                    unidadComercial = alimento.unidadComercial,
                    cantidadPaquetes = cantidadPaquetes,
                    supermercado = supermercado,
                    tipoIngestaOrigen = acumulado.tiposIngesta.toList()
                )
            } else {
                // Sin unidad comercial conocida: se mantienen los gramos planificados.
                ItemListaCompra(
                    id = UUID.randomUUID().toString(),
                    nombreAlimento = nombre,
                    cantidadGramos = acumulado.gramos,
                    unidadComercial = null,
                    cantidadPaquetes = 0,
                    supermercado = supermercado,
                    tipoIngestaOrigen = acumulado.tiposIngesta.toList()
                )
            }
        }

        // Paso 4: supermercados distintos implicados en la lista.
        val supermercados = items
            .mapNotNull { it.supermercado }
            .distinct()

        return Result.success(
            ListaCompra(
                id = UUID.randomUUID().toString(),
                semanaInicio = semanaInicio,
                items = items,
                supermercados = supermercados
            )
        )
    }
}