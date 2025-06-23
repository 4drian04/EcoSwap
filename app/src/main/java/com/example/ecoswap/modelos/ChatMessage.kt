package com.example.ecoswap.modelos

data class ChatMessage(
    var mensajeId: String = "",
    val mensaje: String = "",
    val remitenteId: String = "",
    val time: Long = 0L
)