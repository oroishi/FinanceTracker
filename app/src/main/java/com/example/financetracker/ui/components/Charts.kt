package com.example.financetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financetracker.ui.theme.ChartPalette

data class PieSlice(val label: String, val value: Double, val color: Color)

@Composable
fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    centerText: String? = null
) {
    val total = slices.sumOf { it.value }.takeIf { it > 0 } ?: 0.0
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            if (total <= 0) return@Canvas
            val side = minOf(size.width, size.height)
            val topLeft = Offset((size.width - side) / 2, (size.height - side) / 2)
            val arcSize = Size(side, side)
            val strokeWidth = side * 0.18f
            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = (slice.value / total * 360.0).toFloat()
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(topLeft.x + strokeWidth / 2, topLeft.y + strokeWidth / 2),
                    size = Size(arcSize.width - strokeWidth, arcSize.height - strokeWidth),
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweep
            }
        }
        if (centerText != null) {
            Text(
                text = centerText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun LegendList(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    valueFormatter: (Double) -> String = { String.format("%.2f", it) }
) {
    val total = slices.sumOf { it.value }.takeIf { it > 0 } ?: 1.0
    Column(modifier = modifier) {
        slices.forEach { slice ->
            val percent = (slice.value / total * 100).toInt()
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(slice.color)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = slice.label,
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp
                )
                Text(
                    text = "${valueFormatter(slice.value)} ($percent%)",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class LinePoint(val label: String, val value: Double)

@Composable
fun LineChart(
    incomeSeries: List<LinePoint>,
    expenseSeries: List<LinePoint>,
    incomeColor: Color,
    expenseColor: Color,
    modifier: Modifier = Modifier
) {
    val maxValue = (incomeSeries + expenseSeries).maxOfOrNull { it.value } ?: 0.0
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            val w = size.width
            val h = size.height
            val padding = 24f
            val chartW = w - padding * 2
            val chartH = h - padding * 2
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = padding + chartH * i / gridLines
                drawLine(
                    color = labelColor.copy(alpha = 0.2f),
                    start = Offset(padding, y),
                    end = Offset(w - padding, y),
                    strokeWidth = 1f
                )
            }
            if (maxValue <= 0) return@Canvas
            fun drawSeries(series: List<LinePoint>, color: Color) {
                if (series.size < 2) return
                val stepX = chartW / (series.size - 1)
                val path = Path()
                series.forEachIndexed { index, point ->
                    val x = padding + stepX * index
                    val y = padding + chartH - (point.value / maxValue * chartH).toFloat()
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = color, style = Stroke(width = 4f))
                series.forEachIndexed { index, point ->
                    val x = padding + stepX * index
                    val y = padding + chartH - (point.value / maxValue * chartH).toFloat()
                    drawCircle(color = color, radius = 4f, center = Offset(x, y))
                }
            }
            drawSeries(incomeSeries, incomeColor)
            drawSeries(expenseSeries, expenseColor)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            LegendDot(color = incomeColor, label = "Доходы")
            LegendDot(color = expenseColor, label = "Расходы")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(text = label, fontSize = 13.sp)
    }
}

fun colorForIndex(index: Int): Color = ChartPalette[index % ChartPalette.size]
