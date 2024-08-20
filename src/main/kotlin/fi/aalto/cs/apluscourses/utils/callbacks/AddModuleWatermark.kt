package fi.aalto.cs.apluscourses.utils.callbacks

import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.people.User
import fi.aalto.cs.apluscourses.services.course.CourseManager
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import java.util.function.Supplier
import java.util.stream.Collectors

object AddModuleWatermark {
    private const val ENCODING_LINE = "# -*- coding: utf-8 -*-"
    private const val ENCODING_LINE_WITH_UNICODE =
        "#\u200b\u200c\u200b\u200b\u200b\u200b\u200c\u200c\u200b\u200c\u200b\u200c\u200c\u200b *-* coding: utf-8 *-*"

    private fun createWatermark(userId: Long): String {
        return java.lang.Long.toBinaryString(userId)
            .replace('1', '\u200b')
            .replace('0', '\u200c')
    }

    @Throws(IOException::class)
    private fun addWatermark(path: Path, user: User?) {
        val userId = user?.aplusId ?: 0
        val studentId = user?.studentId ?: "---"
        val studentName = user?.userName ?: "---"

        val watermark = createWatermark(userId)

        Files.lines(path).use { lineStream ->
            val newLinesMutable = lineStream.map<String> { line: String ->
                if (line == ENCODING_LINE || line == ENCODING_LINE_WITH_UNICODE) {
                    return@map ""
                } else if (line.trim { it <= ' ' }.startsWith("#")) {
                    return@map line.replaceFirst("#".toRegex(), "#$watermark")
                }
                line
            }.collect(
                Collectors.toCollection<String, java.util.ArrayList<String>>(
                    Supplier<java.util.ArrayList<String>> { ArrayList() })
            )
            newLinesMutable.add(0, ENCODING_LINE)
            newLinesMutable.add(1, "# Nimi: $studentName")
            newLinesMutable.add(2, "# Opiskelijanumero: $studentId")
            Files.write(path, newLinesMutable)
        }
    }

    /**
     * Adds a watermark to a freshly downloaded module. This is used in TRAKY courses.
     *
     * @param project The project
     * @param module  The currently extracted module
     */
    fun postDownloadModule(project: Project, module: Module) {
        val user = CourseManager.user(project)

        try {
            Files.walk(module.fullPath).use { entries ->
                entries.forEach { path: Path ->
                    val file = path.toFile()
                    if (file.isFile && file.name.lowercase(Locale.getDefault()).endsWith(".py")) {
                        try {
                            addWatermark(path, user)
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
