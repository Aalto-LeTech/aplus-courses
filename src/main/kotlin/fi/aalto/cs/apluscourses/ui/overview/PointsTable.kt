package fi.aalto.cs.apluscourses.ui.overview

import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.ui.PLACEHOLDER_POINTS_ROWS
import fi.aalto.cs.apluscourses.ui.PendingValueLabel
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JPanel
import javax.swing.JProgressBar

sealed interface Points {
    data object Pending : Points
    data class Collected(val collected: Int?, val max: Int?) : Points
    data class Remaining(val untilNext: Int?, val maxOfNext: Int?) : Points
}

/**
 * The table of points per category.
 */
class PointsTable : JPanel(GridBagLayout()) {
    private var categories: List<String>? = null
    private var rows: List<PointsRow> = emptyList()

    init {
        isOpaque = false
        rebuild()
    }

    fun update(newCategories: List<String>?, points: Map<String, Points>) {
        setCategories(newCategories)
        rows.forEach { row ->
            row.setPoints(row.category?.let { points[it] } ?: Points.Pending)
        }
    }

    fun naturalScoreWidth(): Int = rows.maxOfOrNull { it.naturalScoreWidth() } ?: 0
    fun pinScoreWidth(width: Int): Unit = rows.forEach { it.pinScoreWidth(width) }

    private fun setCategories(newCategories: List<String>?) {
        if (newCategories == categories) return
        categories = newCategories
        rebuild()
    }

    /**
     * Without the category names there is nothing to name the rows after, so a fixed number of
     * placeholder rows is shown instead.
     */
    private fun rebuild() {
        removeAll()
        rows = categories?.map { PointsRow(it) } ?: List(PLACEHOLDER_POINTS_ROWS) { PointsRow(null) }
        rows.forEachIndexed { index, row -> row.addTo(this, index) }
        revalidate()
        repaint()
    }
}

/** One row of the table. */
private class PointsRow(val category: String?) {
    private val name = PendingValueLabel(
        placeholderWidth = CATEGORY_WIDTH,
        labelFont = JBUI.Fonts.label().deriveFont(Font.BOLD)
    )
    private val bar = JProgressBar()
    private val score = PendingValueLabel(
        placeholderWidth = POINTS_WIDTH,
        textColor = JBUI.CurrentTheme.ContextHelp.FOREGROUND
    )

    init {
        name.setValue(category)
        setPoints(Points.Pending)
    }

    fun setPoints(points: Points) {
        when (points) {
            is Points.Pending -> {
                fillBar(filled = null, total = null)
                score.setValue(newValue = null, newSuffix = "", newPlaceholderWidth = POINTS_WIDTH)
            }

            is Points.Collected -> {
                fillBar(filled = points.collected, total = points.max)
                score.setValue(
                    newValue = points.collected?.takeIf { points.max != null }?.toString(),
                    newSuffix = points.max?.let { "/$it" } ?: "",
                    newPlaceholderWidth =
                        if (points.max == null) POINTS_WIDTH else SCORE_PLACEHOLDER_WIDTH
                )
            }

            is Points.Remaining -> {
                val untilNext = points.untilNext
                val total = if (untilNext == 0) 1 else points.maxOfNext
                fillBar(filled = if (untilNext != null && total != null) total - untilNext else null, total = total)
                score.setValue(
                    newValue = untilNext?.toString(),
                    newSuffix = "",
                    newPlaceholderWidth = POINTS_WIDTH
                )
            }
        }
    }

    /** An indeterminate bar until both numbers are known. */
    private fun fillBar(filled: Int?, total: Int?) {
        if (filled != null && total != null) {
            bar.isIndeterminate = false
            bar.maximum = total
            bar.value = filled
        } else {
            bar.isIndeterminate = true
        }
    }

    fun naturalScoreWidth(): Int = score.naturalWidth()

    fun pinScoreWidth(width: Int) = score.pinWidth(width)

    fun addTo(table: JPanel, row: Int) {
        val gap = JBUI.scale(VERTICAL_GAP)
        table.add(name, GridBagConstraints().apply {
            gridx = 0; gridy = row
            anchor = GridBagConstraints.LINE_START
            insets = JBUI.insets(gap, 0, gap, JBUI.scale(COLUMN_GAP))
        })
        table.add(bar, GridBagConstraints().apply {
            gridx = 1; gridy = row
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
            insets = JBUI.insets(gap, 0, gap, JBUI.scale(COLUMN_GAP))
        })
        table.add(score, GridBagConstraints().apply {
            gridx = 2; gridy = row
            anchor = GridBagConstraints.LINE_START
            insets = JBUI.insets(gap, 0, gap, 0)
        })
    }
}

private const val CATEGORY_WIDTH = 70
private const val POINTS_WIDTH = 40
private const val SCORE_PLACEHOLDER_WIDTH = 22

private const val COLUMN_GAP = 8
private const val VERTICAL_GAP = 1
