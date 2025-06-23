package com.example.ecoswap.modelos

data class Chatroom (
    var chatroomId: String = "",
    var userIds: List<String> = emptyList(),
    var ultimoMensajeTimestamp: Long = 0L,
    var ultimoMensajeRemitenteId: String = "",
    var ultimoMensaje: String = "",
    val chats: Map<String, ChatMessage> = emptyMap()
)