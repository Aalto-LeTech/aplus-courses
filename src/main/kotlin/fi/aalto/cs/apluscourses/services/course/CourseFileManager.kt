package fi.aalto.cs.apluscourses.services.course

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.annotations.*
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.people.Group
import fi.aalto.cs.apluscourses.utils.APlusLogger
import fi.aalto.cs.apluscourses.utils.Version
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.NonNls
import java.time.ZonedDateTime
import kotlin.io.path.Path

private class InstantConverter : Converter<Instant>() {
    override fun fromString(s: @NonNls String): Instant = Instant.parse(s)
    override fun toString(i: Instant): String = i.toString()
}

private class VersionConverter : Converter<Version>() {
    override fun fromString(s: @NonNls String): Version = Version(s)
    override fun toString(v: Version): String = v.toString()
}

@Service(Service.Level.PROJECT)
@State(
    name = "A+ Courses Project",
    storages = [Storage("aplus_project.xml")]
)
class CourseFileManager(private val project: Project) :
    SimplePersistentStateComponent<CourseFileManager.State>(State()) {
    class State : BaseState() {
        @get:XCollection(
            propertyElementName = "modules",
            style = XCollection.Style.v2
        )
        var modules: MutableList<ModuleMetadata> by list()
        var language by string(null)
        var url by string(null)
        var defaultGroupId by property(-1L)
        var importSettings by property(false)
        var initialized by property(false)

        @get:OptionTag("newsReadTime", converter = InstantConverter::class)
        var newsReadTime by property<Instant>(Instant.fromEpochSeconds(0)) { it == Instant.fromEpochSeconds(0) }

        fun increment() = incrementModificationCount()
    }

    @Tag("module")
    data class ModuleMetadata(
        @Attribute("name") val name: String,
        @Attribute("downloadedAt", converter = InstantConverter::class) var downloadedAt: Instant,
        @Attribute("version", converter = VersionConverter::class) var version: Version
    ) {
        constructor() : this("", Instant.fromEpochSeconds(0), Version.EMPTY)
    }

    fun updateSettings(language: String, aplusUrl: String, importSettings: Boolean) {
        state.language = language
        state.url = aplusUrl
        state.importSettings = importSettings
    }

    fun setDefaultGroup(group: Group) {
        state.defaultGroupId = group.id
    }

    fun setNewsRead() {
        state.newsReadTime = Clock.System.now()
        state.increment()
    }

    fun addModule(module: Module) {
        val moduleName = module.name
        val downloadedAt = Clock.System.now()
        val version = module.latestVersion
        val existing = state.modules.find { it.name == moduleName }
        if (existing != null) {
            existing.downloadedAt = downloadedAt
            existing.version = version
        } else {
            state.modules.add(ModuleMetadata(moduleName, downloadedAt, version))
        }
        state.increment()
    }

    fun getMetadata(moduleName: String): ModuleMetadata? {
        return state.modules.find { it.name == moduleName }
    }

    fun migrateOldConfig() {
        if (state.url == null) {
            val file =
                Path(project.basePath!!).resolve(Project.DIRECTORY_STORE_FOLDER).resolve("a-plus-project.json").toFile()
            if (file.exists()) {
                val oldConfig = file.readText()
                val json = Json {
                    ignoreUnknownKeys = true
                }

                @Serializable
                data class OldMetadata(val downloadedAt: String, val version: Version)

                @Serializable
                data class OldConfig(val language: String, val url: String, val modules: Map<String, OldMetadata>)

                fun oldToNew(oldName: String, old: OldMetadata) = ModuleMetadata(
                    oldName,
                    Instant.fromEpochSeconds(ZonedDateTime.parse(old.downloadedAt).toEpochSecond()),
                    old.version
                )

                val oldConfigJson = json.decodeFromString<OldConfig>(oldConfig)
                state.language = oldConfigJson.language
                state.url = oldConfigJson.url
                state.modules = oldConfigJson.modules.map { oldToNew(it.key, it.value) }.toMutableList()
                APlusLogger.info("Migrated old configuration\nOld config: $oldConfig\nNew config: $state")
            }
        }
    }

    companion object {
        fun getInstance(project: Project): CourseFileManager {
            return project.service<CourseFileManager>()
        }
    }
}