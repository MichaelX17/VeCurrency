package com.example.vecurrency.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun UsFlagIcon(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val radius = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        val path = Path().apply {
            addOval(Rect(center = center, radius = radius))
        }

        drawContext.canvas.save()
        drawContext.canvas.clipPath(path)

        drawRect(color = Color(0xFFB22234))

        val stripeHeight = this.size.height / 7f
        for (i in 1 until 7 step 2) {
            drawRect(
                color = Color.White,
                topLeft = Offset(0f, i * stripeHeight),
                size = Size(this.size.width, stripeHeight)
            )
        }

        drawRect(
            color = Color(0xFF3C3B6E),
            topLeft = Offset(0f, 0f),
            size = Size(this.size.width * 0.5f, stripeHeight * 4f)
        )

        val starRadius = radius * 0.08f
        val cantonWidth = this.size.width * 0.5f
        val cantonHeight = stripeHeight * 4f
        val rows = 3
        val cols = 3
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cx = cantonWidth * (c + 1) / (cols + 1f)
                val cy = cantonHeight * (r + 1) / (rows + 1f)
                drawCircle(color = Color.White, radius = starRadius, center = Offset(cx, cy))
            }
        }

        drawContext.canvas.restore()
    }
}

@Composable
fun EurFlagIcon(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val radius = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        val path = Path().apply {
            addOval(Rect(center = center, radius = radius))
        }

        drawContext.canvas.save()
        drawContext.canvas.clipPath(path)

        drawRect(color = Color(0xFF003399))

        val ringRadius = radius * 0.55f
        val goldColor = Color(0xFFFFCC00)
        val starRadius = radius * 0.09f

        for (i in 0 until 12) {
            val angle = (i * 30.0 - 90.0) * (PI / 180.0)
            val sx = center.x + (ringRadius * cos(angle)).toFloat()
            val sy = center.y + (ringRadius * sin(angle)).toFloat()
            drawCircle(color = goldColor, radius = starRadius, center = Offset(sx, sy))
        }

        drawContext.canvas.restore()
    }
}

@Composable
fun VesFlagIcon(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val radius = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        val path = Path().apply {
            addOval(Rect(center = center, radius = radius))
        }

        drawContext.canvas.save()
        drawContext.canvas.clipPath(path)

        val bandHeight = this.size.height / 3f

        drawRect(
            color = Color(0xFFFFCC00),
            topLeft = Offset(0f, 0f),
            size = Size(this.size.width, bandHeight)
        )
        drawRect(
            color = Color(0xFF00247D),
            topLeft = Offset(0f, bandHeight),
            size = Size(this.size.width, bandHeight)
        )
        drawRect(
            color = Color(0xFFCF142B),
            topLeft = Offset(0f, bandHeight * 2f),
            size = Size(this.size.width, bandHeight)
        )

        val arcRadius = radius * 0.35f
        val starRadius = radius * 0.06f
        val blueCenterY = bandHeight * 1.5f

        for (i in 0 until 7) {
            val angle = (-140.0 + i * 16.666) * (PI / 180.0)
            val sx = center.x + (arcRadius * cos(angle)).toFloat()
            val sy = blueCenterY + (arcRadius * sin(angle)).toFloat() + (radius * 0.15f)
            drawCircle(color = Color.White, radius = starRadius, center = Offset(sx, sy))
        }

        drawContext.canvas.restore()
    }
}

@Composable
fun BinanceIcon(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val radius = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        drawCircle(color = Color(0xFF1E2329), radius = radius, center = center)

        val unit = radius * 0.22f

        drawDiamond(center = Offset(center.x, center.y - unit * 2f), size = unit)
        drawDiamond(center = Offset(center.x, center.y + unit * 2f), size = unit)
        drawDiamond(center = Offset(center.x - unit * 2f, center.y), size = unit)
        drawDiamond(center = Offset(center.x + unit * 2f, center.y), size = unit)
        drawDiamond(center = center, size = unit * 1.2f)
    }
}

private fun DrawScope.drawDiamond(
    center: Offset,
    size: Float,
    color: Color = Color(0xFFF0B90B)
) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        lineTo(center.x + size, center.y)
        lineTo(center.x, center.y + size)
        lineTo(center.x - size, center.y)
        close()
    }
    drawPath(path = path, color = color)
}

@Composable
fun BcvLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val radius = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        drawCircle(color = Color(0xFF1E3A8A), radius = radius, center = center)
        drawCircle(color = Color(0xFF45B5FF), radius = radius * 0.9f, center = center, style = Stroke(width = 2.dp.toPx()))
        drawCircle(color = Color(0xFF0F172A), radius = radius * 0.75f, center = center)
    }
}
