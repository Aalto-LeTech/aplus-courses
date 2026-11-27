package fi.aalto.cs.apluscourses.utils

import com.intellij.util.io.Compressor
import com.intellij.util.io.Decompressor
import java.io.File

object ZipUtil {
    /** [onEntry] runs before filtering each entry, allowing the caller to cancel extraction. */
    fun unzip(zip: File, destination: File, onlyPath: String? = null, onEntry: () -> Unit = {}) {
        Decompressor.Zip(zip.toPath()).filter { name ->
            onEntry()
            name != "/" && (onlyPath == null || name.contains(onlyPath))
        }.extract(destination.toPath())
    }

    /** Additional entries take precedence over files with the same path in [sourceDir]. */
    fun zip(sourceDir: File, zipFile: File, additionalEntries: Map<String, ByteArray> = emptyMap()) {
        val addedEntries = mutableSetOf<String>()
        Compressor.Zip(zipFile.toPath()).use { archive ->
            archive.filter { name, _ -> addedEntries.add(name) }
            additionalEntries.forEach { (name, content) -> archive.addFile(name, content) }
            archive.addDirectory(sourceDir.toPath())
        }
    }
}
