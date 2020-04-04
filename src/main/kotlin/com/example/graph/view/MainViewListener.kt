package com.example.graph.view

import javafx.geometry.BoundingBox
import javafx.geometry.Bounds
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import org.slf4j.LoggerFactory

class MainViewListener(private val diagramView: DiagramView) : ViewListener {
    companion object {
        private val log = LoggerFactory.getLogger(MainViewListener::class.java)
        private const val MIN_X = -1500.0
        private const val MIN_Y = MIN_X
        private const val MAX_X = -MIN_X
        private const val MAX_Y = -MIN_Y
    }

    private val paneItems = mutableListOf<Node>()
    private var itemSelectionBorder: Rectangle? = null
    private var selectedItems = mutableListOf<Node>()
    private val dragContext = DragContext()

    init {
        /**
         * Nodes for example
         */
        addNode(createRectangle(50.0, 50.0, 50.0, 50.0, Color.GRAY, "Hello"))
        addNode(createRectangle(15.00, 50.0, 50.0, 50.0, Color.GREEN, "Hello2"))
    }

    override fun onScaleChanged(scale: Double) {
        itemSelectionBorder?.let {
            it.strokeDashArray.setAll(5.0 / scale, 5.0 / scale)
            it.strokeWidth = 1 / scale
        }
    }

    override fun onClicked(x: Double, y: Double, ctrl: Boolean, shift: Boolean) {
        if (dragContext.mouseAnchorX == x && dragContext.mouseAnchorY == y) {
            selectIfNotSelected(x, y)
        }
    }

    override fun onPressed(x: Double, y: Double, ctrl: Boolean, shift: Boolean) {
        dragContext.mouseAnchorX = x
        dragContext.mouseAnchorY = y

        // Release selection
        if (!ctrl) {
            deselectIfNotInFocuse(x, y)
        }
        selectIfNotSelected(x, y)
    }

    override fun onMoved(x: Double, y: Double, ctrl: Boolean, shift: Boolean) {
        if (selectedItems.isNotEmpty()) {
            val actualScale = diagramView.getSceneScale()
            val dx = ((x - dragContext.mouseAnchorX) / actualScale)
            val dy = ((y - dragContext.mouseAnchorY) / actualScale)

            dragContext.mouseAnchorX = x
            dragContext.mouseAnchorY = y

//            dx = if (dx < MIN_X) MIN_X else if (dx > MAX_X) MAX_X else dx
//            dy = if (dy < MIN_Y) MIN_Y else if (dy > MAX_Y) MAX_Y else dy

            log.info("New coordinates are ($dx,$dy)")
            selectedItems.forEach {
                it.translateX += dx
                it.translateY += dy
            }
            itemSelectionBorder?.let {
                it.translateX += dx
                it.translateY += dy
            }
        }
    }

    override fun onReleased(x: Double, y: Double, ctrl: Boolean, shift: Boolean) {

    }

    private fun deselectIfNotInFocuse(x: Double, y: Double) {
        if (selectedItems.isNotEmpty()) {
            val present = selectedItems.any {
                val point = it.sceneToLocal(x, y)
                it.contains(point)
            }
            if (!present) {
                selectedItems.clear()
                updateSelectionBorder(selectedItems)

            }
        }
    }

    private fun selectIfNotSelected(x: Double, y: Double) {
        paneItems.lastOrNull {
            val point = it.sceneToLocal(x, y)
            it.contains(point) && !selectedItems.contains(it)
        }?.apply {
            selectedItems.add(this)
            updateSelectionBorder(selectedItems)
        }
    }

    private fun addNode(node: Node) {
        paneItems.add(node)
        diagramView.addNodeToScene(node)
    }

    private fun createRectangle(x: Double, y: Double, width: Double, height: Double, color: Color, text: String): Node {
        val rect = Rectangle(x, y, width, height)
        rect.fill = color
        val textView = Text(text)
        textView.x = x + (width - textView.boundsInLocal.width) / 2
        textView.y = y + height / 2
        return Group(rect, textView)
    }

    private fun updateSelectionBorder(selectedNode: List<Node>) {
        // Remove old selection border
        itemSelectionBorder?.let { diagramView.removeNodeFromScene(it) }
        // Create new
        itemSelectionBorder = createSelectionBorder(selectedNode)
        // Put on scene
        itemSelectionBorder?.let { diagramView.addNodeToScene(it) }
    }

    private fun createSelectionBorder(nodes: List<Node>): Rectangle? {
        val bounds = getCommonBorder(nodes)
        return bounds?.let {
            val rect = Rectangle(bounds.minX, bounds.minY, bounds.width, bounds.height)
            val actualScale = diagramView.getSceneScale()
//            rect.translateX = node.translateX
//            rect.translateY = node.translateY
            rect.fill = Color.TRANSPARENT
            rect.stroke = Color.BLUE
            rect.strokeDashArray.setAll(5.0 / actualScale, 5.0 / actualScale)
            rect.strokeWidth = 1 / actualScale
            return rect
        }

    }

    private fun getCommonBorder(nodes: List<Node>): Bounds? {
        var minX: Double? = null
        var minY: Double? = null
        var maxX: Double? = null
        var maxY: Double? = null
        nodes.forEach {
            val bounds = it.boundsInParent
            if (minX == null) {
                minX = bounds.minX
                maxX = bounds.maxX
                minY = bounds.minY
                maxY = bounds.maxY
            } else {
                if (minX!! > bounds.minX) minX = bounds.minX
                if (minY!! > bounds.minY) minY = bounds.minY
                if (maxX!! < bounds.maxX) maxX = bounds.maxX
                if (maxY!! < bounds.maxY) maxY = bounds.maxY
            }
        }
        if (minX != null)
            return BoundingBox(minX!!, minY!!, maxX!! - minX!!, maxY!! - minY!!)
        return null
    }
}