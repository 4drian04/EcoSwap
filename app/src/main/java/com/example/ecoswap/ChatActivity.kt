package com.example.ecoswap

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoswap.callbacks.ChatroomCallback
import com.example.ecoswap.callbacks.UsuarioCallBack
import com.example.ecoswap.listAdapter.ListAdapterChat
import com.example.ecoswap.modelos.Chatroom
import com.example.ecoswap.modelos.Usuario
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.database.FirebaseDatabase


class ChatActivity : AppCompatActivity() {
    lateinit var toolbar: AppBarLayout
    private lateinit var rootView: View
    lateinit var chatRecyclerView: RecyclerView
    lateinit var bottomLayout: RelativeLayout
    lateinit var fotoPerfilUsuarioImageButton: ImageButton
    lateinit var nombreUsuarioTextView: TextView
    lateinit var envioMensajeEditText: EditText
    lateinit var enviarMensajeImageButton: ImageButton
    lateinit var chatroomId: String
    lateinit var chatroomActual: Chatroom
    lateinit var token: String;
    lateinit var softInputAssist: SoftInputAssist
    var root: ViewTreeObserver.OnGlobalLayoutListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Obtenemos los View por el id
        toolbar = findViewById(R.id.toolbar)
        chatRecyclerView = findViewById(R.id.chat_recycler_view)
        bottomLayout = findViewById(R.id.bottom_layout)
        envioMensajeEditText = findViewById(R.id.chat_message_input)
        enviarMensajeImageButton = findViewById(R.id.message_send_btn)
        toolbar.visibility = View.GONE
        chatRecyclerView.visibility = View.GONE
        bottomLayout.visibility = View.GONE
        fotoPerfilUsuarioImageButton = findViewById(R.id.fotoPerfilUsuarioChat)
        nombreUsuarioTextView = findViewById(R.id.nombreUsuarioChat)
        softInputAssist = SoftInputAssist(this)
        val uidOtroUsuario = intent.getStringExtra("otroUsuario")!!
        //Obtenemos el id del chatroom mediante el id de ambos usuarios
        chatroomId = DatabaseService().getChatroomId(MainActivity.sharedPref.getString("uid", "")!!, uidOtroUsuario)
        //Obtenemos los datos del otro usuario para mostrarlos en pantalla
        DatabaseService().obtenerUsuario(uidOtroUsuario, object : UsuarioCallBack {
            override fun onCallback(usuario: Usuario?) {
                token = usuario!!.token
                nombreUsuarioTextView.setText(usuario.nombreUsuario)
                Glide.with(this@ChatActivity)
                    .load(usuario.urlFotoPerfil).placeholder(R.drawable.loading).error(R.drawable.ic_profile)
                    .into(fotoPerfilUsuarioImageButton)
                toolbar.visibility = View.VISIBLE
                chatRecyclerView.visibility = View.VISIBLE
                bottomLayout.visibility = View.VISIBLE
            }
        })

        //Obtenemos el chatroom en el que nos encontramos actualemente
        DatabaseService().getChatroomReference(chatroomId, MainActivity.sharedPref.getString("uid", "")!!, uidOtroUsuario, object :
            ChatroomCallback {
            override fun onCallBack(chatroom: Chatroom) {
                chatroomActual = chatroom
            }
        })
        enviarMensajeImageButton.setOnClickListener {
            var mensaje: String = envioMensajeEditText.text.toString().trim()
            if(mensaje.isNotEmpty()){
                DatabaseService().sendMessage(chatroomActual, mensaje, MainActivity.sharedPref.getString("uid", "")!!) //Se guarda el mensaje en la base de datos
                envioMensajeEditText.setText("")
                if(!token.isNullOrEmpty()){
                    FCMNotificationService().enviarNotificacion(mensaje, token) //Se le envía una notificación al otro usuario, mediante el token y el mensaje que se acaba de enviar
                    mensaje = MainActivity.sharedPref.getString("nombreUsuario", "")!! + ": " + mensaje
                    DatabaseService().guardarNotificacion(uidOtroUsuario, MainActivity.sharedPref.getString("nombreUsuario", "")!!, mensaje) //Se guarda la notificación por si el usuario quere ver la notificación en un futuro
                }
            }
            ListAdapterChat.fechaPrimerMensaje = null
        }
        setupChatRecyclerView()

        fotoPerfilUsuarioImageButton.setOnClickListener { //Cuando el usuario hace clic en perfil del otro usuario, se redirige al perfil del otro usuario
            ProductosUsuario.uid = uidOtroUsuario
            ValoracionUsuario.uid = uidOtroUsuario
            val intent = Intent(this, PerfilOtroUsuario::class.java)
            intent.putExtra("uid", uidOtroUsuario)
            startActivity(intent)
        }
        rootView = findViewById(R.id.main) // Asegúrate de que este ID coincida con tu layout
    }

    private fun setupChatRecyclerView() {
        // Obtener la referencia a la base de datos de Firebase Realtime Database
        val chatMessagesRef = FirebaseDatabase.getInstance().getReference("chatrooms").child(chatroomId).child("chats")

        // Configurar el RecyclerView
        chatRecyclerView = findViewById(R.id.chat_recycler_view)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        chatRecyclerView.layoutManager = layoutManager

        // Inicializar el adapter
        val adapter = ListAdapterChat(mutableListOf(), this)
        chatRecyclerView.adapter = adapter
        DatabaseService().actualizarChat(adapter, chatroomId, chatRecyclerView)
    }
    private fun showKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        //getKeyboardHeight(findViewById(android.R.id.content))
    }
    private fun getKeyboardHeight(rootView: View) {
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            private var previousHeight = 0
            override fun onGlobalLayout() {
                root = this
                val r = Rect()
                rootView.getWindowVisibleDisplayFrame(r)
                val currentHeight = r.height()
                // Calcula la altura del teclado
                val heightDiff = rootView.height - currentHeight
                if (heightDiff > 250) {
                    // Altura del teclado en pixels
                    val keyboardHeight = heightDiff
                    println("Altura del teclado: $keyboardHeight")
                    val params = bottomLayout.layoutParams as RelativeLayout.LayoutParams
                    params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, keyboardHeight-210) // Cambia 50 por el valor que desees
                    bottomLayout.layoutParams = params
                    previousHeight = heightDiff
                }else{
                    val params = bottomLayout.layoutParams as RelativeLayout.LayoutParams
                    params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, 0) // Cambia 50 por el valor que desees
                    bottomLayout.layoutParams = params
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if(root!=null){
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(root)
        }
        softInputAssist.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(root!=null){
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(root)
        }
    }

    override fun onResume() {
        super.onResume()
        softInputAssist.onResume()
    }

    override fun onPause() {
        super.onPause()
        softInputAssist.onPause()
    }

}