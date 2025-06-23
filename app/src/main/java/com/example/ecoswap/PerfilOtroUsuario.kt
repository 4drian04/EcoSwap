package com.example.ecoswap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.ecoswap.activities.ViewPagerAdapter
import com.example.ecoswap.callbacks.UsuarioCallBack
import com.example.ecoswap.callbacks.ValoracionCallback
import com.example.ecoswap.modelos.Usuario
import com.example.ecoswap.modelos.Valoracion
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PerfilOtroUsuario : AppCompatActivity() {
    lateinit var nombreUsuario: TextView
    lateinit var fotoPerfil: ImageView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    lateinit var progresBarLinearLayout: LinearLayout
    lateinit var datosOtroUsuarioLinearLayout: LinearLayout
    lateinit var valorarUsuarioButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_otro_usuario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val uidUsuario =intent.getStringExtra("uid") //Se obtiene el uid del usuario pasado como extra del intent
        nombreUsuario = findViewById(R.id.nombreOtroUsuario)
        tabLayout = findViewById(R.id.tab_layout_otro_usuario)
        viewPager = findViewById(R.id.view_pager_otro_usuario)
        valorarUsuarioButton = findViewById(R.id.valorarUsuarioButton)
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter
        progresBarLinearLayout = findViewById(R.id.progressBarOtroUsuarioLinearLayout)
        fotoPerfil = findViewById(R.id.fotoPerfilOtroUsuario)
        datosOtroUsuarioLinearLayout = findViewById(R.id.datosOtroUsuario)
        datosOtroUsuarioLinearLayout.visibility = View.GONE
        progresBarLinearLayout.visibility = View.VISIBLE
        //Se establece el texto de las pestañas
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Productos Publicados"
                1 -> "Valoraciones"
                else -> null
            }
        }.attach()
        //Se obtiene los datos del usuario, haciendo uso del uid del usuario
        DatabaseService().obtenerUsuario(uidUsuario!!, object : UsuarioCallBack {
            override fun onCallback(usuario: Usuario?) {
                nombreUsuario.setText(usuario!!.nombreUsuario)
                Glide.with(this@PerfilOtroUsuario)
                    .load(usuario.urlFotoPerfil).placeholder(R.drawable.loading).error(R.drawable.ic_profile)
                    .into(fotoPerfil)
                progresBarLinearLayout.visibility = View.GONE
                datosOtroUsuarioLinearLayout.visibility = View.VISIBLE
            }
        })
        //Se comprueba si el usuario actual ha valorado al usuario
        DatabaseService().comprobarValoracionUsuario(uidUsuario, MainActivity.sharedPref.getString("uid", "")!!, object :
            ValoracionCallback {
            override fun onCallback(valoracion: Valoracion?) {
                if(valoracion==null){ //Si no se ha encontrado ninguna valoración, el usuario actual podrá valorar al otro usuario
                    valorarUsuarioButton.visibility = View.VISIBLE
                    valorarUsuarioButton.setOnClickListener { //Se envia al usuario actual a la pantalla de valoración
                        val intent = Intent(this@PerfilOtroUsuario, ValorarUsuario::class.java)
                        intent.putExtra("uidUsuario", uidUsuario)
                        startActivity(intent)
                    }
                }
            }
        })
    }
}