package com.chouten.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import com.chouten.app.calculateFraction
import com.chouten.app.scale
import com.google.common.math.Quantiles.scale


/* source: https://github.com/SmartToolFactory/Compose-Colorful-Sliders */

/**
 * @param value current value of the Slider. If outside of [valueRange] provided, value will be
 * coerced to this range.
 * @param onValueChange lambda that returns value, position of **thumb** as [Offset], vertical
 * center is stored in y.
 * @param modifier modifiers for the Slider layout
 * @param enabled whether or not component is enabled and can be interacted with or not
 * @param valueRange range of values that Slider value can take. Passed [value] will be coerced to
 * this range
 * @param steps if greater than 0, specifies the amounts of discrete values, evenly distributed
 * between across the whole value range. If 0, slider will behave as a continuous slider and allow
 * to choose any value from the range specified. Must not be negative.
 * @param trackHeight height of the track that will be drawn on [Canvas]. half of [trackHeight]
 * is used as **stroke** width.
 * @param thumbRadius radius of thumb of the the slider
 * @param colors [MaterialSliderColors] that will be used to determine the color of the Slider parts in
 * different state. See [MaterialSliderDefaults.defaultColors],
 * [MaterialSliderDefaults.customColors] or other functions to customize.
 * @param borderStroke draws border around the track with given width in dp.
 * @param drawInactiveTrack flag to draw **InActive** track between active progress and track end.
 * @param coerceThumbInTrack when set to true track's start position is matched to thumbs left
 * on start and thumbs right at the end of the track. Use this when [trackHeight] is bigger than
 * [thumbRadius].
 */
@Composable
fun CustomSlider(
    value: Float,
    onValueChange: (Float, Offset) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    trackHeight: Dp = TrackHeight,
    thumbRadius: Dp = ThumbRadius,
    colors: MaterialSliderColors = MaterialSliderDefaults.defaultColors(),
    borderStroke: BorderStroke? = null,
    drawInactiveTrack: Boolean = true,
    coerceThumbInTrack: Boolean = false
) {
    require(steps >= 0) { "steps should be >= 0" }
    val onValueChangeState = rememberUpdatedState(onValueChange)
    val tickFractions = remember(steps) {
        stepsToTickFractions(steps)
    }
    BoxWithConstraints(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .requiredSizeIn(
                minWidth = ThumbRadius * 2,
                minHeight = ThumbRadius * 2
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val width = constraints.maxWidth.toFloat()
        val thumbRadiusInPx: Float
        // Start of the track used for measuring progress,
        // it's line + radius of cap which is half of height of track
        // to draw this on canvas starting point of line
        // should be at trackStart + trackHeightInPx / 2 while drawing
        val trackStart: Float
        // End of the track that is used for measuring progress
        val trackEnd: Float
        val strokeRadius: Float
        with(LocalDensity.current) {
            thumbRadiusInPx = thumbRadius.toPx()
            strokeRadius = trackHeight.toPx() / 2
            trackStart = thumbRadiusInPx.coerceAtLeast(strokeRadius)
            trackEnd = width - trackStart
        }
        // Sales and interpolates from offset from dragging to user value in valueRange
        fun scaleToUserValue(offset: Float) =
            scale(trackStart, trackEnd, offset, valueRange.start, valueRange.endInclusive)

        // Scales user value using valueRange to position on x axis on screen
        fun scaleToOffset(userValue: Float) =
            scale(valueRange.start, valueRange.endInclusive, userValue, trackStart, trackEnd)

        val rawOffset = remember { mutableStateOf(scaleToOffset(value)) }
        CorrectValueSideEffect(
            ::scaleToOffset,
            valueRange,
            trackStart..trackEnd,
            rawOffset,
            value
        )
        val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
        val fraction = calculateFraction(valueRange.start, valueRange.endInclusive, coerced)
        val dragModifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change: PointerInputChange, _: Offset ->
                        if (enabled) {
                            rawOffset.value =
                                if (!isRtl) change.position.x else trackEnd - change.position.x
                            val offsetInTrack = rawOffset.value.coerceIn(trackStart, trackEnd)
                            onValueChangeState.value.invoke(
                                scaleToUserValue(offsetInTrack),
                                Offset(rawOffset.value.coerceIn(trackStart, trackEnd), strokeRadius)
                            )
                        }
                    },
                    onDragEnd = {
                        if (enabled) {
                            onValueChangeFinished?.invoke()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { position: Offset ->
                    if (enabled) {
                        rawOffset.value =
                            if (!isRtl) position.x else trackEnd - position.x
                        val offsetInTrack = rawOffset.value.coerceIn(trackStart, trackEnd)
                        onValueChangeState.value.invoke(
                            scaleToUserValue(offsetInTrack),
                            Offset(rawOffset.value.coerceIn(trackStart, trackEnd), strokeRadius)
                        )
                        onValueChangeFinished?.invoke()
                    }
                }
            }
        SliderImpl(
            enabled = enabled,
            fraction = fraction,
            trackStart = trackStart,
            trackEnd = trackEnd,
            tickFractions = tickFractions,
            colors = colors,
            trackHeight = trackHeight,
            thumbRadius = thumbRadiusInPx,
            coerceThumbInTrack = coerceThumbInTrack,
            drawInactiveTrack = drawInactiveTrack,
            borderStroke = borderStroke,
            modifier = dragModifier
        )
    }
}

@Composable
private fun SliderImpl(
    enabled: Boolean,
    fraction: Float,
    trackStart: Float,
    trackEnd: Float,
    tickFractions: List<Float>,
    colors: MaterialSliderColors,
    trackHeight: Dp,
    thumbRadius: Float,
    coerceThumbInTrack: Boolean,
    drawInactiveTrack: Boolean,
    borderStroke: BorderStroke? = null,
    modifier: Modifier,
) {
    val trackStrokeWidth: Float
    val thumbSize: Dp
    var borderWidth = 0f
    val borderBrush: Brush? = borderStroke?.brush
    with(LocalDensity.current) {
        trackStrokeWidth = trackHeight.toPx()
        thumbSize = (2 * thumbRadius).toDp()
        if (borderStroke != null) {
            borderWidth = borderStroke.width.toPx()
        }
    }
    Box(
        // Constraint max height of Slider to max of thumb or track or minimum touch 48.dp
        modifier
            .heightIn(
                max = trackHeight
                    .coerceAtLeast(thumbSize)
                    .coerceAtLeast(TrackHeight)
            )
    ) {
        // Position that corresponds to center of this slider's thumb
        val thumbCenterPos = (trackStart + (trackEnd - trackStart) * fraction)
        Track(
            modifier = Modifier.fillMaxSize(),
            fraction = fraction,
            tickFractions = tickFractions,
            thumbRadius = thumbRadius,
            trackStart = trackStart,
            trackHeight = trackStrokeWidth,
            coerceThumbInTrack = coerceThumbInTrack,
            colors = colors,
            enabled = enabled,
            borderBrush = borderBrush,
            borderWidth = borderWidth,
            drawInactiveTrack = drawInactiveTrack
        )
        Thumb(
            modifier = Modifier.align(Alignment.CenterStart),
            offset = thumbCenterPos - thumbRadius,
            thumbSize = thumbSize,
            colors = colors,
            enabled = enabled
        )
    }
}

/**
 * Draws active and if [drawInactiveTrack] is set to true inactive tracks on Canvas.
 * If inactive track is to be drawn it's drawn between start and end of canvas. Active track
 * is drawn between start and current value.
 *
 * Drawing both tracks use [SliderBrushColor] to draw a nullable [Brush] first. If it's not then
 * [SliderBrushColor.solidColor] is used to draw with solid colors provided by [MaterialSliderColors]
 */
@Composable
private fun Track(
    modifier: Modifier,
    fraction: Float,
    tickFractions: List<Float>,
    thumbRadius: Float,
    trackStart: Float,
    trackHeight: Float,
    coerceThumbInTrack: Boolean,
    colors: MaterialSliderColors,
    enabled: Boolean,
    borderBrush: Brush?,
    borderWidth: Float,
    drawInactiveTrack: Boolean,
) {
    val debug = false
    // Colors for drawing track and/or ticks
    val activeTrackColor: Brush =
        colors.trackColor(enabled = enabled, active = true).value
    val inactiveTrackColor: Brush =
        colors.trackColor(enabled = enabled, active = false).value
    val inactiveTickColor = colors.tickColor(enabled, active = false).value
    val activeTickColor = colors.tickColor(enabled, active = true).value
    // stroke radius is used for drawing length it adds this radius to both sides of the line
    val strokeRadius = trackHeight / 2
    // Start of drawing in Canvas
    // when not coerced set start of drawing line at trackStart + strokeRadius
    // to limit drawing start edge at track start end edge at track end
    // When coerced move edges of drawing by thumb radius to cover thumb edges in drawing
    // it needs to move to right as stroke radius minus thumb radius to match track start
    val drawStart =
        if (coerceThumbInTrack) trackStart - thumbRadius + strokeRadius else trackStart
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val centerY = center.y
        // left side of the slider that is drawn on canvas, left tip of stroke radius on left side
        val sliderLeft = Offset(drawStart, centerY)
        // right side of the slider that is drawn on canvas, right tip of stroke radius on left side
        val sliderRight = Offset((width - drawStart).coerceAtLeast(drawStart), centerY)
        val sliderStart = if (isRtl) sliderRight else sliderLeft
        val sliderEnd = if (isRtl) sliderLeft else sliderRight
        val sliderValue = Offset(
            sliderStart.x + (sliderEnd.x - sliderStart.x) * fraction,
            center.y
        )
        // InActive Track
        drawLine(
            brush = inactiveTrackColor,
            start = sliderStart,
            end = sliderEnd,
            strokeWidth = trackHeight,
            cap = StrokeCap.Round
        )
        // Active Track
        drawLine(
            brush = activeTrackColor,
            start = sliderStart,
            end = if (drawInactiveTrack) sliderValue else sliderEnd,
            strokeWidth = trackHeight,
            cap = StrokeCap.Round
        )
        if (debug) {
            drawLine(
                color = Color.Yellow,
                start = sliderStart,
                end = sliderEnd,
                strokeWidth = strokeRadius / 4
            )
        }
        borderBrush?.let { brush ->
            drawRoundRect(
                brush = brush,
                topLeft = Offset(sliderStart.x - strokeRadius, (height - trackHeight) / 2),
                size = Size(width = sliderEnd.x - sliderStart.x + trackHeight, trackHeight),
                cornerRadius = CornerRadius(strokeRadius, strokeRadius),
                style = Stroke(width = borderWidth)
            )
        }
        if (drawInactiveTrack) {
            tickFractions.groupBy { it > fraction }
                .forEach { (outsideFraction, list) ->
                    drawPoints(
                        points = list.map {
                            Offset(lerp(sliderStart, sliderEnd, it).x, center.y)
                        },
                        pointMode = PointMode.Points,
                        brush = if (outsideFraction) inactiveTickColor
                        else activeTickColor,
                        strokeWidth = strokeRadius.coerceAtMost(thumbRadius / 2),
                        cap = StrokeCap.Round
                    )
                }
        }
    }
}

@Composable
private fun Thumb(
    modifier: Modifier,
    offset: Float,
    thumbSize: Dp,
    colors: MaterialSliderColors,
    enabled: Boolean
) {
    val thumbColor: Brush = colors.thumbColor(enabled).value
    Spacer(
        modifier = modifier
            .offset { IntOffset(offset.toInt(), 0) }
            .shadow(1.dp, shape = CircleShape)
            .size(thumbSize)
            .then(
                Modifier.background(thumbColor)
            )
    )
}

@Composable
internal fun CorrectValueSideEffect(
    scaleToOffset: (Float) -> Float,
    valueRange: ClosedFloatingPointRange<Float>,
    trackRange: ClosedFloatingPointRange<Float>,
    valueState: MutableState<Float>,
    value: Float
) {
    SideEffect {
        val error = (valueRange.endInclusive - valueRange.start) / 1000
        val newOffset = scaleToOffset(value)
        if (abs(newOffset - valueState.value) > error) {
            if (valueState.value in trackRange) {
                valueState.value = newOffset
            }
        }
    }
}

internal fun stepsToTickFractions(steps: Int): List<Float> {
    return if (steps == 0) emptyList() else List(steps + 2) { it.toFloat() / (steps + 1) }
}

internal val ThumbRadius = 10.dp
internal val TrackHeight = 4.dp
internal val SliderHeight = 48.dp


val ThumbColor = Color(0xfff0f0f0)
val ActiveTrackColor = Color(0xff489cef)
val InactiveTrackColor = Color(0xffcccccc)

/**
 * Data class that contains color or/and brush property for drawing track section of
 * [ColorfulSlider]
 */
data class SliderBrushColor(
    val color: Color = Color.Unspecified,
    val brush: Brush? = null
) {
    /**
     * [Brush] that is not **null** [brush] property or [SolidColor] that is not nullable and
     * contains [color] property as [SolidColor.value]
     */
    val activeBrush: Brush
        get() = brush ?: solidColor

    /**
     * [SolidColor] is a [Brush] that
     * wraps [color] property that is used for [activeBrush] if [brush] property is **null**
     */
    val solidColor = SolidColor(color)
}

/**
 * Creates a [MaterialSliderColors] that represents the different colors used in parts of the
 * [Slider] in different states.
 *
 * For the name references below the words "active" and "inactive" are used. Active part of
 * the slider is filled with progress, so if slider's progress is 30% out of 100%, left (or
 * right in RTL) 30% of the track will be active, the rest is not active.
 *
 * @param thumbColor thumb color when enabled
 * @param disabledThumbColor thumb colors when disabled
 * @param activeTrackColor color of the track in the part that is "active", meaning that the
 * thumb is ahead of it
 * @param inactiveTrackColor color of the track in the part that is "inactive", meaning that the
 * thumb is before it
 * @param disabledActiveTrackColor color of the track in the "active" part when the Slider is
 * disabled
 * @param disabledInactiveTrackColor color of the track in the "inactive" part when the
 * Slider is disabled
 * @param activeTickColor colors to be used to draw tick marks on the active track, if `steps`
 * is specified
 * @param inactiveTickColor colors to be used to draw tick marks on the inactive track, if
 * `steps` are specified on the Slider is specified
 * @param disabledActiveTickColor colors to be used to draw tick marks on the active track
 * when Slider is disabled and when `steps` are specified on it
 * @param disabledInactiveTickColor colors to be used to draw tick marks on the inactive part
 * of the track when Slider is disabled and when `steps` are specified on it
 */
object MaterialSliderDefaults {

    @Composable
    private fun primarySolidColor() = MaterialTheme.colors.primary

    /*
       Thumb
     */
    @Composable
    private fun activeThumbSolidColor() = primarySolidColor()

    @Composable
    private fun disabledThumbSolidColor() = MaterialTheme.colors.onSurface
        .copy(alpha = ContentAlpha.disabled)
        .compositeOver(MaterialTheme.colors.surface)

    /*
      Active Track
     */
    @Composable
    private fun activeTrackSolidColor() = primarySolidColor()

    @Composable
    private fun disabledActiveTrackSolidColor() =
        MaterialTheme.colors.onSurface.copy(alpha = DisabledActiveTrackAlpha)

    /*
      InActive Track
     */
    @Composable
    private fun inactiveTrackSolidColor(color: SliderBrushColor) =
        color.color.copy(alpha = InactiveTrackAlpha)

    @Composable
    private fun disabledInactiveTrackSolidColor(color: SliderBrushColor) =
        color.color.copy(alpha = InactiveTrackAlpha)


    /*
      Tick
     */
    @Composable
    private fun activeTickSolidColor(color: SliderBrushColor) =
        contentColorFor(color.color).copy(alpha = SliderDefaults.TickAlpha)

    @Composable
    private fun inActiveTickSolidColor(color: SliderBrushColor) =
        color.color.copy(alpha = SliderDefaults.TickAlpha)

    @Composable
    private fun disabledActiveTickSolidColor(color: SliderBrushColor) =
        color.color.copy(alpha = SliderDefaults.DisabledTickAlpha)


    @Composable
    private fun disabledInactiveTickSolidColor(color: SliderBrushColor) =
        color.color.copy(alpha = SliderDefaults.DisabledTickAlpha)


    /**
     * [ColorfulSlider] [SliderBrushColor] with [MaterialTheme]'s default [Color]s.
     */
    @Composable
    fun defaultColors(
        thumbColor: SliderBrushColor = SliderBrushColor(
            color = activeThumbSolidColor()
        ),
        disabledThumbColor: SliderBrushColor = SliderBrushColor(
            color = disabledThumbSolidColor()
        ),
        activeTrackColor: SliderBrushColor = SliderBrushColor(
            color = activeTrackSolidColor()
        ),
        disabledActiveTrackColor: SliderBrushColor = SliderBrushColor(
            color = disabledActiveTrackSolidColor()
        ),
        inactiveTrackColor: SliderBrushColor = SliderBrushColor(
            color = inactiveTrackSolidColor(activeTrackColor)
        ),
        disabledInactiveTrackColor: SliderBrushColor = SliderBrushColor(
            color = disabledInactiveTrackSolidColor(disabledActiveTrackColor)
        ),
        activeTickColor: SliderBrushColor = SliderBrushColor(
            color = activeTickSolidColor(activeTrackColor)
        ),
        inactiveTickColor: SliderBrushColor = SliderBrushColor(
            color = inActiveTickSolidColor(activeTrackColor)
        ),
        disabledActiveTickColor: SliderBrushColor = SliderBrushColor(
            color = disabledActiveTickSolidColor(color = activeTickColor)
        ),
        disabledInactiveTickColor: SliderBrushColor = SliderBrushColor(
            color = disabledInactiveTickSolidColor(
                color = disabledInactiveTrackColor
            )
        )
    ): MaterialSliderColors {

        return DefaultMaterialSliderColors(
            thumbColor = thumbColor,
            disabledThumbColor = disabledThumbColor,
            activeTrackColor = activeTrackColor,
            inactiveTrackColor = inactiveTrackColor,
            disabledActiveTrackColor = disabledActiveTrackColor,
            disabledInactiveTrackColor = disabledInactiveTrackColor,
            activeTickColor = activeTickColor,
            inactiveTickColor = inactiveTickColor,
            disabledActiveTickColor = disabledActiveTickColor,
            disabledInactiveTickColor = disabledInactiveTickColor
        )
    }

    /**
     * [ColorfulSlider] [SliderBrushColor] set with blue, white and gray [Color] theme.
     */
    @Composable
    fun materialColors(
        thumbColor: SliderBrushColor = SliderBrushColor(
            color = ThumbColor
        ),
        disabledThumbColor: SliderBrushColor = SliderBrushColor(
            color = disabledThumbSolidColor()
        ),
        activeTrackColor: SliderBrushColor = SliderBrushColor(
            color = ActiveTrackColor
        ),
        disabledActiveTrackColor: SliderBrushColor = SliderBrushColor(
            color = disabledActiveTrackSolidColor()
        ),
        inactiveTrackColor: SliderBrushColor = SliderBrushColor(
            color = InactiveTrackColor
        ),
        disabledInactiveTrackColor: SliderBrushColor = SliderBrushColor(
            color = disabledActiveTrackColor.color.copy(alpha = DisabledInactiveTrackAlpha)

        ),
        activeTickColor: SliderBrushColor = SliderBrushColor(
            color = activeTickSolidColor(activeTrackColor)
        ),
        inactiveTickColor: SliderBrushColor = SliderBrushColor(
            color = inActiveTickSolidColor(activeTrackColor)
        ),
        disabledActiveTickColor: SliderBrushColor = SliderBrushColor(
            color = disabledActiveTickSolidColor(activeTickColor)
        ),
        disabledInactiveTickColor: SliderBrushColor = SliderBrushColor(
            color = disabledInactiveTickSolidColor(disabledInactiveTrackColor)
        )
    ): MaterialSliderColors {

        return DefaultMaterialSliderColors(
            thumbColor = thumbColor,
            disabledThumbColor = disabledThumbColor,
            activeTrackColor = activeTrackColor,
            inactiveTrackColor = inactiveTrackColor,
            disabledActiveTrackColor = disabledActiveTrackColor,
            disabledInactiveTrackColor = disabledInactiveTrackColor,
            activeTickColor = activeTickColor,
            inactiveTickColor = inactiveTickColor,
            disabledActiveTickColor = disabledActiveTickColor,
            disabledInactiveTickColor = disabledInactiveTickColor
        )
    }

    /**
     * [ColorfulSlider] [SliderBrushColor] set with no predefined [Color]s.
     */
    @Composable
    fun customColors(
        thumbColor: SliderBrushColor,
        disabledThumbColor: SliderBrushColor,
        activeTrackColor: SliderBrushColor,
        disabledActiveTrackColor: SliderBrushColor,
        inactiveTrackColor: SliderBrushColor,
        disabledInactiveTrackColor: SliderBrushColor,
        activeTickColor: SliderBrushColor,
        inactiveTickColor: SliderBrushColor,
        disabledActiveTickColor: SliderBrushColor,
        disabledInactiveTickColor: SliderBrushColor

    ): MaterialSliderColors {

        return DefaultMaterialSliderColors(
            thumbColor = thumbColor,
            disabledThumbColor = disabledThumbColor,
            activeTrackColor = activeTrackColor,
            inactiveTrackColor = inactiveTrackColor,
            disabledActiveTrackColor = disabledActiveTrackColor,
            disabledInactiveTrackColor = disabledInactiveTrackColor,
            activeTickColor = activeTickColor,
            inactiveTickColor = inactiveTickColor,
            disabledActiveTickColor = disabledActiveTickColor,
            disabledInactiveTickColor = disabledInactiveTickColor
        )
    }

    class DefaultMaterialSliderColors(
        private val thumbColor: SliderBrushColor,
        private val disabledThumbColor: SliderBrushColor,
        private val activeTrackColor: SliderBrushColor,
        private val disabledActiveTrackColor: SliderBrushColor,
        private val inactiveTrackColor: SliderBrushColor,
        private val disabledInactiveTrackColor: SliderBrushColor,
        private val activeTickColor: SliderBrushColor = SliderBrushColor(),
        private val inactiveTickColor: SliderBrushColor = SliderBrushColor(),
        private val disabledActiveTickColor: SliderBrushColor = SliderBrushColor(),
        private val disabledInactiveTickColor: SliderBrushColor = SliderBrushColor(),
    ) : MaterialSliderColors {

        @Composable
        override fun thumbColor(enabled: Boolean): State<Brush> {
            return rememberUpdatedState(
                if (enabled) thumbColor.activeBrush
                else disabledThumbColor.activeBrush
            )
        }

        @Composable
        override fun trackColor(enabled: Boolean, active: Boolean): State<Brush> {
            return rememberUpdatedState(
                if (enabled) {
                    if (active) activeTrackColor.activeBrush
                    else inactiveTrackColor.activeBrush
                } else {
                    if (active) disabledActiveTrackColor.activeBrush
                    else disabledInactiveTrackColor.activeBrush
                }
            )
        }

        @Composable
        override fun tickColor(enabled: Boolean, active: Boolean): State<Brush> {
            return rememberUpdatedState(
                if (enabled) {
                    if (active) activeTickColor.activeBrush
                    else inactiveTickColor.activeBrush
                } else {
                    if (active) disabledActiveTickColor.activeBrush
                    else disabledInactiveTickColor.activeBrush
                }
            )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as DefaultMaterialSliderColors

            if (thumbColor != other.thumbColor) return false
            if (disabledThumbColor != other.disabledThumbColor) return false
            if (activeTrackColor != other.activeTrackColor) return false
            if (inactiveTrackColor != other.inactiveTrackColor) return false
            if (disabledActiveTrackColor != other.disabledActiveTrackColor) return false
            if (disabledInactiveTrackColor != other.disabledInactiveTrackColor) return false

            return true
        }

        override fun hashCode(): Int {
            var result = thumbColor.hashCode()
            result = 31 * result + disabledThumbColor.hashCode()
            result = 31 * result + activeTrackColor.hashCode()
            result = 31 * result + inactiveTrackColor.hashCode()
            result = 31 * result + disabledActiveTrackColor.hashCode()
            result = 31 * result + disabledInactiveTrackColor.hashCode()
            return result
        }
    }

    /**
     * Default alpha of the inactive part of the track
     */
    private const val InactiveTrackAlpha = 0.24f

    /**
     * Default alpha for the track when it is disabled but active
     */
    private const val DisabledInactiveTrackAlpha = 0.12f

    /**
     * Default alpha for the track when it is disabled and inactive
     */
    private const val DisabledActiveTrackAlpha = 0.32f

    /**
     * Default alpha of the ticks that are drawn on top of the track
     */
    private const val TickAlpha = 0.54f

    /**
     * Default alpha for tick marks when they are disabled
     */
    private const val DisabledTickAlpha = 0.12f

}

/**
 * Interface for providing thumb, track, and tick colors using [Brush]. When providing
 * a [Brush] initially a null check is done whether [SliderBrushColor.brush] is not null,
 * if it's not null gradient is drawn but when tis non-nullable color is used.
 */
@Stable
interface MaterialSliderColors {

    /**
     * Represents the [SliderBrushColor] used for the sliders's thumb, depending on [enabled].
     *
     * @param enabled whether the [Slider] is enabled or not
     */
    @Composable
    fun thumbColor(enabled: Boolean): State<Brush>

    /**
     * Represents the [Brush] used for the slider's track, depending on [enabled] and [active].
     *
     * Active part is filled with progress, so if sliders progress is 30% out of 100%, left (or
     * right in RTL) 30% of the track will be active, the rest is not active.
     *
     * @param enabled whether the [Slider] is enabled or not
     * @param active whether the part of the track is active of not
     */
    @Composable
    fun trackColor(enabled: Boolean, active: Boolean): State<Brush>

    /**
     * Represents the [Brush] used for the sliders's tick which is the dot separating steps,
     * if they are set on the slider, depending on [enabled] and [active].
     *
     * Active tick is the tick that is in the part of the track filled with progress, so if
     * sliders progress is 30% out of 100%, left (or right in RTL) 30% of the track and the ticks
     * in this 30% will be active, the rest is not active.
     *
     * @param enabled whether the [Slider] is enabled or not
     * @param active whether the part of the track this tick is in is active of not
     */
    @Composable
    fun tickColor(enabled: Boolean, active: Boolean): State<Brush>
}