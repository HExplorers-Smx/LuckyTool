package com.hexplorers.splashguard

import android.app.Application
import android.content.Context
import android.util.Log

/**
 * Persistent crash recovery. A failure on the Compose/UI main thread will
 * open a native safe-mode screen on the following launch, with copyable logs.
 * The normal UI remains Miuix/Compose.
 */
object CrashRecovery {
    private const val PREF = "jianjing_crash_recovery"
    private const val SAFE = "safe_mode"
    private const val TRACE = "trace"
    @Volatile private var installed = false

    @Synchronized
    fun install(application: Application) {
        if (installed) return
        installed = true
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            record(application, error)
            if (previous != null) previous.uncaughtException(thread, error)
            else android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    fun record(context: Context, error: Throwable) {
        runCatching {
            context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
                .putBoolean(SAFE, true)
                .putString(TRACE, Log.getStackTraceString(error).take(16000))
                .commit()
        }
        Log.e("JianJing", "Startup failure; next launch will show recovery", error)
    }

    fun isSafe(context: Context): Boolean =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean(SAFE, false)

    fun trace(context: Context): String =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(TRACE, "") ?: ""

    fun retry(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putBoolean(SAFE, false).apply()
    }
}
