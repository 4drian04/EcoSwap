package com.example.ecoswap

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.io.InputStream

class pantalla_registro : AppCompatActivity() {
    lateinit var botonRegistro: Button
    lateinit var email: String;
    lateinit var emailEditText: EditText;
    lateinit var contrasenhaEditText: EditText;
    lateinit var contrasenha: String;
    lateinit var nombreUsuarioEditText: EditText;
    lateinit var nombreUsuario: String;
    lateinit var datosRegistroLayout: LinearLayout
    lateinit var progressBar: LinearLayout
    var selectedImageUri: Uri? = null
    var esRedimensionadoImagen=false;
    lateinit var fotoPerfilImageButton: ImageButton
    var esFotoUsuarioActivo = false;
    var bitmap : Bitmap? = null
    var urlFotoPerfil: String = ""
    val REQUEST_IMAGE_GALLERY = 1;
    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pantalla_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sharedPreferencesEdit = MainActivity.sharedPref.edit()
        datosRegistroLayout = findViewById(R.id.datosRegistro)
        progressBar = findViewById(R.id.progressBarRegistro)
        botonRegistro = findViewById(R.id.registroButton);
        botonRegistro.setOnClickListener {
            datosRegistroLayout.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            emailEditText = findViewById(R.id.editTextEmailRegistro);
            email = emailEditText.text.toString();
            contrasenhaEditText = findViewById(R.id.editTextPasswordRegistro);
            contrasenha = contrasenhaEditText.text.toString();
            nombreUsuarioEditText = findViewById(R.id.editTextNombreUsuarioRegistro)
            nombreUsuario = nombreUsuarioEditText.text.toString();
            if (nombreUsuario.isEmpty()) { //Si el nombre de usuario está vacío, se informará al usuario de ello
                quitarProgressBar()
                nombreUsuarioEditText.setError("Introduce un nombre de usuario")
                nombreUsuarioEditText.requestFocus()
            } else {
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { //Si el usuario escribe un email con un formato incorrecto, se informará
                    quitarProgressBar()
                    if (email.isEmpty()) { //Si el email está vacío, se le informarña al usuario de ello
                        emailEditText.setError("No has introducido ningún email");
                        emailEditText.requestFocus();
                    } else { //Si el email no está vacío quiere decir que simplemente tiene un formato incorrecto, por lo que informamos al usuario de ello
                        Toast.makeText(
                            this,
                            "El email que has introducido no tiene el formato correcto",
                            Toast.LENGTH_LONG
                        ).show();
                    }
                } else {
                    if (contrasenha.isEmpty()) { //Si la contraseña está vacía, informamos al usuario de ello
                        quitarProgressBar()
                        contrasenhaEditText.setError("No has introducido ninguna contraseña");
                        contrasenhaEditText.requestFocus();
                    } else {
                        if(!esFotoUsuarioActivo){ //Si el usuario no ha seleccionado una foto de perfil, le informamos de ello
                            quitarProgressBar()
                            mostrarAlerta("Debes seleccionar una foto de perfil")
                        }else{
                            //Si todo está correcto, se crea el usuario con el correo y la contraseña proporcionado
                            FirebaseAuth.getInstance()
                                .createUserWithEmailAndPassword(email, contrasenha)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        //Si se crea correctamente, se guarda tanto el uid como el nombre de usuario
                                        sharedPreferencesEdit.putString("uid", it.result.user?.uid)
                                        sharedPreferencesEdit.putString("nombreUsuario", nombreUsuario)
                                        sharedPreferencesEdit.putString("authType", AuthProvider.EMAIL.toString())
                                        sharedPreferencesEdit.apply()
                                        Utilities().cambiarFotoPerfil(bitmap!!, false, this, nombreUsuario, storage)

                                        //Guardamos al usuario en la base de datos
                                        DatabaseService().guardarUsuario(
                                            it.result.user!!.uid,
                                            nombreUsuario,
                                            urlFotoPerfil
                                        );
                                        //Establecemos que el uid del usuario es el obtenido al crear la cuenta, para que así no aparezca en el menú principal los productos del propio usuario
                                        ProductosUsuario.uid = it.result.user?.uid!!
                                        val intent = Intent(this, MenuPrincipal::class.java)
                                        startActivity(intent)
                                        finishAffinity();
                                    } else {
                                        quitarProgressBar()
                                        //Se gestionan los posibles errores que puede ocurrir
                                        val errorMessage = when (it.exception) {
                                            is FirebaseAuthUserCollisionException -> "El usuario ya está registrado."
                                            is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil, se requiere como mínimo 6 carácteres"
                                            is FirebaseAuthInvalidCredentialsException -> "Las credenciales son inválidas."
                                            is FirebaseAuthEmailException -> "El formato del correo electrónico es inválido."
                                            is FirebaseAuthException -> "Error de autenticación. Inténtalo de nuevo."
                                            else -> "Error desconocido. Por favor, verifica tu información."
                                        }
                                        mostrarAlerta(errorMessage);
                                    }
                                }
                        }
                    }
                }
            }
        }
        fotoPerfilImageButton = findViewById(R.id.fotoPerfilRegistroImage)
        fotoPerfilImageButton.setOnClickListener {
            handleStoragePermission()
        }
    }

    private fun quitarProgressBar(){
        progressBar.visibility = View.GONE
        datosRegistroLayout.visibility = View.VISIBLE
    }
    /**
     * Muestra la alerta con el mensaje de error pasado por parámetro
     */
    private fun mostrarAlerta(mensaje: String) {
        val builder = AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(mensaje);
        builder.setPositiveButton("Aceptar", null);
        val dialog: AlertDialog = builder.create();
        dialog.show();
    }

    /**
     * Abre la galeria del usuario
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
    }

    /**
     * Gestiona los permisos del almacenamiento para las imágenes
     */
    private fun handleStoragePermission(){
        // En Android 6.0+ es necesario pedir permiso para leer almacenamiento externo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){ //Si la versión del Android es superior a Android 13, se pide un tipo de permiso
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED) {
                    // Pedir permiso
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        REQUEST_IMAGE_GALLERY
                    )
                } else {
                    // Permiso concedido
                    openGallery()
                }
            }else{ // Sin embargo, si la versión del Android es inferior a Android 13, se pide otro permiso
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED) {
                    // Pedir permiso
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_IMAGE_GALLERY
                    )
                } else {
                    // Permiso concedido
                    openGallery()
                }
            }

        } else {
            // En versiones anteriores no se pide permiso en tiempo de ejecución
            openGallery()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_IMAGE_GALLERY){ //Si el request code es el de la galeria, y el permiso ha sido concedido, se abre la galeria
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permiso concedido, abrir galería
                openGallery()
            } else {
                // Permiso denegado, mostrar mensaje al usuario
                Toast.makeText(this,
                    "Permiso denegado para acceder a la galería",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Obtiene la imagen seleccionada por el usuario y lo redimensiona
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_GALLERY -> {
                    selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        bitmap = Utilities().getResizedBitmapFromUri(selectedImageUri!!, 400, 400, contentResolver)
                        fotoPerfilImageButton.setImageBitmap(bitmap)
                        esFotoUsuarioActivo=true
                    }
                }
            }
        }
    }

}
