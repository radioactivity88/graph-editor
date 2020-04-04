package com.example.graph.view

interface ViewListener {

    fun onScaleChanged(scale: Double)

    fun onClicked(x: Double, y: Double, ctrl: Boolean, shift: Boolean)

    fun onPressed(x: Double, y: Double, ctrl: Boolean, shift: Boolean)

    fun onMoved(x: Double, y: Double, ctrl: Boolean, shift: Boolean)

    fun onReleased(x: Double, y: Double, ctrl: Boolean, shift: Boolean)

}