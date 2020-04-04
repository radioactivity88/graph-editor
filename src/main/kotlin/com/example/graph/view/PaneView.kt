package com.example.graph.view


import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import org.slf4j.LoggerFactory
import tornadofx.*

class PaneView : View("Diagram"), DiagramView {
    companion object {
        private val log = LoggerFactory.getLogger(PaneView::class.java)

        private const val MAX_SCALE = 10.0
        private const val MIN_SCALE = .1
        private const val DELTA_SCALE = 1.2
    }

    private val scale = SimpleDoubleProperty(1.0)

    private var toolbox: Parent by singleAssign()


    private var workArea: Pane by singleAssign()

    private var viewListener: ViewListener

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

        addEventFilter(MouseEvent.MOUSE_CLICKED, ::mouseClicked)
        addEventFilter(MouseEvent.MOUSE_PRESSED, ::mousePressed)
        addEventFilter(MouseEvent.MOUSE_DRAGGED, ::mouseDragged)
        addEventFilter(MouseEvent.MOUSE_RELEASED, ::mouseReleased)
        setOnScroll { scrollWheel(it) }
    }

    init {
        viewListener = MainViewListener(this)

    }

    private fun mouseClicked(evt: MouseEvent) {
        viewListener.onClicked(evt.sceneX, evt.sceneY, evt.isControlDown, evt.isShiftDown)
    }

    private fun scrollWheel(evt: ScrollEvent) {

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

        viewListener.onScaleChanged(actualScale)

        evt.consume()
    }

    private fun clamp(value: Double, min: Double, max: Double): Double {
        if (value.compareTo(min) < 0) return min
        return if (value.compareTo(max) > 0) max else value
    }

    private fun mousePressed(evt: MouseEvent) {
        if (!evt.isPrimaryButtonDown) return
        viewListener.onPressed(evt.sceneX, evt.sceneY, evt.isControlDown, evt.isShiftDown)
    }

    private fun mouseDragged(evt: MouseEvent) {
        if (!evt.isPrimaryButtonDown) return
        viewListener.onMoved(evt.sceneX, evt.sceneY, evt.isControlDown, evt.isShiftDown)
    }

    private fun mouseReleased(evt: MouseEvent) {
        viewListener.onReleased(evt.sceneX, evt.sceneY, evt.isControlDown, evt.isShiftDown)
    }

    override fun getSceneScale() = scale.get()

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