package kr.stonecold.removeemergencycall.common

import android.util.Log
import io.github.libxposed.api.XposedInterface
import kr.stonecold.removeemergencycall.BuildConfig

@Suppress("unused")
object XposedUtil {
    private var xposed: XposedInterface? = null

    enum class LogLevel(val level: Int) {
        TRACE(0),
        DEBUG(1),
        VERBOSE(2),
        INFO(3),
        WARNING(4),
        ERROR(5),
        FATAL(6),
        NONE(7)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var currentLogLevel: LogLevel = if (BuildConfig.DEBUG) {
        LogLevel.DEBUG
    } else {
        LogLevel.INFO
    }

    fun attach(xposed: XposedInterface) {
        this.xposed = xposed
    }

    private fun writeLog(priority: Int, tag: String, message: String) {
        xposed?.log(priority, tag, message)
    }

    fun xposedTrace(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.TRACE.level) {
            writeLog(Log.VERBOSE, tag, message)
        }
    }

    fun xposedDebug(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.DEBUG.level) {
            writeLog(Log.DEBUG, tag, message)
        }
    }

    fun xposedVerbose(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.VERBOSE.level) {
            writeLog(Log.VERBOSE, tag, message)
        }
    }

    fun xposedInfo(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.INFO.level) {
            writeLog(Log.INFO, tag, message)
        }
    }

    fun xposedWarning(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.WARNING.level) {
            writeLog(Log.WARN, tag, message)
        }
    }

    fun xposedError(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.ERROR.level) {
            writeLog(Log.ERROR, tag, message)
        }
    }

    fun xposedFatal(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.FATAL.level) {
            writeLog(Log.ASSERT, tag, message)
        }
    }

    fun xposedException(tag: String, message: String) {
        if (currentLogLevel.level <= LogLevel.ERROR.level) {
            writeLog(Log.ERROR, tag, message)
        }
    }
}
