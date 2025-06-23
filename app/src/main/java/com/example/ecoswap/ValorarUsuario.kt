package com.example.ecoswap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ecoswap.callbacks.UsuarioCallBack
import com.example.ecoswap.modelos.Usuario


class ValorarUsuario : AppCompatActivity() {
    private lateinit var ratingBar: RatingBar
    private lateinit var justificacionEditText: EditText
    private lateinit var enviarButton: Button
    private lateinit var tituloValoracion: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_valorar_usuario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ratingBar = findViewById(R.id.ratingBar)
        justificacionEditText = findViewById(R.id.justificacionEditText)
        tituloValoracion = findViewById(R.id.tituloValoracion)
        //Se obtiene el usuario para mostrar en el título el nombre del usuario
        DatabaseService().obtenerUsuario(intent.getStringExtra("uidUsuario")!!, object :
            UsuarioCallBack {
            override fun onCallback(usuario: Usuario?) {
                tituloValoracion.setText(""+tituloValoracion.text + " " + usuario!!.nombreUsuario)
            }
        })
        enviarButton = findViewById(R.id.enviarValoracionButton)
        enviarButton.setOnClickListener {
            val rating = ratingBar.rating
            val justificacion = justificacionEditText.text.toString()

            if (rating == 0f) { //Si el rating es 0, se informa al usuario que debe de calificar al usuario
                Toast.makeText(this, "Por favor, califica al usuario antes de enviar.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Valoración enviada.", Toast.LENGTH_LONG).show()
                //Se guarda la valoración en la base de datos
                val respuesta = DatabaseService().enviarValoracionUsuario(MainActivity.sharedPref.getString("uid", "")!!, intent.getStringExtra("uidUsuario")!!, justificacion, rating)
                Toast.makeText(this, respuesta, Toast.LENGTH_LONG).show()
                MenuPrincipal.selectedFragment = MenuPrincipalFragment() //Una vez valorado al usuario se le envía de nuevo al menú pricnipal
                intent = Intent(this, MenuPrincipal::class.java)
                startActivity(intent)
            }
        }
    }
}