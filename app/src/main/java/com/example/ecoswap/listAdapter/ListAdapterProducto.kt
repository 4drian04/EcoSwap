package com.example.ecoswap.listAdapter

import com.example.ecoswap.modelos.Producto
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
import com.example.ecoswap.DetalleProducto
import com.example.ecoswap.ProductosAIntercambiar
import com.example.ecoswap.R

class ListAdapterProducto(
    private var productoList: List<Producto>?,
    private var contextActivity: Context?
) : RecyclerView.Adapter<ListAdapterProducto.ProductoViewHolder?>() {

    /**
     * Este método devuelve el número de items que tiene la lista de productos
     */
    override fun getItemCount(): Int {
        return productoList!!.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_list_adapter_producto, parent, false)
        return ProductoViewHolder(view, contextActivity)
    }

    // Vincula los datos del producto en la posición dada con el ViewHolder
    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val productoActual = productoList!![position]
        holder.bindData(productoActual)
        holder.itemView.setOnClickListener {
            if(DetalleProducto.esProductoIntercambiado){
                if(ProductosAIntercambiar.productoElegido == null){
                    ProductosAIntercambiar.productoElegido = productoActual
                    holder.itemView.setBackgroundResource(R.color.verde_lima)
                }else{
                    if(ProductosAIntercambiar.productoElegido!! == productoActual){
                        holder.itemView.setBackgroundResource(R.color.white)
                        ProductosAIntercambiar.productoElegido = null;
                    }
                }
            }else{
                val intent = Intent(contextActivity, DetalleProducto::class.java)
                intent.putExtra("productoId", productoActual.productoId)
                contextActivity!!.startActivity(intent)
            }
        }
    }

    class ProductoViewHolder internal constructor(itemView: View, private val contextActivity: Context?) :
        RecyclerView.ViewHolder(itemView) {
        var nombreProducto: TextView = itemView.findViewById<TextView>(R.id.nombreProducto)
        var fotoProducto: ImageView = itemView.findViewById<ImageView>(R.id.imagenProducto)
        lateinit var apikeyUsuario: String

        /**
         * Recibe un objeto Producto y asigna los valores a los elementos de la vista.
         */
        fun bindData(item: Producto) {
            try {
                nombreProducto.setText(item.nombre)
                Glide.with(contextActivity!!).load(item.fotoProducto).placeholder(R.drawable.loading).error(R.drawable.producto)
                    .error(R.drawable.producto).into(fotoProducto)
            } catch (e: Exception) {
                Log.d("XXXX", e.toString())
            }
        }
    }
}

