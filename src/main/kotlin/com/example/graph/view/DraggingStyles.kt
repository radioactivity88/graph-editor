package com.example.graph.view

import javafx.scene.paint.Color
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class DraggingStyles : Stylesheet() {

    companion object {
        val wrapper by cssclass()
        val toolboxItem by cssclass()
        val workAreaSelected by cssclass()
        val workAreaNotSelected by cssclass()
        val workArea by cssclass()
    }

    init {
        wrapper {
            backgroundColor += Color.WHITE
        }
        workArea {
            backgroundColor += Color.LIGHTGRAY
            borderColor += box(Color.BLACK)
            borderWidth += box(1.px)
        }
        toolboxItem {
            padding = box(4.px)
            stroke = Color.BLACK
            strokeWidth = 1.px
            and(hover) {
                opacity = 0.7
            }
        }
        workAreaNotSelected {
            borderColor += box(Color.TRANSPARENT)
            borderWidth += box(1.px)
        }
        workAreaSelected {
            borderColor += box(Color.BLACK)
            borderWidth += box(1.px)
        }
    }
}