package com.example.ecoswap.callbacks

import com.example.ecoswap.modelos.Chatroom

// Interfaz ChatroomCallback para manejar callbacks relacionados con un chatroom
interface ChatroomCallback {

    // Este método se invoca cuando se recibe un chatroom
    fun onCallBack(chatroom: Chatroom)
}