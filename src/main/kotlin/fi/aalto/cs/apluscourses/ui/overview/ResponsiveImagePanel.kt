package fi.aalto.cs.apluscourses.ui.overview

import com.intellij.openapi.application.EDT
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.scale.ScaleContext
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.ui.Shimmer
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.intellij.openapi.application.PathManager
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import org.jetbrains.annotations.NonNls
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints
import java.net.URI
import javax.swing.JPanel
import kotlin.math.max

class ResponsiveImagePanel(
    width: Int,
    val maxWidth: Int = width,
    private val maxHeight: Int = DEFAULT_MAX_HEIGHT,
    private val scope: CoroutineScope,
    initialAspectRatio: Double = BANNER_ASPECT_RATIO,
    private val onImageResolved: (width: Int, height: Int) -> Unit = { _, _ -> },
    private val cacheDir: Path = Path.of(PathManager.getSystemPath(), CACHE_DIR),
) : JPanel() {
    private var image: Image? = null
    private var width: Int = 100

    private var heightMultiplier: Double = initialAspectRatio

    private val shimmer = Shimmer(this)

    private var url: String? = null
    private var loadJob: Job? = null

    init {
        updateWidth(width)
    }

    fun setUrl(newUrl: String?) {
        if (newUrl == url) return
        url = newUrl
        loadJob?.cancel()
        if (newUrl == null) {
            shimmer.stop()
            return
        }
        loadJob = scope.launch {
            val loaded = withContext(Dispatchers.IO) { loadBanner(newUrl) }
            withContext(Dispatchers.EDT) {
                if (url != newUrl) return@withContext
                if (loaded == null) {
                    shimmer.stop()
                    repaint()
                    return@withContext
                }
                val (loadedImage, size) = loaded
                image = loadedImage
                heightMultiplier = size.height.toDouble() / size.width.toDouble()
                onImageResolved(size.width, size.height)
                shimmer.stop()
                revalidate()
                repaint()
            }
        }
    }

    private fun loadBanner(url: String): Pair<Image, Dimension>? = try {
        decode(cachedBanner(url) ?: URI(url).toURL())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        CoursesLogger.warn("Could not load the course banner from $url: ${e.message}")
        null
    }

    private fun decode(source: URL): Pair<Image, Dimension>? {
        val icon = IconLoader.findIcon(source, true)
        val loadedImage = icon?.let { IconLoader.toImage(it, ScaleContext.createIdentity()) }
        return when {
            icon == null || loadedImage == null -> null
            icon.iconWidth <= 0 || icon.iconHeight <= 0 -> null
            else -> loadedImage to Dimension(icon.iconWidth, icon.iconHeight)
        }
    }

    private fun cachedBanner(url: String): URL? = try {
        val cached = cacheDir.resolve(cacheKey(url))
        if (Files.exists(cached) && Files.size(cached) > 0) {
            CoursesLogger.info("Course banner served from the cache")
            cached.toUri().toURL()
        } else {
            Files.createDirectories(cached.parent)
            val partial = Files.createTempFile(cached.parent, PARTIAL_PREFIX, PARTIAL_SUFFIX)
            URI(url).toURL().openStream().use { input ->
                Files.newOutputStream(partial).use(input::copyTo)
            }
            Files.move(partial, cached, StandardCopyOption.REPLACE_EXISTING)
            cached.toUri().toURL()
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        CoursesLogger.warn("Could not cache the course banner from $url: ${e.message}")
        null
    }

    private fun cacheKey(url: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(url.toByteArray())
            .joinToString("") { HEX_BYTE.format(it) }

    fun updateWidth(width: Int) {
        this.width = max(width, maxWidth)
        repaint()
    }

    override fun addNotify() {
        super.addNotify()
        if (image == null) shimmer.start()
    }

    override fun removeNotify() {
        shimmer.stop()
        super.removeNotify()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val loadedImage = image
        if (loadedImage == null) {
            shimmer.paint(g, width, height)
            return
        }
        val imageWidth = loadedImage.getWidth(null)
        val imageHeight = loadedImage.getHeight(null)
        if (imageWidth <= 0 || imageHeight <= 0 || width <= 0) return

        val (sliceTop, sliceBottom) = visibleSlice(imageWidth, imageHeight, width, height)

        val graphics2D = g.create() as Graphics2D
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics2D.drawImage(
            loadedImage,
            0, 0, width, height,
            0, sliceTop, imageWidth, sliceBottom,
            this
        )
        graphics2D.dispose()
    }

    override fun getWidth(): Int = width

    override fun getHeight(): Int =
        minOf((width * heightMultiplier).toInt(), JBUI.scale(maxHeight))

    override fun getPreferredSize(): Dimension = Dimension(width, height)
}

/**
 * The horizontal band of the source image that is visible once the banner's height is capped.
 */
internal fun visibleSlice(
    imageWidth: Int,
    imageHeight: Int,
    targetWidth: Int,
    targetHeight: Int
): Pair<Int, Int> {
    val scale = targetWidth.toDouble() / imageWidth
    val visibleHeight = (targetHeight / scale).toInt().coerceIn(1, imageHeight)
    val top = (imageHeight - visibleHeight) / 2
    return top to (top + visibleHeight)
}

@NonNls
private const val CACHE_DIR = "aplus-courses/banners"

@NonNls
private const val PARTIAL_PREFIX = "banner"

@NonNls
private const val PARTIAL_SUFFIX = ".part"

@NonNls
private const val HEX_BYTE = "%02x"

/** Above this height the banner takes too much of a wide tool window. */
private const val DEFAULT_MAX_HEIGHT = 220

/** Height reserved for the course banner, as a fraction of its width, until anything is known. */
const val BANNER_ASPECT_RATIO: Double = 0.5

fun CourseFileManager.rememberedBannerAspectRatio(): Double {
    val width = state.bannerImageWidth
    val height = state.bannerImageHeight
    return if (width > 0 && height > 0) height.toDouble() / width.toDouble() else BANNER_ASPECT_RATIO
}
