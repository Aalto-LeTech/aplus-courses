package fi.aalto.cs.apluscourses.ui.exercise

import com.intellij.ide.ui.UISettings
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.fields.ExtendableTextComponent
import com.intellij.util.ui.JBFont
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import java.awt.*
import java.awt.geom.RoundRectangle2D
import javax.swing.Icon
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode


class ExercisesTreeRenderer : NodeRenderer() {
    private lateinit var item: ExercisesView.ExercisesTreeItem

    override fun customizeCellRenderer(
        tree: JTree,
        value: Any,
        isSelected: Boolean,
        isExpanded: Boolean,
        isLeaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ) {
        this.item = (value as DefaultMutableTreeNode).userObject as ExercisesView.ExercisesTreeItem
        when (val item = value.userObject as ExercisesView.ExercisesTreeItem) {
            is ExercisesView.NewSubmissionItem -> {
                isEnabled = true
                append(item.displayName(), SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES, true)
                icon = if (item.missingModule) CoursesIcons.ModuleDisabled else CoursesIcons.Plus
                toolTipText = message("ui.exercise.ExercisesTreeRenderer.submit")
            }

            is ExercisesView.ExerciseGroupItem -> {
                val group = item.group
                append("", SimpleTextAttributes.REGULAR_ATTRIBUTES, true) // disable search highlighting
                append(group.name, SimpleTextAttributes.REGULAR_ATTRIBUTES, false)
                isEnabled = true
                icon = if (group.isOpen) {
                    CoursesIcons.ExerciseGroup
                } else {
                    CoursesIcons.ExerciseGroupClosed
                }
                toolTipText = ""
            }

            is ExercisesView.ExerciseItem -> {
                val exercise = item.exercise
                append(exercise.name, SimpleTextAttributes.REGULAR_ATTRIBUTES, true)
                isEnabled = exercise.isSubmittable
                icon = ExtendableTextComponent.Extension { statusToIcon(getStatus(exercise)) }.getIcon(false)
            }

            is ExercisesView.SubmissionResultItem -> {
                val submission = item.submission
                isEnabled = true
                append(item.displayName(), SimpleTextAttributes.REGULAR_ATTRIBUTES, true)
                append(" ${submission.id}", SimpleTextAttributes.GRAYED_ATTRIBUTES, false)
                if (submission.status == SubmissionResult.Status.WAITING) {
                    icon = CoursesIcons.Loading
                }
            }

            is ExercisesView.ExercisesRootItem -> {}
        }

    }

    override fun paintComponent(g: Graphics?) {
        if (g == null) return

        val g2d = g.create() as Graphics2D

        try {
            if (!prepareToPaint(g2d)) return


            val width = width
            val height = height
            val fontMetrics = g2d.fontMetrics
            val pointsText = pointsText()
            val pointsTextBounds = fontMetrics.getStringBounds(pointsText, g2d).bounds
            val status = statusText()
            val statusBounds = fontMetrics.getStringBounds(status, g2d).bounds

            val pointsTextX = (width - pointsTextBounds.width) - 10
            val pointsTextY = getTextBaseLine(fontMetrics, height)
            val statusX = (width - statusBounds.width) - (pointsTextBounds.width + 18)

            if (pointsText.isNotEmpty()) {
                clipComponent(g2d, statusX, height)
                drawBackground(g2d, pointsTextBounds, pointsTextX, pointsTextY)
                drawText(g2d, pointsText, pointsTextX, pointsTextY)
            } else {
                super.paintComponent(g2d)
            }
            drawStatusText(g2d, status, statusX, pointsTextY)
        } finally {
            g2d.dispose()
        }
    }

    private fun prepareToPaint(g2d: Graphics2D): Boolean {
        UISettings.setupAntialiasing(g2d)
        val item = this.item
        return when (item) {
            is ExercisesView.ExerciseGroupItem -> item.group.maxPoints != 0
            is ExercisesView.SubmissionResultItem -> item.submission.status != SubmissionResult.Status.WAITING
            else -> true
        }
    }

    private fun statusText(): String {
        val item = this.item
        return when (item) {
            is ExercisesView.ExerciseItem -> {
                val exercise = item.exercise

                if (getStatus(exercise) == Status.OPTIONAL_PRACTICE) {
                    exercise.difficulty
                } else {
                    val lateString = if (exercise.isLate()) "late, " else ""
                    "${lateString}${exercise.submissionResults.size} of ${exercise.maxSubmissions} ${exercise.difficulty}"
                }
            }

            is ExercisesView.SubmissionResultItem -> {
                val submission = item.submission
                val isRejected = submission.status == SubmissionResult.Status.REJECTED
                val isLate = (submission.latePenalty
                    ?: 0.0) > 0 || submission.status == SubmissionResult.Status.UNOFFICIAL
                if (isRejected) {
                    "rejected"
                } else if (isLate) {
                    "late"
                } else {
                    ""
                }
            }

            else -> ""
        }
    }

    private fun pointsText(): String {
        val item = this.item

        return when (item) {
            is ExercisesView.ExerciseItem -> "${item.exercise.userPoints}/${item.exercise.maxPoints}"
            is ExercisesView.ExerciseGroupItem -> "${item.group.userPoints}/${item.group.maxPoints}"
            is ExercisesView.SubmissionResultItem -> "${item.submission.userPoints}/${item.submission.maxPoints}"
            else -> ""
        }
    }

    private fun pointsBackground(item: ExercisesView.ExercisesTreeItem): Color {
        return when (item) {
            is ExercisesView.ExerciseItem -> statusToColor(getStatus(item.exercise))
            is ExercisesView.SubmissionResultItem -> submissionResultToColor(item.submission)
            else -> JBColor(0x005EB8, 0x005EB8)
        }
    }

    private fun statusToColor(status: Status): Color {
        val baseColor = when (status) {
            Status.FULL_POINTS -> JBColor(0x8bc34a, 0x8bc34a)
            Status.NO_POINTS, Status.PARTIAL_POINTS -> JBColor(0xffb74d, 0xffb74d)
            else -> JBColor(0xc5c5c5, 0xc5c5c5)
        }
        return if (isSubmittable()) baseColor else ColorUtil.withAlpha(baseColor, 0.5)
    }

    private fun submissionResultToColor(submission: SubmissionResult): Color {
        return if (submission.userPoints == submission.maxPoints) {
            JBColor(0x8bc34a, 0x8bc34a)
        } else {
            JBColor(0xffb74d, 0xffb74d)
        }
    }

    private fun determineFont(item: ExercisesView.ExercisesTreeItem): Font {
        return when (item) {
            is ExercisesView.ExerciseGroupItem -> JBFont.regular().deriveFont(Font.BOLD)
            else -> JBFont.regular()
        }
    }

    private fun clipComponent(g2d: Graphics2D, statusX: Int, height: Int) {
        val originalClip = g2d.clip
        g2d.clipRect(0, 0, statusX - 12, height)
        super.paintComponent(g2d)
        g2d.clip = originalClip
    }

    private fun drawBackground(
        g2d: Graphics2D,
        pointsTextBounds: Rectangle,
        pointsTextX: Int,
        pointsTextY: Int
    ) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.color = pointsBackground(item)
        if (!isSubmittable()) {
            g2d.color = ColorUtil.withAlpha(g2d.color, 0.5)
        }
        val roundRectangle = RoundRectangle2D.Double(
            pointsTextX - 15.0,
            pointsTextY - pointsTextBounds.height + 2.0,
            pointsTextBounds.width.toDouble() + 10.0,
            pointsTextBounds.height.toDouble() + 2.0,
            pointsTextBounds.height.toDouble(),
            pointsTextBounds.height.toDouble()
        )
        if (item is ExercisesView.SubmissionResultItem) {
            g2d.draw(roundRectangle)
        } else {
            g2d.fill(roundRectangle)
        }
    }

    private fun drawText(g2d: Graphics2D, text: String, x: Int, y: Int) {
        if (item !is ExercisesView.SubmissionResultItem) {
            g2d.color =
                if (this.item is ExercisesView.ExerciseItem) {
                    val color = JBColor(0x000000, 0x000000)
                    if (!isSubmittable()) {
                        ColorUtil.withAlpha(color, 0.7)
                    } else {
                        color
                    }
                } else {
                    JBColor(0xFFFFFF, 0xFFFFFF)
                }
        }
        g2d.font = determineFont(item)
        g2d.drawString(text, x - 10, y)
    }

    private fun drawStatusText(g2d: Graphics2D, status: String, x: Int, y: Int) {
        g2d.color = JBColor.gray
        if (!isSubmittable()) {
            g2d.color = ColorUtil.withAlpha(g2d.color, 0.5)
        }
        g2d.drawString(status, x - 10, y)
    }

    private fun isSubmittable(): Boolean {
        val item = this.item
        return when (item) {
            is ExercisesView.ExerciseItem -> item.exercise.isSubmittable
            else -> true
        }
    }

    companion object {
        private fun getStatus(exercise: Exercise): Status {
            return if (exercise.isInGrading()) {
                Status.IN_GRADING
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
                Status.OPTIONAL_PRACTICE -> CoursesIcons.OptionalPractice
                Status.NO_SUBMISSIONS -> CoursesIcons.NoSubmissions
                Status.NO_POINTS -> CoursesIcons.NoPoints
                Status.PARTIAL_POINTS -> CoursesIcons.PartialPoints
                Status.FULL_POINTS -> CoursesIcons.FullPoints
                Status.LATE -> CoursesIcons.Late
                Status.IN_GRADING -> CoursesIcons.Loading
            }
        }

        enum class Status {
            OPTIONAL_PRACTICE,
            NO_SUBMISSIONS,
            NO_POINTS,
            PARTIAL_POINTS,
            FULL_POINTS,
            IN_GRADING,
            LATE
        }
    }
}
