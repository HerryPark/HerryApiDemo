package com.herry.libs.log

import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("unused")
object Trace {
    private val DEBUG = AtomicBoolean(false)

    fun setDebug(debug: Boolean) {
        DEBUG.set(debug)
    }

    private fun isDebug(): Boolean {
        return DEBUG.get()
    }

    fun e(tag: String, log: String, throwable: Throwable? = null) {
        Log.e(tag, log, throwable)
    }

    fun e(log: String, throwable: Throwable? = null) {
        val callerElement = Exception().stackTrace[1]
        this.e(callerElement.fileName, "[${callerElement.fileName} (${callerElement.lineNumber})] $log", throwable)
    }
    
    fun d(tag: String, log: String) {
        if (isDebug()) {
            Log.d(tag, log)
        }
    }

    fun d(log: String) {
        val callerElement = Exception().stackTrace[1]
        this.d(callerElement.fileName, "[${callerElement.fileName} (${callerElement.lineNumber})] $log")
    }

    fun w(tag: String, log: String) {
        if (isDebug()) {
            Log.w(tag, log)
        }
    }

    fun w(log: String) {
        val callerElement = Exception().stackTrace[1]
        this.w(callerElement.fileName, "[${callerElement.fileName} (${callerElement.lineNumber})] $log")
    }

    fun i(tag: String, log: String) {
        if (isDebug()) {
            Log.i(tag, log)
        }
    }

    fun i(log: String) {
        val callerElement = Exception().stackTrace[1]
        this.i(callerElement.fileName, "[${callerElement.fileName} (${callerElement.lineNumber})] $log")
    }

    fun v(tag: String, log: String) {
        if (isDebug()) {
            Log.v(tag, log)
        }
    }

    fun v(log: String) {
        val callerElement = Exception().stackTrace[1]
        this.v(callerElement.fileName, "[${callerElement.fileName} (${callerElement.lineNumber})] $log")
    }
}