package fi.aalto.cs.apluscourses.generator

import com.intellij.ide.projectWizard.ProjectWizardJdkIntent
import com.intellij.ide.starters.local.StarterContext
import com.intellij.ide.starters.shared.CommonStarterInitialStep
import com.intellij.ide.starters.shared.StarterWizardSettings
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.application
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.api.CourseConfig
import fi.aalto.cs.apluscourses.services.Plugins
import fi.aalto.cs.apluscourses.ui.Utils
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil.languageCodeToName
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.NonNls
import javax.swing.JComponent
import javax.swing.JPanel

@ApiStatus.Experimental
class CourseSettingsStep(
    wizard: WizardContext,
    builder: ModuleBuilder,
    parentDis: Disposable,
    private val config: APlusModuleConfig
) : CommonStarterInitialStep(
    wizard, StarterContext(), builder, parentDis,
    StarterWizardSettings(
        emptyList(),
        emptyList(),
        isExampleCodeProvided = false,
        isPackageNameEditable = false,
        languageLevels = emptyList(),
        defaultLanguageLevel = null,
        packagingTypes = emptyList(),
        applicationTypes = emptyList(),
        testFrameworks = emptyList(),
        customizedMessages = null,
        showProjectTypes = false
    )
) {

    private var mainPanel = JBScrollPane()
    private val selectedLanguage = AtomicProperty("")
    private val dontImportSettings = AtomicProperty(false)
    private var selectedSdk: AtomicProperty<ProjectWizardJdkIntent>? = null
    private var pluginsPanel: PluginsPanel? = null

    override fun updateStep() {
        val courseConfig = config.courseConfig ?: return
        val languages = courseConfig.languages
        @NonNls val finnishCode = "fi"

        pluginsPanel = PluginsPanel()

        if (languages.contains(finnishCode)) selectedLanguage.set(finnishCode) else selectedLanguage.set(languages.first())

        mainPanel = JBScrollPane(panel {
            panel {
                group(message("generator.APlusModuleBuilder.language")) {
                    row {
                        text(
                            message("generator.APlusModuleBuilder.finnishInfo")
                        )
                    }.visible(languages.size > 1 && languages.contains(finnishCode))
                    row {
                        segmentedButton(courseConfig.languages) {
                            text = languageCodeToName(it)
                        }.bind(selectedLanguage)
                    }
                }
                group(message("generator.APlusModuleBuilder.settings")) {
                    row {
                        text(
                            message("generator.APlusModuleBuilder.settingsInfo")
                        )
                    }
                    row {
                        checkBox(message("generator.APlusModuleBuilder.leaveSettings")).bindSelected(
                            dontImportSettings
                        )
                    }
                }
                if (config.programmingLanguage == "scala") {
                    group(message("generator.APlusModuleBuilder.extra")) {
                        addSdkUi()
                    }
                } else {
                    selectedSdk = null
                }
                if (courseConfig.requiredPlugins.isNotEmpty()) {
                    group(message("generator.APlusModuleBuilder.plugins")) {
                        row {
                            text(message("generator.APlusModuleBuilder.pluginsInfo"))
                        }
                        row {
                            cell(pluginsPanel!!.content)
                        }
                    }
                }
            }.customize(UnscaledGaps(32, 32, 32, 32))
        }).apply {
            verticalScrollBarPolicy = JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        }

        component.revalidate()
        component.repaint()
        if (!courseConfig.requiredPlugins.isEmpty()) pluginsPanel!!.load(courseConfig.requiredPlugins)
    }

    override fun getComponent(): JComponent = mainPanel

    override fun updateDataModel() {
        config.language = selectedLanguage.get()
        config.jdk = sdkProperty.get()
        config.importSettings = !dontImportSettings.get()
    }

    inner class PluginsPanel {
        val content: JPanel = JPanel()

        fun load(plugins: List<CourseConfig.RequiredPlugin>) {
            content.add(Utils.loadingPanel())

            application.service<Plugins>().runInBackground(plugins) { components ->
                val newContent = panel {
                    components
                        .forEach {
                            row {
                                contextHelp(
                                    it.description
                                        ?: message("generator.APlusModuleBuilder.defaultDescription")
                                )
                                icon(it.icon)
                                panel {
                                    row {
                                        label(it.name).bold()
                                    }.rowComment("${it.version} ${it.vendor ?: ""}")
                                }
                            }.topGap(TopGap.SMALL)
                        }
                }

                content.removeAll()
                content.add(newContent)

                component.revalidate()
                component.repaint()
            }
        }
    }
}