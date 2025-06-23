package com.example.ecoswap.callbacks

import com.example.ecoswap.modelos.Producto
// Interfaz ProductoCallback para manejar callbacks relacionados con los productos
interface ProductosCallBack {
    // Este método se invoca cuando se recibe una lista de productos
    fun onCallback(producto: List<Producto>?)
}