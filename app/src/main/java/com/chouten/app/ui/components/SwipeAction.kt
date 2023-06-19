package com.chouten.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


data class SwipeActionsConfig(
    val threshold: Float,
    val icon: ImageVector,
    val iconTint: Color,
    val background: Color,
    val stayDismissed: Boolean,
    val onDismiss: () -> Unit,
)

val DefaultSwipeActionsConfig = SwipeActionsConfig(
    threshold = 0.5f,
    icon = Icons.Default.Menu,
    iconTint = Color.Transparent,
    background = Color.Transparent,
    stayDismissed = false,
    onDismiss = {},
)

@OptIn(
    ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun SwipeActions(
    modifier: Modifier = Modifier,
    startActionsConfig: SwipeActionsConfig = DefaultSwipeActionsConfig,
    endActionsConfig: SwipeActionsConfig = DefaultSwipeActionsConfig,
    content: @Composable (DismissState) -> Unit,
) = BoxWithConstraints(modifier) {
    val width = constraints.maxWidth.toFloat()
    val height = constraints.maxHeight.toFloat()
    var willDismissDirection: DismissDirection? by remember {
        mutableStateOf(null)
    }
    val state = rememberDismissState(
        confirmStateChange = {
            if (willDismissDirection == DismissDirection.StartToEnd
                && it == DismissValue.DismissedToEnd
            ) {
                startActionsConfig.onDismiss()
                startActionsConfig.stayDismissed
            } else if (willDismissDirection == DismissDirection.EndToStart &&
                it == DismissValue.DismissedToStart
            ) {
                endActionsConfig.onDismiss()
                endActionsConfig.stayDismissed
            } else {
                false
            }
        }
    )
    LaunchedEffect(key1 = Unit, block = {
        snapshotFlow { state.offset.value }
            .collect {
                willDismissDirection = when {
                    it > width * startActionsConfig.threshold -> DismissDirection.StartToEnd
                    it < -width * endActionsConfig.threshold -> DismissDirection.EndToStart
                    else -> null
                }
            }
    })
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(key1 = willDismissDirection, block = {
        if (willDismissDirection != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    })
    val dismissDirections by remember(startActionsConfig, endActionsConfig) {
        derivedStateOf {
            mutableSetOf<DismissDirection>().apply {
                if (startActionsConfig != DefaultSwipeActionsConfig) add(DismissDirection.StartToEnd)
                if (endActionsConfig != DefaultSwipeActionsConfig) add(DismissDirection.EndToStart)
            }
        }
    }
    SwipeToDismiss(
        state = state,
        modifier = Modifier,
        directions = dismissDirections,
        dismissThresholds = {
            if (it == DismissDirection.StartToEnd)
                FractionalThreshold(startActionsConfig.threshold)
            else FractionalThreshold(endActionsConfig.threshold)
        },
        background = {
            AnimatedContent(
                targetState = Pair(state.dismissDirection, willDismissDirection != null),
                transitionSpec = {
                    fadeIn(
                        tween(0),
                        initialAlpha = if (targetState.second) 1f else 0f,
                    ) togetherWith fadeOut(
                        tween(0),
                        targetAlpha = if (targetState.second) .7f else 0f,
                    )
                }
            ) { (direction, willDismiss) ->
                val revealSize = remember { Animatable(if (willDismiss) 0f else 1f) }
                val iconSize = remember { Animatable(if (willDismiss) .8f else 1f) }
                LaunchedEffect(key1 = Unit, block = {
                    if (willDismiss) {
                        revealSize.snapTo(0f)
                        launch {
                            revealSize.animateTo(1f, animationSpec = tween(400))
                        }
                        iconSize.snapTo(.8f)
                        iconSize.animateTo(
                            1.45f,
                            spring(
                                dampingRatio = Spring.DampingRatioHighBouncy,
                            )
                        )
                        iconSize.animateTo(
                            1f,
                            spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                            )
                        )
                    }
                })
                Box(
                    modifier = Modifier
                        .fillMaxWidth(1F)
                        .height(65.dp)
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            color = when (direction) {
                                DismissDirection.StartToEnd -> if (willDismiss) startActionsConfig.background else startActionsConfig.iconTint
                                DismissDirection.EndToStart -> if (willDismiss) endActionsConfig.background else endActionsConfig.iconTint
                                else -> Color.Transparent
                            },
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .align(
                                when (direction) {
                                    DismissDirection.StartToEnd -> Alignment.CenterStart
                                    else -> Alignment.CenterEnd
                                }
                            )
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .scale(iconSize.value)
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = (10 * (1f - iconSize.value)).roundToInt()
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (direction) {
                            DismissDirection.StartToEnd -> {
                                Image(
                                    painter = rememberVectorPainter(image = startActionsConfig.icon),
                                    colorFilter = ColorFilter.tint(if (willDismiss) startActionsConfig.iconTint else startActionsConfig.background),
                                    contentDescription = null
                                )
                            }

                            DismissDirection.EndToStart -> {
                                Image(
                                    painter = rememberVectorPainter(image = endActionsConfig.icon),
                                    colorFilter = ColorFilter.tint(if (willDismiss) endActionsConfig.iconTint else endActionsConfig.background),
                                    contentDescription = null
                                )
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    ) {
        content(state)
    }
}