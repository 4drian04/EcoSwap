package com.example.ecoswap.callbacks

import com.example.ecoswap.modelos.Valoracion
// Interfaz ValoracionCallback para manejar callbacks relacionados con la valoración
interface ValoracionCallback {
    // Este método se invoca cuando se recibe una valoración
    fun onCallback(valoracion: Valoracion?)
}