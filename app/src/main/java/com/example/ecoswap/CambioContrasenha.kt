package com.example.ecoswap

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class CambioContrasenha : AppCompatActivity() {
    lateinit var contrasenhaActual: EditText
    lateinit var contrasenhaNueva: EditText
    lateinit var confirmarNuevaContrasenha: EditText
    lateinit var confirmarNuevaContrasenhaButton: Button
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cambio_contrasenha)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        contrasenhaActual = findViewById(R.id.contrasenhaActual)
        contrasenhaNueva = findViewById(R.id.nuevaContrasenha)
        confirmarNuevaContrasenha = findViewById(R.id.confirmarNuevaContrasenha)
        confirmarNuevaContrasenhaButton = findViewById(R.id.realizarcambioContrasenhaButton)
        confirmarNuevaContrasenhaButton.setOnClickListener {
            cambioContrasenha()
        }
    }

    private fun cambioContrasenha(){
        val contrasenhaActualString = contrasenhaActual.text.toString();
        val contrasenhaNuevaString = contrasenhaNueva.text.toString()
        val confirmarNuevaContrasenhaString = confirmarNuevaContrasenha.text.toString()
        if(contrasenhaActualString.isEmpty()){
            contrasenhaActual.setError("Debes introducir la contraseña actual")
            contrasenhaActual.requestFocus()
        }else{
            if(contrasenhaNuevaString.isEmpty()){
                contrasenhaNueva.setError("Debes introducir la nueva contraseña")
                contrasenhaNueva.requestFocus()
            }else{
                if(confirmarNuevaContrasenhaString.isEmpty()){
                    confirmarNuevaContrasenha.setError("Debes de confirmar la nueva contraseña")
                    confirmarNuevaContrasenha.requestFocus()
                }else{
                    if(contrasenhaNuevaString.length<6){
                        contrasenhaNueva.setError("La contraseña debe tener al menos 6 carácteres")
                        contrasenhaNueva.requestFocus()
                    }else{
                        if(contrasenhaNuevaString!=confirmarNuevaContrasenhaString){
                            confirmarNuevaContrasenha.setError("Las nueva contraseña y la confirmación no coincide")
                            confirmarNuevaContrasenha.requestFocus()
                        }else{
                            val user = auth.currentUser
                            val email = user!!.email
                            val credential = EmailAuthProvider.getCredential(email!!, contrasenhaActualString)
                            user.reauthenticate(credential)
                                .addOnCompleteListener { authTask ->
                                    if (authTask.isSuccessful) {
                                        // Actualizar la contraseña
                                        user.updatePassword(contrasenhaNuevaString)
                                            .addOnCompleteListener { updateTask ->
                                                if (updateTask.isSuccessful) {
                                                    Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_LONG).show()
                                                    finish()
                                                } else {
                                                    Toast.makeText(this, "Error al actualizar la contraseña", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                    } else {
                                        Toast.makeText(this, "La contraseña actual es incorrecta", Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                    }
                }
            }
        }
    }
}