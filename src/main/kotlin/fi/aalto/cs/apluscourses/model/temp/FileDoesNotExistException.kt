package fi.aalto.cs.apluscourses.model.temp

import java.nio.file.Path

class FileDoesNotExistException(
    val path: Path,
    val name: String
) : Exception("")
