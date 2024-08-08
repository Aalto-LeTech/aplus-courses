package fi.aalto.cs.apluscourses.model.temp

import fi.aalto.cs.apluscourses.utils.Version
import java.time.ZonedDateTime

data class ModuleMetadata(val version: Version, val downloadedAt: ZonedDateTime)