package com.example.ecoswap.listAdapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoswap.DetalleIntercambio
import com.example.ecoswap.R
import com.example.ecoswap.modelos.Intercambio

class ListAdapterIntercambio (
    private var listaIntercambio: List<Intercambio>?, // Lista de intercambios
    private var contextActivity: Context?
) : RecyclerView.Adapter<ListAdapterIntercambio.IntercambioViewHolder?>() {


    /**
     * Devuelve el número total de intercambios en la lista
     */
    override fun getItemCount(): Int {
        return listaIntercambio!!.size
    }

    /**
     * Crea un nuevo ViewHolder para un elemento de la lista
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IntercambioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_list_adapter_intercambio, parent, false)
        return IntercambioViewHolder(view, contextActivity)
    }

    override fun onBindViewHolder(holder: IntercambioViewHolder, position: Int) {
        val intercambioActual = listaIntercambio!![position]
        holder.bindData(intercambioActual)
        holder.itemView.setOnClickListener {
            DetalleIntercambio.intercambio = intercambioActual
            val intent = Intent(contextActivity, DetalleIntercambio::class.java) // Crea un intent para abrir DetalleIntercambio
            contextActivity!!.startActivity(intent)
        }
    }

    class IntercambioViewHolder internal constructor(itemView: View, private val contextActivity: Context?) :
        RecyclerView.ViewHolder(itemView) {
        val imagenProducto1: ImageView = itemView.findViewById(R.id.imagenProducto1)
        val nombreProducto1: TextView = itemView.findViewById(R.id.nombreProducto1)
        val imagenProducto2: ImageView = itemView.findViewById(R.id.imagenProducto2)
        val nombreProducto2: TextView = itemView.findViewById(R.id.nombreProducto2)

        /**
         * Método que recibe un objeto Intercambio y asigna los valores a los elementos de la vista.
         */
        fun bindData(item: Intercambio) {
            try {
                val productosList = item.productos.values.toList()

                // Asignar los datos del primer producto
                if (productosList.isNotEmpty()) {
                    val producto1 = productosList[0]
                    nombreProducto1.text = producto1.nombre
                    Glide.with(contextActivity!!)
                        .load(producto1.fotoProducto).placeholder(R.drawable.loading).error(R.drawable.producto)
                        .into(imagenProducto1)
                }

                // Asignar los datos del segundo producto
                if (productosList.size > 1) {
                    val producto2 = productosList[1]
                    nombreProducto2.text = producto2.nombre
                    Glide.with(contextActivity!!)
                        .load(producto2.fotoProducto).placeholder(R.drawable.loading).error(R.drawable.producto)
                        .into(imagenProducto2)
                } else {
                    // Si no hay segundo producto, puedes ocultar la vista o manejarlo como desees
                    nombreProducto2.text = "Dinero"
                    imagenProducto2.setImageResource(R.drawable.dinero) // Cambia a un placeholder si no hay segundo producto
                }
            } catch (e: Exception) {
                Log.d("XXXX", e.toString())
            }
        }
    }
}