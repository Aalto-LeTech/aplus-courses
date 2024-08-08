package fi.aalto.cs.apluscourses.utils

import java.io.File
import java.nio.file.Path

object FileUtil {
    /**
     * Get all files in a directory that have been modified after a given timestamp.
     * @param directory The directory to search for files.
     * @param timestamp The timestamp to compare the last modified time of the files to in milliseconds since epoch.
     */
    fun getChangedFilesInDirectory(directory: String, timestamp: Long): List<Path> {
        return File(directory).walk().filter {
            it.lastModified() > timestamp
        }.map {
            it.toPath()
        }.toList()
    }

    /**
     * Find a file in a directory by its name.
     * @param directory The directory to search for the file.
     * @param fileName The name of the file to find.
     */
    fun findFileInDirectory(directory: String, fileName: String): Path? {
        return File(directory).walk().find {
            it.name == fileName
        }?.toPath()
    }

    fun findFileInDirectoryStartingWith(directory: String, fileNameStart: String): Path? {
        return File(directory).walk().find {
            it.name.startsWith(fileNameStart)
        }?.toPath()
    }
}