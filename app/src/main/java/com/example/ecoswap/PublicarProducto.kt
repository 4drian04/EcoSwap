package com.example.ecoswap

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoswap.listAdapter.ListAdapterCategorias
import com.example.ecoswap.modelos.Categorias
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.io.InputStream


class PublicarProducto : AppCompatActivity() {
    companion object{
        var latitud: Double? = null;
        var longitud: Double? = null;
        var categoriasSeleccionadas = ArrayList<String>()
    }
    lateinit var productoImageButton: ImageButton
    lateinit var progressBarLinearLayout: LinearLayout
    lateinit var datosProductosLinearLayout: LinearLayout
    lateinit var publicarProductoLinearLayout: LinearLayout
    lateinit var nombreProductoEditText: EditText;
    lateinit var descripcionProductoEditText: EditText
    lateinit var nombreProducto: String
    lateinit var descripcionProducto: String
    lateinit var publicarProductoButton: Button
    lateinit var  localizacionImageButton: ImageButton
    lateinit var categoriasRecyclerView: RecyclerView
    var bitmap : Bitmap? = null
    var esRedimensionadoImagen=false;
    var selectedImageUri: Uri? = null
    var imageBitmap: Bitmap? = null
    var isCameraSelected: Boolean = false
    var esFotoProductoActivo = false;
    val REQUEST_IMAGE_GALLERY = 1;
    val REQUEST_IMAGE_CAMERA = 2;
    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_publicar_producto)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        progressBarLinearLayout = findViewById(R.id.linearLayoutProgressBar)
        datosProductosLinearLayout = findViewById(R.id.datosProductosLayout)
        categoriasRecyclerView = findViewById(R.id.categoriasRecyclerView)
        categoriasRecyclerView.setLayoutManager(LinearLayoutManager(this))
        val categoriasAdapter = ListAdapterCategorias(Categorias.categories,  true)
        categoriasRecyclerView.adapter = categoriasAdapter
        publicarProductoLinearLayout = findViewById(R.id.publicarProductoLayout)
        productoImageButton = findViewById(R.id.imagenPublicarProducto)
        productoImageButton.setOnClickListener {
            showImageSourceDialog();
        }
        localizacionImageButton = findViewById(R.id.localizacionPublicarProductoButton)
        localizacionImageButton.setOnClickListener {
            val intent = Intent(this, UbicacionProducto::class.java)
            intent.putExtra("esPublicarProducto", true)
            startActivity(intent)
        }
        publicarProductoButton = findViewById(R.id.publicarProductoButton)
        publicarProductoButton.setOnClickListener {
            nombreProductoEditText = findViewById(R.id.nombrePublicarProducto)
            nombreProducto = nombreProductoEditText.text.toString()
            descripcionProductoEditText = findViewById(R.id.descripcionPublicarProducto)
            descripcionProducto = descripcionProductoEditText.text.toString()
            if(nombreProducto.isEmpty()){ //Se comprueba si el nombre del producto está vacío, de ser así, informamos de ello al usuario
                nombreProductoEditText.setError("Introduce un nombre al producto")
                nombreProductoEditText.requestFocus()
            }else{
                if(descripcionProducto.isEmpty()){ //Si la descripción está vacía, informamos de ello al usuario
                    descripcionProductoEditText.setError("Introduce una breve descripcion al producto")
                    descripcionProductoEditText.requestFocus()
                }else{
                    if(!esFotoProductoActivo){ //Si el usuario no ha subido una foto del producto, se informará de ello al usuario
                        Toast.makeText(this, "Debes subir una foto del producto", Toast.LENGTH_SHORT).show()
                    }else{
                        if(latitud==null || longitud == null){ //Si el usuario no ha establecido una ubicación de preferencia para el intercambio del producto, se le informará de ello
                            Toast.makeText(this, "Debes seleccionar un lugar de preferencia para el producto", Toast.LENGTH_LONG).show()
                        }else{
                            if(categoriasSeleccionadas.isEmpty()){ //Si el usuario no ha seleccionado ninguna categoria para publicar el producto, se le informará de ello al usuario
                                Toast.makeText(this, "Seleccione al menos una categoria para el producto", Toast.LENGTH_LONG).show()
                            }else{
                                if(isCameraSelected){
                                    uploadBitmapToFirebase(imageBitmap!!)
                                }else{
                                    if(esRedimensionadoImagen){
                                        uploadBitmapToFirebase(bitmap!!)
                                    }else{
                                        uploadImageToFirebase(selectedImageUri!!)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Cuando el usuario quiera subir la imagen del producto, se le preguntará si lo quiere subir de la galeria o la camara
     */
    private fun showImageSourceDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una opción")
        val opciones = arrayOf("Galería", "Cámara")
        builder.setItems(opciones, DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                0 -> handleStoragePermission() //En caso de elegir la galeria, se comprueba los permisos de almacenamiento
                1 -> handleCameraPermission() //En caso de elegir la cámara, se comprobará los permisos de la cámara
            }
        })
        builder.show()
    }

    /**
     * Abre la galería del usuario
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
    }

    /**
     * Abre la cámara del usuario
     */
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAMERA)
    }

    /**
     * Comprueba los permisos de almacenamiento
     */
    private fun handleStoragePermission(){
        // En Android 6.0+ es necesario pedir permiso para leer almacenamiento externo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){ //Si la versión de Android es superior a Android 13, se pide un tipo de permiso
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
            }else{ //Si la versión de Android es inferior a Android 13, se pide otro tipo de permiso
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

    /**
     * Maneja los permisos de la cámara
     */
    private fun handleCameraPermission() {
        // Comprueba si el permiso de cámara ya ha sido concedido.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Verifica si es necesario mostrar una explicación al usuario.

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
                // Si es necesario, muestra un mensaje explicando por qué se necesita el permiso.
                Toast.makeText(
                    this,
                    "Se necesita acceso a la cámara para tomar fotos.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Solicita el permiso de cámara al usuario.
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.CAMERA),
                REQUEST_IMAGE_CAMERA
            )
        } else {
            // Si el permiso ya fue concedido, abre directamente la cámara.
            openCamera()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_GALLERY -> { //Si el request code es la galeria, se guarda como un bitmap
                    selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        bitmap = Utilities().getResizedBitmapFromUri(selectedImageUri!!, 400, 400, contentResolver)
                        if (bitmap != null) {
                            productoImageButton.setImageBitmap(bitmap)
                        }
                        esFotoProductoActivo=true
                    }
                }
                REQUEST_IMAGE_CAMERA -> { //Si el request code es la cámara se guarda  también como un bitmap
                    isCameraSelected=true
                    imageBitmap = data?.extras?.get("data") as? Bitmap
                    if (imageBitmap != null) {
                        productoImageButton.setImageBitmap(imageBitmap)
                        esFotoProductoActivo=true
                    }
                }
            }
            productoImageButton.isEnabled = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Verifica si la respuesta es para el permiso de cámara.
        if (requestCode == REQUEST_IMAGE_CAMERA) {
            // Comprueba si el permiso fue concedido.
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el permiso fue concedido, abre la cámara.
                openCamera()
            } else {
                // Si el permiso fue denegado, muestra un mensaje informativo al usuario.
                Toast.makeText(this, "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show()
            }
        }

        if(requestCode == REQUEST_IMAGE_GALLERY){
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
     * Sube la imagen a Firebase en caso de que la imagen sea URI
     */
    private fun uploadImageToFirebase(imageUri: Uri) {
        datosProductosLinearLayout.visibility = View.GONE
        publicarProductoLinearLayout.visibility = View.INVISIBLE
        progressBarLinearLayout.visibility = View.VISIBLE
        val database = FirebaseDatabase.getInstance().reference
        val productId = database.child("Producto").push().key
        val storageRef = storage.reference.child("images/${productId}.jpg")
        val uploadTask = storageRef.putFile(imageUri)
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                // Aquí podrías obtener el URL de descarga si quieres
                val downloadUrl = uri.toString()
                try {
                    //Una vez guardado la imagen y obtenido la URL, se guarda el producto en la base de datos
                    DatabaseService().guardarProducto(downloadUrl, nombreProducto, descripcionProducto, database, productId!!, latitud!!, longitud!!)
                    progressBarLinearLayout.visibility = View.GONE
                    Toast.makeText(this, "Producto subido correctamente", Toast.LENGTH_SHORT).show()
                    MenuPrincipal.selectedFragment = Perfil() //Una vez se sube la imagen, se redirige al usuario de nuevo a su perfil
                    val intent = Intent(this, MenuPrincipal::class.java)
                    startActivity(intent)
                    finish();
                }catch(e: Exception){ //Si da error al subir la imagen, se le informará al usuario de ello
                    progressBarLinearLayout.visibility = View.GONE
                    datosProductosLinearLayout.visibility = View.VISIBLE
                    publicarProductoLinearLayout.visibility = View.VISIBLE
                    Toast.makeText(this, "Error al subir el producto. Vuelve a intentarlo", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener { //Si ocurre algún error al subir la imagen, se le infoma al usuario de ello
            progressBarLinearLayout.visibility = View.GONE
            datosProductosLinearLayout.visibility = View.VISIBLE
            publicarProductoLinearLayout.visibility = View.VISIBLE
            Toast.makeText(this, "Error subiendo imagen: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Si la imagen es un bitmap se hará uso de este método para subirlo a Firebase
     */
    private fun uploadBitmapToFirebase(bitmap: android.graphics.Bitmap) {
        datosProductosLinearLayout.visibility = View.GONE
        publicarProductoLinearLayout.visibility = View.GONE
        progressBarLinearLayout.visibility = View.VISIBLE
        val database = FirebaseDatabase.getInstance().reference
        val productId = database.child("Producto").push().key
        val storageRef = storage.reference.child("images/${productId}.jpg")
        val data = Utilities().convertirAData(bitmap)
        val uploadTask = storageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                try {
                    //Una vez subido la imagen y obtenido la URL, se guarda el producto en la base de datos
                    DatabaseService().guardarProducto(downloadUrl, nombreProducto, descripcionProducto, database, productId!!, latitud!!, longitud!!)
                    progressBarLinearLayout.visibility = View.GONE
                    Toast.makeText(this, "Producto subido correctamente", Toast.LENGTH_SHORT).show()
                    MenuPrincipal.selectedFragment = Perfil()
                    val intent = Intent(this, MenuPrincipal::class.java)
                    startActivity(intent)
                    finishAffinity();
                }catch(e: Exception){ //Si da error al subir la imagen, se le informará al usuario de ello
                    progressBarLinearLayout.visibility = View.GONE
                    datosProductosLinearLayout.visibility = View.VISIBLE
                    publicarProductoLinearLayout.visibility = View.VISIBLE
                    Toast.makeText(this, "Error al subir el producto. Vuelve a intentarlo", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener { //Si ocurre algún error al subir la imagen, se le infoma al usuario de ello
            progressBarLinearLayout.visibility = View.GONE
            datosProductosLinearLayout.visibility = View.VISIBLE
            publicarProductoLinearLayout.visibility = View.VISIBLE
            Toast.makeText(this, "Error subiendo el producto: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}