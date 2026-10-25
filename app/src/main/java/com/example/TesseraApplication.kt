package com.example

import android.app.Application
import android.content.Context
import android.util.Log

class TesseraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupGlobalCrashHandler()
    }

    private fun setupGlobalCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("TesseraApplication", "Uncaught exception in thread ${thread.name}: ${throwable.message}", throwable)
                val stackTrace = Log.getStackTraceString(throwable)
                getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString(
                        "LAST_SERVICE_CRASH",
                        "[Global:${thread.name}] ${throwable.javaClass.simpleName}: ${throwable.message}\n$stackTrace"
                    )
                    .commit() // Commit síncrono para garantir persistência antes da saída do processo
            } catch (t: Throwable) {
                Log.e("TesseraApplication", "Failed to save crash log", t)
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
