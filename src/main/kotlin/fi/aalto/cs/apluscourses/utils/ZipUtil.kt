package fi.aalto.cs.apluscourses.utils

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ZipUtil {
    fun unzip(zip: File, destination: File, onlyPath: String? = null) {
        val canonicalDest = destination.canonicalFile.toPath()

        ZipFile(zip).use { zf ->
            zf.entries().asSequence().forEach { entry ->
                if (onlyPath != null && !entry.name.contains(onlyPath)) return@forEach

                // Skip the root directory entry "/"
                if (entry.name == "/") return@forEach

                val resolved = canonicalDest.resolve(entry.name).normalize()
                if (!resolved.startsWith(canonicalDest)) {
                    throw IOException("Unsafe zip entry: ${entry.name}")
                }

                val outFile = resolved.toFile()
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile.mkdirs()
                    zf.getInputStream(entry).use { input ->
                        outFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a zip file from a directory
     *
     * @param sourceDir The directory to zip
     * @param zipFile The output zip file
     * @param additionalEntries Map of additional entries to include in the zip (entryName to content)
     */
    fun zip(sourceDir: File, zipFile: File, additionalEntries: Map<String, ByteArray> = emptyMap()) {
        if (zipFile.exists()) {
            zipFile.delete()
        }

        // Keep track of entries we've already added to avoid duplicates
        val addedEntries = mutableSetOf<String>()

        FileOutputStream(zipFile).use { fos ->
            ZipOutputStream(fos).use { zos ->
                // Add additional entries first
                additionalEntries.forEach { (entryName, content) ->
                    val normalizedEntryName = entryName.replace('\\', '/')
                    if (addedEntries.add(normalizedEntryName)) {
                        zos.putNextEntry(ZipEntry(normalizedEntryName))
                        zos.write(content)
                        zos.closeEntry()
                    }
                }

                // Add files from the source directory
                sourceDir.walkTopDown().forEach { file ->
                    // File names in ZIP should always use forward slashes.
                    // See section 4.4.17 of the ".ZIP File Format Specification" v6.3.6 FINAL.
                    val relativePath = file.relativeTo(sourceDir)
                        .invariantSeparatorsPath.replace('\\', '/')

                    val entryName = if (file.isDirectory) {
                        "$relativePath/"
                    } else {
                        relativePath
                    }

                    // Only add the entry if we haven't added it already
                    if (addedEntries.add(entryName)) {
                        val zipEntry = ZipEntry(entryName)
                        zos.putNextEntry(zipEntry)
                        if (file.isFile) {
                            file.inputStream().use { it.copyTo(zos) }
                        }
                        zos.closeEntry()
                    }
                }
            }
        }
    }
}