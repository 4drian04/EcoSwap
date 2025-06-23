package com.example.ecoswap.callbacks

import com.example.ecoswap.modelos.Usuario

/**
 * Definimos esta interfaz, ya que nos va a permitir notificar al fragmento
 * que el usuario se ha obtenido
 */
interface UsuarioCallBack {
    // Este método se invoca cuando se recibe una lista de productos
    fun onCallback(usuario: Usuario?)
}