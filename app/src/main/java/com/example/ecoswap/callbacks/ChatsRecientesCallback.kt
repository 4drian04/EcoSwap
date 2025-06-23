package com.example.ecoswap.callbacks

import com.example.ecoswap.modelos.Chatroom

// Interfaz ChatsRecientesCallback para manejar callbacks relacionados con los chats
interface ChatsRecientesCallback {
    // Este método se invoca cuando se recibe una lista de Chatroom
    fun onCallBack(chats: List<Chatroom>)
}