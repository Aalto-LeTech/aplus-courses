package fi.aalto.cs.apluscourses.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ZipUtilTest {
    @Test
    fun `round trip preserves nested files and empty directories and additional entries override originals`(@TempDir dir: Path) {
        val source = Files.createDirectory(dir.resolve("source"))
        Files.createDirectories(source.resolve("nested/empty"))
        Files.writeString(source.resolve("nested/answer.txt"), "answer")
        Files.writeString(source.resolve("students.txt"), "old")
        val zip = dir.resolve("module.zip").toFile()

        ZipUtil.zip(source.toFile(), zip, mapOf("students.txt" to "new".encodeToByteArray()))
        ZipFile(zip).use { archive ->
            assertEquals(1, archive.entries().asSequence().count { it.name == "students.txt" })
        }
        val out = dir.resolve("out")
        ZipUtil.unzip(zip, out.toFile())
        assertEquals("answer", Files.readString(out.resolve("nested/answer.txt")))
        assertEquals("new", Files.readString(out.resolve("students.txt")))
        assertTrue(Files.isDirectory(out.resolve("nested/empty")))
    }

    @Test
    fun `path filtering keeps matching entries and overwrite replaces old files`(@TempDir dir: Path) {
        val zip = archive(dir, listOf("keep/file.txt" to "new", "other/file.txt" to "ignore"))
        val out = Files.createDirectories(dir.resolve("out/keep")).parent
        Files.writeString(out.resolve("keep/file.txt"), "old")

        ZipUtil.unzip(zip, out.toFile(), onlyPath = "keep/")

        assertEquals("new", Files.readString(out.resolve("keep/file.txt")))
        assertFalse(Files.exists(out.resolve("other")))
    }

    @Test
    fun `extraction refuses a path that escapes the destination`(@TempDir dir: Path) {
        val zip = archive(dir, listOf("../escape.txt" to "bad"))

        assertThrows(IOException::class.java) { ZipUtil.unzip(zip, dir.resolve("out").toFile()) }
        assertFalse(Files.exists(dir.resolve("escape.txt")))
    }

    @Test
    fun `a root directory entry does not prevent extraction`(@TempDir dir: Path) {
        val zip = archive(dir, listOf("/" to "", "file.txt" to "content"))

        ZipUtil.unzip(zip, dir.resolve("out").toFile())

        assertEquals("content", Files.readString(dir.resolve("out/file.txt")))
    }

    private fun archive(dir: Path, entries: List<Pair<String, String>>) = dir.resolve("input.zip").toFile().also { zip ->
        ZipOutputStream(zip.outputStream()).use { output ->
            entries.forEach { (name, content) ->
                output.putNextEntry(ZipEntry(name))
                output.write(content.encodeToByteArray())
                output.closeEntry()
            }
        }
    }
}
