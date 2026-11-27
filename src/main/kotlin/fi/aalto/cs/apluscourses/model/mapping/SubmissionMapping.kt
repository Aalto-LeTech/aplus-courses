package fi.aalto.cs.apluscourses.model.mapping

import fi.aalto.cs.aplus.client.SubmissionFile
import java.nio.file.Path

/** Creates multipart file parts keyed by their form field names. */
fun toSubmissionFiles(files: Map<String, Path>): List<SubmissionFile> =
    files.map { (key, path) -> SubmissionFile(key, path.toFile()) }
