package com.example.graph.model

open class State(id: String, payload: IPayload) : Element(id, payload) {
    var parent : Element? = null
    val edges = ArrayList<Edge>()
}