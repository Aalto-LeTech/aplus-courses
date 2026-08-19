package fi.aalto.cs.apluscourses.ui

import org.jetbrains.annotations.NonNls
import com.intellij.openapi.observable.properties.ObservableProperty
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.ui.JBColor
import com.intellij.ui.components.BrowserLink
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.Animator
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.annotations.TestOnly
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.HierarchyEvent
import java.awt.event.HierarchyListener
import java.awt.RenderingHints
import java.awt.geom.RoundRectangle2D
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt

/**
 * The shared pulse phase for the loading animations.
 */
object ShimmerClock {
    private val subscribers = mutableSetOf<Shimmer>()

    private val animator by lazy { PulseAnimator() }

    /** Whether [animator] is currently running. */
    private var pacing = false

    /** How far through the pulse cycle we are, in `[0, 1)`. */
    var phase: Float = 0f
        private set

    private var lastPaintedStep = Int.MIN_VALUE

    /** The block's current tint strength. */
    val intensity: Float
        get() {
            val eased = (1f - cos(phase * 2f * PI.toFloat())) / 2f
            return MIN_INTENSITY + (1f - MIN_INTENSITY) * eased
        }

    /** Starts over from phase zero and leaves nothing running. */
    @TestOnly
    fun reset() {
        phase = 0f
        lastPaintedStep = Int.MIN_VALUE
        subscribers.clear()
        refreshPacing()
    }

    /** Steps the pulse by one frame without waiting on the animator. */
    @TestOnly
    fun tick() {
        phase = (phase + 1f / TOTAL_FRAMES) % 1f
        repaintOnScreen()
    }

    /** The frame is derived by the platform from elapsed time, so a busy EDT drops frames. */
    internal fun onAnimatorFrame(frame: Int, totalFrames: Int) {
        phase = frame.toFloat() / totalFrames
        repaintOnScreen()
    }

    /** Repaints visible placeholders, skipping frames whose quantized intensity has not changed. */
    private fun repaintOnScreen() {
        val step = (intensity * QUANTIZATION).roundToInt()
        if (step == lastPaintedStep) return
        lastPaintedStep = step
        subscribers.toList().forEach { if (it.isOnScreen) it.repaintTarget() }
    }

    fun subscribe(shimmer: Shimmer) {
        subscribers += shimmer
        refreshPacing()
    }

    fun unsubscribe(shimmer: Shimmer) {
        subscribers -= shimmer
        refreshPacing()
    }

    fun isSubscribed(shimmer: Shimmer): Boolean = shimmer in subscribers

    /**
     * Runs the animator only while a placeholder is on screen. A collapsed tool window or hidden
     * tab keeps placeholders attached, so `isShowing` is the test, not hierarchy membership.
     */
    internal fun refreshPacing() {
        val shouldRun = subscribers.any { it.isOnScreen }
        if (shouldRun == pacing) return
        pacing = shouldRun
        if (shouldRun) animator.resume() else animator.suspend()
    }

    /** One full pulse cycle, in milliseconds. */
    internal const val CYCLE_MS = 2500

    /** Sixty frames a second across [CYCLE_MS]. */
    internal const val TOTAL_FRAMES = 150

    /** The dimmest the block gets, as a fraction of its peak. */
    private const val MIN_INTENSITY = 0.55f

    /** Distinct intensities worth repainting for. */
    private const val QUANTIZATION = 64
}

@Service(Service.Level.APP)
internal class SkeletonAnimationScope(val cs: CoroutineScope)

@NonNls
private const val PULSE_NAME = "A+ skeleton pulse"

private class PulseAnimator : Animator(
    PULSE_NAME,
    ShimmerClock.TOTAL_FRAMES,
    ShimmerClock.CYCLE_MS,
    isRepeatable = true,
    isForward = true,
    coroutineScope = service<SkeletonAnimationScope>().cs
) {
    override fun paintNow(frame: Int, totalFrames: Int, cycle: Int) =
        ShimmerClock.onAnimatorFrame(frame, totalFrames)
}

/** The pulsing gray fill shared by every placeholder. */
class Shimmer(private val component: JComponent) {
    private val onShowingChanged = HierarchyListener { event ->
        if (event.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong() != 0L) {
            ShimmerClock.refreshPacing()
        }
    }

    val isRunning: Boolean get() = ShimmerClock.isSubscribed(this)

    internal val isOnScreen: Boolean get() = component.isShowing

    internal fun repaintTarget() = component.repaint()

    fun start() {
        if (isRunning) return
        component.addHierarchyListener(onShowingChanged)
        ShimmerClock.subscribe(this)
    }

    fun stop() {
        if (!isRunning) return
        component.removeHierarchyListener(onShowingChanged)
        ShimmerClock.unsubscribe(this)
    }

    fun paint(g: Graphics, width: Int, height: Int, top: Int = 0) {
        if (width <= 0 || height <= 0) return

        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            val arc = JBUI.scale(ARC).toFloat()
            g2.color = pulsed(BASE_COLOR)
            g2.fill(RoundRectangle2D.Float(0f, top.toFloat(), width.toFloat(), height.toFloat(), arc, arc))
        } finally {
            g2.dispose()
        }
    }

    /**
     * [base] with its alpha scaled by the pulse.
     */
    private fun pulsed(base: Color): Color =
        Color(base.red, base.green, base.blue, (base.alpha * ShimmerClock.intensity).roundToInt())

    private companion object {
        const val ARC = 6

        val BASE_COLOR: JBColor = JBColor(Color(0, 0, 0, 30), Color(255, 255, 255, 28))
    }
}

fun tabularFont(): Font {
    val base = JBUI.Fonts.label()
    return Font(Font.MONOSPACED, base.style, base.size)
}

/**
 * A gray placeholder standing in for a piece of text. It takes up a whole line of [font], so a
 * row of placeholders is as tall as the row of text that replaces it.
 */
class SkeletonBlock(private val blockWidth: Int, font: Font? = null) : JComponent() {
    private val shimmer = Shimmer(this)

    @get:TestOnly
    val isShimmering: Boolean get() = shimmer.isRunning

    init {
        isOpaque = false
        if (font != null) this.font = font
    }

    private fun lineHeight(): Int = getFontMetrics(font).height

    override fun getPreferredSize(): Dimension = Dimension(JBUI.scale(blockWidth), lineHeight())

    override fun getMinimumSize(): Dimension = Dimension(JBUI.scale(MIN_WIDTH), lineHeight())

    override fun addNotify() {
        super.addNotify()
        shimmer.start()
    }

    override fun removeNotify() {
        shimmer.stop()
        super.removeNotify()
    }

    override fun paintComponent(g: Graphics) {
        val bar = (lineHeight() * BAR_HEIGHT_RATIO).toInt().coerceAtLeast(1)
        shimmer.paint(g, width, bar, top = (height - bar) / 2)
    }

    private companion object {
        const val MIN_WIDTH = 16
        const val BAR_HEIGHT_RATIO = 0.62
    }
}

/**
 * A cell rendering [value] as text, or an equally sized placeholder while it is null.
 */
fun Row.pendingText(
    value: ObservableProperty<String?>,
    placeholderWidth: Int,
    font: Font? = null,
    textColor: Color? = null
): Cell<PendingValueLabel> {
    val label = PendingValueLabel(placeholderWidth, textColor = textColor, labelFont = font)
    label.setValue(value.get())
    value.afterChange { label.setValue(it) }
    return cell(label)
}

class PendingValueLabel(
    private var placeholderWidth: Int,
    private val prefix: String = "",
    private var suffix: String = "",
    private val textColor: Color? = null,
    private val labelFont: Font? = null,
    tabularValue: Boolean = false,
) : JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)) {
    private var pinnedWidth: Int? = null
    private var value: String? = null

    private val numberFont: Font? = if (tabularValue) tabularFont() else labelFont

    init {
        isOpaque = false
        rebuild()
    }

    fun setValue(
        newValue: String?,
        newSuffix: String = suffix,
        newPlaceholderWidth: Int = placeholderWidth
    ) {
        if (newValue == value && newSuffix == suffix && newPlaceholderWidth == placeholderWidth) return
        value = newValue
        suffix = newSuffix
        placeholderWidth = newPlaceholderWidth
        rebuild()
    }

    private fun rebuild() {
        removeAll()
        if (prefix.isNotEmpty()) add(label(prefix, labelFont))
        value?.let { add(label(it, numberFont)) } ?: add(SkeletonBlock(placeholderWidth, numberFont))
        if (suffix.isNotEmpty()) add(label(suffix, numberFont))
        revalidate()
        repaint()
    }

    override fun getPreferredSize(): Dimension {
        val natural = super.getPreferredSize()
        return pinnedWidth?.let { Dimension(maxOf(it, natural.width), natural.height) } ?: natural
    }

    fun naturalWidth(): Int = super.getPreferredSize().width

    fun pinWidth(width: Int) {
        if (pinnedWidth == width) return
        pinnedWidth = width
        revalidate()
    }

    private fun label(text: String, withFont: Font?) = JBLabel(text).apply {
        textColor?.let { foreground = it }
        withFont?.let { font = it }
    }
}

/** A link whose address is not known yet, with a placeholder holding its place until it is. */
class PendingLink(
    private val placeholderWidth: Int,
    private val text: String,
    private val icon: Icon? = null
) : JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)) {
    private var url: String? = null

    init {
        isOpaque = false
        rebuild()
    }

    fun setUrl(newUrl: String?) {
        if (newUrl == url) return
        url = newUrl
        rebuild()
    }

    private fun rebuild() {
        removeAll()
        add(url?.let(::link) ?: SkeletonBlock(placeholderWidth))
        revalidate()
        repaint()
    }

    private fun link(url: String) = BrowserLink(text, url).apply {
        this@PendingLink.icon?.let { setIcon(it, false) }
        isFocusPainted = false
    }
}

/** How many placeholder rows to show when not even the category names are known yet. */
const val PLACEHOLDER_POINTS_ROWS: Int = 3

const val COURSE_NAME_WIDTH: Int = 170
const val USER_NAME_WIDTH: Int = 90
const val LINK_WIDTH: Int = 110

/** Wide enough for "Week 1 closing 3.9.2026 23:59 (in 2 weeks)". */
const val CLOSING_WIDTH: Int = 210
