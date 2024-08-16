package fi.aalto.cs.apluscourses.utils.temp

import com.intellij.ide.plugins.PluginEnabler
import com.intellij.ide.plugins.PluginManagerCore.isPluginInstalled
import com.intellij.ide.plugins.PluginManagerCore.getPlugin
import com.intellij.ide.plugins.PluginManagerMain
import com.intellij.ide.plugins.PluginNode
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.annotations.RequiresEdt
import fi.aalto.cs.apluscourses.api.CourseConfig.RequiredPlugin
import fi.aalto.cs.apluscourses.utils.APlusLogger

object PluginAutoInstaller {
    private val logger: com.intellij.openapi.diagnostic.Logger = APlusLogger.logger

    private fun shouldDownloadPlugin(id: PluginId): Boolean {
        return !isPluginInstalled(id)
    }

    private fun shouldEnablePlugin(id: PluginId): Boolean {
        val plugin: com.intellij.ide.plugins.IdeaPluginDescriptor? = getPlugin(id)
        return plugin != null && !plugin.isEnabled
    }

    /**
     * Verifies that all plugins with given plugin IDs are installed and enabled. If not, the function
     * asks the user for consent to install and enable all missing plugins.
     *
     * @param project     The project which initiated the operation.
     * @param pluginNames List of plugin ID strings. The plugin ID can be found on the plugin's
     * JetBrains Marketplace website.
     * @param callback    An interface implementing functions invoked by this method. The functions
     * are related to showing the consent dialog box to the user.
     * @return True, if all dependencies are satisfied and there's nothing to be done. False, if
     * some dependencies were missing, and they were installed or enabled. Null, if the operation
     * has been cancelled due to missing consent or an error.
     */
    @RequiresEdt
    fun ensureDependenciesInstalled(
        project: Project,
        pluginNames: List<RequiredPlugin>,
//        callback: PluginInstallerCallback
    ): Boolean? {
        val pluginIDs = pluginNames.map { PluginId.getId(it.id) }.toSet()
        // Downloading and enabling plugins are different operations, so we handle these separately.
        val pluginsToDownload = pluginIDs.filter { shouldDownloadPlugin(it) }.toSet()
        val pluginsToEnable = pluginIDs.filter { shouldEnablePlugin(it) }.toSet()

        pluginsToDownload.forEach { logger.info("Plugin to download: ${it.idString}") }
        pluginsToEnable.forEach { logger.info("Plugin to enable: ${it.idString}") }


        if (pluginsToDownload.isEmpty() && pluginsToEnable.isEmpty()) {
            return true
        }

        // allPluginNames = list of PluginDependencies for all plugins in pluginsToDownload or pluginsToEnable
        val missingPluginIds = (pluginsToDownload + pluginsToEnable).toList()
            .map { it.idString }
            .toSet()

        val allPluginNames = pluginNames
            .filter { p: RequiredPlugin -> missingPluginIds.contains(p.id) }


//        val consentResult: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
//            callback.askForInstallationConsent(allPluginNames)
//        if (consentResult == PluginInstallerCallback.ConsentResult.IGNORE_INSTALL) {
//            return true // pretend that everything went fine
//        } else if (consentResult == PluginInstallerCallback.ConsentResult.REJECTED) {
//            return null // signal to the caller to abort the operation
//        }

        // Enable all requires plugins that are installed, but disabled.
        PluginEnabler.getInstance().enableById(pluginsToEnable)

//        val downloadablePluginNodes: kotlin.collections.MutableList<PluginNode?> = pluginsToDownload.stream()
//            .map<PluginNode?> { id: com.intellij.openapi.extensions.PluginId? -> PluginNode(id) }
//            .collect(java.util.stream.Collectors.toList())

        //    final var installerWorker = new PluginInstallerWorker(project, notifier, downloadablePluginNodes);

//    return ProgressManager.getInstance().runProcessWithProgressSynchronously(installerWorker,
//        "Downloading required plugins", true, project);
        return false
    }

    private class PluginInstallerWorker(
        val project: Project,  //                                  @Nullable Notifier notifier,
        val downloadablePluginNodes: List<PluginNode>
    ) : com.intellij.openapi.util.ThrowableComputable<Boolean?, java.lang.RuntimeException?> {

        override fun compute(): Boolean? {
            val indicator = java.util.Objects.requireNonNull<com.intellij.openapi.progress.ProgressIndicator>(
                com.intellij.openapi.progress.ProgressManager.getInstance().progressIndicator
            )
            indicator.isIndeterminate = true

            // The semaphore must be released (i.e., permits must be raised) once per each plugin installed.
            // Only when the semaphore's permit count reaches 1, this function can return.
            val lock = java.util.concurrent.Semaphore(-downloadablePluginNodes.size + 1)

            // "true" means that installation is successful (so far)
            val installationResult = java.util.concurrent.atomic.AtomicBoolean(true)

            com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(Runnable {
                try {
                    val onInstallationComplete = java.util.function.Consumer { result: Boolean? ->
                        installationResult.set(installationResult.get() and result!!)
                        lock.release()
                    }

                    com.intellij.openapi.application.WriteAction.run<java.io.IOException?>(com.intellij.util.ThrowableRunnable {
                        PluginManagerMain.downloadPlugins(
                            downloadablePluginNodes,
                            kotlin.collections.mutableListOf<PluginNode?>(),
                            false,
                            null,
                            PluginEnabler.getInstance(),
                            com.intellij.openapi.application.ModalityState.defaultModalityState(),
                            onInstallationComplete
                        )
                    })
                } catch (ex: java.io.IOException) {
//          if (notifier != null) {
//            notifier.notify(new NetworkErrorNotification(ex), project);
//          }

                    installationResult.set(false)
                }
            })

            while (true) {
                // Semaphore acquisition will succeed once all plugins have been installed.
                if (lock.tryAcquire()) {
                    // If plugin installation failed at any point (for example, an attempt to install a non-existent plugin),
                    // we return null to indicate a failed operation.
                    return if (installationResult.get()) false else null
                }

                // This will throw an exception if a user has cancelled the operation. It won't actually cancel
                // the installation of the current plugin; that would corrupt the IDE. But it will at least return
                // from this function and close the progress bar.
                indicator.checkCanceled()

                // In practice, an interruption should never happen.
                try {
                    Thread.sleep(50)
                } catch (ex: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return null
                }
            }
        }
    }
}
