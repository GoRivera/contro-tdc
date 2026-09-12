package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreditCard
import com.example.data.model.FuelEntry
import com.example.domain.CreditCardCalculator
import com.example.ui.components.FuelEfficiencyTrendChart
import com.example.ui.util.rememberPrivacyCurrencyFormat
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableLongStateOf
import java.util.Calendar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FuelTimeFilter(val label: String, val days: Int) {
    LAST_15_DAYS("Últimos 15 días", 15),
    LAST_30_DAYS("Últimos 30 días", 30),
    CUSTOM("Personalizado", -1),
    ALL_TIME("Todas las cargas", 0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelScreen(
    fuelEntries: List<FuelEntry>,
    cards: List<CreditCard>,
    onAddFuelEntry: (
        cardId: Long,
        km: Double,
        fuelType: String,
        pricePerLiter: Double,
        liters: Double,
        isDivided: Boolean,
        dividedWith: String,
        dividedCount: Int,
        notes: String,
        dateMillis: Long
    ) -> Unit,
    onDeleteFuelEntry: (FuelEntry) -> Unit,
    onUpdateFuelEntry: (
        entry: FuelEntry,
        cardId: Long,
        km: Double,
        fuelType: String,
        pricePerLiter: Double,
        liters: Double,
        isDivided: Boolean,
        dividedWith: String,
        dividedCount: Int,
        notes: String,
        dateMillis: Long
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val currencyFormat = rememberPrivacyCurrencyFormat()
    val dateFormat = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "MX"))
    val shortDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX"))

    var selectedFilter by remember { mutableStateOf(FuelTimeFilter.LAST_30_DAYS) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedEntryForDetail by remember { mutableStateOf<FuelEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<FuelEntry?>(null) }
    var entryToEdit by remember { mutableStateOf<FuelEntry?>(null) }

    val nowMillis = System.currentTimeMillis()
    var customStartDateMillis by remember { mutableLongStateOf(nowMillis - (30L * 24 * 60 * 60 * 1000)) }
    var customEndDateMillis by remember { mutableLongStateOf(nowMillis) }

    val filteredEntries = remember(fuelEntries, selectedFilter, customStartDateMillis, customEndDateMillis) {
        when (selectedFilter) {
            FuelTimeFilter.LAST_15_DAYS -> {
                val cutoff = nowMillis - (15L * 24 * 60 * 60 * 1000)
                fuelEntries.filter { it.dateMillis >= cutoff }
            }
            FuelTimeFilter.LAST_30_DAYS -> {
                val cutoff = nowMillis - (30L * 24 * 60 * 60 * 1000)
                fuelEntries.filter { it.dateMillis >= cutoff }
            }
            FuelTimeFilter.CUSTOM -> {
                val startCal = Calendar.getInstance().apply {
                    timeInMillis = customStartDateMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance().apply {
                    timeInMillis = customEndDateMillis
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                fuelEntries.filter { it.dateMillis in startCal.timeInMillis..endCal.timeInMillis }
            }
            FuelTimeFilter.ALL_TIME -> fuelEntries
        }
    }

    // Cálculos de Resumen
    val totalCost = filteredEntries.sumOf { it.totalCost }
    val totalLiters = filteredEntries.sumOf { it.litersLoaded }
    val totalKm = filteredEntries.sumOf { it.kmDriven }
    val avgEfficiency = if (totalLiters > 0.0) totalKm / totalLiters else 0.0
    val avgPricePerLiter = if (totalLiters > 0.0) totalCost / totalLiters else 0.0
    val avgCostPerKm = if (totalKm > 0.0) totalCost / totalKm else 0.0

    // Fase 3: Puntos para gráfico de tendencia de rendimiento
    val validFuelTrendPoints = remember(filteredEntries) {
        filteredEntries
            .filter { it.kmDriven > 0.0 && it.litersLoaded > 0.0 }
            .sortedBy { it.dateMillis }
            .map { entry ->
                val kmPerL = entry.kmDriven / entry.litersLoaded
                val label = shortDateFormat.format(Date(entry.dateMillis))
                label to kmPerL
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("fuel_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado con Botón para Agregar Carga
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rendimiento y Cargas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Control exacto de combustible y gastos de viaje",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_add_fuel_entry")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nueva Carga", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Filtro de 15 días, 30 días, personalizado o todas
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FuelTimeFilter.values().forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.label, fontSize = 12.sp, fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (selectedFilter == filter) {
                                { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                if (selectedFilter == FuelTimeFilter.CUSTOM) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth().testTag("custom_date_filter_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Fecha Inicio
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val cal = Calendar.getInstance().apply { timeInMillis = customStartDateMillis }
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                val sel = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }
                                                customStartDateMillis = sel.timeInMillis
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text("Desde:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(shortDateFormat.format(Date(customStartDateMillis)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Fecha Fin
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val cal = Calendar.getInstance().apply { timeInMillis = customEndDateMillis }
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                val sel = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59) }
                                                customEndDateMillis = sel.timeInMillis
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text("Hasta:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(shortDateFormat.format(Date(customEndDateMillis)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tarjeta de Resumen / KPIs de las cargas filtradas
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RESUMEN • ${selectedFilter.label.uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "${filteredEntries.size} ${if (filteredEntries.size == 1) "carga" else "cargas"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // KPI 1: Rendimiento promedio
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Rendimiento",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.2f", avgEfficiency)} km/l",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // KPI 2: Precio promedio
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Precio Promedio",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${currencyFormat.format(avgPricePerLiter)}/L",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // KPI 3: Gasto Total
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gasto Total",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currencyFormat.format(totalCost),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // KPI 4: Litros Totales
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Litros Totales",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.3f", totalLiters)} L",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // KPI 5: Costo por Kilómetro
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Costo por Km",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${currencyFormat.format(avgCostPerKm)}/km",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        // KPI 6: Distancia Total
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Km Recorridos",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", totalKm)} km",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Fase 3: Gráfico de Tendencia de Rendimiento (km/L)
        if (validFuelTrendPoints.size >= 2) {
            item {
                FuelEfficiencyTrendChart(
                    points = validFuelTrendPoints,
                    averageKmPerLiter = avgEfficiency
                )
            }
        }

        // Título de la lista de transacciones
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial de Cargas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Toca para ver detalle",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Requisito del usuario:
        // "En la pantalla de resumen con esas transacciones, se mostrará solamente el rendimiento y el precio por litro de la carga. Al tocarla, se podrá ver el detalle completo."
        if (filteredEntries.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Sin cargas en este periodo",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Presiona 'Nueva Carga' para registrar un gasto de gasolina.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredEntries, key = { it.id }) { entry ->
                val card = cards.firstOrNull { it.id == entry.cardId }
                val isPremium = entry.fuelType.contains("Premium", ignoreCase = true) || entry.fuelType.contains("Roja", ignoreCase = true)

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedEntryForDetail = entry }
                        .testTag("fuel_entry_item_${entry.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lado Izquierdo: Rendimiento
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Indicador de combustible (Rojo para Premium, Verde para Regular)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isPremium) Color(0xFFD32F2F).copy(alpha = 0.12f) else Color(0xFF2E7D32).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = if (isPremium) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "RENDIMIENTO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", entry.efficiencyKmPerL)} km/l",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = shortDateFormat.format(Date(entry.dateMillis)),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = " • ",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${currencyFormat.format(entry.costPerKm)}/km",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }

                        // Lado Derecho: Precio por litro y Acciones
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "PRECIO POR LITRO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${currencyFormat.format(entry.pricePerLiter)} / L",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPremium) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isPremium) Color(0xFFD32F2F).copy(alpha = 0.15f) else Color(0xFF2E7D32).copy(alpha = 0.15f),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = if (isPremium) "Premium (Roja)" else "Regular (Verde)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPremium) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { entryToEdit = entry },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("btn_edit_fuel_${entry.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar carga",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Modal de Detalle Completo de una carga
    selectedEntryForDetail?.let { entry ->
        val card = cards.firstOrNull { it.id == entry.cardId }
        val isPremium = entry.fuelType.contains("Premium", ignoreCase = true) || entry.fuelType.contains("Roja", ignoreCase = true)

        AlertDialog(
            onDismissRequest = { selectedEntryForDetail = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isPremium) Color(0xFFD32F2F).copy(alpha = 0.15f) else Color(0xFF2E7D32).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = if (isPremium) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Detalle de Carga de Gasolina",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Resumen Rendimiento y Precio
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Rendimiento", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "${String.format(Locale.US, "%.2f", entry.efficiencyKmPerL)} km/l",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Precio por litro", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "${currencyFormat.format(entry.pricePerLiter)} / L",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPremium) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    // Datos exactos de la carga
                    DetailRow(
                        label = "Tipo de combustible",
                        value = entry.fuelType,
                        color = if (isPremium) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                    )
                    DetailRow(
                        label = "Litros cargados (5 decimales)",
                        value = "${String.format(Locale.US, "%.5f", entry.litersLoaded)} L"
                    )
                    DetailRow(
                        label = "Kilometraje recorrido",
                        value = "${String.format(Locale.US, "%.1f", entry.kmDriven)} km"
                    )
                    DetailRow(
                        label = "Costo total de la carga",
                        value = currencyFormat.format(entry.totalCost),
                        isHighlight = true
                    )
                    DetailRow(
                        label = "Costo por kilómetro",
                        value = "${currencyFormat.format(entry.costPerKm)} / km"
                    )
                    DetailRow(
                        label = "Tarjeta utilizada",
                        value = if (card != null) "${card.name} (••${card.last4Digits} - ${card.bank})" else "Tarjeta eliminada"
                    )
                    DetailRow(
                        label = "Modalidad del gasto",
                        value = if (entry.isDivided) "Dividido (${if (entry.dividedWith.isNotBlank()) entry.dividedWith else "Compartido"})" else "Meramente Personal"
                    )
                    if (entry.isDivided && entry.personalShare > 0.0) {
                        DetailRow(
                            label = "Tu aportación personal",
                            value = currencyFormat.format(entry.personalShare)
                        )
                    }
                    DetailRow(
                        label = "Fecha de carga",
                        value = dateFormat.format(Date(entry.dateMillis))
                    )
                    if (entry.notes.isNotBlank()) {
                        DetailRow(label = "Notas", value = entry.notes)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEntryForDetail = null }) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = {
                            val toEdit = selectedEntryForDetail
                            selectedEntryForDetail = null
                            entryToEdit = toEdit
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar")
                    }

                    val isEditable = (System.currentTimeMillis() - entry.dateMillis) <= (60L * 24 * 60 * 60 * 1000)
                    if (isEditable) {
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(
                            onClick = {
                                val toDel = selectedEntryForDetail
                                selectedEntryForDetail = null
                                if (toDel != null) {
                                    entryToDelete = toDel
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar")
                        }
                    }
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Diálogo de confirmación para eliminar carga
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Eliminar carga de gasolina?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Esta acción eliminará el registro de combustible y el cargo correspondiente de ${currencyFormat.format(entry.totalCost)} en la tarjeta.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFuelEntry(entry)
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de Editar Carga
    entryToEdit?.let { entry ->
        AddFuelEntryDialog(
            cards = cards,
            entryToEdit = entry,
            onDismiss = { entryToEdit = null },
            onSave = { cardId, km, fuelType, price, liters, isDivided, dividedWith, dividedCount, notes, dateMillis ->
                onUpdateFuelEntry(entry, cardId, km, fuelType, price, liters, isDivided, dividedWith, dividedCount, notes, dateMillis)
                entryToEdit = null
            }
        )
    }

    // Diálogo de Nueva Carga
    if (showAddDialog) {
        AddFuelEntryDialog(
            cards = cards,
            entryToEdit = null,
            onDismiss = { showAddDialog = false },
            onSave = { cardId, km, fuelType, price, liters, isDivided, dividedWith, dividedCount, notes, dateMillis ->
                onAddFuelEntry(cardId, km, fuelType, price, liters, isDivided, dividedWith, dividedCount, notes, dateMillis)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    color: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = if (isHighlight) 14.sp else 12.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = color ?: (if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFuelEntryDialog(
    cards: List<CreditCard>,
    entryToEdit: FuelEntry? = null,
    onDismiss: () -> Unit,
    onSave: (cardId: Long, km: Double, fuelType: String, pricePerLiter: Double, liters: Double, isDivided: Boolean, dividedWith: String, dividedCount: Int, notes: String, dateMillis: Long) -> Unit
) {
    val context = LocalContext.current
    val currencyFormat = rememberPrivacyCurrencyFormat()
    val fullDateFormat = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "MX"))
    val isDark = isSystemInDarkTheme()

    // Requisito 2: "Al registrar una carga de gasolina, se debe solicitar la fecha en que se realizó. Asumiendo predefinidamente que es hoy."
    var selectedDateMillis by remember { mutableLongStateOf(entryToEdit?.dateMillis ?: System.currentTimeMillis()) }

    // Requisito 1: "Mostrará cuál de las tarjetas es la óptima para hacer el pago (la que tenga mayor número de días antes del pago exceptuando las departamentales)"
    val recommendations = remember(cards) { CreditCardCalculator.evaluateCardsForPurchase(cards) }
    val optimalCardRec = remember(recommendations) {
        recommendations.firstOrNull { it.isBestOption && !it.card.isDepartmental }
            ?: recommendations.filter { !it.card.isDepartmental }.maxByOrNull { it.daysOfFinancing }
            ?: recommendations.firstOrNull()
    }
    val recMap = remember(recommendations) { recommendations.associateBy { it.card.id } }

    var selectedCardId by remember {
        mutableStateOf(entryToEdit?.cardId ?: optimalCardRec?.card?.id ?: cards.firstOrNull()?.id ?: 0L)
    }

    // Requisito 2: "Pedirá el kilometraje recorrido."
    var kmText by remember {
        mutableStateOf(if (entryToEdit != null && entryToEdit.kmDriven > 0) entryToEdit.kmDriven.toInt().toString() else "")
    }

    // Requisito 3: "Permitirá elegir entre Gasolina Premium (Roja) o Regular (verde) pero la roja será la predefinida."
    var selectedFuelType by remember {
        mutableStateOf(entryToEdit?.fuelType ?: "Gasolina Premium (Roja)")
    }

    // Requisito 4: "Pedirá el precio de la gasolina por litro."
    var priceText by remember {
        mutableStateOf(if (entryToEdit != null) entryToEdit.pricePerLiter.toString() else "")
    }

    // Requisito 5: "Pedirá la cantidad de litros cargados (con 5 decimales)"
    var litersText by remember {
        mutableStateOf(if (entryToEdit != null) entryToEdit.litersLoaded.toString() else "")
    }

    // Requisito 6: "El gasto debe poder dividirse o asumirse meramente personal."
    // Corrección: antes el reparto siempre se calculaba 50/50 sin importar el texto capturado aquí;
    // ahora "dividedCountText" captura el número real de personas y ese es el que se usa para calcular
    // el monto personal, dejando el texto libre solo como referencia informativa (con quién se divide).
    var isDivided by remember { mutableStateOf(entryToEdit?.isDivided ?: false) }
    var dividedWithText by remember { mutableStateOf(entryToEdit?.dividedWith ?: "") }
    var dividedCountText by remember { mutableStateOf((entryToEdit?.dividedCount ?: 2).toString()) }
    var notesText by remember { mutableStateOf(entryToEdit?.notes ?: "") }
    val dividedCountValue = dividedCountText.toIntOrNull()?.coerceAtLeast(2) ?: 2

    // Cálculos en vivo:
    // 1. Costo total = litros x precio por litro
    // 2. Rendimiento = kilometraje / litros
    val kmValue = kmText.toDoubleOrNull() ?: 0.0
    val priceValue = priceText.toDoubleOrNull() ?: 0.0
    val litersValue = litersText.toDoubleOrNull() ?: 0.0

    val calculatedTotalCost = if (litersValue > 0.0 && priceValue > 0.0) {
        litersValue * priceValue
    } else 0.0

    val calculatedEfficiency = if (kmValue > 0.0 && litersValue > 0.0) {
        kmValue / litersValue
    } else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = if (selectedFuelType.contains("Roja")) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (entryToEdit != null) "Editar Carga de Gasolina" else "Captura de Gasolina",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Requisito 1: Tarjeta Óptima
                item {
                    Column {
                        Text(
                            text = "1. Tarjeta para el Pago:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        optimalCardRec?.let { opt ->
                            val bannerColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E6C38)
                            val bannerBg = if (isDark) Color(0xFF1B3820) else Color(0xFF2E6C38).copy(alpha = 0.12f)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = bannerBg,
                                border = BorderStroke(1.dp, bannerColor.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = bannerColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Tarjeta Óptima Recomendada:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = bannerColor
                                        )
                                        Text(
                                            text = "${opt.card.name} (${opt.card.bank}) • ${opt.daysOfFinancing} días de financiamiento",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Selector de tarjetas con indicador de días de crédito
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            cards.forEach { card ->
                                val rec = recMap[card.id]
                                val creditDays = rec?.daysOfFinancing ?: 0
                                val isOpt = card.id == optimalCardRec?.card?.id
                                val isSelected = card.id == selectedCardId

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    },
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 0.8.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .clickable { selectedCardId = card.id }
                                        .testTag("fuel_card_select_${card.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(card.primaryColorHex))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = card.name,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isOpt) {
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "★",
                                                        fontSize = 10.sp,
                                                        color = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "$creditDays días crédito",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else if (isDark) {
                                                    Color(0xFF81C784)
                                                } else {
                                                    Color(0xFF2E6C38)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Requisito 2: "Al registrar una carga de gasolina, se debe solicitar la fecha en que se realizó. Asumiendo predefinidamente que es hoy."
                item {
                    Column {
                        Text(
                            text = "Fecha de la Carga (Predefinido Hoy):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val chosen = Calendar.getInstance().apply { set(y, m, d, 12, 0, 0) }
                                            selectedDateMillis = chosen.timeInMillis
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = fullDateFormat.format(Date(selectedDateMillis)),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Toca para cambiar la fecha",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Cambiar",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Requisito 3: Tipo de Gasolina (Roja por defecto)
                item {
                    Column {
                        Text(
                            text = "2. Tipo de Combustible (Roja predefinida):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Premium (Roja)
                            val isRedSelected = selectedFuelType == "Gasolina Premium (Roja)"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isRedSelected) Color(0xFFD32F2F).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.5.dp, if (isRedSelected) Color(0xFFD32F2F) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFuelType = "Gasolina Premium (Roja)" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(Color(0xFFD32F2F), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Premium (Roja)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isRedSelected) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Regular (Verde)
                            val isGreenSelected = selectedFuelType == "Gasolina Regular (Verde)"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isGreenSelected) Color(0xFF2E7D32).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.5.dp, if (isGreenSelected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFuelType = "Gasolina Regular (Verde)" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(Color(0xFF2E7D32), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Regular (Verde)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGreenSelected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Requisito 2: Kilometraje recorrido
                item {
                    OutlinedTextField(
                        value = kmText,
                        onValueChange = { kmText = it },
                        label = { Text("Kilometraje recorrido (km)") },
                        placeholder = { Text("Ej. 420.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_fuel_km")
                    )
                }

                // Requisito 4: Precio por litro
                item {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Precio por litro ($/L)") },
                        placeholder = { Text("Ej. 25.80") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Icon(Icons.Default.LocalGasStation, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_fuel_price")
                    )
                }

                // Requisito 5: Litros cargados (con 5 decimales)
                item {
                    OutlinedTextField(
                        value = litersText,
                        onValueChange = { litersText = it },
                        label = { Text("Litros cargados (hasta 5 decimales)") },
                        placeholder = { Text("Ej. 38.54120") },
                        supportingText = { Text("Acepta precisión de hasta 5 decimales") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_fuel_liters")
                    )
                }

                // Requisito 6: Dividir gasto o Meramente Personal
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isDivided) Icons.Default.People else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isDivided) "Gasto Dividido" else "Gasto Meramente Personal",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Switch(
                                    checked = isDivided,
                                    onCheckedChange = { isDivided = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }

                            if (isDivided) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = dividedCountText,
                                        onValueChange = { new -> dividedCountText = new.filter { it.isDigit() }.take(2) },
                                        label = { Text("Entre cuántas") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.width(120.dp)
                                    )
                                    OutlinedTextField(
                                        value = dividedWithText,
                                        onValueChange = { dividedWithText = it },
                                        label = { Text("Con quién (opcional)") },
                                        placeholder = { Text("Ej. Familiar / Pareja") },
                                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Text(
                                    text = if (calculatedTotalCost > 0.0) {
                                        "Tu parte: ${currencyFormat.format(calculatedTotalCost / dividedCountValue)} (de ${currencyFormat.format(calculatedTotalCost)} entre $dividedCountValue)"
                                    } else {
                                        "Tu parte se calculará entre $dividedCountValue personas"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Caja de Cálculos en Vivo (Costo total y Rendimiento)
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "RESULTADOS CALCULADOS EN VIVO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Costo Total (Litros × Precio):",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = currencyFormat.format(calculatedTotalCost),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Rendimiento (km / Litros):",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (calculatedEfficiency > 0.0) "${String.format(Locale.US, "%.2f", calculatedEfficiency)} km/l" else "-- km/l",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Notas adicionales
                item {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Notas / Ubicación (Opcional)") },
                        placeholder = { Text("Ej. Gasolinera Shell Periférico") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedCardId != 0L && kmValue > 0.0 && litersValue > 0.0 && priceValue > 0.0) {
                        onSave(
                            selectedCardId,
                            kmValue,
                            selectedFuelType,
                            priceValue,
                            litersValue,
                            isDivided,
                            if (isDivided) dividedWithText else "",
                            dividedCountValue,
                            notesText,
                            selectedDateMillis
                        )
                    }
                },
                enabled = selectedCardId != 0L && kmValue > 0.0 && litersValue > 0.0 && priceValue > 0.0,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_save_fuel_entry")
            ) {
                Text(if (entryToEdit != null) "Guardar Cambios" else "Guardar Carga", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("dialog_add_fuel_entry")
    )
}
