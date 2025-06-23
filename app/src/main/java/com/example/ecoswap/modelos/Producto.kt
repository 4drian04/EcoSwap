package com.example.ecoswap.modelos

data class Producto(
    var userId: String = "",
    var fotoProducto: String = "",
    var descripcion: String = "",
    var nombre: String = "",
    var productoId: String = "",
    var latitud: Double = 0.0,
    var longitud: Double = 0.0,
    var categorias: ArrayList<String> = ArrayList<String>()
) {

    constructor() : this("", "", "", "", "", 0.0, 0.0)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Producto

        return productoId == other.productoId
    }

    override fun hashCode(): Int {
        return productoId.hashCode()
    }
}