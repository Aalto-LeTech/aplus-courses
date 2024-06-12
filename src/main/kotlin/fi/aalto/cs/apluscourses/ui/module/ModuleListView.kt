package fi.aalto.cs.apluscourses.ui.module

import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.ListExpandableItemsHandler
import com.intellij.ui.components.JBList
import com.intellij.ui.hover.TreeHoverListener
import com.intellij.ui.hover.addHoverAndPressStateListener
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.util.ui.UIUtil
import fi.aalto.cs.apluscourses.model.Student
import fi.aalto.cs.apluscourses.presentation.module.ModuleListElementViewModel
import icons.PluginIcons
import java.awt.Component
import javax.swing.*

class ModuleListView : JBList<ModuleListElementViewModel>() {
    //BaseListView<ModuleListElementViewModel?>() {
    val expandedItems = mutableSetOf<Int>()

    init {
        TreeHoverListener.DEFAULT.addTo(this)
        cellRenderer = ModuleListCellRenderer(this)
//        addMouseListener(ModuleListButtonMouseListener(this))
//        addMouseListener(object : MouseAdapter() {
//            override fun mouseClicked(e: MouseEvent) {
//                val index = locationToIndex(e.point)
//                if (index != -1) {
//                    if (expandedItems.contains(index)) {
//                        expandedItems.remove(index)
//                    } else {
//                        expandedItems.add(index)
//                    }
//                    this@ModuleListView.updateUI()
//                }
//            }
//        })
    }

    override fun createExpandableItemsHandler(): ListExpandableItemsHandler {
        return MyEIH(this)
    }

    internal class MyEIH(list: ModuleListView) : ListExpandableItemsHandler(list) {
//        override fun getCellRendererAndBounds(key: Int?): com.intellij.openapi.util.Pair<Component, Rectangle>? {
//            if (key == null) {
//                return null
//            }
//            val bounds = myComponent.getCellBounds(key, key) ?: return null
//
//            val renderer = myComponent.cellRenderer ?: return null
//            val model = myComponent.model
//            if (key >= model.size) {
//                return null
//            }
//
//            val rendererComponent = renderer.getListCellRendererComponent(
//                myComponent, model.getElementAt(key), key,
//                myComponent.isSelectedIndex(key), myComponent.hasFocus()
//            )
//
//            if (myComponent is ModuleListView && (myComponent as ModuleListView).expandedItems.contains(key)) {
//                val panel = JPanel()
//                panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
//                panel.add(rendererComponent)
//                val extraInfo = JLabel(
//                    (model.getElementAt(key) as ModuleListElementViewModel).status ?: "No additional info"
//                )
//                panel.add(extraInfo)
//                AppUIUtil.targetToDevice(panel, myComponent)
//                bounds.height = panel.preferredSize.height
//                return com.intellij.openapi.util.Pair.create(panel, bounds)
//            }
//
//            AppUIUtil.targetToDevice(rendererComponent, myComponent)
//            bounds.width = rendererComponent.preferredSize.width
//            bounds.height = rendererComponent.preferredSize.height
//            return com.intellij.openapi.util.Pair.create(rendererComponent, bounds)
//        }
    }
}

class ModuleListCellRenderer(private val listView: ModuleListView) :
    ColoredListCellRenderer<ModuleListElementViewModel?>() {

    override fun getListCellRendererComponent(
        list: JList<out ModuleListElementViewModel?>,
        value: ModuleListElementViewModel?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        val mainLabel = JLabel(value?.name ?: "Unknown")
        panel.add(mainLabel)

        // Check if the item is expanded
        if (!listView.expandedItems.contains(index)) {
            // Add additional information
            val extraInfo = JLabel(value?.status ?: "No additional info")
            val button = JButton("Install")
            button.addActionListener {
                println("Button clicked for item: ${value?.name}")
            }
            addHoverAndPressStateListener(
                button,
                { a, b -> println("hi ${a}, ${b}") },
                { a, b -> println("hi2 ${a}, ${b}") })
            button.isEnabled = true
            panel.add(extraInfo)
            panel.add(button)
        }

        if (isSelected) {
            panel.background = list.selectionBackground
            mainLabel.foreground = list.selectionForeground
        } else {
            panel.background = list.background
            mainLabel.foreground = list.foreground
        }

        return panel
    }

    override fun customizeCellRenderer(
        list: JList<out ModuleListElementViewModel?>,
        value: ModuleListElementViewModel?,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {
//        icon = PluginIcons.A_PLUS_USER_ACTIVE
//        append(value.presentableName)
//        SpeedSearchUtil.applySpeedSearchHighlighting(list, this, true, selected)
        if (!selected && index % 2 == 0) {
            background = UIUtil.getDecoratedRowColor()
        }
    }

}

/*class ModuleListButtonMouseListener(private val list: ModuleListView) : MouseAdapter() {

    override fun mouseClicked(e: MouseEvent) {
        val index = list.locationToIndex(e.point)
        if (index >= 0) {
            val item = list.model.getElementAt(index)
            val cellBounds = list.getCellBounds(index, index)
//            if (cellBounds.contains(e.point)) {
            val renderer = list.cellRenderer
            val cellComponent = renderer.getListCellRendererComponent(
                list,
                item,
                index,
                list.isSelectedIndex(index),
                list.hasFocus()
            )

            val component = cellComponent.getComponentAt(e.point)
            println(component)
            println(cellComponent.com)
            if (component is JButton
//                    && component.bounds.contains(
//                        e.point.x - cellBounds.x,
//                        e.point.y - cellBounds.y
//                    )
            ) {
                component.doClick()
            }
//            }
        }
    }
}*/
