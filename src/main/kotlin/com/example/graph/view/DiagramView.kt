package com.example.graph.view

import javafx.scene.Node

interface DiagramView {

    fun getSceneScale(): Double

    fun addNodeToScene(node: Node)

    fun removeNodeFromScene(node: Node)

    fun findNodesUnderCursor(x: Double, y: Double): List<Node>
}