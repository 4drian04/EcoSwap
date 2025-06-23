package com.example.ecoswap.modelos

data class Intercambio(
    var intercambioId: String = "",
    val comprador: String = "",
    val estado: String = "",
    val productos: Map<String, Producto> = mapOf(),
    val vendedor: String = "",
    val compradorVistoBueno: Boolean = false,
    val vendedorVistoBueno: Boolean = false
)