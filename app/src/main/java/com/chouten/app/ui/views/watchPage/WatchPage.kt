package com.chouten.app.ui.views.watchPage

import android.app.Activity
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun WatchPage(
    provider: WatchPageViewModel
) {
    val context = LocalContext.current
    val orientationOnLoad = rememberSaveable {
        (context as Activity).requestedOrientation
    }

    if (provider.sources.isNotEmpty()) {
        val _player = remember {
            ExoPlayer.Builder(
                context
            ).build()
                .apply {
                    setMediaItem(
                        MediaItem.fromUri(
                            Uri.parse(
                                provider.sources.first().file
                            )
                        )
                    )
                    prepare()
                }
        }
        _player.playWhenReady = true
        _player.videoScalingMode =
            C.VIDEO_SCALING_MODE_SCALE_TO_FIT
        _player.repeatMode = ExoPlayer.REPEAT_MODE_OFF

        // Force the user's screen to
        // be oriented to landscape.
        (context as Activity).requestedOrientation =
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        DisposableEffect(
            AndroidView(
                factory = {
                    PlayerView(context).apply {
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                        player = _player
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                }
            )
        ) {
            // Restore the user's screen to
            // its original orientation.
            context.requestedOrientation = orientationOnLoad

            onDispose {
                _player.release()
            }
        }
    }
}