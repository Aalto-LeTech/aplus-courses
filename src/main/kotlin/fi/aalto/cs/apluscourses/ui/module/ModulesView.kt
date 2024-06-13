package fi.aalto.cs.apluscourses.ui.module

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.util.heightProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.util.preferredHeight
import com.intellij.ui.util.preferredWidth
import com.intellij.util.ui.UIUtil
import fi.aalto.cs.apluscourses.model.Module
import fi.aalto.cs.apluscourses.presentation.CourseViewModel
import fi.aalto.cs.apluscourses.presentation.module.ModuleListElementViewModel
import fi.aalto.cs.apluscourses.services.CoursesClient
import icons.PluginIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jdesktop.swingx.painter.AbstractLayoutPainter
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import kotlin.random.Random

class ModulesView(val project: Project) : SimpleToolWindowPanel(true, true) {
    private var availableModules = mutableListOf<ModuleListElementViewModel>()
    private var installedModules = mutableListOf<ModuleListElementViewModel>()
    private val mainPanel = JPanel()
    private val itemPanels = mutableListOf<CustomItemPanel>()

    init {
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        val content = JBScrollPane(mainPanel)
        content.horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        setContent(content)
    }

    private fun addModule(
        name: String,
        status: String,
        module: Module,
        index: Int,
        installed: Boolean,
    ) {
        val itemPanel = CustomItemPanel(name, status, module, index, installed, project)
//        itemPanel.addMouseListener(object : MouseAdapter() {
//            override fun mouseClicked(e: MouseEvent?) {
//                itemPanels.forEach {
//                    if (it !== itemPanel && it.isExpanded()) {
//                        it.collapse()
//                    }
//                }
//                itemPanel.expand()
//            }
//        })
        itemPanel.alignmentX = Component.LEFT_ALIGNMENT
        itemPanels.add(itemPanel)
        mainPanel.add(itemPanel)
        revalidate()
        repaint()
    }

    private fun addLabel(text: String) {
        val label = JBLabel(text, UIUtil.ComponentStyle.LARGE)
        label.alignmentX = Component.LEFT_ALIGNMENT
        mainPanel.add(label)
    }

    /**
     * Update this modules view with the given view model (which may be null).
     */
    fun viewModelChanged(course: CourseViewModel?) {
        ApplicationManager.getApplication().invokeLater(
            {
                mainPanel.removeAll()
                itemPanels.clear()
                if (course == null) {
                    installedModules.clear()
                    availableModules.clear()
                    return@invokeLater
                }
                val visible = course.modules.streamVisibleItems().toList()
                val count = visible.size
                availableModules = visible.subList(0, count / 2)
                installedModules = visible.subList(count / 2, count)


                addLabel("Available Modules")
                for ((index, module) in availableModules.withIndex()) {
                    addModule(module.name, module.status, module.model, index, false)
                }

                addLabel("Installed Modules")
                for ((index, module) in installedModules.withIndex()) {
                    addModule(module.name, module.status, module.model, index, true)
                }

//                if (modules.isEmpty()) {
//                    mainPanel.add(JBLabel(PluginResourceBundle.getText("ui.toolWindow.subTab.modules.noModules")))
//                }

                revalidate()
                repaint()
                updateUI()
            }, ModalityState.any()
        )
    }


    class CustomItemPanel(
        name: String,
        status: String,
        val module: Module,
        index: Int,
        installed: Boolean,
        val project: Project
    ) :
        JPanel(BorderLayout()) {
        private val detailsPanel: JPanel
        private val expandButton: JBLabel
        private val infoLabel: JBLabel
        private var isExpanded = false
        private var fileSize = ""

        init {
            val background = if (index % 2 == 0) UIUtil.getTableBackground()
            else if (JBColor.isBright()) UIUtil.getTableBackground().brighter()
            else ColorUtil.darker(UIUtil.getTableBackground(), 1)
            val headerPanel = JPanel(BorderLayout())
            headerPanel.background = background
            headerPanel.border = BorderFactory.createEmptyBorder(2, 8, 2, 2)

            val nameLabel = JBLabel(name, PluginIcons.A_PLUS_MODULE, JBLabel.LEFT)
            nameLabel.foreground = JBColor.foreground()
            headerPanel.add(nameLabel, BorderLayout.CENTER)

            val updateAvailable = installed && module.version != module.localVersion
            // module.isUpdatable
            val statusPanel = JPanel()
            statusPanel.isOpaque = false
            if (updateAvailable) {
                val updateAvailableLabel = JBLabel("Update available!")
                statusPanel.add(updateAvailableLabel)
            }

            expandButton = JBLabel(AllIcons.General.ChevronDown)
            statusPanel.add(expandButton)
            headerPanel.add(statusPanel, BorderLayout.EAST)

            add(headerPanel, BorderLayout.NORTH)

            detailsPanel = JPanel(BorderLayout())
            detailsPanel.background = background
            detailsPanel.border = BorderFactory.createEmptyBorder(2, 8, 2, 8)

            val detailsTextPanel = JPanel()
            detailsTextPanel.layout = BoxLayout(detailsTextPanel, BoxLayout.Y_AXIS)
            detailsTextPanel.isOpaque = false

            val detailsColor = JBColor.gray

//        val statusLabel = JBLabel(status)
//        statusLabel.foreground = JBColor.foreground()
//        detailsPanel.add(statusLabel, BorderLayout.NORTH)
            infoLabel = JBLabel()
            val button = if (installed) {
                val versionLabel = JBLabel(
                    """<html><body>
                    <span>Version ${module.localVersion}${if (updateAvailable) " " else "<br>"}Installed ${module.metadata.downloadedAt?.toLocalDate()}</span>
                </body></html>""".trimIndent()
                )
                versionLabel.foreground = detailsColor
                detailsTextPanel.add(versionLabel)

                if (updateAvailable) {
                    val hasChangelog = module.changelog != ""
                    val version = "Version ${module.version}"
                    val changesLabel = if (hasChangelog)
                        JBLabel(
                            """<html><body>
                            <span>${version} Changelog:</span><br>${module.changelog}
                        </body></html>""".trimIndent()
                        )
                    else
                        JBLabel("$version available")
                    changesLabel.foreground = detailsColor
                    println(changesLabel.text)
                    detailsTextPanel.add(changesLabel)
                }
                detailsPanel.add(detailsTextPanel, BorderLayout.CENTER)

                val updateButton = JButton(if (updateAvailable) "Update" else "Reinstall")
                updateButton.isRolloverEnabled = true
                updateButton.isOpaque = false
                updateButton.addActionListener {
                    println("Update clicked for: $name")
                }
                updateButton
            } else {
                detailsPanel.add(infoLabel, BorderLayout.CENTER)


                val installButton = JButton("Install")
                installButton.isOpaque = false
                installButton.addActionListener {
                    println("installButton clicked for: $name")
                }
                installButton
            }

            val buttonContainer = JPanel()
            val buttonContainerLayout = BoxLayout(buttonContainer, BoxLayout.Y_AXIS)
            buttonContainer.layout = buttonContainerLayout
            buttonContainer.isOpaque = false
            buttonContainer.add(Box.createVerticalGlue())
            buttonContainer.add(button)
            detailsPanel.add(buttonContainer, BorderLayout.EAST)

            detailsPanel.isVisible = false
            add(detailsPanel, BorderLayout.CENTER)
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    toggleExpand()
                }

                override fun mouseEntered(e: MouseEvent?) {
                    val hoverBackground = UIUtil.getTableBackground(true, false)
                    headerPanel.background = hoverBackground
                    detailsPanel.background = hoverBackground
                }

                override fun mouseExited(e: MouseEvent?) {
                    headerPanel.background = background
                    detailsPanel.background = background
                }
            })
        }

        private fun updateInfoLabel() {
            infoLabel.text = """
            <html><body>
                <span>${fileSize}</span>
            </body></html>
            """.trimIndent()
        }

        private fun toggleExpand() {
            if (fileSize == "") {
                fileSize = "loading..."
                updateInfoLabel()
                val client = project.service<CoursesClient>()
                client.cs.launch {
                    val size = client.getFileSize(module.url)
                    if (size != null) {
                        fileSize = "${size / 1024 / 1024.0}MB"
                    }
                    withContext(Dispatchers.EDT) {
                        updateInfoLabel()
                    }
                }
            }
            isExpanded = !isExpanded
            detailsPanel.isVisible = isExpanded
            expandButton.icon = if (isExpanded) AllIcons.General.ChevronUp else AllIcons.General.ChevronDown
            revalidate()
            repaint()
        }

        fun collapse() {
            if (isExpanded) {
                toggleExpand()
            }
        }

        fun expand() {
            if (!isExpanded) {
                toggleExpand()
            }
        }

        fun isExpanded(): Boolean {
            return isExpanded
        }
    }
}