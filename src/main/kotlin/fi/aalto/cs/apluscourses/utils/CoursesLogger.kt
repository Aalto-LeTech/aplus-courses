package fi.aalto.cs.apluscourses.utils

import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.annotations.NonNls

object CoursesLogger {
    private val logger: Logger = Logger.getInstance(CoursesLogger::class.java)

    fun info(@NonNls message: String) {
        logger.info(message)
    }

    fun debug(@NonNls message: String) {
        logger.debug(message)
    }

    fun warn(@NonNls message: String) {
        logger.warn(message)
    }

    fun warn(@NonNls message: String, throwable: Throwable) {
        logger.warn(message, throwable)
    }

    fun error(@NonNls message: String) {
        logger.error(message)
    }

    fun error(@NonNls message: String, throwable: Throwable) {
        logger.error(message, throwable)
    }
}
