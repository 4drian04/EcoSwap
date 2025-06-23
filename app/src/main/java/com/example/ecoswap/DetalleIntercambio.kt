package com.example.ecoswap

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.ecoswap.callbacks.UsuarioCallBack
import com.example.ecoswap.callbacks.ValoracionCallback
import com.example.ecoswap.modelos.Intercambio
import com.example.ecoswap.modelos.Producto
import com.example.ecoswap.modelos.Usuario
import com.example.ecoswap.modelos.Valoracion

class DetalleIntercambio : AppCompatActivity() {

    companion object{
        lateinit var intercambio: Intercambio
    }

    lateinit var imagenPrimerProductoDetalleIntercambio: ImageView
    lateinit var imagenSegundoProductoDetalleIntercambio: ImageView
    lateinit var fotoPerfilPrimerUsuarioDetalleIntercambio: ImageButton
    lateinit var fotoPerfilSegundoUsuarioDetalleIntercambio: ImageButton
    lateinit var datosDetalleIntercambioLinearLayout: LinearLayout
    lateinit var botonesIntercambioLinearLayout: LinearLayout
    lateinit var cancelarIntercambioButton: Button
    lateinit var confirmarIntercambioButton: ImageButton
    lateinit var detalleIntercambioProgressBarLinearLayout: LinearLayout
    lateinit var chatIntercambio : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_intercambio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Obtenemos el id de los View
        detalleIntercambioProgressBarLinearLayout = findViewById(R.id.detalleIntercambioProgressBar)
        datosDetalleIntercambioLinearLayout = findViewById(R.id.datosDetalleIntercambio)
        chatIntercambio = findViewById(R.id.chatIntercambio)
        botonesIntercambioLinearLayout = findViewById(R.id.botonesIntercambios)
        datosDetalleIntercambioLinearLayout.visibility = View.GONE
        botonesIntercambioLinearLayout.visibility = View.GONE
        detalleIntercambioProgressBarLinearLayout.visibility = View.VISIBLE
        imagenPrimerProductoDetalleIntercambio = findViewById(R.id.imagenPrimerProductoDetalleIntercambio)
        imagenSegundoProductoDetalleIntercambio = findViewById(R.id.imagenSegundoProductoDetalleIntercambio)
        cancelarIntercambioButton = findViewById(R.id.cancelarIntercambio)
        confirmarIntercambioButton = findViewById(R.id.confirmarIntercambio)
        fotoPerfilPrimerUsuarioDetalleIntercambio = findViewById(R.id.fotoPerfilPrimerUsuarioDetalleIntercambio)
        fotoPerfilSegundoUsuarioDetalleIntercambio = findViewById(R.id.fotoPerfilSegundoUsuarioDetalleIntercambio)
        //Obtenemos los productos como una lista, hay que recordar que el intercambio se obtiene desde la pantalla anterior, ya que es una variable estática
        val productos = intercambio.productos.values.toList()
        //Ponemos las fotos de los productos mediante la dependencia Glide
        var otroUsuario: String = ""
        //Obtenemos los datos del usuario vendedor para mostrarlo por pantalla
        DatabaseService().obtenerUsuario(intercambio.vendedor, object : UsuarioCallBack {
            override fun onCallback(usuario: Usuario?) {
                Glide.with(this@DetalleIntercambio)
                    .load(usuario!!.urlFotoPerfil)
                    .into(fotoPerfilPrimerUsuarioDetalleIntercambio)
                if(intercambio.vendedor == productos[0].userId){
                    Glide.with(this@DetalleIntercambio)
                        .load(productos[0].fotoProducto)
                        .into(imagenPrimerProductoDetalleIntercambio)
                    if(productos.size>1){
                        Glide.with(this@DetalleIntercambio)
                            .load(productos[1].fotoProducto)
                            .into(imagenSegundoProductoDetalleIntercambio)
                    }else{
                        imagenSegundoProductoDetalleIntercambio.setImageResource(R.drawable.dinero)
                    }
                }else{
                    Glide.with(this@DetalleIntercambio)
                        .load(productos[0].fotoProducto).placeholder(R.drawable.loading).error(R.drawable.ic_profile)
                        .into(imagenSegundoProductoDetalleIntercambio)
                    if(productos.size>1){
                        Glide.with(this@DetalleIntercambio)
                            .load(productos[1].fotoProducto).placeholder(R.drawable.loading).error(R.drawable.ic_profile)
                            .into(imagenPrimerProductoDetalleIntercambio)
                    }else{
                        imagenSegundoProductoDetalleIntercambio.setImageResource(R.drawable.dinero)
                    }
                }
                //Si el usuario actual no es el vendedor, hacemos que sea clickable para poder redirigirnos a su perfil
                if(intercambio.vendedor!=MainActivity.sharedPref.getString("uid", "")){
                    fotoPerfilPrimerUsuarioDetalleIntercambio.setOnClickListener {
                        ProductosUsuario.uid = intercambio.vendedor
                        ValoracionUsuario.uid = intercambio.vendedor
                        val intent = Intent(this@DetalleIntercambio, PerfilOtroUsuario::class.java)
                        intent.putExtra("uid", intercambio.vendedor)
                        startActivity(intent)
                    }
                    otroUsuario = intercambio.vendedor //Guardamos el id del otro usuario para poder utilizarlo posteriormente
                }
            }
        })
        DatabaseService().obtenerUsuario(intercambio.comprador, object : UsuarioCallBack {
            override fun onCallback(usuario: Usuario?) {
                Glide.with(this@DetalleIntercambio)
                    .load(usuario!!.urlFotoPerfil)
                    .into(fotoPerfilSegundoUsuarioDetalleIntercambio)
                //Si el usuario actual no es el comprador, hacemos que sea clickable para poder redirigirnos a su perfil
                if(intercambio.comprador!=MainActivity.sharedPref.getString("uid", "")){
                    fotoPerfilSegundoUsuarioDetalleIntercambio.setOnClickListener {
                        ProductosUsuario.uid = intercambio.comprador
                        ValoracionUsuario.uid = intercambio.comprador
                        val intent = Intent(this@DetalleIntercambio, PerfilOtroUsuario::class.java)
                        intent.putExtra("uid", intercambio.comprador)
                        startActivity(intent)
                    }
                    otroUsuario = intercambio.comprador
                }
            }
        })
        //Si el usuario hace click en el icono del chat, se redirige al chat con el otro usuario
        chatIntercambio.setOnClickListener {
            val intent = Intent(this@DetalleIntercambio, ChatActivity::class.java)
            intent.putExtra("otroUsuario", otroUsuario)
            startActivity(intent)
        }
        detalleIntercambioProgressBarLinearLayout.visibility = View.GONE
        datosDetalleIntercambioLinearLayout.visibility = View.VISIBLE
        botonesIntercambioLinearLayout.visibility = View.VISIBLE

        cancelarIntercambioButton.setOnClickListener {
            confirmarEliminacionIntercambio()
        }

        confirmarIntercambioButton.setOnClickListener {
            if(intercambio.comprador == MainActivity.sharedPref.getString("uid", "")){ //Comprobamos si el usuario actual es el comprador
                confirmacionIntercambio(productos, "compradorVistoBueno", intercambio.vendedor, true)
            }else{ //Si el usuario actual es el vendedor, hacemos lo mismo pero como vendedor
                confirmacionIntercambio(productos, "vendedorVistoBueno", intercambio.comprador, false)
            }
        }

        if(intercambio.comprador == MainActivity.sharedPref.getString("uid", "")){
            if(intercambio.compradorVistoBueno){ //Si el usuario actual es el comprador y ha dado el visto bueno, no le aparecerá los botones, ya que no tiene sentido que aparezca de nuevo los botones para confirmar o cancelar el intercambio
                botonesIntercambioLinearLayout.visibility = View.GONE
            }
        }else{
            if(intercambio.vendedorVistoBueno){
                botonesIntercambioLinearLayout.visibility = View.GONE
            }
        }
    }

    private fun confirmarIntercambio(productos: List<Producto>, vistoBueno: String, uidOtroUsuario:String, esComprador: Boolean){
        //Actualizamos el visto bueno en la base de datos (es por ello que debemos comprobar si el usuario actual es comprador o vendedor)
        DatabaseService().actualizarVistoBueno(intercambio.intercambioId, vistoBueno){ exito ->
            if(exito){
                confirmarIntercambioButton.visibility = View.GONE
                Toast.makeText(this, "Has confirmado el intercambio correctamente", Toast.LENGTH_LONG).show()
                DatabaseService().comprobarVistoBueno(intercambio.intercambioId, esComprador) {esVistoBueno->
                    if(esVistoBueno){
                        DatabaseService().actualizarEstado(intercambio.intercambioId){ actualizado ->
                            if(actualizado){
                                //Si se ha actualizado el estado correctamente, construimos el mensaje para la notificación
                                val mensaje = "El intercambio de ${productos[0].nombre} por ${if (productos.size > 1) productos[1].nombre else "dinero"} se ha realizado correctamente"
                                //Obtenemos los datos del usuario para enviar la notificación
                                DatabaseService().obtenerUsuario(uidOtroUsuario, object :
                                    UsuarioCallBack {
                                    override fun onCallback(usuario: Usuario?) {
                                        FCMNotificationService().enviarNotificacion(mensaje, usuario!!.token)
                                        DatabaseService().guardarNotificacion(uidOtroUsuario, MainActivity.sharedPref.getString("nombreUsuario", "")!!, mensaje)
                                    }
                                })
                                //Como el intercambio se ha realizado, eliminamos los productos y las imagenes, dejando solo el intercambio realizado, de esta manera, ahorramos y optimizamos costes de la base de datos
                                DatabaseService().eliminarProductoFirebase(productos[0].productoId)
                                DatabaseService().eliminarArchivoFirebaseStorage("images/" + productos[0].productoId + ".jpg")
                                if(productos.size>1){
                                    DatabaseService().eliminarProductoFirebase(productos[1].productoId)
                                    DatabaseService().eliminarArchivoFirebaseStorage("images/" + productos[1].productoId + ".jpg")
                                }
                                //Si el usuario actual no ha valorado al otro usuario nunca, permitimos poder valorar al otro usuario
                                DatabaseService().comprobarValoracionUsuario(uidOtroUsuario, MainActivity.sharedPref.getString("uid", "")!!, object:
                                    ValoracionCallback {
                                    override fun onCallback(valoracion: Valoracion?) {
                                        if(valoracion!=null){
                                            activityMenuPrincipal()
                                        }else{
                                            preguntarValorarUsuario()
                                        }
                                    }
                                })
                            }
                        }
                    }else{
                        finish() //Probar
                    }
                }
            }else{
                Toast.makeText(this, "No se ha podido confirmar el intercambio. Intentalo de nuevo", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmacionIntercambio(productos: List<Producto>, vistoBueno: String, uidOtroUsuario:String, esComprador:Boolean) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("¿Seguro que quieres confirmar el intercambio?") //Preguntamos al usuario si está seguro de la eliminación del intercambio
        val opciones = arrayOf("Si", "No")
        builder.setItems(opciones, DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                0 -> confirmarIntercambio(productos, vistoBueno, uidOtroUsuario, esComprador) //En caso de que diga que si, se elimina el intercambio y se le redirige al menú principal
                1 -> null
            }
        })
        builder.show()
    }

    private fun confirmarEliminacionIntercambio() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("¿Seguro que quieres eliminar el intercambio?") //Preguntamos al usuario si está seguro de la eliminación del intercambio
        val opciones = arrayOf("Si", "No")
        builder.setItems(opciones, DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                0 -> cambiarActivity() //En caso de que diga que si, se elimina el intercambio y se le redirige al menú principal
                1 -> null
            }
        })
        builder.show()
    }

    private fun cambiarActivity(){
        //Se elimina el intercambio y se envía al usuario al menú principal
        DatabaseService().eliminarIntercambio(intercambio.intercambioId) { exito ->
            if(exito){
                val mensaje = MainActivity.sharedPref.getString("nombreUsuario", "")!! + " ha eliminado un intercambio"
                val uid: String
                if(intercambio.comprador == MainActivity.sharedPref.getString("uid", "")){ //Dependiendo de si el usuario es comprador o no, se coge un uid o no
                    uid = intercambio.vendedor
                }else{
                    uid = intercambio.comprador
                }
                DatabaseService().obtenerUsuario(uid, object : UsuarioCallBack { //Se envia la notificación al otro usuario de que se ha cancelado el intercambio
                    override fun onCallback(usuario: Usuario?) {
                        FCMNotificationService().enviarNotificacion(mensaje, usuario!!.token)
                        DatabaseService().guardarNotificacion(uid, MainActivity.sharedPref.getString("nombreUsuario", "")!!, mensaje)
                    }
                })
                finish()
            }else{ //Si no se ha podido cancelar el intercambio, se le informa al usuario
                Toast.makeText(this, "No se ha podido eliminar el intercambio, vuelve a intentarlo más tarde.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Se le pregunta al usuario si quiere valorar al usuario
     */
    private fun preguntarValorarUsuario(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("¿Quieres valorar al usuario?")
        val opciones = arrayOf("Si", "No")
        builder.setItems(opciones, DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                0 -> activityValorarUsuario()
                1 -> activityMenuPrincipal()
            }
        })
        builder.show()
    }

    /**
     * En caso de que quiera valorar al usuario, se envía al activity para valorar al usuario
     */
    private fun activityValorarUsuario(){
        val intent = Intent(this, ValorarUsuario::class.java)
        if(intercambio.vendedor == MainActivity.sharedPref.getString("uid", "")){
            intent.putExtra("uidUsuario", intercambio.comprador)
        }else{
            intent.putExtra("uidUsuario", intercambio.vendedor)
        }
        startActivity(intent)
        finish()
    }

    /**
     * Si no quiere valorar al usuario, s ele envía al menú principal
     */
    private fun activityMenuPrincipal(){
        Toast.makeText(this, "El intercambio se ha realizado correctamente. ¡Disfrutad del producto!", Toast.LENGTH_LONG).show()
        MenuPrincipal.selectedFragment = MenuPrincipalFragment()
        val intent = Intent(this, MenuPrincipal::class.java)
        startActivity(intent)
        finishAffinity()
    }
}