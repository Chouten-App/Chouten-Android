package com.chouten.app

import android.content.Context
import android.net.NetworkCapabilities.*
import android.os.*
import android.util.Log
import android.view.*
import android.view.animation.*
import android.widget.*
import androidx.core.view.*
import com.chouten.app.data.AppPaths
import com.chouten.app.data.SnackbarVisualsWithError
import java.io.*
import java.util.*
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