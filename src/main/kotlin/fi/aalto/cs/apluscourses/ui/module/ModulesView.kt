package fi.aalto.cs.apluscourses.ui.module

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.SearchTextField
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.AnActionLink
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.TextComponentEmptyText
import com.intellij.ui.util.maximumWidth
import com.intellij.ui.util.preferredHeight
import com.intellij.util.application
import com.intellij.util.ui.UIUtil
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.model.Course
import fi.aalto.cs.apluscourses.model.component.Component.Status
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.Opener
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import fi.aalto.cs.apluscourses.utils.temp.DateDifferenceFormatter.formatTimeUntilNow
import icons.PluginIcons
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.NonNls
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Point
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class ModulesView(val project: Project) : SimpleToolWindowPanel(true, true) {
    private var modules = mutableListOf<Module>()
    private var actionRequired = mutableListOf<Module>()
    private var available = mutableListOf<Module>()
    private var installed = mutableListOf<Module>()
    private val mainPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
    }
    private val itemPanels = mutableListOf<ModuleRenderer>()
    val searchTextField: SearchTextField = object : SearchTextField(false) {
        override fun preprocessEventForTextField(e: KeyEvent): Boolean {
            super.preprocessEventForTextField(e)
            searchChanged(this.text)
            return false
        }
    }.apply {
        textEditor.apply {
            emptyText.text = "Search Modules.."
            accessibleContext.accessibleName = "Search Modules"
            TextComponentEmptyText.setupPlaceholderVisibility(this)
        }
    }


    private fun getPanelAt(point: Point) =
        itemPanels.find { it.bounds.contains(point) }

    init {
        val content = JBScrollPane(mainPanel)
        content.horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        setContent(content)
        mainPanel.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                itemPanels.forEach { it.updateBackground(false) }
                getPanelAt(e.point)?.updateBackground(true)
            }
        })
        mainPanel.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                getPanelAt(e.point)?.let {
                    val isExpanded = it.isExpanded
                    collapseAll()
                    if (!isExpanded) {
                        it.expand()
                    }
                }
            }

            override fun mouseExited(e: MouseEvent) {
                val bounds = content.bounds
                val point = e.point
                val scrollbarWidth = content.verticalScrollBar.width
                if (!bounds.contains(point) || point.x > bounds.width - scrollbarWidth) {
                    itemPanels.forEach { it.updateBackground(false) }
                }
            }
        })
    }

    private fun collapseAll() = itemPanels.forEach { it.collapse() }

    private fun addModule(
        module: Module,
        index: Int
    ) {
        val itemPanel = ModuleRenderer(module, index, project) { collapseAll() }
        itemPanel.isOpaque = false
        itemPanel.alignmentX = LEFT_ALIGNMENT
        itemPanel.maximumSize = Dimension(itemPanel.maximumWidth, itemPanel.preferredHeight)
        itemPanels.add(itemPanel)
        mainPanel.add(itemPanel)
    }

    private fun addLabel(text: String) {
        val label = TitledSeparator(text)
        label.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
        label.alignmentX = LEFT_ALIGNMENT
        label.maximumSize = Dimension(label.maximumWidth, label.preferredHeight)
        mainPanel.add(label)
    }

    fun searchChanged(text: String) {
        for (item in itemPanels) {
            item.setVisibility(item.module.name.lowercase().contains(text.lowercase()))
        }
    }

    private fun updateView() {
        val openModule = itemPanels.find { it.isExpanded }
        val categories = modules.groupBy { it.category }
        actionRequired = categories[Module.Category.ACTION_REQUIRED]?.toMutableList() ?: mutableListOf()
        available = categories[Module.Category.AVAILABLE]?.toMutableList() ?: mutableListOf()
        installed = categories[Module.Category.INSTALLED]?.toMutableList() ?: mutableListOf()

        mainPanel.removeAll()
        itemPanels.clear()
        if (actionRequired.isNotEmpty()) addLabel("Action Required")
        for ((index, module) in actionRequired.withIndex()) {
            addModule(module, index)
        }
        if (available.isNotEmpty()) addLabel("Available Modules")
        for ((index, module) in available.withIndex()) {
            addModule(module, index)
        }
        if (installed.isNotEmpty()) addLabel("Installed Modules")
        for ((index, module) in installed.withIndex()) {
            addModule(module, index)
        }
        mainPanel.add(Box.createVerticalGlue())
        searchChanged(searchTextField.text)
        openModule?.let { showModule(it.module, false) }
        itemPanels.find { it.module.name == openModule?.module?.name }?.expand()
    }

    fun showModule(module: Module, scroll: Boolean) {
        collapseAll()
        val item = itemPanels.find { it.module.name == module.name }
        if (item != null) {
            item.expand()
            if (scroll) (content as JBScrollPane?)?.verticalScrollBar?.value = item.location.y - 100
        }
    }

    /**
     * Update this modules view with the given view model (which may be null).
     */
    fun viewModelChanged(course: Course?) {
        println("modules vm changed")
        application.invokeLater {
            if (course == null) {
                mainPanel.removeAll()
                itemPanels.clear()
                actionRequired.clear()
                installed.clear()
                available.clear()
                return@invokeLater
            }
            val visible = course.modules
            modules = visible.toMutableList()
            updateView()

            if (modules.isEmpty()) {
                mainPanel.add(JBLabel(MyBundle.message("ui.toolWindow.subTab.modules.noModules")))
            }
        }
    }


    private class ModulePanel(
        val module: Module,
        index: Int,
        val project: Project,
        val collapseAll: () -> Unit
    ) : JPanel(BorderLayout()) {
        private val detailsPanel: JPanel
        private val expandButton: JBLabel
        private val infoLabel: JBLabel
        private var isExpanded = false
        private var fileSize = ""

        private val icon
            get() = if (module.category == Module.Category.AVAILABLE) PluginIcons.A_PLUS_MODULE_DISABLED
            else if (module.category == Module.Category.INSTALLED) PluginIcons.A_PLUS_MODULE
            else if (module.isUpdateAvailable) PluginIcons.A_PLUS_INFO
            else PluginIcons.A_PLUS_NO_POINTS

        init {
            val background = if (index % 2 == 0) UIUtil.getTableBackground()
            else if (JBColor.isBright()) UIUtil.getTableBackground().brighter()
            else ColorUtil.darker(UIUtil.getTableBackground(), 1)
            val headerPanel = JPanel(BorderLayout())
            headerPanel.background = background
            headerPanel.border = BorderFactory.createEmptyBorder(0, 10, 0, 4)

            val nameLabel = JBLabel(
                module.name,
                icon,
                JBLabel.LEFT
            )
            nameLabel.foreground = JBColor.foreground()
            headerPanel.add(nameLabel, BorderLayout.CENTER)

            val updateAvailable = module.isUpdateAvailable //installed && module.version != module.localVersion

            val statusPanel = JPanel()
            statusPanel.isOpaque = false

            expandButton = JBLabel(AllIcons.General.ChevronDown)
            headerPanel.add(statusPanel, BorderLayout.EAST)

            add(headerPanel, BorderLayout.NORTH)

            detailsPanel = JPanel(BorderLayout())
            detailsPanel.background = background
            detailsPanel.border = BorderFactory.createEmptyBorder(2, 10, 2, 10)

            val detailsTextPanel = JPanel()
            detailsTextPanel.layout = BoxLayout(detailsTextPanel, BoxLayout.Y_AXIS)
            detailsTextPanel.isOpaque = false

            val detailsColor = JBColor.gray

            infoLabel = JBLabel()
            infoLabel.foreground = detailsColor
//            val statusLabel = JBLabel("module.info")
//            statusLabel.foreground = detailsColor
//            detailsTextPanel.add(statusLabel)
            detailsPanel.add(detailsTextPanel, BorderLayout.CENTER)

//                if (updateAvailable) {
//                    val hasChangelog = module.changelog != ""
//                    val version = "Version ${module.version}"
//                    val changesLabel = if (hasChangelog)
//                        JBLabel(
//                            """<html><body>
//                            <span>${version} Changelog:</span><br>${module.changelog}
//                        </body></html>""".trimIndent()
//                        )
//                    else
//                        JBLabel("$version available")
//                    changesLabel.foreground = detailsColor
//                    println(changesLabel.text)
//                    detailsTextPanel.add(changesLabel)
//                }
//                detailsPanel.add(detailsTextPanel, BorderLayout.CENTER)
            val button = if (module.category == Module.Category.INSTALLED) {
                val installedTime = if (module.metadata != null) {
                    "Installed ${formatTimeUntilNow(module.metadata!!.downloadedAt)}"
                } else {
                    "Metadata not found"
                }
                val versionLabel = JBLabel(installedTime)
                versionLabel.foreground = detailsColor
                detailsTextPanel.add(versionLabel)

                val nextExercise = ExercisesUpdaterService.getInstance(project).state.exerciseGroups
                    .flatMap { group -> group.exercises.map { it to group } }
                    .filter { it.first.module?.name == module.name }
                    .firstOrNull { it.first.userPoints == 0 }

                val opener = project.service<Opener>()
                if (nextExercise != null) {
                    val exercise = nextExercise.first
                    val groupName = nextExercise.second.name
                    val nextExerciseLabel = JBLabel("Next exercise: ${groupName}")
                    val externalLink = ActionLink(exercise.name) {
                        opener.showExercise(exercise)
                    }.apply {
                        setIcon(PluginIcons.A_PLUS_NO_SUBMISSIONS, false)
                    }
                    nextExerciseLabel.foreground = detailsColor
                    detailsTextPanel.add(nextExerciseLabel)
                    detailsTextPanel.add(externalLink)
                }

                val projectTreeLink = ActionLink("Show in Project Tree") {
                    println("Show in Project Tree clicked for: $name")
                    opener.showModuleInProjectTree(module)
                }.apply {
                    setLinkIcon()
                }
                detailsTextPanel.add(projectTreeLink)

                if (module.documentationExists) {
                    val documentationLink = AnActionLink(
                        "Open Documentation",
                        opener.openDocumentationAction(module, "doc/index.html"), "here"
                    )// TODO
                        .apply {
                            setLinkIcon()
                        }
                    detailsTextPanel.add(documentationLink)
                }
                null
            } else if (module.category == Module.Category.AVAILABLE) {
                detailsPanel.add(infoLabel, BorderLayout.CENTER)


                val isInstalling = module.status == Status.LOADING
                val installButton = JButton(if (isInstalling) "Installing..." else "Install")
                installButton.isOpaque = false
                installButton.isEnabled = !isInstalling
                installButton.addActionListener {
                    println("installButton clicked for: $name")
                    installButton.text = "Installing..."
                    installButton.isEnabled = false
                    project.service<CourseManager>().installModule(module)
                }
                installButton
            } else {
                val missingDependencies = CourseManager.getInstance(project).state.missingDependencies[module.name]
                if (missingDependencies == null) {
                    val statusLabel = JBLabel("Update available")
                    statusPanel.add(statusLabel)
                    val updateAvailableLabel =
                        JBLabel("Update available: ${module.metadata?.version} → ${module.latestVersion}")
                    val changelog = module.changelog
                    if (changelog?.isNotEmpty() == true) {
                        val changelogLabel = JBLabel("Changelog:")
                        changelogLabel.foreground = detailsColor
                        detailsTextPanel.add(changelogLabel)
                        val changelogText = JBLabel(module.changelog)
                        changelogText.foreground = detailsColor
                        detailsTextPanel.add(changelogText)
                    }
                    updateAvailableLabel.foreground = detailsColor
                    detailsTextPanel.add(updateAvailableLabel)
                    val updateButton = JButton("Update")
                    updateButton.isOpaque = false
                    updateButton.addActionListener {
                        println("installButton clicked for: $name")
//                    project.service<ModuleInstaller>().installModule(module)
                        updateButton.text = "Installing..."
                        updateButton.isEnabled = false
                    }
                    detailsPanel.add(detailsTextPanel, BorderLayout.CENTER)
                    updateButton
                } else {
                    val statusLabel = JBLabel("Missing dependencies")
                    statusPanel.add(statusLabel)

                    val missingDependenciesLabel = JBLabel("Missing dependencies:")
                    missingDependenciesLabel.foreground = detailsColor
                    detailsTextPanel.add(missingDependenciesLabel)
                    missingDependencies.forEach { component ->
                        if (component is Module) {
                            val externalLink = ActionLink(component.name) {
                                project.service<Opener>().showModule(component)
                            }.apply {
                                setIcon(PluginIcons.A_PLUS_MODULE_DISABLED, false)
                            }
                            detailsTextPanel.add(externalLink)
                        } else {
                            val dependencyLabel = JBLabel(component.name)
                            dependencyLabel.foreground = detailsColor
                            detailsTextPanel.add(dependencyLabel)
                        }
                    }
                    val installButton = JButton("Install")
                    installButton.isOpaque = false
                    installButton.addActionListener {
                        println("installButton clicked for: $name")
                        installButton.text = "Installing..."
                        installButton.isEnabled = false
                        project.service<CourseManager>().installModule(module)
                    }
                    detailsPanel.add(detailsTextPanel, BorderLayout.CENTER)
                    installButton
                }
                null
            }

            statusPanel.add(expandButton)


            val buttonContainer = JPanel()
            val buttonContainerLayout = BoxLayout(buttonContainer, BoxLayout.Y_AXIS)
            buttonContainer.layout = buttonContainerLayout
            buttonContainer.isOpaque = false
            buttonContainer.add(Box.createVerticalGlue())
            if (button != null) { // TODO
                buttonContainer.add(button)
            }
            detailsPanel.add(buttonContainer, BorderLayout.EAST)

            detailsPanel.isVisible = false
            add(detailsPanel, BorderLayout.CENTER)
            addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent?) {
                    val isOpen = isExpanded
                    collapseAll()
                    if (!isOpen) {
                        expand()
                    }
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
            infoLabel.text = fileSize
//                """
//            <html><body>
//                <span>${fileSize}</span>
//            </body></html>
//            """.trimIndent()
        }

        @NonNls
        fun formatFileSize(sizeInBytes: Long): String {
            val sizeInKB = sizeInBytes / 1024.0
            val sizeInMB = sizeInKB / 1024.0

            return when {
                sizeInMB >= 1 -> "%.2f MB".format(sizeInMB)
                sizeInKB >= 1 -> "%.2f KB".format(sizeInKB)
                else -> "$sizeInBytes B"
            }
        }

        private fun toggleExpand() {
            if (fileSize == "") {
                val url = module.zipUrl.replace("/", "/<wbr>")
//                fileSize = "Available at $url"
                val formattedText = """
                <html><body>
                    <span style="white-space: nowrap;">Available at </span>
                    <span>${url}</span>
                    <span style="white-space: nowrap;">(????)</span>
                </body></html>
                """.trimIndent()
                fileSize = formattedText
                updateInfoLabel()
                val client = CoursesClient.getInstance(project)
                client.cs.launch { // TODO
                    val size = client.getFileSize(Url(module.zipUrl))
                    if (size != null) {
                        val formattedSize = formatFileSize(size)
                        val formattedText = """
                        <html><body>
                            <span style="white-space: nowrap;">Available at </span>
                            <span>${url} </span>
                            <span style="white-space: nowrap;">($formattedSize)</span>
                        </body></html>
                        """.trimIndent()
                        fileSize = formattedText//"Available at $url (${formatFileSize(size)})"
                        updateInfoLabel()
                    }
                    withContext(Dispatchers.EDT) {
                        updateInfoLabel()
                    }
                }
            }
            isExpanded = !isExpanded
            detailsPanel.isVisible = isExpanded
            expandButton.icon = if (isExpanded) AllIcons.General.ChevronUp else AllIcons.General.ChevronDown
            maximumSize = Dimension(maximumWidth, preferredHeight)
            revalidate()
            repaint()
        }

        fun setVisibility(visible: Boolean) {
            isVisible = visible
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