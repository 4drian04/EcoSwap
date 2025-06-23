package com.example.ecoswap.listAdapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoswap.MainActivity
import com.example.ecoswap.PerfilOtroUsuario
import com.example.ecoswap.ProductosUsuario
import com.example.ecoswap.R
import com.example.ecoswap.ValoracionUsuario
import com.example.ecoswap.modelos.Intercambio

class ListAdapterIntercambioRealizado (
    private var listaIntercambio: List<Intercambio>?, // Lista de intercambios realizados
    private var contextActivity: Context?
) : RecyclerView.Adapter<ListAdapterIntercambioRealizado.IntercambioViewHolder?>() {

    /**
     * Método que devuelve el número total de intercambios en la lista
     */
    override fun getItemCount(): Int {
        return listaIntercambio!!.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IntercambioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_list_adapter_intercambio_realizado, parent, false)
        return IntercambioViewHolder(
            view,
            contextActivity
        )
    }

    /**
     * Vincula los datos del intercambio en la posición dada
     */
    override fun onBindViewHolder(holder: IntercambioViewHolder, position: Int) {
        val intercambioActual = listaIntercambio!![position]
        holder.bindData(intercambioActual)
        holder.itemView.setOnClickListener {
            val intent = Intent(contextActivity, PerfilOtroUsuario::class.java);
            // Determina si el usuario actual es el vendedor o el comprador
            if(intercambioActual.vendedor == MainActivity.sharedPref.getString("uid", "")!!){
                // Si el usuario actual es el vendedor, establece el ID del comprador, esto es para visitar el perfil del otro usuario
                ProductosUsuario.uid = intercambioActual.comprador;
                ValoracionUsuario.uid = intercambioActual.comprador
                intent.putExtra("uid", intercambioActual.comprador)
            }else{
                // Si el usuario actual es el comprador, establece el ID del vendedor
                ProductosUsuario.uid = intercambioActual.vendedor;
                ValoracionUsuario.uid = intercambioActual.vendedor
                intent.putExtra("uid", intercambioActual.vendedor)
            }
            contextActivity!!.startActivity(intent)
        }
    }

    class IntercambioViewHolder internal constructor(itemView: View, private val contextActivity: Context?) :
        RecyclerView.ViewHolder(itemView) {
        val nombreProducto1: TextView = itemView.findViewById(R.id.primerProductoIntercambiadoNombre)
        val nombreProducto2: TextView = itemView.findViewById(R.id.segundoProductoIntercambiadoNombre)

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
                }

                // Asignar los datos del segundo producto
                if (productosList.size > 1) {
                    val producto2 = productosList[1]
                    nombreProducto2.text = producto2.nombre
                } else {
                    // Si no hay segundo producto, puedes ocultar la vista o manejarlo como desees
                    nombreProducto2.text = "Dinero"
                }
            } catch (e: Exception) {
                Log.d("XXXX", e.toString())
            }
        }
    }
}