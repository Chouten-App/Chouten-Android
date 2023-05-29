package com.chouten.app

import android.content.Context
import android.content.Intent
import android.net.NetworkCapabilities.*
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.animation.*
import android.widget.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.view.*
import com.chouten.app.data.AlertData
import com.chouten.app.data.AppPaths
import com.chouten.app.data.ModuleModel
import com.chouten.app.data.RequestCodes
import com.chouten.app.data.SnackbarVisualsWithError
import java.io.*
import java.util.*
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
 *
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
                    println("Extracting: $filePath")

                    if (!entry.isDirectory) {
                        println("Extracting file: $filePath")
                        // if the entry is a file, extracts it
                        extractFile(input, filePath)
                    } else {
                        println("Extracting directory: $filePath")
                        // if the entry is a directory, make the directory
                        val dir = File(filePath)
                        dir.mkdir()
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