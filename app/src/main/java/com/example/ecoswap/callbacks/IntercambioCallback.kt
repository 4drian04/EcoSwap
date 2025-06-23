package com.example.ecoswap.callbacks

import com.example.ecoswap.modelos.Intercambio

// Interfaz IntercambioCallback para manejar callbacks relacionados con los intercambios
interface IntercambioCallback {
    // Este método se invoca cuando se recibe una lista de intercambios
    fun onCallBack(intercambios: List<Intercambio>)
}