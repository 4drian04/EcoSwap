package com.example.ecoswap.listAdapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoswap.ChatActivity
import com.example.ecoswap.DatabaseService
import com.example.ecoswap.MainActivity
import com.example.ecoswap.R
import com.example.ecoswap.callbacks.UsuarioCallBack
import com.example.ecoswap.modelos.Chatroom
import com.example.ecoswap.modelos.Usuario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.hours

class ListAdapterChatReciente (
    private var listaChats: List<Chatroom>, // Lista de chats recientes
    private var contextActivity: Context?
) : RecyclerView.Adapter<ListAdapterChatReciente.ChatRecienteViewHolder?>() {

    /**
     * Método que devuelve el número total de chats en la lista
     */
    override fun getItemCount(): Int {
        return listaChats.size
    }

    /**
     * Vincula los datos del chat en la posición dada
     */
    override fun onBindViewHolder(holder: ChatRecienteViewHolder, position: Int) {
        val chatActual = listaChats[position]
        holder.bindData(chatActual)
        // Listener para manejar el clic en el elemento del chat
        holder.itemView.setOnClickListener {
            val intent = Intent(contextActivity, ChatActivity::class.java)
            var uidOtroUsuario = ""
            // Determina el ID del otro usuario en el chat
            if(chatActual.userIds.get(0) == MainActivity.sharedPref.getString("uid", "")){
                uidOtroUsuario = chatActual.userIds.get(1)
            }else{
                uidOtroUsuario = chatActual.userIds.get(0)
            }
            ListAdapterChat.fechaPrimerMensaje = ""
            // Pasa el ID del otro usuario al intent
            intent.putExtra("otroUsuario", uidOtroUsuario)
            contextActivity!!.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRecienteViewHolder {
        val view: View =
            LayoutInflater.from(contextActivity).inflate(R.layout.activity_list_adapter_chat_reciente, parent, false)
        return ChatRecienteViewHolder(view, contextActivity)
    }

    class ChatRecienteViewHolder internal constructor(itemView: View, private val contextActivity: Context?) :
        RecyclerView.ViewHolder(itemView) {
        val fotoPerfilChatRecienteImageView: ImageView = itemView.findViewById(R.id.fotoPerfilChatReciente)
        val nombreUsuarioTextView: TextView = itemView.findViewById(R.id.nombreUsuarioChatReciente)
        val ultimoMensajeTextView: TextView = itemView.findViewById(R.id.ultimoMensaje)
        val horaUltimoMensaje: TextView = itemView.findViewById(R.id.horaUltimoMensaje)

        /**
         * Método que recibe un objeto Chatroom y asigna los valores a los elementos de la vista.
         */
        fun bindData(item: Chatroom) {
            var uidOtroUsuario = ""
            if(item.userIds.get(0) == MainActivity.sharedPref.getString("uid", "")){
                uidOtroUsuario = item.userIds.get(1)
            }else{
                uidOtroUsuario = item.userIds.get(0)
            }
            // Obteniene la información del usuario de la base de datos
            DatabaseService().obtenerUsuario(uidOtroUsuario, object : UsuarioCallBack {
                override fun onCallback(usuario: Usuario?) {
                    nombreUsuarioTextView.setText(usuario!!.nombreUsuario)
                    ultimoMensajeTextView.setText(item.ultimoMensaje)
                    val timestamp = formatTimestampToDate(item.ultimoMensajeTimestamp)
                    horaUltimoMensaje.setText(timestamp)
                    Glide.with(contextActivity!!)
                        .load(usuario.urlFotoPerfil).placeholder(R.drawable.loading).error(R.drawable.ic_profile)
                        .into(fotoPerfilChatRecienteImageView)
                }
            })
        }
        fun formatTimestampToDate(timestamp: Long): String {
            // Crear un objeto Date a partir del timestamp
            val date = Date(timestamp)
            // Crear un formateador de fecha
            val dateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault()) // Formato: día mes

            // Formatear la fecha a una cadena
            return dateFormat.format(date)
        }
    }
}