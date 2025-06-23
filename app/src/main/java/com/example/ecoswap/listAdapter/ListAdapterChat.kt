package com.example.ecoswap.listAdapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoswap.MainActivity
import com.example.ecoswap.R
import com.example.ecoswap.modelos.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ListAdapterChat (
    private var listaMensajes: MutableList<ChatMessage>, // Lista mutable de mensajes de chat
    private var contextActivity: Context? // Context de la actividad que utiliza el adaptador
        ) : RecyclerView.Adapter<ListAdapterChat.ChatModelViewHolder?>(){
    private var fecha: String = ""
    companion object{
        var fechaPrimerMensaje: String? = ""
    }
    /*
     Devuelve el número total de mensajes en la lista
     */
    override fun getItemCount(): Int {
        return listaMensajes.size
    }

    /*
     Vincula los datos del mensaje en la posición dada
     */
    override fun onBindViewHolder(holder: ChatModelViewHolder, position: Int) {
        val chatActual = listaMensajes[position]
        var esFechaCambiada = false
        val fechaActual = formatTimestampToDate(chatActual.time)
        // Verifica si hay un siguiente mensaje para comparar
        if (position < listaMensajes.size - 1) {
            val siguienteChat = listaMensajes[position + 1]
            val fechaSiguiente = formatTimestampToDate(siguienteChat.time)
            // Mostrar la fecha solo si es diferente de la siguiente
            if (fechaActual != fechaSiguiente) {
                holder.fechaTextView.text = fechaActual
                holder.fechaTextView.visibility = View.VISIBLE
                esFechaCambiada = true
            } else {
                holder.fechaTextView.visibility = View.GONE
            }
        } else {
            // Si es el último mensaje, siempre mostrar la fecha
            holder.fechaTextView.text = fechaActual
            holder.fechaTextView.visibility = View.VISIBLE
            esFechaCambiada=true
        }
        // Verifica si el remitente del mensaje es el usuario actual
        if(chatActual.remitenteId.equals(MainActivity.sharedPref.getString("uid","")!!)){
            // Si es el remitente, oculta el layout de chat a la izquierda y muestra el de la derecha
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatTextview.setText(chatActual.mensaje);
            if(!esFechaCambiada){
                val paramRight = holder.rightChatLayout.layoutParams as RelativeLayout.LayoutParams
                paramRight.topMargin = 0 // Establecer el margen superior
                holder.rightChatLayout.layoutParams = paramRight
            }
        }else{
            // Si no es el remitente, oculta el layout de chat a la derecha y muestra el de la izquierda
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatTextview.setText(chatActual.mensaje);
            if(!esFechaCambiada){
                val paramsLeft = holder.leftChatLayout.layoutParams as RelativeLayout.LayoutParams
                paramsLeft.topMargin = 0 // Establecer el margen superior
                holder.leftChatLayout.layoutParams = paramsLeft
            }
        }
    }
    /*
     Método para añadir un nuevo mensaje a la lista
     */
    fun addMensaje(mensaje: ChatMessage){
        listaMensajes.add(0, mensaje)
        notifyItemInserted(0) // Notifica al adaptador que se ha insertado un nuevo elemento
    }

    fun formatTimestampToDate(timestamp: Long): String {
        // Crear un objeto Date a partir del timestamp
        val date = Date(timestamp)
        // Crear un formateador de fecha
        val dateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault()) // Formato: día mes

        // Formatear la fecha a una cadena
        return dateFormat.format(date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatModelViewHolder {
        val view: View =
            LayoutInflater.from(contextActivity).inflate(R.layout.activity_list_adapter_chat, parent, false)
        return ChatModelViewHolder(view)
    }
    class ChatModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var leftChatLayout: LinearLayout = itemView.findViewById(R.id.left_chat_layout)
        var rightChatLayout: LinearLayout = itemView.findViewById(R.id.right_chat_layout)
        var leftChatTextview: TextView = itemView.findViewById(R.id.left_chat_textview)
        var rightChatTextview: TextView = itemView.findViewById(R.id.right_chat_textview)
        var fechaTextView: TextView = itemView.findViewById(R.id.fechaMensaje)
    }
}