package com.example.ui.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FarmBlue

@Composable
fun GrowthWeightChart(
    dataPoints: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(top = 16.dp, bottom = 24.dp, start = 40.dp, end = 20.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val minY = 30f
            val maxY = 75f
            val yRange = maxY - minY

            // Draw horizontal grid lines & labels (30kg, 40kg, 50kg, 60kg, 70kg)
            val gridSteps = listOf(30f, 40f, 50f, 60f, 70f)
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 28f
                textAlign = android.graphics.Paint.Align.RIGHT
            }

            gridSteps.forEach { step ->
                val y = height - ((step - minY) / yRange * height)
                drawLine(
                    color = Color(0xFFEAEAEA),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "${step.toInt()}kg",
                    -12f,
                    y + 10f,
                    paint
                )
            }

            if (dataPoints.size < 2) return@Canvas

            // Compute points
            val stepX = width / (dataPoints.size - 1)
            val offsets = dataPoints.mapIndexed { index, pair ->
                val x = index * stepX
                val y = height - ((pair.second - minY) / yRange * height)
                Offset(x, y)
            }

            // Path for gradient area underneath
            val fillPath = Path().apply {
                moveTo(offsets.first().x, height)
                offsets.forEach { lineTo(it.x, it.y) }
                lineTo(offsets.last().x, height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(FarmBlue.copy(alpha = 0.25f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // Line stroke path
            val linePath = Path().apply {
                moveTo(offsets.first().x, offsets.first().y)
                for (i in 1 until offsets.size) {
                    lineTo(offsets[i].x, offsets[i].y)
                }
            }

            drawPath(
                path = linePath,
                color = FarmBlue,
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw points and X-axis labels
            val xLabelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 26f
                textAlign = android.graphics.Paint.Align.CENTER
            }

            offsets.forEachIndexed { index, offset ->
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = offset
                )
                drawCircle(
                    color = FarmBlue,
                    radius = 3.5.dp.toPx(),
                    center = offset
                )

                // Label below chart
                val label = dataPoints[index].first
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    offset.x,
                    height + 34f,
                    xLabelPaint
                )
            }
        }
    }
}
