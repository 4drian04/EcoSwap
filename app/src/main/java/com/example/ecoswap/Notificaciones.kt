package com.example.ecoswap

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoswap.callbacks.NotificacionesCallBack
import com.example.ecoswap.listAdapter.ListAdapterNotificacion
import com.example.ecoswap.modelos.Notificacion

class Notificaciones : AppCompatActivity() {
    lateinit var progressBar: LinearLayout
    lateinit var informacionNotificaciones: LinearLayout
    lateinit var notificacionesRecyclerView: RecyclerView
    lateinit var noHayNotificacionesLinearLayout: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notificaciones)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        progressBar = findViewById(R.id.progressBarNotificacionesLinearLayout)
        noHayNotificacionesLinearLayout = findViewById(R.id.noHayNotificacionesLayout)
        notificacionesRecyclerView = findViewById(R.id.notificacionesRecvylverView)
        notificacionesRecyclerView.setLayoutManager(LinearLayoutManager(this))
        informacionNotificaciones = findViewById(R.id.listaNotificaciones)
        informacionNotificaciones.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        //Se obtiene las notificaciones que le han llegado al usuario y se muestra por pantalla
        DatabaseService().getNotificacionesUsuario(MainActivity.sharedPref.getString("uid", "")!!, object :
            NotificacionesCallBack {
            override fun onCallback(notificaciones: ArrayList<Notificacion>) {
                progressBar.visibility = View.GONE
                informacionNotificaciones.visibility = View.VISIBLE
                if(notificaciones.isEmpty()){
                    noHayNotificacionesLinearLayout.visibility = View.VISIBLE
                    notificacionesRecyclerView.visibility = View.GONE
                }else{
                    val notificacionAdapter = ListAdapterNotificacion(notificaciones, this@Notificaciones)
                    notificacionesRecyclerView.adapter = notificacionAdapter
                }
            }
        })
    }

    fun comprobarNotificaciones(notificaciones: ArrayList<Notificacion>){
        if(notificaciones.isEmpty()){
            noHayNotificacionesLinearLayout.visibility = View.VISIBLE
            notificacionesRecyclerView.visibility = View.GONE
        }
    }
}