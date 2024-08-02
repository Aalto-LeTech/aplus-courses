package fi.aalto.cs.apluscourses.model.component.old

import fi.aalto.cs.apluscourses.model.temp.ModuleMetadata
import fi.aalto.cs.apluscourses.utils.Version
import io.ktor.http.*
import org.jetbrains.annotations.NonNls
import java.io.IOException
import java.time.ZonedDateTime
import java.util.*

abstract class OldModule
/**
 * Constructs a module with the given name and URL.
 *
 * @param name         The name of the module.
 * @param url          The URL from which the module can be downloaded.
 * @param changelog    A string containing changes for an update.
 * @param version      A version number that uniquely identifies different versions of the same
 * module.
 * @param localVersion The current downloaded version number.
 * @param downloadedAt The time when the module was downloaded.
 */ protected constructor(
    val name: String,
    val url: Url,
    var changelog: String,
    @JvmField
    protected var version: Version,
    @JvmField
    protected var localVersion: Version?,
    protected var downloadedAt: ZonedDateTime,
    override var originalName: String = name
) : OldComponent(name) {
    /* synchronize with this when accessing variable fields of this class */
    private val moduleLock = Any()

    @Throws(IOException::class)
    override fun fetch() {
        fetchInternal()
        synchronized(moduleLock) {
            downloadedAt = ZonedDateTime.now()
            localVersion = version
        }
    }

    override val isUpdatable: Boolean
        /**
         * Tells whether or not the module is updatable.
         *
         * @return True, if the module is loaded and the local version is not the newest one; otherwise
         * false.
         */
        get() {
//            if (stateMonitor.get() != LOADED) {
//                return false
//            }
            synchronized(moduleLock) {
                return !version.equals(localVersion)
            }
        }

    val isMajorUpdate: Boolean
        /**
         * Returns true if the major version number has changed.
         */
        get() {
//            if (stateMonitor.get() != LOADED) {
//                return false
//            }
            synchronized(moduleLock) {
                return version.major != Optional.ofNullable(
                    localVersion
                ).orElse(Version.DEFAULT).major
            }
        }

    @Throws(IOException::class)
    protected abstract fun fetchInternal()

    /**
     * Tells whether the module has local changes.
     *
     * @return True if there are local changes, otherwise false.
     */
    override fun hasLocalChanges(): Boolean {
        var downloadedAtVal: ZonedDateTime?
        synchronized(moduleLock) {
            downloadedAtVal = this.downloadedAt
        }
        if (downloadedAtVal == null) {
            return false
        }
        return hasLocalChanges(downloadedAtVal!!)
    }

    protected abstract fun hasLocalChanges(downloadedAt: ZonedDateTime): Boolean

    val metadata: ModuleMetadata
        /**
         * Returns metadata (data that should be stored locally) of the module.
         *
         * @return A [ModuleMetadata] object.
         */
        get() {
            synchronized(moduleLock) {
                return ModuleMetadata(
                    Optional.ofNullable(
                        localVersion
                    ).orElse(version), downloadedAt
                )
            }
        }

    /**
     * Returns the version ID (not local).
     *
     * @return Version ID.
     */
    fun getVersion(): Version {
        synchronized(moduleLock) {
            return version
        }
    }

    fun getLocalVersion(): Version? {
        synchronized(moduleLock) {
            return localVersion
        }
    }

    /**
     * Updates the non-local version. Returns true if the version changed, false otherwise.
     */
    fun updateVersion(newVersion: Version): Boolean {
        synchronized(moduleLock) {
            val changed = !version.equals(newVersion)
            if (changed) {
                version = newVersion
            }
            return changed
        }
    }

    /**
     * Updates the changelog.
     */
    fun updateChangelog(newChangelog: String) {
        synchronized(moduleLock) {
            changelog = newChangelog
        }
    }

    abstract fun copy(newName: String): OldModule?

    @NonNls
    override fun toString(): String = "Module{name='$originalName'}"
}
