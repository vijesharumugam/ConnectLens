package com.connectlens.app.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.connectlens.app.core.designsystem.ChartIncoming
import com.connectlens.app.core.designsystem.ChartMissed
import com.connectlens.app.core.designsystem.ChartOutgoing
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Interactive canvas bar chart showing calls per day with tap tooltips and rounded gradient bars.
 */
@Composable
fun AnalyticsBarChart(
    data: Map<LocalDate, Int>,
    modifier: Modifier = Modifier,
    label: String = "Calls"
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text      = "No data available for this range",
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val sorted   = data.entries.sortedBy { it.key }
    val maxValue = sorted.maxOf { it.value }.coerceAtLeast(1)

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    var triggered by remember { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "barAnimation"
    )
    LaunchedEffect(Unit) { triggered = true }

    val description = "$label chart: ${sorted.size} entries, peak ${sorted.maxOf { it.value }}"

    Column(modifier = modifier.fillMaxWidth()) {
        // Floating Tooltip Header
        val selectedItem = selectedIndex?.let { sorted.getOrNull(it) }
        if (selectedItem != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "${selectedItem.key.format(DateTimeFormatter.ofPattern("EEE, dd MMM"))}: ${selectedItem.value} $label",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .semantics { contentDescription = description }
                .pointerInput(sorted) {
                    detectTapGestures { tapOffset ->
                        val chartHeight = size.height - 24.dp.toPx()
                        val totalBars   = sorted.size
                        val barWidth    = (size.width / (totalBars * 1.5f)).coerceAtMost(32.dp.toPx())
                        val gap         = (size.width - barWidth * totalBars) / (totalBars + 1)

                        val tappedIndex = sorted.indices.firstOrNull { index ->
                            val x = gap + index * (barWidth + gap)
                            tapOffset.x in x..(x + barWidth) && tapOffset.y <= chartHeight
                        }
                        selectedIndex = if (selectedIndex == tappedIndex) null else tappedIndex
                    }
                }
        ) {
            val chartHeight = size.height - 24.dp.toPx()
            val totalBars   = sorted.size
            val barWidth    = (size.width / (totalBars * 1.5f)).coerceAtMost(32.dp.toPx())
            val gap         = (size.width - barWidth * totalBars) / (totalBars + 1)

            // Horizontal grid lines
            for (i in 1..3) {
                val y = chartHeight * (1f - i / 3f)
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            }

            // Draw bars
            sorted.forEachIndexed { index, (_, count) ->
                val barHeight = (count.toFloat() / maxValue) * chartHeight * animationProgress
                val x = gap + index * (barWidth + gap)
                val y = chartHeight - barHeight

                val isSelected = (index == selectedIndex)
                val brush = Brush.verticalGradient(
                    colors = if (isSelected) {
                        listOf(secondaryColor, primaryColor)
                    } else {
                        listOf(primaryColor, primaryColor.copy(alpha = 0.7f))
                    }
                )

                drawRoundRect(
                    brush       = brush,
                    topLeft     = Offset(x, y),
                    size        = Size(barWidth, barHeight.coerceAtLeast(4f)),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
            }
        }
    }
}

/**
 * Bar chart showing total talk-time (seconds) per day.
 */
@Composable
fun AnalyticsDurationChart(
    data: Map<LocalDate, Long>,
    modifier: Modifier = Modifier,
    label: String = "Talk Time (mins)"
) {
    AnalyticsBarChart(
        data     = data.mapValues { (it.value / 60).toInt().coerceAtLeast(if (it.value > 0) 1 else 0) },
        label    = label,
        modifier = modifier
    )
}

/**
 * 24-Hour Call Activity Heatmap / Hourly distribution bar chart.
 */
@Composable
fun HourlyHeatmapChart(
    hourlyData: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    val maxCount = hourlyData.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)

    var selectedHour by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        selectedHour?.let { hour ->
            val count = hourlyData[hour] ?: 0
            val ampm = if (hour < 12) "AM" else "PM"
            val hFormatted = if (hour % 12 == 0) 12 else hour % 12
            Text(
                text = "Time $hFormatted:00 $ampm • $count Calls",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp).align(Alignment.CenterHorizontally)
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .pointerInput(hourlyData) {
                    detectTapGestures { offset ->
                        val barWidth = size.width / 24f
                        val hour = (offset.x / barWidth).toInt().coerceIn(0, 23)
                        selectedHour = if (selectedHour == hour) null else hour
                    }
                }
        ) {
            val chartHeight = size.height - 20.dp.toPx()
            val barWidth = size.width / 24f

            // Baseline
            drawLine(gridColor, Offset(0f, chartHeight), Offset(size.width, chartHeight), strokeWidth = 2f)

            for (hour in 0..23) {
                val count = hourlyData[hour] ?: 0
                val barHeight = (count.toFloat() / maxCount) * chartHeight
                val x = hour * barWidth
                val y = chartHeight - barHeight

                val color = if (hour == selectedHour) {
                    tertiaryColor
                } else if (hour in 9..18) {
                    primaryColor
                } else {
                    primaryColor.copy(alpha = 0.5f)
                }

                drawRoundRect(
                    color = color,
                    topLeft = Offset(x + 2f, y),
                    size = Size(barWidth - 4f, barHeight.coerceAtLeast(3f)),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("12 AM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("6 AM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("12 PM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("6 PM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("11 PM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Day of Week call intensity chart (Mon - Sun).
 */
@Composable
fun DayOfWeekChart(
    dowData: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val maxVal = dowData.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    val barColor = MaterialTheme.colorScheme.secondary

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val chartHeight = size.height - 20.dp.toPx()
            val totalBars = 7
            val barWidth = (size.width / (totalBars * 1.8f)).coerceAtMost(36.dp.toPx())
            val gap = (size.width - barWidth * totalBars) / (totalBars + 1)

            for (i in 1..7) {
                val count = dowData[i] ?: 0
                val barHeight = (count.toFloat() / maxVal) * chartHeight
                val x = gap + (i - 1) * (barWidth + gap)
                val y = chartHeight - barHeight

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight.coerceAtLeast(4f)),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            labels.forEach { label ->
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Call Duration Buckets Breakdown Chart.
 */
@Composable
fun CallDurationHistogram(
    buckets: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val maxVal = buckets.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        buckets.forEach { (range, count) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = range,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(54.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barWidth = (count.toFloat() / maxVal) * size.width
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(0f, 0f),
                            size = Size(barWidth.coerceAtLeast(6f), size.height),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    }
                }
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

/**
 * Donut chart showing breakdown of call types with central statistics readout.
 */
@Composable
fun CallTypeDonutChart(
    incoming: Int,
    outgoing: Int,
    missed: Int,
    modifier: Modifier = Modifier
) {
    val total = (incoming + outgoing + missed).coerceAtLeast(1)

    var triggered by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue   = if (triggered) 1f else 0f,
        animationSpec = tween(800),
        label         = "donutAnimation"
    )
    LaunchedEffect(Unit) { triggered = true }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .size(150.dp)
            ) {
                val strokeWidth = 26.dp.toPx()
                val radius      = (size.minDimension / 2f) - strokeWidth / 2f
                val inset       = Offset(size.width / 2f - radius, size.height / 2f - radius)
                val arcSize     = Size(radius * 2, radius * 2)

                data class Segment(val count: Int, val color: Color)
                val segments = listOf(
                    Segment(incoming, ChartIncoming),
                    Segment(outgoing, ChartOutgoing),
                    Segment(missed, ChartMissed)
                )

                var startAngle = -90f
                segments.forEach { seg ->
                    val sweep = (seg.count.toFloat() / total) * 360f * animProgress
                    if (sweep > 0f) {
                        drawArc(
                            color       = seg.color,
                            startAngle  = startAngle,
                            sweepAngle  = sweep,
                            useCenter   = false,
                            topLeft     = inset,
                            size        = arcSize,
                            style       = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += (seg.count.toFloat() / total) * 360f
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$total",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total Calls",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Legend
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DonutLegendItem(color = ChartIncoming, label = "Incoming ($incoming)")
            DonutLegendItem(color = ChartOutgoing, label = "Outgoing ($outgoing)")
            DonutLegendItem(color = ChartMissed,   label = "Missed ($missed)")
        }
    }
}

@Composable
private fun DonutLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color)
        }
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
