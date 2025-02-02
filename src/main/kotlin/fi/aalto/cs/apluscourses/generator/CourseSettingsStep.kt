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
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.application
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.services.Plugins
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil.languageCodeToName
import org.jetbrains.annotations.NonNls
import javax.swing.JComponent

class CourseSettingsStep(
    wizard: WizardContext,
    starter: StarterContext,
    val builder: ModuleBuilder,
    parentDis: Disposable,
    settings: StarterWizardSettings,
    val config: APlusModuleConfig
) : CommonStarterInitialStep(wizard, starter, builder, parentDis, settings) {

    private var mainPanel = JBScrollPane()
    private val selectedLanguage = AtomicProperty<String>("")
    private val dontImportSettings = AtomicProperty(false)
    private var selectedSdk: AtomicProperty<ProjectWizardJdkIntent>? = null
    private var placeholder: Placeholder? = null

    override fun updateStep() {
        val courseConfig = config.courseConfig ?: return
        val languages = courseConfig.languages
        @NonNls val finnishCode = "fi"

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
                        row {
                            panel { addSdkUi() }
                        }
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
                            placeholder().apply {
                                placeholder = this
                            }.resizableColumn().align(AlignX.FILL)
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
        if (courseConfig.requiredPlugins.isEmpty()) return
        application.service<Plugins>().runInBackground(courseConfig.requiredPlugins) { components ->
            placeholder?.component = panel {
                components.map {
                    it.remove(4) // Remove checkbox and install button
                    it.remove(3)
                    it
                }.forEach {
                    row {
                        contextHelp(
                            it.pluginDescriptor.description
                                ?: message("generator.APlusModuleBuilder.defaultDescription")
                        )
                        cell(it).resizableColumn().align(AlignX.FILL).applyToComponent {
                            background = null
                            isOpaque = false
                        }
                    }.topGap(TopGap.SMALL)
                }
            }
            component.revalidate()
            component.repaint()
        }
    }

    override fun getComponent(): JComponent = mainPanel

    override fun updateDataModel() {
        config.language = selectedLanguage.get()
        config.jdk = sdkProperty.get()
        config.importSettings = !dontImportSettings.get()
    }
}