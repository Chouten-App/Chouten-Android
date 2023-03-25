package com.chouten.app

import android.app.Activity
import android.content.Context
import android.content.res.Resources.getSystem
import android.net.NetworkCapabilities.*
import android.os.*
import android.util.Log
import android.view.*
import android.view.animation.*
import android.widget.*
import androidx.core.view.*
import java.io.*
import java.util.*
import kotlin.math.*


// half-copied from saikou https://github.com/saikou-app/saikou/blob/main/app/src/main/java/ani/saikou/Functions.kt

val Int.dp: Float get() = (this / getSystem().displayMetrics.density)
val Float.px: Int get() = (this * getSystem().displayMetrics.density).toInt()

fun logger(e: Any?, print: Boolean = true) {
    if (print)
        println(e)
}

fun saveData(fileName: String, data: Any?, activity: Context? = null) {
    tryWith {
        if (activity != null) {
            val fos: FileOutputStream = activity.openFileOutput(fileName, Context.MODE_PRIVATE)
            val os = ObjectOutputStream(fos)
            os.writeObject(data)
            os.close()
            fos.close()
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> loadData(fileName: String, activity: Context, toast: Boolean = true): T? {
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
        if (toast) Log.d("Chouten", "Error loading data $fileName")//snackString("Error loading data $fileName")
        e.printStackTrace()
    }
    return null
}

fun toast(string: String?, activity: Activity) {
    if (string != null) {
        activity.apply {
            runOnUiThread {
                Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
            }
        }
        logger(string)
    }
}