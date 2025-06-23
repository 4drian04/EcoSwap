package com.example.ecoswap.callbacks

import com.example.ecoswap.modelos.Valoracion
// Interfaz ValoracionesCallback para manejar callbacks relacionados con las valoraciones
interface ValoracionesCallback {
    // Este método se invoca cuando se recibe una lista de valoraciones
    fun onCallback(valoraciones: List<Valoracion>)
}