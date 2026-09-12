package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CashFlowRelease
import com.example.ui.util.AppHaptics
import com.example.ui.util.LocalPrivacyMode
import com.example.ui.util.PrivacyFormat
import java.text.NumberFormat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class ChartSlice(
    val label: String,
    val amount: Double,
    val color: Color
)

/**
 * Gráfico interactivo tipo Dona para visualización de gastos por categoría o responsable.
 */
@Composable
fun SpendingDonutChart(
    slices: List<ChartSlice>,
    currencyFormat: NumberFormat,
    title: String = "Distribución de Gastos",
    modifier: Modifier = Modifier,
    onSliceSelected: ((ChartSlice?) -> Unit)? = null
) {
    if (slices.isEmpty()) return

    val isPrivate = LocalPrivacyMode.current
    val haptic = LocalHapticFeedback.current
    val totalAmount = remember(slices) { slices.sumOf { it.amount } }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    // Animación de barrido al cargar
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .testTag("spending_donut_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                if (selectedIndex != -1) {
                    Text(
                        text = "Ver total",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.clickable {
                            AppHaptics.light(haptic)
                            selectedIndex = -1
                            onSliceSelected?.invoke(null)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas de la dona con texto al centro
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .size(180.dp)
                        .pointerInput(slices) {
                            detectTapGestures { tapOffset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val dx = tapOffset.x - center.x
                                val dy = tapOffset.y - center.y
                                val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                                val outerRadius = size.width / 2f
                                val innerRadius = outerRadius - 34.dp.toPx()

                                if (dist in innerRadius..outerRadius && totalAmount > 0) {
                                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    if (angle < 0) angle += 360f
                                    // Ajuste por desfase inicial (-90 grados)
                                    val normalizedAngle = (angle + 90f) % 360f

                                    var accumulatedSweep = 0f
                                    var found = false
                                    for (i in slices.indices) {
                                        val sweep = ((slices[i].amount / totalAmount) * 360f).toFloat()
                                        if (normalizedAngle in accumulatedSweep..(accumulatedSweep + sweep)) {
                                            AppHaptics.light(haptic)
                                            selectedIndex = if (selectedIndex == i) -1 else i
                                            onSliceSelected?.invoke(if (selectedIndex == -1) null else slices[i])
                                            found = true
                                            break
                                        }
                                        accumulatedSweep += sweep
                                    }
                                    if (!found) {
                                        selectedIndex = -1
                                        onSliceSelected?.invoke(null)
                                    }
                                }
                            }
                        }
                ) {
                    val strokeWidth = 32.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val arcSize = Size(radius * 2f, radius * 2f)
                    val arcTopLeft = Offset(center.x - radius, center.y - radius)

                    var startAngle = -90f

                    if (totalAmount <= 0) {
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.4f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        return@Canvas
                    }

                    for (i in slices.indices) {
                        val slice = slices[i]
                        val rawSweep = ((slice.amount / totalAmount) * 360f).toFloat()
                        val sweep = rawSweep * animationProgress.value

                        val isSelected = (selectedIndex == i)
                        val actualStroke = if (isSelected) strokeWidth + 6.dp.toPx() else strokeWidth
                        val alpha = if (selectedIndex == -1 || isSelected) 1f else 0.4f

                        drawArc(
                            color = slice.color.copy(alpha = alpha),
                            startAngle = startAngle,
                            sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                            useCenter = false,
                            topLeft = Offset(center.x - (radius), center.y - (radius)),
                            size = arcSize,
                            style = Stroke(width = actualStroke, cap = StrokeCap.Round)
                        )
                        startAngle += rawSweep
                    }
                }

                // Centro informativo
                val displayLabel = if (selectedIndex in slices.indices) slices[selectedIndex].label else "Total"
                val displayAmount = if (selectedIndex in slices.indices) slices[selectedIndex].amount else totalAmount
                val displayPercent = if (totalAmount > 0 && selectedIndex in slices.indices) {
                    " (${((slices[selectedIndex].amount / totalAmount) * 100).toInt()}%)"
                } else ""

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = displayLabel + displayPercent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currencyFormat.format(displayAmount),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Leyenda de categorías / responsables
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                slices.take(6).chunked(2).forEach { rowSlices ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowSlices.forEach { slice ->
                            val isSelected = selectedIndex == slices.indexOf(slice)
                            val percent = if (totalAmount > 0) ((slice.amount / totalAmount) * 100).toInt() else 0
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        AppHaptics.light(haptic)
                                        val idx = slices.indexOf(slice)
                                        selectedIndex = if (selectedIndex == idx) -1 else idx
                                        onSliceSelected?.invoke(if (selectedIndex == -1) null else slice)
                                    }
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent)
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(slice.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${slice.label} ($percent%)",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        if (rowSlices.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Gráfico de barras animadas de proyección de flujo de efectivo liberado por MSI.
 */
@Composable
fun CashFlowBarChart(
    projections: List<CashFlowRelease>,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    if (projections.isEmpty()) return

    val haptic = LocalHapticFeedback.current
    val isPrivate = LocalPrivacyMode.current
    var selectedMonthIndex by remember { mutableIntStateOf(0) }

    val activeItems = projections.take(8)
    val maxFreed = remember(activeItems) { activeItems.maxOfOrNull { it.monthlyAmountFreed }?.coerceAtLeast(1.0) ?: 1.0 }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .testTag("cashflow_bar_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIBERACIÓN DE FLUJO MENSUAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Dinero que se libera en tu presupuesto cada mes al terminar planes MSI:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Barras animadas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                activeItems.forEachIndexed { index, proj ->
                    val isSelected = (selectedMonthIndex == index)
                    val ratio = (proj.monthlyAmountFreed / maxFreed).toFloat().coerceIn(0.12f, 1f)
                    val barHeightFraction by animateFloatAsState(
                        targetValue = ratio,
                        animationSpec = tween(durationMillis = 650 + (index * 75), easing = FastOutSlowInEasing),
                        label = "bar_height_$index"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                AppHaptics.light(haptic)
                                selectedMonthIndex = index
                            }
                            .padding(horizontal = 2.dp)
                    ) {
                        // Etiqueta de monto liberado
                        if (isSelected || activeItems.size <= 4) {
                            Text(
                                text = "+${currencyFormat.format(proj.monthlyAmountFreed)}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        // Barra
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (isSelected) 0.85f else 0.7f)
                                .height((100 * barHeightFraction).dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = if (isSelected) {
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                            )
                                        } else {
                                            listOf(
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                                            )
                                        }
                                    )
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Etiqueta del mes
                        Text(
                            text = proj.monthYearLabel.take(3),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Detalle del mes seleccionado
            if (selectedMonthIndex in activeItems.indices) {
                val selectedProj = activeItems[selectedMonthIndex]
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "En ${selectedProj.monthYearLabel} se liberan ${currencyFormat.format(selectedProj.monthlyAmountFreed)}/mes",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (selectedProj.finishingItems.isNotEmpty()) {
                                Text(
                                    text = "Termina(n): ${selectedProj.finishingItems.joinToString(", ")}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Gráfico de curva y tendencia de rendimiento de combustible (km/L).
 */
@Composable
fun FuelEfficiencyTrendChart(
    points: List<Pair<String, Double>>,
    averageKmPerLiter: Double,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) return

    val haptic = LocalHapticFeedback.current
    var selectedIndex by remember { mutableIntStateOf(points.lastIndex) }

    val values = points.map { it.second }
    val minValue = remember(values) { (values.minOrNull() ?: 0.0) * 0.85 }
    val maxValue = remember(values) { (values.maxOrNull() ?: 20.0) * 1.15 }
    val valueRange = (maxValue - minValue).coerceAtLeast(1.0)

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .testTag("fuel_trend_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TENDENCIA DE RENDIMIENTO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Prom: ${String.format(java.util.Locale.US, "%.1f", averageKmPerLiter)} km/L",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas del gráfico de líneas suavizadas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .pointerInput(points) {
                            detectTapGestures { tapOffset ->
                                val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                                val closestIndex = ((tapOffset.x + (stepX / 2)) / stepX).toInt().coerceIn(0, points.lastIndex)
                                AppHaptics.light(haptic)
                                selectedIndex = closestIndex
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val stepX = width / (points.size - 1).coerceAtLeast(1)

                    val coords = points.mapIndexed { i, p ->
                        val x = i * stepX
                        val yRatio = 1f - ((p.second - minValue) / valueRange).toFloat().coerceIn(0f, 1f)
                        val y = yRatio * (height - 30.dp.toPx()) + 15.dp.toPx()
                        Offset(x, y)
                    }

                    // Línea de promedio punteada
                    val avgYRatio = 1f - ((averageKmPerLiter - minValue) / valueRange).toFloat().coerceIn(0f, 1f)
                    val avgY = avgYRatio * (height - 30.dp.toPx()) + 15.dp.toPx()
                    drawLine(
                        color = outlineColor.copy(alpha = 0.5f),
                        start = Offset(0f, avgY),
                        end = Offset(width, avgY),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Path relleno con degradado
                    val fillPath = Path().apply {
                        moveTo(coords.first().x, height)
                        coords.forEach { lineTo(it.x, it.y) }
                        lineTo(coords.last().x, height)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.35f),
                                primaryColor.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Línea principal
                    val strokePath = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        for (i in 1 until coords.size) {
                            val prev = coords[i - 1]
                            val curr = coords[i]
                            val midX = (prev.x + curr.x) / 2f
                            cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                        }
                    }

                    drawPath(
                        path = strokePath,
                        color = primaryColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Puntos
                    coords.forEachIndexed { i, coord ->
                        val isSelected = (i == selectedIndex)
                        drawCircle(
                            color = if (isSelected) primaryColor else surfaceColor,
                            radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                            center = coord
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                            center = coord,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Detalle del punto seleccionado
            if (selectedIndex in points.indices) {
                val pt = points[selectedIndex]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Carga: ${pt.first}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.2f", pt.second)} km/L",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
