package com.example.ecoswap.callbacks

import com.example.ecoswap.modelos.Notificacion

// Interfaz NotificacionesCallback para manejar callbacks relacionados con las notificaciones
interface NotificacionesCallBack {
    // Este método se invoca cuando se recibe una lista de notificaciones
    fun onCallback(notificaciones: ArrayList<Notificacion>)
}