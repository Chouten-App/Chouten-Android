package com.chouten.app.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

// https://stackoverflow.com/questions/66427587/how-to-have-dashed-border-in-jetpack-compose
fun Modifier.dashedBorder(
    strokeWidth: Dp,
    color: Color,
    cornerRadius: Dp,
    interval: Float = 5f
) =
    composed {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }
        val cornerRadiusPx = density.run { cornerRadius.toPx() }

        this.then(
            Modifier.drawWithCache {
                onDrawBehind {
                    val stroke =
                        Stroke(
                            width = strokeWidthPx,
                            pathEffect =
                            PathEffect.dashPathEffect(
                                floatArrayOf(100 / interval, 100 / interval), 0f
                            )
                        )

                    drawRoundRect(
                        style = stroke,
                        color = color,
                        cornerRadius = CornerRadius(cornerRadiusPx)
                    )
                }
            })
    }
