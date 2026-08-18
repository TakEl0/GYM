/**
 * @file PantallaResumenSesionGYM.kt
 * @brief Pantalla de resumen de la sesión de entrenamiento en vivo de la aplicación GYM.
 * Muestra el resultado final de la sesión: icono de éxito, duración, volumen total
 * levantado (Σ kg × reps), número de series realizadas y la estimación de 1RM por
 * ejercicio (promedio Epley/Brzycki), junto con el botón de volver a las rutinas.
 */
package com.gym.app.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gym.app.domain.usecase.entrenamiento.EjercicioUnRM
import com.gym.app.domain.usecase.entrenamiento.ResumenSesion
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.AzulSecundario
import com.gym.app.presentation.ui.theme.CianAcento
import com.gym.app.presentation.ui.theme.SuperficieOscura
import kotlin.math.roundToInt

/**
 * @brief Pantalla de resumen de la sesión de entrenamiento en vivo.
 *
 * Muestra el [ResumenSesion] calculado al finalizar la sesión: el volumen total
 * levantado (Σ pesoKg × repeticiones), las series realizadas y la estimación de 1RM
 * por ejercicio, además de la duración de la sesión. El botón "Volver a rutinas"
 * ejecuta [alVolver].
 *
 * @param resumen Resumen estadístico de la sesión finalizada.
 * @param duracionMinutos Duración de la sesión en minutos.
 * @param alVolver Acción de navegación de vuelta a las rutinas.
 */
@Composable
fun PantallaResumenSesionGYM(
    resumen: ResumenSesion,
    duracionMinutos: Int,
    alVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Sesión completada",
            tint = CianAcento,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "¡Sesión completada!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Duración: ${formatearDuracion(duracionMinutos)}",
            style = MaterialTheme.typography.bodyMedium,
            color = AzulSecundario
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Volumen total levantado en la sesión.
        TarjetaEstadisticaResumen(
            etiqueta = "Volumen total",
            valor = "${formatearKg(resumen.volumenTotalKg)} kg",
            descripcion = "Σ peso × repeticiones de todas las series"
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Series realizadas en la sesión.
        TarjetaEstadisticaResumen(
            etiqueta = "Series realizadas",
            valor = resumen.seriesTotales.toString(),
            descripcion = "Series registradas durante la sesión"
        )

        if (resumen.ejerciciosConUnRM.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "1RM estimado por ejercicio",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            resumen.ejerciciosConUnRM.forEach { ejercicio ->
                FilaEjercicioUnRM(ejercicio = ejercicio)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = alVolver,
            colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.FitnessCenter,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Volver a rutinas", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * @brief Tarjeta genérica de estadística del resumen con etiqueta, valor destacado
 * y descripción complementaria.
 * @param etiqueta Etiqueta de la estadística.
 * @param valor Valor principal de la estadística.
 * @param descripcion Descripción complementaria.
 */
@Composable
private fun TarjetaEstadisticaResumen(
    etiqueta: String,
    valor: String,
    descripcion: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                color = CianAcento,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = descripcion,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * @brief Fila con el 1RM estimado de un ejercicio: nombre, mejor serie de la sesión
 * (peso × repeticiones) y la proyección de 1RM con la fórmula Epley/Brzycki.
 * @param ejercicio Estimación de 1RM del ejercicio.
 */
@Composable
private fun FilaEjercicioUnRM(ejercicio: EjercicioUnRM) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = ejercicio.nombre,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${formatearKg(ejercicio.mejorSeriePesoKg)} kg × " +
                    "${ejercicio.mejorSerieReps} → 1RM ≈ ${formatearKg(ejercicio.estimacionUnRM)} kg",
                style = MaterialTheme.typography.labelMedium,
                color = CianAcento
            )
        }
    }
}

/**
 * @brief Formatea la duración de la sesión como `mm:ss`.
 * @param duracionMinutos Duración en minutos.
 * @return Cadena formateada (p. ej. "45:00").
 */
private fun formatearDuracion(duracionMinutos: Int): String =
    "%02d:%02d".format(duracionMinutos, 0)

/**
 * @brief Formatea un peso en kilogramos: entero si no tiene decimales, con un
 * decimal en caso contrario (p. ej. "80" o "112.5").
 * @param valor Peso en kilogramos.
 * @return Cadena formateada del peso.
 */
private fun formatearKg(valor: Double): String {
    val redondeado = (valor * 10.0).roundToInt() / 10.0
    return if (redondeado % 1.0 == 0.0) {
        redondeado.roundToInt().toString()
    } else {
        redondeado.toString()
    }
}