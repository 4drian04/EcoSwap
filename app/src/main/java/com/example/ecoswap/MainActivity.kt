package com.example.ecoswap

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ecoswap.callbacks.UsuarioCallBack
import com.example.ecoswap.modelos.Usuario
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    companion object{
     const val sharedFile = "com.example.ecoswap";
        lateinit var sharedPref: SharedPreferences
            private set
        var estaEnPerfil = false; // Variable para verificar si el usuario está en el perfil
    }
    private val GOOGLE_SIGN_IN = 100;
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }
    lateinit var buttonLogin: Button
    lateinit var buttonRegister: Button;
    lateinit var googleButton: ImageButton;
    lateinit var twitterButton: ImageButton;
    lateinit var email: String
    lateinit var contrasenha: String
    lateinit var contrasenhaEditText: EditText
    lateinit var emailEditText: EditText
    lateinit var progressBar: LinearLayout
    lateinit var datosInicioSesionLinearLayout: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        FirebaseApp.initializeApp(this) //Inicializamos Firebase para poder utilizar sus funciones
        MenuPrincipal.selectedFragment= MenuPrincipalFragment() //Indicamos que el fragemnto seleccionado es el MenuPrincipal, para que cuando iniciemos sesión aparezcamos ahí
        sharedPref = this.getSharedPreferences(sharedFile, Context.MODE_PRIVATE);
        val sharedPreferencesEdit = sharedPref.edit();
        var uid = sharedPref.getString("uid", "");
        if(!uid.isNullOrEmpty()){ //Si ya hay guardado un uid, quiere decir que el usuairo ya se ha registrado previamente, por lo que lo enviaremos al menu principal
            enviarMenuPrincipal()
        }
        datosInicioSesionLinearLayout = findViewById(R.id.datosInicioSesion)
        progressBar = findViewById(R.id.progresBarInicioSesion)
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener {
            datosInicioSesionLinearLayout.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            emailEditText = findViewById(R.id.editTextEmailLogin);
            email = emailEditText.text.toString();
            contrasenhaEditText = findViewById(R.id.editTextPasswordLogin);
            contrasenha = contrasenhaEditText.text.toString();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { //Se comprueba si el email tiene un formato válido
                //Si no tiene un formato válido, comprobamos si está vacío, o simplemente tiene un formato invalido
                if (email.isEmpty()) { //Si el email está vacío, se indica el error al usuario
                    quitarProgressBar()
                    emailEditText.setError("No has introducido ningún email");
                    emailEditText.requestFocus();
                } else { //Si el email no tiene un formato adecuado, se informa al usuario de ello
                    quitarProgressBar()
                    Toast.makeText(
                        this,
                        "El email que has introducido no tiene el formato correcto",
                        Toast.LENGTH_LONG
                    ).show();
                }
            } else { //Si el email tiene un formato correcto, vamos a comprobar la contraseña
                if (contrasenha.isEmpty()) { //Si la contraseña está vacía, se informa al usuario de ello
                    quitarProgressBar()
                    contrasenhaEditText.setError("No has introducido ninguna contraseña");
                    contrasenhaEditText.requestFocus();
                } else {
                    // Intentamos iniciar sesión con email y contraseña
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, contrasenha)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                sharedPreferencesEdit.putString("uid", it.result.user?.uid) // Si el inicio ha sido exitoso se guarda el uid
                                sharedPreferencesEdit.apply()
                                DatabaseService().obtenerUsuario(it.result.user!!.uid, object :
                                    UsuarioCallBack {
                                    override fun onCallback(usuario: Usuario?) {
                                        // Guardamos el nombre de usuario y el tipo de autenticación
                                        sharedPreferencesEdit.putString("nombreUsuario", usuario!!.nombreUsuario);
                                        sharedPreferencesEdit.putString("fotoPerfil", usuario.urlFotoPerfil)
                                        sharedPreferencesEdit.putString("authType", AuthProvider.EMAIL.toString())
                                        sharedPreferencesEdit.apply()
                                        // Obtenemos el token de FCM para el tema de las notificaciones
                                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val token = task.result
                                                //Se actualiza el token en la base de datos
                                                DatabaseService().actualizarTokenUsuario(token, it.result.user!!.uid)
                                            }
                                        }
                                    }

                                })
                                val intent = Intent(this, MenuPrincipal::class.java)
                                startActivity(intent)
                            } else { // Si el inicio de sesión falla, mostrar un mensaje de error
                                progressBar.visibility = View.GONE
                                datosInicioSesionLinearLayout.visibility = View.VISIBLE
                                val errorMessage = when (it.exception) {
                                    is FirebaseAuthInvalidUserException -> "El usuario no existe."
                                    is FirebaseAuthInvalidCredentialsException -> "Las credenciales son inválidas."
                                    is FirebaseAuthUserCollisionException -> "El usuario ya está registrado."
                                    else -> "Error de autenticación. Inténtalo de nuevo."
                                }
                                mostrarAlerta(errorMessage);
                            }
                        }
                }
            }
        }
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener {
            val intent = Intent(this, pantalla_registro::class.java) // Redirigir al usuario a la pantalla de registro
            startActivity(intent)
        }
        googleButton = findViewById(R.id.buttonGoogle)
        googleButton.setOnClickListener {
            datosInicioSesionLinearLayout.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken("140045485210-uae3o0nq92v4vgc8ian0ophq6u0hb74e.apps.googleusercontent.com").requestEmail().build();
            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut();
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN); // Iniciar sesión con Google
        }

        twitterButton = findViewById(R.id.buttonTwitter);
        twitterButton.setOnClickListener {
            datosInicioSesionLinearLayout.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            val firebaseAuth = FirebaseAuth.getInstance()
            val provider = OAuthProvider.newBuilder("twitter.com")
            provider.addCustomParameter("lang", "es") // Establecer el idioma
            val pendingResultTask = firebaseAuth.pendingAuthResult
            if (pendingResultTask != null) {
                // There's something already here! Finish the sign-in for your user.
                pendingResultTask
                    .addOnSuccessListener {
                        sharedPreferencesEdit.putString("uid", it.user?.uid)
                        var usuario = DatabaseService().obtenerUsuario(it.user!!.uid, object :
                            UsuarioCallBack {
                            override fun onCallback(usuario: Usuario?) {
                                if(usuario==null){ // Si el usuario no existe, se crea uno nuevo en la base de datos
                                    var nombreUsuario = it.user!!.email;
                                    nombreUsuario = nombreUsuario!!.split("@")[0];
                                    DatabaseService().guardarUsuario(it.user!!.uid, nombreUsuario, it.user!!.photoUrl.toString());
                                    sharedPreferencesEdit.putString("nombreUsuario", nombreUsuario);
                                    sharedPreferencesEdit.putString("fotoPerfil", it.user!!.photoUrl.toString())
                                }else{ // Si el usuario ya existe, se guarda su nombre en el preferences
                                    sharedPreferencesEdit.putString("nombreUsuario", usuario.nombreUsuario);
                                    sharedPreferencesEdit.putString("fotoPerfil", usuario.urlFotoPerfil)
                                }
                                sharedPreferencesEdit.putString("authType", AuthProvider.TWITTER.toString())
                                sharedPreferencesEdit.apply()
                                enviarMenuPrincipal()
                            }
                        });
                        // User is signed in.
                        // IdP data available in
                        // The OAuth access token can also be retrieved:
                         //((OAuthCredential)it.getCredential()).getAccessToken().
                        // The OAuth secret can be retrieved by calling:
                        // ((OAuthCredential)authResult.getCredential()).getSecret().
                    }
                    .addOnFailureListener {
                        quitarProgressBar()
                        mostrarAlerta("Ha ocurrido un error al intentar iniciar sesión con Twitter (X). Inténtalo de nuevo")
                    }
            } else {
                firebaseAuth
                    .startActivityForSignInWithProvider(this, provider.build())
                    .addOnSuccessListener {
                        sharedPreferencesEdit.putString("uid", it.user?.uid) // Guardar el uid
                        var usuario = DatabaseService().obtenerUsuario(it.user!!.uid, object :
                            UsuarioCallBack {
                            override fun onCallback(usuario: Usuario?) {
                                if(usuario==null){ // Si el usuario no existe, se crea uno nuevo en la base de datos
                                    var nombreUsuario = it.user!!.email;
                                    nombreUsuario = nombreUsuario!!.split("@")[0];
                                    DatabaseService().guardarUsuario(it.user!!.uid, nombreUsuario!!,
                                        it.user!!.photoUrl.toString()
                                    );
                                    sharedPreferencesEdit.putString("nombreUsuario", nombreUsuario);
                                    sharedPreferencesEdit.putString("fotoPerfil", it.user!!.photoUrl.toString())
                                }else{
                                    sharedPreferencesEdit.putString("nombreUsuario", usuario.nombreUsuario);
                                    sharedPreferencesEdit.putString("fotoPerfil", usuario.urlFotoPerfil)
                                }
                                sharedPreferencesEdit.putString("authType", AuthProvider.TWITTER.toString())
                                sharedPreferencesEdit.apply()
                                enviarMenuPrincipal()
                            }
                        });
                        // User is signed in.
                        // IdP data available in
                        // authResult.getAdditionalUserInfo().getProfile().
                        // The OAuth access token can also be retrieved:
                        // ((OAuthCredential)authResult.getCredential()).getAccessToken().
                        // The OAuth secret can be retrieved by calling:
                        // ((OAuthCredential)authResult.getCredential()).getSecret().
                    }
                    .addOnFailureListener { exception -> handleAuthError(exception)
                        quitarProgressBar()
                        mostrarAlerta("Ha ocurrido un error al intentar iniciar sesión con Twitter (X). Inténtalo de nuevo")
                    }
            }
        }
        askNotificationPermission()
    }

    /**
     * Se envía al usuario al menú principal
     */
    private fun enviarMenuPrincipal(){
        // Guardar el uid en las clases de productos y valoraciones
        ProductosUsuario.uid = sharedPref.getString("uid", "")!!
        ValoracionUsuario.uid = sharedPref.getString("uid", "")!!
        val intent = Intent(this, MenuPrincipal::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Se muestra el error correspondiente, pasado el error como parametro
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
     * Maneja los posibles errores al iniciar de sesión con Twitter
     */
    private fun handleAuthError(exception: Exception) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                "Credenciales inválidas. Por favor, verifica tu información."
            }
            is FirebaseAuthInvalidUserException -> {
                "No se encontró el usuario. Por favor, intenta registrarte."
            }
            is FirebaseAuthUserCollisionException -> {
                "Ya existe una cuenta con este correo electrónico."
            }
            else -> {
                "Ha ocurrido un error al intentar iniciar sesión con Twitter. Inténtalo de nuevo."
            }
        }
        mostrarAlerta(errorMessage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_SIGN_IN){ // Verifica si la solicitud es para iniciar sesión con Google
            val task = GoogleSignIn.getSignedInAccountFromIntent(data); // Obtiene la cuenta de Google
            try{
                val account = task.getResult(ApiException::class.java); // Obtiene el resultado de la tarea
                if(account!=null){
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null);
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                        if(it.isSuccessful){
                            val sharedPreferencesEdit = sharedPref.edit();
                            sharedPreferencesEdit.putString("uid", it.result.user?.uid) //Si el inicio de sesión ha sido exitoso, se guarda el uid del usuario
                            var usuario = DatabaseService().obtenerUsuario(it.result.user!!.uid, object :
                                UsuarioCallBack {
                                override fun onCallback(usuario: Usuario?) {
                                    Log.d("PRESIIIII QUEE", usuario.toString())
                                    if(usuario == null){ // Si el usuario no existe, se crea uno nuevo y se guarda en la base de datos
                                        var nombreUsuario = it.result.user!!.email;
                                        nombreUsuario = nombreUsuario!!.split("@")[0];
                                        DatabaseService().guardarUsuario(it.result.user!!.uid, nombreUsuario, it.result.user!!.photoUrl.toString());
                                        sharedPreferencesEdit.putString("nombreUsuario", nombreUsuario);//Se guarda el nombre del usuario
                                        sharedPreferencesEdit.putString("fotoPerfil", it.result.user!!.photoUrl.toString())
                                    }else{
                                        sharedPreferencesEdit.putString("nombreUsuario", usuario.nombreUsuario); //Si el usuario existe, se obtiene el usuario y se guarda su nombre de usuario en el preferences
                                        sharedPreferencesEdit.putString("fotoPerfil", usuario.urlFotoPerfil)
                                    }
                                    sharedPreferencesEdit.putString("authType", AuthProvider.GOOGLE.toString()) // Guarda el tipo de autenticación
                                    sharedPreferencesEdit.apply()
                                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val token = task.result
                                            DatabaseService().actualizarTokenUsuario(token, it.result.user!!.uid) //Se actualiza el token en la base de datos para el tema de las notificaciones
                                        }
                                    }
                                    val intent = Intent(this@MainActivity, MenuPrincipal::class.java)
                                    startActivity(intent) //Se envía al usuario al menú principal y se quita esta activity de la pila de activities
                                    finish();
                                }
                            });
                        }else{ //Si hay algún error, se muestra una alerta en pantalla
                            quitarProgressBar()
                            mostrarAlerta("No se ha podido iniciar sesión, vuelve a intentarlo.")
                        }
                    };
                }
            }catch(e: ApiException){
                quitarProgressBar()
                mostrarAlerta("Ha ocurrido un error inesperado. Vuelve a intentarlo.")
            }

        }
    }

    /**
     * Se pregunta al usuario por los permisos de las notificaciones
     */
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                Toast.makeText(this, "Necesitamos los permisos de notificación para que te puedan llegar notificaciones al teléfono móvil", Toast.LENGTH_LONG).show()
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun quitarProgressBar(){
        progressBar.visibility = View.GONE
        datosInicioSesionLinearLayout.visibility = View.VISIBLE
    }
}