package fi.aalto.cs.apluscourses.ui.module

import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.model.component.old.OldComponent
import fi.aalto.cs.apluscourses.model.component.old.OldModule
import icons.PluginIcons
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.swing.Icon

class ModuleViewModel(private val module: OldModule) {//: ListElementViewModel<Module?>(module) {
//    init {
//        module.stateChanged.addListener(this) { obj: ModuleViewModel -> obj.onChanged() }
//    }

    val get = module

    val name: String
        get() = module.name

    val url: String
        get() = module.url.toString()

    val tooltip: String
        /**
         * Returns the changelog if it's defined and an update is available,
         * else the timestamp of the module if it's defined, else the URL.
         *
         * @return A [String] with info about the module.
         */
        get() {
            val timestamp = module.metadata.downloadedAt
            val changelog = module.changelog
            if (isUpdateAvailable && changelog.isNotEmpty()) {
                return MyBundle.message("presentation.moduleTooltip.changelog", changelog)
            } else if (timestamp != null) {
                return MyBundle.message(
                    "presentation.moduleTooltip.timestamp",
                    timestamp.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
                )
            }
            return MyBundle.message("presentation.moduleTooltip.moduleURL", url)
        }

    val isUpdateAvailable: Boolean
        get() = module.isUpdatable

    val canOpenDocumentation: Boolean
        get() = module.documentationExists()

    private val errorStatus: String
        get() {
            if (module.errorCause == OldComponent.ERR_FILES_MISSING) {
                return MyBundle.message("presentation.moduleStatuses.errorFilesMissing")
            }
            return MyBundle.message("presentation.moduleStatuses.errorUnknown")
        }

    val statusOld: String
        /**
         * Returns a textual representation of the status.
         *
         * @return A [String] describing the status.
         */
        get() {
            return "status missing TODO"
//            when (module.stateMonitor.get()) {
//                OldComponent.UNRESOLVED -> return PluginResourceBundle.getText("presentation.moduleStatuses.unknown")
//                OldComponent.NOT_INSTALLED, OldComponent.FETCHED -> return PluginResourceBundle.getText("presentation.moduleStatuses.doubleClickToInstall")
//                OldComponent.FETCHING -> return PluginResourceBundle.getText("presentation.moduleStatuses.downloading")
//                OldComponent.LOADING -> return PluginResourceBundle.getText("presentation.moduleStatuses.installing")
//                OldComponent.LOADED -> {}
//                OldComponent.UNINSTALLING -> return PluginResourceBundle.getText("presentation.moduleStatuses.uninstalling")
//                OldComponent.UNINSTALLED -> return PluginResourceBundle.getText("presentation.moduleStatuses.uninstalled")
//                OldComponent.ACTION_ABORTED -> return PluginResourceBundle.getText("presentation.moduleStatuses.cancelling")
//                else -> return errorStatus
//            }
//            return when (module.dependencyStateMonitor.get()) {
//                OldComponent.DEP_INITIAL -> PluginResourceBundle.getText("presentation.dependencyStatus.installedDependenciesUnknown")
//                OldComponent.DEP_WAITING -> PluginResourceBundle.getText("presentation.dependencyStatus.waitingForDependencies")
//                OldComponent.DEP_LOADED -> PluginResourceBundle.getText("presentation.dependencyStatus.installed")
//                else -> PluginResourceBundle.getText("presentation.dependencyStatus.errorInDependencies")
//            }
        }

    val icon: Icon
        get() = if (category == Category.AVAILABLE) PluginIcons.A_PLUS_MODULE_DISABLED
        else if (category == Category.INSTALLED) PluginIcons.A_PLUS_MODULE
        else if (isUpdateAvailable) PluginIcons.A_PLUS_INFO
        else PluginIcons.A_PLUS_NO_POINTS

    val status: String
        get() = if (module.hasError()) errorStatus
        else if (isUpdateAvailable) MyBundle.message("ui.toolWindow.subTab.modules.module.updateAvailable")
        else "status missing TODO"

    val info: String
        get() {
            val inner = if (category == Category.AVAILABLE) ""
            else if (category == Category.INSTALLED) "<p>Version ${module.getLocalVersion()}<br>Installed ${
                module.metadata.downloadedAt.toLocalDateTime()
            }</p>"
            else if (isUpdateAvailable) "<p>Version ${module.getLocalVersion()} Installed ${
                module.metadata.downloadedAt.toLocalDateTime()
            }</p><p>${module.getVersion()} Changelog:</p><br>${module.changelog}"
            else "PluginIcons.A_PLUS_NO_POINTS"
            return "<html><body>$inner</body></html>"
        }

    val isBoldface: Boolean = false
//        get() = module.hasError() && module.stateMonitor.get() == OldComponent.LOADED

    val isInstalling: Boolean = false
//        get() = module.stateMonitor.get() > OldComponent.NOT_INSTALLED && module.stateMonitor.get() < OldComponent.LOADED

    enum class Category {
        ACTION_REQUIRED, INSTALLED, AVAILABLE
    }

    val category: Category
        get() {
//            val state = module.stateMonitor.get()
//            val dependencyState = module.dependencyStateMonitor.get()
//            println("state: $state, dependencyState: $dependencyState, module: ${module.name}")
//            return if (state < OldComponent.LOADED) {
//                Category.AVAILABLE
//            } else if (state <= OldComponent.ERROR || dependencyState == OldComponent.DEP_ERROR || isUpdateAvailable) {
//                Category.ACTION_REQUIRED
//            } else {
//                Category.INSTALLED
//            }
            return Category.AVAILABLE
        }

}
