package fi.aalto.cs.apluscourses.utils

import java.io.File
import java.nio.file.Path

object FileUtil {
    /**
     * Get all files in a directory that have been modified after a given timestamp.
     * @param directory The directory to search for files.
     * @param timestamp The timestamp to compare the last modified time of the files to in milliseconds since epoch.
     */
    fun getChangedFilesInDirectory(directory: File, timestamp: Long): List<Path> {
        return directory.walk().filter {
            it.lastModified() > timestamp && it.isFile
        }.map {
            it.toPath()
        }.toList()
    }

    fun getChangedFilesInDirectory(directory: String, timestamp: Long): List<Path> =
        getChangedFilesInDirectory(File(directory), timestamp)

    fun getAllFilesInDirectory(directory: File): List<Path> {
        return directory.walk().map {
            it.toPath()
        }.toList()
    }

    fun deleteFilesInDirectory(directory: File, skipDirectory: Path) {
        directory.walk().forEach {
            if (it.isFile && !it.toPath().startsWith(skipDirectory)) {
                it.delete()
            }
        }
        directory.walk().forEach {
            if (it.isDirectory && it.list().isEmpty()) {
                it.delete()
            }
        }
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