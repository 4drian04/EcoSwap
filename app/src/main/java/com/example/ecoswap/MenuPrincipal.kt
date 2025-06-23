package com.example.ecoswap

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MenuPrincipal : AppCompatActivity() {
    companion object{
        var selectedFragment: Fragment? = null
    }
    //Esto controla el fragment que se tiene que mostrar en pantalla según la opción que elija el usuario
    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {
            R.id.nav_home -> selectedFragment = MenuPrincipalFragment() //Si el usuairo hace clic en el menú principal, se redirige alli
            R.id.nav_exchanges -> selectedFragment = IntercambiosRealizados() //Si selecciona la opción de intercambios realizados, se redirige alli
            R.id.nav_pending -> selectedFragment = IntercambiosPendientes() //Si selecciona la opción de intercambios pendientes, se redirige alli
            R.id.nav_chats -> selectedFragment = Chats() //Si selecciona la opción de chats recientes, se redirige alli
            R.id.nav_profile -> {
                //Se obtiene el uid del usuario actual para mostrar sus productos y valoraciones (ya que ese uid se va cambiando según nos vayamos al perfil de otro usuario)
                ValoracionUsuario.uid = MainActivity.sharedPref.getString("uid", "")!!
                ProductosUsuario.uid = MainActivity.sharedPref.getString("uid", "")!!
                selectedFragment = Perfil() //Se redirige al perfil del usuario
            }
        }

        supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, selectedFragment!!).commit()
        true
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener(navListener)

        // Cargar el fragmento por defecto
        supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, selectedFragment!!).commit()
    }
}