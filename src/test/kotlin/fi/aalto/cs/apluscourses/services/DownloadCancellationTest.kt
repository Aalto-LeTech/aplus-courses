package fi.aalto.cs.apluscourses.services

import fi.aalto.cs.apluscourses.utils.ZipUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DownloadCancellationTest {
    private fun zipOf(dir: Path, entries: Int): File {
        val zip = dir.resolve("many.zip").toFile()
        ZipOutputStream(zip.outputStream()).use { out ->
            repeat(entries) { i ->
                out.putNextEntry(ZipEntry("file$i.txt"))
                out.write("x".repeat(64).toByteArray())
                out.closeEntry()
            }
        }
        return zip
    }

    @Test
    fun `unzip stops when the caller aborts between entries`(@TempDir dir: Path) {
        val zip = zipOf(dir, entries = 500)
        val target = dir.resolve("out").toFile()
        var seen = 0

        assertThrows(IllegalStateException::class.java) {
            ZipUtil.unzip(zip, target) {
                seen++
                check(seen < 10) { ABORT_MESSAGE }
            }
        }

        assertTrue(seen < 20, "extraction should stop at the abort, saw $seen entries")
        assertTrue(
            (target.listFiles()?.size ?: 0) < 20,
            "only the entries before the abort should have been written"
        )
    }

    @Test
    fun `unzip honours coroutine cancellation`(@TempDir dir: Path): Unit = runBlocking {
        val zip = zipOf(dir, entries = 2000)
        val target = dir.resolve("out").toFile()

        val reachedAnEntry = CountDownLatch(1)
        val cancelled = CountDownLatch(1)

        val job = async(Dispatchers.IO) {
            val self = requireNotNull(coroutineContext[Job])
            ZipUtil.unzip(zip, target) {
                if (reachedAnEntry.count > 0) {
                    reachedAnEntry.countDown()
                    cancelled.await()
                }
                self.ensureActive()
            }
        }
        assertTrue(
            reachedAnEntry.await(10, TimeUnit.SECONDS),
            "unzip never consulted the per-entry callback"
        )
        job.cancel()
        cancelled.countDown()

        assertThrows(CancellationException::class.java) { runBlocking { job.await() } }
        assertTrue(
            (target.listFiles()?.size ?: 0) < 2000,
            "cancellation should have stopped it short of the whole archive"
        )
    }

    private companion object {
        @NonNls
        const val ABORT_MESSAGE = "aborted"
    }
}
