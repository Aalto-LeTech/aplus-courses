package fi.aalto.cs.apluscourses.model.mapping

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText

class SubmissionMappingTest {
    @Test
    fun `submitted files become multipart parts named after their form spec key`(@TempDir dir: Path) {
        val file = dir.resolve("Solution.scala").also { it.writeText("object Solution") }

        val part = toSubmissionFiles(mapOf("file1" to file)).single()

        assertEquals("file1", part.fieldName)
        assertEquals("Solution.scala", part.fileName)
        assertEquals("object Solution", part.content.decodeToString())
    }

    @Test
    fun `submitting no files yields no parts`() {
        assertTrue(toSubmissionFiles(emptyMap()).isEmpty())
    }

}
