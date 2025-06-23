package com.example.ecoswap.listAdapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoswap.DatabaseService
import com.example.ecoswap.MainActivity
import com.example.ecoswap.R
import com.example.ecoswap.callbacks.UsuarioCallBack
import com.example.ecoswap.modelos.Usuario
import com.example.ecoswap.modelos.Valoracion

class ListAdapterValoracion(
    private var listaValoracion: List<Valoracion>?, // Lista de valoraciones
    private var contextActivity: Context?
) : RecyclerView.Adapter<ListAdapterValoracion.ValoracionViewHolder?>(){

    /**
     * Devuelve el número total de valoraciones en la lista
     */
    override fun getItemCount(): Int {
        return listaValoracion!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ValoracionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_list_adapter_valoracion, parent, false)
        return ValoracionViewHolder(view, contextActivity)
    }

    override fun onBindViewHolder(holder: ValoracionViewHolder, position: Int) {
        val valoracionActual = listaValoracion!![position]
        holder.bindData(valoracionActual)
    }

    class ValoracionViewHolder internal constructor(itemView: View, private val contextActivity: Context?) :
        RecyclerView.ViewHolder(itemView) {
        var nombreUsuario: TextView = itemView.findViewById<TextView>(R.id.nombreUsuarioValoracion)
        var descripcionValoracion: TextView = itemView.findViewById<TextView>(R.id.descripcionValoracion)
        var rating: RatingBar = itemView.findViewById<RatingBar>(R.id.userRatingBar)
        var imagenUsuario : ImageView = itemView.findViewById<ImageView>(R.id.userImageView)

        /**
         * Recibe un objeto Valoracion y asigna los valores a los elementos de la vista.
         */
        fun bindData(item: Valoracion) {
            try {
                DatabaseService().obtenerUsuario(item.uid, object : UsuarioCallBack {
                    override fun onCallback(usuario: Usuario?) {
                        nombreUsuario.setText(usuario!!.nombreUsuario)
                        descripcionValoracion.setText(item.descripcion)
                        rating.rating = item.estrellas
                        Glide.with(contextActivity!!)
                            .load(usuario.urlFotoPerfil).error(R.drawable.ic_profile)
                            .into(imagenUsuario)
                    }
                })
            } catch (e: Exception) {
                Log.d("XXXX", e.toString())
            }
        }
    }
}