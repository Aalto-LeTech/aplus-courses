package fi.aalto.cs.apluscourses.ui.overview

import java.awt.Component
import java.awt.Container
import java.awt.Graphics
import java.awt.Image
import javax.swing.ImageIcon

/**
 * An `Icon` that scales its image to fill the component area,
 * excluding any border or insets, optionally maintaining the image's aspect
 * ratio by padding and centering the scaled image horizontally or vertically.
 *
 * The class is a drop-in replacement for `ImageIcon`, except that
 * the no-argument constructor is not supported.
 *
 * As the size of the Icon is determined by the size of the component in which
 * it is displayed, `StretchIcon` must only be used in conjunction
 * with a component and layout that does not depend on the size of the
 * component's Icon.
 *
 * Modified from [https://github.com/tips4java/tips4java/blob/main/source/StretchIcon.java]
 */
internal class StretchIcon(image: Image) : ImageIcon(image) {
    /**
     * Determines whether the aspect ratio of the image is maintained.
     * Set to `false` to allow th image to distort to fill the component.
     */
    private var proportionate: Boolean = true

    private var width = 1
    private var height = 1

    /**
     * Paints the icon. The image is reduced or magnified to fit the component to which
     * it is painted.
     *
     * If the proportion has not been specified, or has been specified as `true`,
     * the aspect ratio of the image will be preserved by padding and centering the image
     * horizontally or vertically. Otherwise, the image may be distorted to fill the
     * component it is painted to.
     *
     * If this icon has no image observer, this method uses the `c` component
     * as the observer.
     *
     * @param c the component to which the Icon is painted.  This is used as the
     * observer if this icon has no image observer
     * @param g the graphics context
     * @param x not used.
     * @param y not used.
     *
     * @see ImageIcon.paintIcon
     */
    @Synchronized
    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
        var imageX = x
        var imageY = y
        val image = image ?: return
        val insets = (c as Container).insets
//        imageX = insets.left
//        imageY = insets.top

        var w = c.getWidth() - imageX - insets.right
        var h = c.getHeight() - imageY - insets.bottom

        if (proportionate) {
            var iw = image.getWidth(c)
            var ih = image.getHeight(c)

            if (iw * h < ih * w) {
                iw = (h * iw) / ih
//                imageX += (w - iw) / 2
                w = iw
            } else {
                ih = (w * ih) / iw
//                imageY += (h - ih) / 2
                h = ih
            }
        }

        width = w
        height = h

        val io = imageObserver
        g.drawImage(image, x, y, w, h, io ?: c)
    }

    override fun getIconWidth(): Int = width

    override fun getIconHeight(): Int = height
}