/**
 * @file PantallaListaCompraGYM.kt
 * @brief Pantalla de Lista de la Compra de la aplicación GYM en Jetpack Compose.
 * Muestra la lista semanal consolidada agrupada por supermercado, permite marcar
 * cada ítem como comprado y ofrece el botón flotante para generar la lista a
 * partir de los planes de comidas de la semana.
 */
package com.gym.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.ItemListaCompra
import com.gym.app.domain.model.ListaCompra
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.AzulSecundario
import com.gym.app.presentation.ui.theme.CianAcento
import com.gym.app.presentation.ui.theme.SuperficieElevada
import com.gym.app.presentation.ui.theme.SuperficieOscura
import com.gym.app.presentation.viewmodel.ListaCompraViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * @data class FilaListaCompraUi
 * @brief Modelo de fila de la lista de la compra para la interfaz.
 * Una fila es un encabezado de supermercado (sin ítem) o un ítem concreto.
 * @property listaId Identificador de la lista a la que pertenece la fila.
 * @property supermercado Nombre del supermercado (solo filas de encabezado).
 * @property item Ítem de compra (solo filas de producto).
 */
private data class FilaListaCompraUi(
    val listaId: String,
    val supermercado: String? = null,
    val item: ItemListaCompra? = null
)

/**
 * @brief Pantalla de Lista de la Compra de la aplicación GYM.
 *
 * Crea el [ListaCompraViewModel] a partir del [ContenedorDependencias] y muestra
 * la lista de la compra más reciente agrupada por supermercado. Cada ítem dispone
 * de una casilla para marcarlo como comprado y el botón flotante genera la lista
 * semanal a partir de los planes de comidas de la semana actual.
 *
 * @param contenedor Contenedor de dependencias de la aplicación.
 */
@Composable
fun PantallaListaCompraGYM(contenedor: ContenedorDependencias) {
    val viewModel: ListaCompraViewModel = viewModel { ListaCompraViewModel(contenedor) }
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::generarListaSemanal,
                containerColor = AzulPrimario,
                contentColor = Color.White
            ) {
                if (estado.generando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.AddShoppingCart,
                        contentDescription = "Generar lista semanal"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(8.dp))
                CabeceraListaCompra()
                Spacer(modifier = Modifier.height(16.dp))
            }

            val lista = estado.listaMasReciente
            if (lista == null) {
                if (!estado.cargando) {
                    EstadoVacioLista()
                }
            } else {
                CabeceraSemana(lista = lista)
                ListaPorSupermercados(lista = lista, onMarcar = viewModel::marcarItem)
            }

            if (!estado.error.isNullOrBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = estado.error!!,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

/**
 * @brief Cabecera de la pantalla de Lista de la Compra.
 */
@Composable
private fun CabeceraListaCompra() {
    Column {
        Text(
            text = "Lista de la compra",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Tu compra semanal consolidada por supermercado",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * @brief Estado vacío mostrado cuando aún no se ha generado ninguna lista.
 */
@Composable
private fun EstadoVacioLista() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = null,
                    tint = AzulPrimario,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Todavía no hay lista de la compra",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pulsa el botón + para generar la lista semanal a partir " +
                    "de tus planes de comidas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * @brief Cabecera con la semana de la lista y el estado de compra.
 * @param lista Lista de la compra a representar.
 */
@Composable
private fun CabeceraSemana(lista: ListaCompra) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Semana del ${formatearSemana(lista)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${lista.items.size} ítems · ${lista.totalItemsPendientes} pendientes",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${lista.items.size - lista.totalItemsPendientes}/${lista.items.size}",
                style = MaterialTheme.typography.titleLarge,
                color = CianAcento,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * @brief Lista de ítems de la compra agrupada por supermercado mediante encabezados.
 * @param lista Lista de la compra a representar.
 * @param onMarcar Acción al marcar o desmarcar un ítem como comprado.
 */
@Composable
private fun ListaPorSupermercados(
    lista: ListaCompra,
    onMarcar: (listaId: String, itemId: String, comprado: Boolean) -> Unit
) {
    val filas = construirFilas(lista)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        items(filas, key = { fila -> claveFila(fila) }) { fila ->
            if (fila.item == null) {
                EncabezadoSupermercado(nombre = fila.supermercado ?: "Otros")
            } else {
                val item = fila.item
                FilaItemCompra(
                    listaId = fila.listaId,
                    item = item,
                    onMarcar = onMarcar
                )
            }
        }
    }
}

/**
 * @brief Encabezado de supermercado dentro de la lista.
 * @param nombre Nombre del supermercado.
 */
@Composable
private fun EncabezadoSupermercado(nombre: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(AzulPrimario, CircleShape)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = nombre,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * @brief Fila con un ítem de la compra y su casilla de marcado.
 * @param listaId Identificador de la lista que contiene el ítem.
 * @param item Ítem de compra a representar.
 * @param onMarcar Acción al cambiar el estado de comprado.
 */
@Composable
private fun FilaItemCompra(
    listaId: String,
    item: ItemListaCompra,
    onMarcar: (listaId: String, itemId: String, comprado: Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.comprado,
                onCheckedChange = { comprado -> onMarcar(listaId, item.id, comprado) },
                colors = CheckboxDefaults.colors(
                    checkedColor = CianAcento,
                    uncheckedColor = AzulSecundario
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombreAlimento,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (item.comprado) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = if (item.comprado) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (item.comprado) TextDecoration.LineThrough else null
                )
                Text(
                    text = cantidadLegible(item),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.comprado) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Comprado",
                    tint = CianAcento,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * @brief Construye las filas de la interfaz agrupando los ítems por supermercado.
 * @param lista Lista de la compra de origen.
 * @return Lista de [FilaListaCompraUi] con encabezados e ítems intercalados.
 */
private fun construirFilas(lista: ListaCompra): List<FilaListaCompraUi> = buildList {
    lista.supermercados.forEach { supermercado ->
        add(FilaListaCompraUi(listaId = lista.id, supermercado = supermercado))
        lista.itemsPorSupermercado(supermercado).forEach { item ->
            add(FilaListaCompraUi(listaId = lista.id, item = item))
        }
    }
    val itemsSinSupermercado = lista.items.filter { it.supermercado == null }
    if (itemsSinSupermercado.isNotEmpty()) {
        add(FilaListaCompraUi(listaId = lista.id, supermercado = "Otros"))
        itemsSinSupermercado.forEach { item ->
            add(FilaListaCompraUi(listaId = lista.id, item = item))
        }
    }
}

/**
 * @brief Clave estable para cada fila de la lista (encabezado o ítem).
 * @param fila Fila de la interfaz.
 * @return Cadena única para el `LazyColumn`.
 */
private fun claveFila(fila: FilaListaCompraUi): String {
    val item = fila.item
    return if (item != null) {
        "item_${fila.listaId}_${item.id}"
    } else {
        "cabecera_${fila.listaId}_${fila.supermercado}"
    }
}

/**
 * @brief Describe la cantidad de un ítem de la compra de forma legible.
 * @param item Ítem de compra.
 * @return Cadena con los paquetes y gramos (p. ej. "2 paquetes · 360 g").
 */
private fun cantidadLegible(item: ItemListaCompra): String {
    val paquetes = if (item.cantidadPaquetes > 0) {
        "${item.cantidadPaquetes} ${item.unidadComercial ?: "unidades"}"
    } else {
        "${redondearGramos(item.cantidadGramos)} g"
    }
    return paquetes
}

/**
 * @brief Redondea una cantidad de gramos a entero para mostrarla de forma compacta.
 * @param gramos Gramos del ítem.
 * @return Valor redondeado al entero más próximo.
 */
private fun redondearGramos(gramos: Double): Int = gramos.roundToInt()

/**
 * @brief Formatea la fecha de inicio de la semana de la lista (p. ej. "10/08/2026").
 * @param lista Lista de la compra.
 * @return Fecha formateada en castellano.
 */
private fun formatearSemana(lista: ListaCompra): String {
    val formateador = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "ES"))
    return lista.semanaInicio.format(formateador)
}