package com.example.util

import android.util.Log
import com.example.data.model.DiagnosticLogEntry
import com.example.data.model.DiagnosticSeverity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object MediaDiagnostics {
    private const val TAG = "OpenCutDiagnostics"
    private const val MAX_ENTRIES = 120

    private val _logs = MutableStateFlow<List<DiagnosticLogEntry>>(emptyList())
    val logs: StateFlow<List<DiagnosticLogEntry>> = _logs.asStateFlow()

    fun info(category: String, message: String, details: String = "") {
        Log.i("$TAG/$category", if (details.isBlank()) message else "$message | $details")
        append(category, DiagnosticSeverity.INFO, message, details)
    }

    fun warn(category: String, message: String, details: String = "") {
        Log.w("$TAG/$category", if (details.isBlank()) message else "$message | $details")
        append(category, DiagnosticSeverity.WARNING, message, details)
    }

    fun error(category: String, message: String, details: String = "", throwable: Throwable? = null) {
        val fullDetails = buildString {
            if (details.isNotBlank()) append(details)
            if (throwable != null) {
                if (isNotEmpty()) append(" | ")
                append(throwable.javaClass.simpleName)
                append(": ")
                append(throwable.message ?: "unknown")
            }
        }
        Log.e("$TAG/$category", "$message | $fullDetails", throwable)
        append(category, DiagnosticSeverity.ERROR, message, fullDetails)
    }

    fun clear() {
        _logs.value = emptyList()
    }

    private fun append(
        category: String,
        severity: DiagnosticSeverity,
        message: String,
        details: String
    ) {
        val entry = DiagnosticLogEntry(
            category = category,
            severity = severity,
            message = message,
            details = details
        )
        _logs.update { current ->
            (listOf(entry) + current).take(MAX_ENTRIES)
        }
    }
}
