package com.example.graph

import com.example.graph.view.DraggingStyles
import com.example.graph.view.PaneView
import tornadofx.App
import tornadofx.launch


class DiagramApp : App(PaneView::class, DraggingStyles::class) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch<DiagramApp>(args)
        }
    }
}
