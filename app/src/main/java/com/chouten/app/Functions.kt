package com.chouten.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.NetworkCapabilities.*
import android.net.Uri
import android.os.*
import android.os.Build.VERSION.SDK_INT
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.animation.*
import android.widget.*
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.view.*
import com.chouten.app.data.AlertData
import com.chouten.app.data.AppPaths
import com.chouten.app.data.ModuleModel
import com.chouten.app.data.RequestCodes
import com.chouten.app.data.SnackbarVisualsWithError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile
import kotlin.math.*


// half-copied from saikou https://github.com/saikou-app/saikou/blob/main/app/src/main/java/ani/saikou/Functions.kt

@Suppress("UNCHECKED_CAST")
fun <T> loadData(
    fileName: String,
    activity: Context,
): T? {
    try {
        if (activity.fileList() != null)
            if (fileName in activity.fileList()) {
                val fileIS: FileInputStream = activity.openFileInput(fileName)
                val objIS = ObjectInputStream(fileIS)
                val data = objIS.readObject() as T
                objIS.close()
                fileIS.close()
                return data
            }
    } catch (e: Exception) {
        PrimaryDataLayer.enqueueSnackbar(
            SnackbarVisualsWithError(
                "Error Loading $fileName",
                true
            )
        )
        e.printStackTrace()
    }
    return null
}

fun createAppDirectory() {
    try {
        AppPaths._toCreate.forEach { dir ->
            val path = File(AppPaths.baseDir, "$dir/")
            if (!path.isDirectory) path.mkdirs()
            AppPaths.addedDirs[dir] = path
        }
    } catch (e: IOException) {
        PrimaryDataLayer.enqueueSnackbar(
            SnackbarVisualsWithError(
                "Could not create Chouten Directories",
                true
            )
        )

        e.localizedMessage?.let { Log.e("CHOUTEN", it) }
        e.printStackTrace()
    }
}

fun checkPermissions() {
    val requiresFilesPerms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        !Environment.isExternalStorageManager()
    } else {
        // Check for storage permissions
        val permission = ActivityCompat.checkSelfPermission(
            App,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        permission != android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    if (requiresFilesPerms) {
        PrimaryDataLayer.enqueueAlert(
            AlertData(
                title = "Grant Permissions",
                message = "Chouten needs access to all files to function properly.",
                confirmButtonText = "Grant Permissions",
                confirmButtonAction = {
                    startActivityForResult(
                        App,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = Uri.parse("package:${App.packageName}")
                            }
                        } else {
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts(
                                    "package",
                                    App.packageName,
                                    null
                                )
                            }
                        },
                        RequestCodes.allowAllFiles,
                        null
                    )
                },
                icon = Icons.Filled.Settings
            )
        )
    }
}

fun Int.toBoolean() = this == 1

/**
 * UnzipUtils class extracts files and sub-directories of a standard zip file to
 * a destination directory.
 */
object UnzipUtils {
    /**
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    @Throws(IOException::class)
    fun unzip(zipFilePath: File, destDirectory: String) {

        File(destDirectory).run {
            if (!exists()) mkdirs()
        }

        ZipFile(zipFilePath).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    val filePath = destDirectory + File.separator + entry.name
                    if (!entry.name.contains("/")) {
                        // if the entry is a file, extracts it
                        extractFile(input, filePath)
                    } else {
                        // if the entry is a directory, make the directory
                        val dirPath = filePath.substring(0, filePath.lastIndexOf("/"))
                        File(dirPath).mkdirs()
                        if (!entry.name.endsWith("/")) extractFile(input, filePath)

                    }

                }

            }
        }
    }

    /**
     * Extracts a zip entry (file entry)
     * @param inputStream
     * @param destFilePath
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun extractFile(inputStream: InputStream, destFilePath: String) {
        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
        val bytesIn = ByteArray(inputStream.available())
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }
}

operator fun SnapshotStateList<ModuleModel>.get(moduleId: String): ModuleModel {
    return this.first { it.id == moduleId }
}
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Long.formatMinSec(): String {
    return if (this == 0L) {
        "00:00"
    } else {
        String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(this),
            TimeUnit.MILLISECONDS.toSeconds(this) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(this)
                    )
        )
    }
}

/**
 * [Linear Interpolation](https://en.wikipedia.org/wiki/Linear_interpolation) function that moves
 * amount from it's current position to start and amount
 * @param start of interval
 * @param end of interval
 * @param amount e closed unit interval [0, 1]
 */
internal fun lerp(start: Float, end: Float, amount: Float): Float {
    return (1 - amount) * start + amount * end
}
/**
 * Scale x1 from start1..end1 range to start2..end2 range
 */
internal fun scale(start1: Float, end1: Float, pos: Float, start2: Float, end2: Float) =
    lerp(start2, end2, calculateFraction(start1, end1, pos))
/**
 * Scale x.start, x.endInclusive from a1..b1 range to a2..b2 range
 */
internal fun scale(
    start1: Float,
    end1: Float,
    range: ClosedFloatingPointRange<Float>,
    start2: Float,
    end2: Float
) =
    scale(start1, end1, range.start, start2, end2)..scale(
        start1,
        end1,
        range.endInclusive,
        start2,
        end2
    )
/**
 * Calculate fraction for value between a range [end] and [start] coerced into 0f-1f range
 */
fun calculateFraction(start: Float, end: Float, pos: Float) =
    (if (end - start == 0f) 0f else (pos - start) / (end - start)).coerceIn(0f, 1f)

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }

    val transition = rememberInfiniteTransition()
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
        )
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                Color(0xFF8F8B8B),
                MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

// Remove ripple effect from clickable elements
class NoRippleInteractionSource : MutableInteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) {}
    override fun tryEmit(interaction: Interaction) = true
}