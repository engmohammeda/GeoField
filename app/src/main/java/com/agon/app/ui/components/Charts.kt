package com.agon.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agon.app.data.PointCategory
import com.agon.app.data.SurveyCalculator
import com.agon.app.data.SurveyPoint
import com.agon.app.ui.Format
import com.agon.app.ui.theme.LocalCategoryPalette

/**
 * Elevation profile drawn with Compose Canvas — no chart library required.
 */
@Composable
fun ElevationChart(
    points: List<SurveyPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
    gridColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
    labelColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor, fontWeight = FontWeight.Medium)

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (points.size < 2) return@Canvas

            val leftPad = 42.dp.toPx()
            val rightPad = 12.dp.toPx()
            val topPad = 14.dp.toPx()
            val bottomPad = 24.dp.toPx()
            val chartW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
            val chartH = (size.height - topPad - bottomPad).coerceAtLeast(1f)

            val minZ = points.minOf { it.elevation }
            val maxZ = points.maxOf { it.elevation }
            val span = (maxZ - minZ).coerceAtLeast(0.001)
            val zMin = minZ - span * 0.2
            val zMax = maxZ + span * 0.2

            fun xAt(i: Int): Float = leftPad + chartW * (i.toFloat() / (points.size - 1))
            fun yAt(z: Double): Float =
                topPad + chartH * (1f - ((z - zMin) / (zMax - zMin)).toFloat())

            val gridLines = 4
            for (i in 0..gridLines) {
                val t = i.toFloat() / gridLines
                val gy = topPad + chartH * t
                drawLine(
                    color = gridColor,
                    start = Offset(leftPad, gy),
                    end = Offset(size.width - rightPad, gy),
                    strokeWidth = 1.dp.toPx(),
                )
                val value = zMax - (zMax - zMin) * t
                val layout: TextLayoutResult = measurer.measure(Format.number(value, 1), labelStyle)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        leftPad - layout.size.width - 6.dp.toPx(),
                        gy - layout.size.height / 2f,
                    ),
                )
            }

            val linePath = Path()
            points.forEachIndexed { index, point ->
                val px = xAt(index)
                val py = yAt(point.elevation)
                if (index == 0) linePath.moveTo(px, py) else linePath.lineTo(px, py)
            }

            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(xAt(points.size - 1), topPad + chartH)
                lineTo(xAt(0), topPad + chartH)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.30f), lineColor.copy(alpha = 0.02f)),
                    startY = topPad,
                    endY = topPad + chartH,
                ),
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )

            points.forEachIndexed { index, point ->
                val center = Offset(xAt(index), yAt(point.elevation))
                drawCircle(color = lineColor, radius = 4.5.dp.toPx(), center = center)
                drawCircle(color = Color.White, radius = 1.8.dp.toPx(), center = center)
            }

            listOf(points.first(), points.last()).forEach { point ->
                val index = points.indexOf(point)
                val layout = measurer.measure(point.code, labelStyle)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        (xAt(index) - layout.size.width / 2f).coerceIn(leftPad - 10.dp.toPx(), size.width - layout.size.width),
                        topPad + chartH + 6.dp.toPx(),
                    ),
                )
            }
        }
    }
}

/**
 * Plan view of the traverse: polygon, vertices, north arrow and scale bar.
 */
@Composable
fun TraversePlan(
    points: List<SurveyPoint>,
    closed: Boolean,
    modifier: Modifier = Modifier,
    lineColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
    gridColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
    labelColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
    errorColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.error,
) {
    val palette = LocalCategoryPalette.current
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor, fontWeight = FontWeight.Bold)

    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas
        val bounds = SurveyCalculator.bounds(points) ?: return@Canvas

        val padding = 34.dp.toPx()
        val usableW = (size.width - padding * 2).coerceAtLeast(1f)
        val usableH = (size.height - padding * 2).coerceAtLeast(1f)
        val spanE = bounds.width.coerceAtLeast(0.001)
        val spanN = bounds.height.coerceAtLeast(0.001)
        val scale = minOf(usableW / spanE.toFloat(), usableH / spanN.toFloat())

        fun px(p: SurveyPoint): Float =
            padding + usableW / 2f + ((p.easting - bounds.centerE).toFloat() * scale)
        fun py(p: SurveyPoint): Float =
            padding + usableH / 2f - ((p.northing - bounds.centerN).toFloat() * scale)

        // faint grid
        val gridStep = 46.dp.toPx()
        var gx = 0f
        while (gx < size.width) {
            drawLine(gridColor.copy(alpha = 0.45f), Offset(gx, 0f), Offset(gx, size.height), 1f)
            gx += gridStep
        }
        var gy = 0f
        while (gy < size.height) {
            drawLine(gridColor.copy(alpha = 0.45f), Offset(0f, gy), Offset(size.width, gy), 1f)
            gy += gridStep
        }

        if (points.size >= 2) {
            val path = Path()
            points.forEachIndexed { index, point ->
                val x = px(point)
                val y = py(point)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            if (closed) path.close()

            drawPath(
                path = path,
                color = lineColor.copy(alpha = 0.12f),
            )
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round),
            )

            if (closed) {
                val last = points.last()
                val first = points.first()
                drawLine(
                    color = errorColor,
                    start = Offset(px(last), py(last)),
                    end = Offset(px(first), py(first)),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(8.dp.toPx(), 6.dp.toPx()),
                        0f,
                    ),
                )
            }
        }

        points.forEachIndexed { index, point ->
            val color = when (point.category) {
                PointCategory.CORNER -> palette.corner
                PointCategory.TRAVERSE -> palette.traverse
                PointCategory.BENCHMARK -> palette.benchmark
                PointCategory.FEATURE -> palette.feature
                PointCategory.OBSERVATION -> palette.observation
            }
            val center = Offset(px(point), py(point))
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = center)
            drawCircle(color = color, radius = 4.5.dp.toPx(), center = center)
            val layout = measurer.measure(point.code, labelStyle)
            val offsetX = if (index % 2 == 0) {
                center.x + 8.dp.toPx()
            } else {
                center.x - layout.size.width - 8.dp.toPx()
            }
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    offsetX.coerceIn(2.dp.toPx(), (size.width - layout.size.width - 2.dp.toPx()).coerceAtLeast(2.dp.toPx())),
                    (center.y - layout.size.height - 4.dp.toPx()).coerceAtLeast(2.dp.toPx()),
                ),
            )
        }

        // north arrow
        val arrowX = size.width - 22.dp.toPx()
        val arrowY = 26.dp.toPx()
        val arrow = Path().apply {
            moveTo(arrowX, arrowY - 12.dp.toPx())
            lineTo(arrowX - 5.dp.toPx(), arrowY + 8.dp.toPx())
            lineTo(arrowX, arrowY + 4.dp.toPx())
            lineTo(arrowX + 5.dp.toPx(), arrowY + 8.dp.toPx())
            close()
        }
        drawPath(arrow, lineColor)
        val nLayout = measurer.measure("N", labelStyle)
        drawText(
            textLayoutResult = nLayout,
            topLeft = Offset(arrowX - nLayout.size.width / 2f, arrowY + 9.dp.toPx()),
        )
    }
}

/**
 * Circular gauge showing a "1 : N" precision ratio.
 */
@Composable
fun AccuracyGauge(
    denominator: Double,
    modifier: Modifier = Modifier,
    color: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
    trackColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
    label: String = "",
    caption: String = "",
) {
    val measurer = rememberTextMeasurer()
    val valueStyle = TextStyle(fontSize = 22.sp, color = color, fontWeight = FontWeight.Bold)
    val captionStyle = TextStyle(
        fontSize = 10.sp,
        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
    )

    Canvas(modifier = modifier) {
        val stroke = 9.dp.toPx()
        val inset = stroke / 2f + 2.dp.toPx()
        val arcSize = androidx.compose.ui.geometry.Size(
            size.width - inset * 2,
            size.height - inset * 2,
        )
        val topLeft = Offset(inset, inset)
        drawArc(
            color = trackColor,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        val ratio: Float = if (denominator.isInfinite() || denominator <= 0) {
            1f
        } else {
            (denominator / 20000.0).coerceIn(0.04, 1.0).toFloat()
        }
        drawArc(
            color = color,
            startAngle = 135f,
            sweepAngle = 270f * ratio,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        val valueText = if (denominator.isInfinite() || denominator <= 0) "—" else "1:${denominator.toInt()}"
        val valueLayout = measurer.measure(valueText, valueStyle)
        drawText(
            textLayoutResult = valueLayout,
            topLeft = Offset(
                (size.width - valueLayout.size.width) / 2f,
                (size.height - valueLayout.size.height) / 2f - 6.dp.toPx(),
            ),
        )
        if (caption.isNotEmpty()) {
            val captionLayout = measurer.measure(caption, captionStyle)
            drawText(
                textLayoutResult = captionLayout,
                topLeft = Offset(
                    (size.width - captionLayout.size.width) / 2f,
                    (size.height + valueLayout.size.height) / 2f - 4.dp.toPx(),
                ),
            )
        }
        if (label.isNotEmpty()) {
            val labelLayout = measurer.measure(label, captionStyle)
            drawText(
                textLayoutResult = labelLayout,
                topLeft = Offset(
                    (size.width - labelLayout.size.width) / 2f,
                    size.height - labelLayout.size.height - 2.dp.toPx(),
                ),
            )
        }
    }
}

@Composable
fun ChartSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    androidx.compose.material3.Surface(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer,
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
