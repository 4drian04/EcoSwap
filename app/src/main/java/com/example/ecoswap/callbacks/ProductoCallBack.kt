package com.example.ecoswap.callbacks

import com.example.ecoswap.modelos.Producto
// Interfaz ProductoCallback para manejar callbacks relacionados con los productos
interface ProductoCallBack {
    // Este método se invoca cuando se recibe un producto
    fun onCallback(producto: Producto?)
}