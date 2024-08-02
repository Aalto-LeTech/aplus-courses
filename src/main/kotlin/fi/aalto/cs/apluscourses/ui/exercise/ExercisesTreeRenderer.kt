package fi.aalto.cs.apluscourses.ui.exercise

import com.intellij.ide.ui.UISettings
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.fields.ExtendableTextComponent
import com.intellij.util.ui.JBFont
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import icons.PluginIcons
import java.awt.*
import java.awt.geom.RoundRectangle2D
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode


class ExercisesTreeRenderer : NodeRenderer() {
    private lateinit var item: ExercisesView.ExercisesTreeItem

    private val actionButton: JButton = JButton("Submit").apply {
//        isFocusable = false
//        addMouseListener(object : MouseAdapter() {
//            override fun mouseEntered(e: MouseEvent?) {
//                background = JBColor.red//JBUI.CurrentTheme.ActionButton.hoverBackground()
//            }
//
//            override fun mouseExited(e: MouseEvent?) {
//                background = JBColor.background()
//            }
//        })

    }

//    private var hovering = false
//    private var mousePosition: Point? = Point()

    override fun customizeCellRenderer(
        tree: JTree,
        value: Any,
        isSelected: Boolean,
        isExpanded: Boolean,
        isLeaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ) {
//        hovering = getHoveredRow(tree) == row
//        mousePosition = tree.mousePosition
//        background = if (row % 2 == 0) {
//            JBColor.red
//        } else {
//            JBColor.background()
//        }
        this.item = (value as DefaultMutableTreeNode).userObject as ExercisesView.ExercisesTreeItem
        when (val item = value.userObject as ExercisesView.ExercisesTreeItem) {
            is ExercisesView.NewSubmissionItem -> {
                isEnabled = true
                append(item.displayName(), SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES, true)
                icon = if (item.missingModule) PluginIcons.A_PLUS_MODULE_DISABLED else PluginIcons.A_PLUS_PLUS
                toolTipText = message("ui.exercise.ExercisesTreeRenderer.submit")
            }

            is ExercisesView.ExerciseGroupItem -> {
                val group = item.group
                append("", SimpleTextAttributes.REGULAR_ATTRIBUTES, true) // disable search highlighting
                append(group.name, SimpleTextAttributes.REGULAR_ATTRIBUTES, false)
                isEnabled = true
                icon = if (group.exercises.any { exercise -> !exercise.isDetailsLoaded }) {
                    PluginIcons.A_PLUS_LOADING
                } else if (group.isOpen) {
                    PluginIcons.A_PLUS_EXERCISE_GROUP
                } else {
                    PluginIcons.A_PLUS_EXERCISE_GROUP_CLOSED
                }
                toolTipText = ""
            }

            is ExercisesView.ExerciseItem -> {
                val exercise = item.exercise
                append(exercise.name, SimpleTextAttributes.REGULAR_ATTRIBUTES, true)
//                if (viewModel.statusText.trim { it <= ' ' }.isNotEmpty()) {
//                    append(" [" + viewModel.statusText + "]", STATUS_TEXT_STYLE)
//                }
                isEnabled = exercise.isSubmittable
//                toolTipText = if (exercise.isSubmittable
//                ) PluginResourceBundle.getText("ui.exercise.ExercisesTreeRenderer.useUploadButton")
//                else PluginResourceBundle.getText("ui.exercise.ExercisesTreeRenderer.cannotSubmit")
                icon = ExtendableTextComponent.Extension { statusToIcon(getStatus(exercise)) }.getIcon(false)
            }

            is ExercisesView.SubmissionResultItem -> {
                val submission = item.submission
                isEnabled = true
                append(item.displayName(), SimpleTextAttributes.REGULAR_ATTRIBUTES, true)
                append(" ${submission.id}", SimpleTextAttributes.GRAYED_ATTRIBUTES, false)
                if (submission.status == SubmissionResult.Status.WAITING) {
                    icon = PluginIcons.A_PLUS_LOADING
                }
//                append(" [" + viewModel.statusText + "]", STATUS_TEXT_STYLE, false)
//                toolTipText = PluginResourceBundle.getAndReplaceText(
//                    "ui.exercise.ExercisesTreeRenderer.tooltip",
//                    submission.id.toString()
//                )
            }

            is ExercisesView.ExercisesRootItem -> {}
        }

    }


    override fun paintComponent(g: Graphics?) {
        if (g == null) {
            return
        }
        val g2d = g.create() as Graphics2D
        UISettings.setupAntialiasing(g2d)
        val clip: Shape?
        val width = width
        val height = height
        if (isOpaque) {
            // paint background for expanded row
            g2d.color = background
            g2d.fillRect(0, 0, width, height)
        }

        var white = true
        var status = ""
        var isSubmittable = true
        val text = when (val item = this.item) {
            is ExercisesView.ExerciseItem -> {
//                if (hovering && item.exercise.isSubmittable) {
//                    super.paintComponent(g2d)
//                    val size = size
//                    val buttonSize = actionButton.preferredSize
//                    actionButton.setBounds(
//                        size.width - buttonSize.width - 5,
//                        0, buttonSize.width, height
//                    )
////                     transparent backround
//                    actionButton.isOpaque = false
//                    // check if hovering
//                    if (mousePosition != null && mousePosition!!.x < size.width - buttonSize.width - 5
//                    ) {
//                        actionButton.graphics?.color = JBColor(0x000000, 0x000000)
//                        println("dijawofijaoiwf")
//                        actionButton.border = JBUI.Borders.customLine(JBColor(0x000000, 0x000000), 1, 0, 0, 0)
//                    } else {
//                        actionButton.isSelected = false
//                        actionButton.border = JBUI.Borders.empty()
//                    }
//
//                    SwingUtilities.paintComponent(g2d, actionButton, this, actionButton.bounds)
//                    return
//                }
                val exercise = item.exercise
                isSubmittable = exercise.isSubmittable
//                if (submittable) {
//                    g2d.
//                }
                white = false
                val userPoints = exercise.userPoints
                val maxPoints = exercise.maxPoints
                g2d.font = JBFont.regular()
                val statusEnum = getStatus(exercise)
                g2d.color = when (statusEnum) {
                    Status.FULL_POINTS ->
                        JBColor(0x8bc34a, 0x8bc34a)

                    Status.NO_POINTS, Status.PARTIAL_POINTS ->
                        JBColor(0xffb74d, 0xffb74d)

                    else -> JBColor(0xc5c5c5, 0xc5c5c5)
                }
                if (!isSubmittable) {
                    g2d.color = ColorUtil.withAlpha(g2d.color, 0.5)
                }
                val statusText: String =
                    if (statusEnum == Status.OPTIONAL_PRACTICE) {
                        exercise.difficulty
                    } else {
                        val lateString = if (exercise.isLate()) "late, " else ""
                        "${lateString}${exercise.submissionResults.size} of ${exercise.maxSubmissions} ${exercise.difficulty}"
                    }
                status = statusText
                "${userPoints}/${maxPoints}"
            }

            is ExercisesView.ExerciseGroupItem -> {
                val group = item.group
                if (group.maxPoints == 0) {
                    super.paintComponent(g)
                    return
                }
                g2d.font = JBFont.regular().deriveFont(Font.BOLD)
                g2d.color = JBColor(0x005EB8, 0x005EB8)
                "${group.userPoints}/${group.maxPoints}"
            }

            is ExercisesView.SubmissionResultItem -> {
                val submission = item.submission
                val isWaiting = submission.status == SubmissionResult.Status.WAITING
                if (isWaiting) {
                    super.paintComponent(g)
                    return
                }
                val isLate = (submission.latePenalty
                    ?: 0.0) > 0 || submission.status == SubmissionResult.Status.UNOFFICIAL
                val isRejected = submission.status == SubmissionResult.Status.REJECTED
                g2d.font = JBFont.regular()
                g2d.color = if (submission.userPoints == submission.maxPoints) {
                    JBColor(0x8bc34a, 0x8bc34a)
                } else {
                    JBColor(0xffb74d, 0xffb74d)
                }
                if (isRejected) {
                    status = "rejected"
                } else if (isLate) {
                    status = "late"
                }
                "${submission.userPoints}/${submission.maxPoints}"
            }

            else -> {
                super.paintComponent(g)
                return
            }

        }


        val fontMetrics: FontMetrics = g2d.fontMetrics
        val stringBounds = fontMetrics.getStringBounds(text, g2d).bounds
        val textX = (width - stringBounds.width) - 10
        val textY =
            getTextBaseLine(g2d.fontMetrics, height) // height - stringBounds.height) / 2 + fontMetrics.ascent + 1

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val roundRectangle =
            RoundRectangle2D.Double(
                textX - 15.0,
                textY - stringBounds.height + 2.0,
                stringBounds.width.toDouble() + 10.0,
                stringBounds.height.toDouble() + 2.0,
                stringBounds.height.toDouble(),
                stringBounds.height.toDouble()
            )
        when (item) {
            is ExercisesView.SubmissionResultItem -> {
                g2d.draw(roundRectangle)
            }

            else -> {
                g2d.fill(roundRectangle)
                g2d.color =
                    if (white)
                        JBColor(0xFFFFFF, 0xFFFFFF)
                    else
                        JBColor(0x000000, 0x000000)
            }
        }

        if (!isSubmittable) {
            g2d.color = ColorUtil.withAlpha(g2d.color, 0.8)
        }
        g2d.drawString(text, textX - 10, textY)

        if (status.isNotEmpty()) {
            val statusBounds = fontMetrics.getStringBounds(status, g2d).bounds
            val statusX = (width - statusBounds.width) - (stringBounds.width + 18)
            g2d.color = JBColor.gray
            if (!isSubmittable) {
                g2d.color = ColorUtil.withAlpha(g2d.color, 0.5)
            }
            g2d.drawString(status, statusX - 10, textY)
        }


        clip = g2d.clip
        g2d.clipRect(0, 0, width, height)

        super.paintComponent(g2d)

        // restore clip area if needed
        if (clip != null) g2d.clip = clip
    }

    companion object {
        private fun getStatus(exercise: Exercise): Status {
            return if (exercise.isInGrading()) {
                Status.IN_GRADING
            } else if (!exercise.isDetailsLoaded) {
                Status.LOADING
            } else if (exercise.isOptional) {
                Status.OPTIONAL_PRACTICE
            } else if (exercise.submissionResults.isEmpty()) {
                Status.NO_SUBMISSIONS
            } else if (exercise.userPoints == exercise.maxPoints) {
                Status.FULL_POINTS
            } else if (exercise.isLate()) {
                Status.LATE
            } else if (exercise.userPoints == 0) {
                Status.NO_POINTS
            } else {
                Status.PARTIAL_POINTS
            }
        }

        private fun statusToIcon(exerciseStatus: Status): Icon {
            return when (exerciseStatus) {
                Status.OPTIONAL_PRACTICE -> PluginIcons.A_PLUS_OPTIONAL_PRACTICE
                Status.NO_SUBMISSIONS -> PluginIcons.A_PLUS_NO_SUBMISSIONS
                Status.NO_POINTS -> PluginIcons.A_PLUS_NO_POINTS
                Status.PARTIAL_POINTS -> PluginIcons.A_PLUS_PARTIAL_POINTS
                Status.FULL_POINTS -> PluginIcons.A_PLUS_FULL_POINTS
                Status.LATE -> PluginIcons.A_PLUS_LATE
                Status.IN_GRADING, Status.LOADING -> PluginIcons.A_PLUS_LOADING
            }
        }

        private val STATUS_TEXT_STYLE = SimpleTextAttributes(
            SimpleTextAttributes.STYLE_ITALIC or SimpleTextAttributes.STYLE_SMALLER, null
        )

        enum class Status {
            OPTIONAL_PRACTICE,
            NO_SUBMISSIONS,
            NO_POINTS,
            PARTIAL_POINTS,
            FULL_POINTS,
            IN_GRADING,
            LATE,
            LOADING
        }
    }
}
