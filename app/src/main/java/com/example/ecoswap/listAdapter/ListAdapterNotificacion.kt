package com.example.ecoswap.listAdapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoswap.DatabaseService
import com.example.ecoswap.MainActivity
import com.example.ecoswap.Notificaciones
import com.example.ecoswap.R
import com.example.ecoswap.modelos.Notificacion

class ListAdapterNotificacion (
    private var listaNotificacion: ArrayList<Notificacion>, // Lista de notificaciones
    private var contextActivity: Context?,
) : RecyclerView.Adapter<ListAdapterNotificacion.NotificacionViewHolder?>() {

    /**
     * Devuelve el número total de notificaciones en la lista
     */
    override fun getItemCount(): Int {
        return listaNotificacion.size
    }
    // Vincula los datos de la notificación en la posición dada con el ViewHolder
    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        val notificacionActual = listaNotificacion[position]
        holder.bindData(notificacionActual, listaNotificacion, this, position)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_list_adapter_notificacion, parent, false)
        return NotificacionViewHolder(view)
    }

    class NotificacionViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tituloNotificacion: TextView = itemView.findViewById<TextView>(R.id.tituloNotificacion)
        var descripcionNotificacion: TextView = itemView.findViewById<TextView>(R.id.descripcionNotificacionText)
        var borrarNotificacionButton: ImageButton = itemView.findViewById<ImageButton>(R.id.borrarNotificacionButton)
        /**
         * Recibe un objeto Notificacion y asigna los valores a los elementos de la vista.
         *
         * @param item Objeto Notificacion que contiene la información de la notificación
         * @param listaNotificacion Lista de notificaciones para poder modificarla
         * @param holder Referencia al adaptador para notificar cambios
         * @param position Posición de la notificación en la lista
         */
        fun bindData(item: Notificacion, listaNotificacion: ArrayList<Notificacion>, holder: ListAdapterNotificacion, position: Int) {
            try {
                tituloNotificacion.setText(item.titulo)
                descripcionNotificacion.setText(item.cuerpo)
                borrarNotificacionButton.setOnClickListener {
                    // Elimina la notificación de la base de datos
                    DatabaseService().eliminarNotificacion(item.notificacionId, MainActivity.sharedPref.getString("uid","")!!)

                    // Obtén la posición actual del elemento
                    val currentPosition = adapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        listaNotificacion.removeAt(currentPosition) // Elimina el elemento de la lista
                        holder.notifyItemRemoved(currentPosition) // Notifica al adaptador que se ha eliminado un elemento
                    }
                }
            } catch (e: Exception) {
                Log.d("XXXX", e.toString())
            }
        }
    }
}