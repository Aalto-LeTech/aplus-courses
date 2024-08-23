package fi.aalto.cs.apluscourses.utils

import com.intellij.openapi.diagnostic.Logger

object APlusLogger {
    val logger: Logger = Logger.getInstance(APlusLogger::class.java)
    fun info(message: String) {
        logger.info(message)
    }
}
