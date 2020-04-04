package com.example.graph

import com.example.graph.view.PaneView
import tornadofx.App
import tornadofx.launch


class DiagramApp : App(PaneView::class, Styles::class) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch<DiagramApp>(args)
        }
    }
}
