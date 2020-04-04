package com.example.graph.model

class Edge(id: String, payload: IPayload) : Element(id = id, payload = payload) {
    var parent : Element? = null
    var source : State? = null
    var dest : State? = null
}