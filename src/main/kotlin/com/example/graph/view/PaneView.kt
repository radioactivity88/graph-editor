package com.example.graph.view


import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.text.Text
import org.slf4j.LoggerFactory
import tornadofx.*

class PaneView : View("Diagram"), DiagramView {
    companion object {
        private val log = LoggerFactory.getLogger(PaneView::class.java)
        private const val MIN_X = -1500.0
        private const val MIN_Y = MIN_X
        private const val MAX_X = -MIN_X
        private const val MAX_Y = -MIN_Y

        private const val MAX_SCALE = 10.0
        private const val MIN_SCALE = .1
        private const val DELTA_SCALE = 1.2
    }

    private val scale = SimpleDoubleProperty(1.0)

    private var toolbox: Parent by singleAssign()

    private val paneItems = mutableListOf<Node>()
    private var workArea: Pane by singleAssign()

    private var itemSelectionBorder: Rectangle? = null
    private var selectedItem: Node? = null
    private val dragContext = DragContext()

    private val viewListener = MainViewListener()

    override val root = hbox {
        addClass(DraggingStyles.wrapper)

        toolbox = vbox {

//            fun createToolboxItem(c: Color): Rectangle {
//                return rectangle(width = RECTANGLE_WIDTH, height = RECTANGLE_HEIGHT) {
//                    fill = c
//                    properties["rectColor"] = c
//                    addClass(DraggingStyles.toolboxItem)
//                }
//            }
//
//            add(createToolboxItem(Color.RED))
//            add(createToolboxItem(Color.BLUE))
//            add(createToolboxItem(Color.YELLOW))

            spacing = 10.0
            padding = Insets(10.0)
            alignment = Pos.CENTER

            hboxConstraints {
                hgrow = Priority.NEVER
            }
        }

        anchorpane {
            workArea = pane {

                addClass(DraggingStyles.workArea)

                anchorpaneConstraints {
                    leftAnchor = 0.0
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
                scaleXProperty().bind(scale)
                scaleYProperty().bind(scale)
            }

            hboxConstraints {
                hgrow = Priority.ALWAYS
            }
        }
        vboxConstraints {
            vgrow = Priority.ALWAYS
        }

        padding = Insets(10.0)
        spacing = 10.0

        addEventFilter(MouseEvent.MOUSE_PRESSED, ::startDrag)
        addEventFilter(MouseEvent.MOUSE_DRAGGED, ::drag)
        addEventFilter(MouseEvent.MOUSE_RELEASED, ::endDrag)
        setOnScroll { scroll(it) }

        /**
         * Nodes for example
         */
        addNode(createRectangle(50.0, 50.0, 50.0, 50.0, Color.GRAY, "Hello"))
        addNode(createRectangle(15.00, 50.0, 50.0, 50.0, Color.GREEN, "Hello2"))
    }

    private fun createRectangle(x: Double, y: Double, width: Double, height: Double, color: Color, text: String): Node {
        val rect = Rectangle(x, y, width, height)
        rect.fill = color
        val text = Text(x + width / 2, y + height / 2, text)
        return Group(rect, text)
    }

    private fun scroll(evt: ScrollEvent) {

        var actualScale = scale.get()
        val oldScale = actualScale

        if (evt.deltaY < 0) {
            actualScale /= DELTA_SCALE
        } else {
            actualScale *= DELTA_SCALE
        }
        actualScale = clamp(actualScale, MIN_SCALE, MAX_SCALE)

        val f = (actualScale / oldScale) - 1
        val dx = (evt.sceneX - (workArea.boundsInParent.width / 2 + workArea.boundsInParent.minX))
        val dy = (evt.sceneY - (workArea.boundsInParent.height / 2 + workArea.boundsInParent.minY))
        scale.set(actualScale)
        workArea.translateX -= dx * f
        workArea.translateY -= dy * f

        itemSelectionBorder?.let {
            it.strokeDashArray.setAll(5.0 / actualScale, 5.0 / actualScale)
            it.strokeWidth = 1 / actualScale
        }

        evt.consume()
    }

    private fun clamp(value: Double, min: Double, max: Double): Double {
        if (value.compareTo(min) < 0) return min
        return if (value.compareTo(max) > 0) max else value
    }

    private fun startDrag(evt: MouseEvent) {
        if (!evt.isPrimaryButtonDown) return
        viewListener.onPressed(evt.sceneX, evt.sceneY, evt.isControlDown, evt.isShiftDown)

        if (selectedItem != null) {
            val point = selectedItem!!.sceneToLocal(evt.sceneX, evt.sceneY)
            if (!selectedItem!!.contains(point)) {
                removeSelection()
            }
        }

        if (selectedItem == null) {
            paneItems
                .lastOrNull {
                    val point = it.sceneToLocal(evt.sceneX, evt.sceneY)
                    it.contains(point)
                }
                .apply { selectedItem = this }
        }

        if (selectedItem != null) {
            if (itemSelectionBorder == null) {
                itemSelectionBorder = createSelectionBorder(selectedItem!!)
                workArea.add(itemSelectionBorder!!)
            }
            dragContext.mouseAnchorX = evt.sceneX
            dragContext.mouseAnchorY = evt.sceneY
            dragContext.translateAnchorX = selectedItem!!.translateX
            dragContext.translateAnchorY = selectedItem!!.translateY
        }
    }

    private fun removeSelection() {
        selectedItem = null
        itemSelectionBorder.let { workArea.children.remove(it) }
        itemSelectionBorder = null
    }

    private fun createSelectionBorder(node: Node): Rectangle {
        val bounds = node.boundsInLocal
        val rect = Rectangle(bounds.minX, bounds.minY, bounds.width, bounds.height)
        val actualScale = scale.get()
        rect.translateX = node.translateX
        rect.translateY = node.translateY
        rect.fill = Color.TRANSPARENT
        rect.stroke = Color.BLUE
        rect.strokeDashArray.setAll(5.0 / actualScale, 5.0 / actualScale)
        rect.strokeWidth = 1 / actualScale
        return rect
    }

    private fun drag(evt: MouseEvent) {
        if (!evt.isPrimaryButtonDown) return
        viewListener.onMoved(evt.sceneX, evt.sceneY, evt.isControlDown, evt.isShiftDown)

        if (selectedItem != null) {
            val actualScale = scale.get()
            var x = dragContext.translateAnchorX + ((evt.sceneX - dragContext.mouseAnchorX) / actualScale)
            var y = dragContext.translateAnchorY + ((evt.sceneY - dragContext.mouseAnchorY) / actualScale)

            x = if (x < MIN_X) MIN_X else if (x > MAX_X) MAX_X else x
            y = if (y < MIN_Y) MIN_Y else if (y > MAX_Y) MAX_Y else y

            log.info("New coordinates are ($x,$y)")
            selectedItem!!.translateX = x
            selectedItem!!.translateY = y
            itemSelectionBorder?.let {
                it.translateX = x
                it.translateY = y
            }
            evt.consume()
        }
    }

    private fun endDrag(evt: MouseEvent) {
        viewListener.onReleased(evt.sceneX, evt.sceneY, evt.isControlDown, evt.isShiftDown)
    }


    private fun addNode(node: Node) {
        paneItems.add(node)
        workArea.add(node)
    }

    override fun addNodeToScene(node: Node) {
        workArea.add(node)
    }

    override fun removeNodeFromScene(node: Node) {
        workArea.children.remove(node)
    }

    override fun findNodesUnderCursor(x: Double, y: Double): List<Node> {
        return workArea.children.filter {
            val point = it.sceneToLocal(x, y)
            it.contains(point)
        }
    }
}