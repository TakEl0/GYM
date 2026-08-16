/**
 * @file PantallaNutricionGYM.kt
 * @brief Pantalla de Nutrición de la aplicación GYM en Jetpack Compose.
 * Muestra el plan de comidas del día (tomas y sus ingredientes), el resumen
 * nutricional planificado frente a consumido con barras de progreso de macros y
 * el botón de rebalanceo intra-día que expone los ajustes propuestos por el
 * motor Naturvitia.
 */
package com.gym.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.AjusteToma
import com.gym.app.domain.model.IngredienteToma
import com.gym.app.domain.model.PlanComida
import com.gym.app.domain.model.ResumenNutricional
import com.gym.app.domain.model.Toma
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.AzulSecundario
import com.gym.app.presentation.ui.theme.CianAcento
import com.gym.app.presentation.ui.theme.SuperficieElevada
import com.gym.app.presentation.ui.theme.SuperficieOscura
import com.gym.app.presentation.viewmodel.NutricionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.round

/**
 * @brief Pantalla de Nutrición de la aplicación GYM.
 *
 * Crea el [NutricionViewModel] a partir del [ContenedorDependencias] y muestra de
 * forma reactiva el plan de comidas de hoy, las ingestas consumidas y el resumen
 * nutricional. Si no existe plan para hoy se muestra un estado vacío con un botón
 * "Importar dieta" (que navega al perfil como marcador temporal). El botón
 * "Rebalancear" invoca el motor de rebalanceo y muestra los ajustes propuestos.
 *
 * @param contenedor Contenedor de dependencias de la aplicación.
 * @param alNavegar Acción de navegación por ruta (p. ej. para el botón
 * "Importar dieta"); por defecto no hace nada.
 */
@Composable
fun PantallaNutricionGYM(
    contenedor: ContenedorDependencias,
    alNavegar: (String) -> Unit = {}
) {
    val viewModel: NutricionViewModel = viewModel { NutricionViewModel(contenedor) }
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            CabeceraNutricion()
            Spacer(modifier = Modifier.height(20.dp))

            val plan = estado.planHoy
            if (plan == null) {
                if (!estado.cargando) {
                    EstadoVacioPlan(onImportarDieta = { alNavegar(DestinoGYM.PERFIL.ruta) })
                }
            } else {
                CabeceraObjetivosDia(plan = plan, resumen = estado.resumen)
                Spacer(modifier = Modifier.height(16.dp))

                estado.resumen?.let { resumen ->
                    SeccionProgresoMacros(resumen = resumen)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "Tomas del día",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Las tomas se ordenan según su orden ordinal del plan (1 = desayuno ...).
                plan.tomas.sortedBy { it.orden }.forEach { toma ->
                    TarjetaToma(
                        toma = toma,
                        consumida = estado.ingestasHoy.any { it.tipoIngesta == toma.tipoIngesta }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                BotonRebalanceo(
                    rebalanceando = estado.rebalanceando,
                    onClick = viewModel::rebalancear
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            estado.ajustesRebalanceo.takeIf { it.isNotEmpty() }?.let { ajustes ->
                ResultadosRebalanceo(
                    ajustes = ajustes,
                    onCerrar = viewModel::descartarAjustes
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!estado.error.isNullOrBlank()) {
                MensajeError(texto = estado.error!!)
                Spacer(modifier = Modifier.height(16.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * @brief Cabecera de la pantalla de Nutrición con la fecha actual.
 */
@Composable
private fun CabeceraNutricion() {
    Column {
        Text(
            text = "Nutrición",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Tu plan y progreso de hoy",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatearFechaNutricion(LocalDate.now()),
            style = MaterialTheme.typography.labelMedium,
            color = AzulSecundario
        )
    }
}

/**
 * @brief Estado vacío mostrado cuando no existe plan de comidas para hoy.
 * @param onImportarDieta Acción del botón "Importar dieta".
 */
@Composable
private fun EstadoVacioPlan(onImportarDieta: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(SuperficieElevada, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.RestaurantMenu,
                    contentDescription = null,
                    tint = AzulPrimario,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay plan de comidas para hoy",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Importa tu dieta desde el perfil para comenzar el seguimiento.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onImportarDieta,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = AzulPrimario
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Importar dieta",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * @brief Cabecera con los objetivos del día (planificados) y los consumidos.
 * @param plan Plan de comidas de hoy (fuente de los objetivos planificados).
 * @param resumen Resumen nutricional calculado (puede ser null en carga).
 */
@Composable
private fun CabeceraObjetivosDia(plan: PlanComida, resumen: ResumenNutricional?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = plan.nombre,
                style = MaterialTheme.typography.labelMedium,
                color = AzulSecundario
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Objetivos del día",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DatoObjetivo(
                    etiqueta = "Planificadas",
                    valor = redondear1(plan.kcalTotales).toString(),
                    unidad = "kcal",
                    modificador = Modifier.weight(1f)
                )
                DatoObjetivo(
                    etiqueta = "Consumidas",
                    valor = redondear1(resumen?.kcalConsumidas ?: 0.0).toString(),
                    unidad = "kcal",
                    modificador = Modifier.weight(1f)
                )
                DatoObjetivo(
                    etiqueta = "Restantes",
                    valor = redondear1(resumen?.kcalRestantes ?: plan.kcalTotales).toString(),
                    unidad = "kcal",
                    modificador = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * @brief Dato numérico individual dentro de la cabecera de objetivos.
 * @param etiqueta Descripción del dato.
 * @param valor Valor numérico formateado.
 * @param unidad Unidad de medida del valor.
 * @param modificador Modificador del contenedor.
 */
@Composable
private fun DatoObjetivo(
    etiqueta: String,
    valor: String,
    unidad: String,
    modificador: Modifier = Modifier
) {
    Column(
        modifier = modificador
            .background(SuperficieElevada, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = unidad,
            style = MaterialTheme.typography.labelSmall,
            color = AzulSecundario
        )
    }
}

/**
 * @brief Tarjeta individual de una toma del plan con sus ingredientes.
 * @param toma Toma a representar.
 * @param consumida Indica si ya existe una ingesta registrada de este tipo.
 */
@Composable
private fun TarjetaToma(toma: Toma, consumida: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tipoIngestaLegible(toma.tipoIngesta),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = toma.horaSugerida?.let { "Hora sugerida: $it" }
                            ?: "${redondear1(toma.kcal)} kcal",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (consumida) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Ingesta consumida",
                            tint = CianAcento,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "Consumida",
                            style = MaterialTheme.typography.labelSmall,
                            color = CianAcento
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            toma.ingredientes.forEach { ingrediente ->
                FilaIngrediente(ingrediente = ingrediente)
            }
        }
    }
}

/**
 * @brief Fila con un ingrediente de la toma (nombre, gramaje y pesaje).
 * @param ingrediente Ingrediente a representar.
 */
@Composable
private fun FilaIngrediente(ingrediente: IngredienteToma) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(AzulSecundario, CircleShape)
        )
        Spacer(modifier = Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ingrediente.nombre,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = pesajeLegible(ingrediente.pesaje),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "${redondear1(ingrediente.cantidadGramos)} g",
            style = MaterialTheme.typography.bodyMedium,
            color = AzulSecundario,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * @brief Sección de progreso de macronutrientes con barras de avance.
 * @param resumen Resumen nutricional calculado del día.
 */
@Composable
private fun SeccionProgresoMacros(resumen: ResumenNutricional) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Progreso de macros",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            BarraMacro(
                etiqueta = "Energía",
                consumido = resumen.kcalConsumidas,
                objetivo = resumen.kcalObjetivo,
                unidad = "kcal",
                colorBarra = AzulPrimario
            )
            Spacer(modifier = Modifier.height(10.dp))
            BarraMacro(
                etiqueta = "Proteínas",
                consumido = resumen.proteinasConsumidasG,
                objetivo = resumen.proteinasObjetivoG,
                unidad = "g",
                colorBarra = CianAcento
            )
            Spacer(modifier = Modifier.height(10.dp))
            BarraMacro(
                etiqueta = "Carbohidratos",
                consumido = resumen.carbohidratosConsumidosG,
                objetivo = resumen.carbohidratosObjetivoG,
                unidad = "g",
                colorBarra = AzulSecundario
            )
            Spacer(modifier = Modifier.height(10.dp))
            BarraMacro(
                etiqueta = "Grasas",
                consumido = resumen.grasasConsumidasG,
                objetivo = resumen.grasasObjetivoG,
                unidad = "g",
                colorBarra = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * @brief Barra de progreso individual de un macronutriente.
 * @param etiqueta Nombre del macronutriente.
 * @param consumido Cantidad consumida.
 * @param objetivo Cantidad objetivo.
 * @param unidad Unidad de medida (kcal o g).
 * @param colorBarra Color de la barra de progreso.
 */
@Composable
private fun BarraMacro(
    etiqueta: String,
    consumido: Double,
    objetivo: Double,
    unidad: String,
    colorBarra: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${redondear1(consumido)} / ${redondear1(objetivo)} $unidad",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progreso(consumido = consumido, objetivo = objetivo),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = colorBarra,
            trackColor = SuperficieElevada
        )
    }
}

/**
 * @brief Botón de rebalanceo intra-día con indicador de progreso.
 * @param rebalanceando Indica si el rebalanceo está en curso.
 * @param onClick Acción de rebalanceo.
 */
@Composable
private fun BotonRebalanceo(rebalanceando: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !rebalanceando,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = AzulPrimario
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (rebalanceando) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Rebalancear comidas pendientes",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * @brief Panel con los ajustes propuestos por el motor de rebalanceo.
 * @param ajustes Lista de [AjusteToma] propuestos.
 * @param onCerrar Acción al cerrar el panel.
 */
@Composable
private fun ResultadosRebalanceo(ajustes: List<AjusteToma>, onCerrar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieElevada)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ajustes propuestos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                androidx.compose.material3.TextButton(onClick = onCerrar) {
                    Text("Cerrar", color = AzulSecundario)
                }
            }
            ajustes.forEach { ajuste ->
                if (ajuste.cambios.isEmpty()) {
                    Text(
                        text = "Tu plan está dentro de la tolerancia. No se requieren cambios.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    ajuste.cambios.forEach { cambio ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = CianAcento,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = cambio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * @brief Mensaje de error genérico mostrado en la pantalla.
 * @param texto Texto del mensaje de error.
 */
@Composable
private fun MensajeError(texto: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

/**
 * @brief Traduce el tipo de ingesta a una etiqueta legible en castellano.
 * @param tipo Tipo de ingesta (constante de [Toma]).
 * @return Etiqueta legible (p. ej. "Desayuno", "Media mañana").
 */
private fun tipoIngestaLegible(tipo: String): String = when (tipo) {
    Toma.TIPO_DESAYUNO -> "Desayuno"
    Toma.TIPO_MEDIA_MAÑANA -> "Media mañana"
    Toma.TIPO_COMIDA -> "Comida"
    Toma.TIPO_MERIENDA -> "Merienda"
    Toma.TIPO_CENA -> "Cena"
    Toma.TIPO_POST_ENTRENO -> "Post-entreno"
    else -> tipo.lowercase(Locale.getDefault())
        .replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
}

/**
 * @brief Traduce el tipo de pesaje a una etiqueta legible en castellano.
 * @param pesaje Valor del pesaje (COCINADO o CRUDO).
 * @return Etiqueta legible (p. ej. "pesado en crudo").
 */
private fun pesajeLegible(pesaje: String): String = when (pesaje) {
    IngredienteToma.PESAJE_COCINADO -> "pesado en cocinado"
    IngredienteToma.PESAJE_CRUDO -> "pesado en crudo"
    else -> pesaje.lowercase(Locale.getDefault())
}

/**
 * @brief Calcula el progreso como fracción 0..1 para las barras de avance.
 * @param consumido Cantidad consumida.
 * @param objetivo Cantidad objetivo.
 * @return Fracción de progreso limitada a 1 (100 %).
 */
private fun progreso(consumido: Double, objetivo: Double): Float {
    if (objetivo <= 0.0) return 0f
    return (consumido / objetivo).toFloat().coerceIn(0f, 1f)
}

/**
 * @brief Redondea un valor numérico a 1 decimal.
 * @param valor Valor a redondear.
 * @return Valor redondeado a 1 decimal.
 */
private fun redondear1(valor: Double): Double = round(valor * 10.0) / 10.0

/**
 * @brief Formatea la fecha en castellano (p. ej. "lunes, 16 de agosto").
 * @param fecha Fecha a formatear.
 * @return Fecha formateada con la primera letra en mayúscula.
 */
private fun formatearFechaNutricion(fecha: LocalDate): String {
    val formateador = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES"))
    val texto = fecha.format(formateador)
    return texto.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
}