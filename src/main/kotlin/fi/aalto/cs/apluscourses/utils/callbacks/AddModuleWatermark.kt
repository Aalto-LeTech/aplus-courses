package fi.aalto.cs.apluscourses.utils.callbacks

import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.model.Course
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.people.User
import fi.aalto.cs.apluscourses.services.course.CourseManager
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.Locale
import java.util.stream.Collectors

object AddModuleWatermark {
    private const val ENCODING_LINE = "# -*- coding: utf-8 -*-"
    private const val ENCODING_LINE_WITH_UNICODE =
        "#\u200b\u200c\u200b\u200b\u200b\u200b\u200c\u200c\u200b\u200c\u200b\u200c\u200c\u200b *-* coding: utf-8 *-*"

    @Throws(IOException::class)
    private fun addWatermark(path: Path, user: User?, module: Module, course: Course?) {
        val userId = user?.aplusId ?: 0
        val studentId = user?.studentId ?: "---"
        val studentName = user?.userName ?: "---"
        val courseName = course?.name ?: "---"

        val newLines = Files.lines(path).use { lineStream ->
            lineStream.filter { line ->
                line != ENCODING_LINE && line != ENCODING_LINE_WITH_UNICODE
            }.collect(Collectors.toCollection { mutableListOf<String>() })
        }

        val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        newLines.addAll(
            0, listOf(
                ENCODING_LINE,
                "# Name: $studentName",
                "# Student ID: $studentId",
                "# $courseName",
                "# Date: ${date.dayOfMonth}/${date.monthNumber}/${date.year}",
                "# Version: ${module.latestVersion}",
            )
        )

        val hash = generateHash(userId.toString())

        newLines.addAll(
            listOf(
                "",
                "# Do not remove or modify this",
                "# Unique hash: $hash"
            )
        )

        Files.write(path, newLines)
    }

    private fun generateHash(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Adds a watermark to a freshly downloaded module. This is used in TRAKY courses.
     *
     * @param project The project
     * @param module  The currently extracted module
     */
    fun postDownloadModule(project: Project, module: Module) {
        val user = CourseManager.user(project)
        val course = CourseManager.course(project)

        try {
            Files.walk(module.fullPath).use { entries ->
                entries.forEach { path: Path ->
                    val file = path.toFile()
                    if (file.isFile && file.name.lowercase(Locale.getDefault()).endsWith(".py")) {
                        try {
                            addWatermark(path, user, module, course)
                        } catch (e: IOException) {
                            throw UncheckedIOException(e)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}
