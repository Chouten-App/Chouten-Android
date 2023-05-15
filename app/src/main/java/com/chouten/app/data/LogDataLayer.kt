package com.chouten.app.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.chouten.app.ModuleLayer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogDataLayer() {
    private var _logEntries = mutableStateListOf<LogEntry>()
    val logEntries: List<LogEntry>
        get() = _logEntries

    fun addLogEntry(entry: LogEntry) {
        Log.d("CHOUTEN/LOG", entry.message)
        _logEntries.add(entry)
    }

    fun clearLogs() {
        _logEntries.clear()
    }
}

data class LogEntry(
    val timestamp: String = SimpleDateFormat(
        "HH:mm:ss",
        Locale.getDefault()
    ).format(Date()),
    val title: String,
    val module: ModuleModel = ModuleLayer.selectedModule ?: throw Exception("No module selected"),
    val message: String,
    val isError: Boolean,
)