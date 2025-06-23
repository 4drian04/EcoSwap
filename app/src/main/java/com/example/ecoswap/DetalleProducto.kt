package com.example.ecoswap

import com.example.ecoswap.modelos.Producto
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoswap.callbacks.ProductoCallBack
import com.example.ecoswap.callbacks.UsuarioCallBack
import com.example.ecoswap.listAdapter.ListAdapterCategorias
import com.example.ecoswap.modelos.Usuario

class DetalleProducto : AppCompatActivity() {

    companion object{
        var esProductoIntercambiado = false
    }

    lateinit var localizacionDetalleProducto: ImageButton
    lateinit var chatUsuarioDetalleProducto: ImageButton
    lateinit var nombreProductoTextView: TextView
    lateinit var descripcionProductoTextView: TextView
    lateinit var autorProducto: LinearLayout
    lateinit var progressBarrLinearLayout: LinearLayout
    lateinit var datosDetalleProducto: LinearLayout
    lateinit var botonesLinearLayout: LinearLayout
    lateinit var intercambiarEliminarProductoButton: Button
    lateinit var imagenDetalleProductoImageView: ImageView
    lateinit var imagenDetalleProductoUsuario: ImageButton
    lateinit var nombreUsuarioDetalleProducto: TextView
    lateinit var detalleProducto: Producto
    lateinit var categoriasRecyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_producto)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Obtenemos el id de los views
        datosDetalleProducto = findViewById(R.id.datosDetalleProductoLinearLayout)
        botonesLinearLayout = findViewById(R.id.intercambiarEliminarLinearLayout)
        progressBarrLinearLayout = findViewById(R.id.detalleProductoProgressBar)
        datosDetalleProducto.visibility = View.GONE
        botonesLinearLayout.visibility = View.GONE
        progressBarrLinearLayout.visibility = View.VISIBLE
        localizacionDetalleProducto = findViewById(R.id.localizacionDetalleProductoButton)
        chatUsuarioDetalleProducto = findViewById(R.id.chatUsuarioDetalleProductoButton)
        nombreProductoTextView = findViewById(R.id.nombreDetalleProducto)
        descripcionProductoTextView = findViewById(R.id.descripcionDetalleProducto)
        intercambiarEliminarProductoButton = findViewById(R.id.intercambiarProductoButton)
        imagenDetalleProductoImageView = findViewById(R.id.imagenDetalleProducto)
        imagenDetalleProductoUsuario = findViewById(R.id.imagenDetalleProductoUsuario)
        autorProducto = findViewById(R.id.autorProductoLinearLayout)
        imagenDetalleProductoUsuario = findViewById(R.id.imagenDetalleProductoUsuario)
        nombreUsuarioDetalleProducto = findViewById(R.id.nombreUsuarioDetalleProducto)
        categoriasRecyclerView = findViewById(R.id.categoriasRecyclerView)
        categoriasRecyclerView.setLayoutManager(LinearLayoutManager(this))
        //Si el usuario accede al detalle del producto desde su perfil, no se muestra los botones para intercambiar
        if(MainActivity.estaEnPerfil){
            chatUsuarioDetalleProducto.visibility = View.GONE
            intercambiarEliminarProductoButton.setText("Eliminar producto")
            autorProducto.visibility = View.GONE
        }
        //Mediante el ID que se pasa como extra, obtenemos el producto para mostrarlo por pantalla
        DatabaseService().obtenerProducto(intent.getStringExtra("productoId")!!, object :
            ProductoCallBack {
            override fun onCallback(producto: Producto?) {
                if (producto != null) {
                    detalleProducto = producto //Obtenemos el detalle del producto
                    nombreProductoTextView.setText(producto.nombre)
                    descripcionProductoTextView.setText(producto.descripcion)
                    Glide.with(this@DetalleProducto).load(producto.fotoProducto)
                        .error(R.drawable.producto).into(imagenDetalleProductoImageView)
                    progressBarrLinearLayout.visibility = View.GONE
                    datosDetalleProducto.visibility = View.VISIBLE
                    botonesLinearLayout.visibility = View.VISIBLE
                    //Si el usuario accede a detalle producto desde el menú principal o desde el perfil de otro usuario, se obtiene el nombre y la foto de perfil del otro usuario
                    if(!MainActivity.estaEnPerfil){
                        DatabaseService().buscarIntercambioPorProductoYUsuario(producto.productoId, MainActivity.sharedPref.getString("uid", "")!!) {encontrado ->
                            if(encontrado){
                                botonesLinearLayout.visibility = View.GONE
                            }
                        }
                        DatabaseService().obtenerUsuario(producto.userId, object : UsuarioCallBack {
                            override fun onCallback(usuario: Usuario?) {
                                nombreUsuarioDetalleProducto.setText(usuario!!.nombreUsuario)
                                Glide.with(this@DetalleProducto).load(usuario.urlFotoPerfil)
                                    .error(R.drawable.producto).into(imagenDetalleProductoUsuario)
                            }
                        })
                    }
                    //Obtenemos las categorias del producto
                    val categoriaAdapter = ListAdapterCategorias(producto.categorias,  false)
                    categoriasRecyclerView.adapter = categoriaAdapter
                } else {
                    // Manejo del caso en que no se encontró el producto
                    Toast.makeText(this@DetalleProducto, "No se ha podido obtener el producto", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        })
        intercambiarEliminarProductoButton.setOnClickListener {
            if(MainActivity.estaEnPerfil){ //Elimina el producto, ya que es un producto accedido desde el perfil del usuario
                datosDetalleProducto.visibility = View.GONE
                botonesLinearLayout.visibility = View.GONE
                progressBarrLinearLayout.visibility = View.VISIBLE
                //Se elimina el producto de la base de datos, así como su imagen
                DatabaseService().eliminarProductoFirebase(intent.getStringExtra("productoId")!!)
                DatabaseService().eliminarArchivoFirebaseStorage("images/${intent.getStringExtra("productoId")!!}.jpg")
                MenuPrincipal.selectedFragment = Perfil()
                val intent = Intent(this, MenuPrincipal::class.java)
                startActivity(intent)
                finish();
            }else{ //Intercambia el producto, ya que entra en este Activity desde el menu principal
                mostrarMetodosPago()
            }
        }

        //Se va al chat con el id del otro usuario
        chatUsuarioDetalleProducto.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("otroUsuario", detalleProducto.userId)
            startActivity(intent)
        }
        //Si se hace clic en la foto del otro usuario, se redirige a su perfil
        imagenDetalleProductoUsuario.setOnClickListener {
            ProductosUsuario.uid = detalleProducto.userId
            ValoracionUsuario.uid = detalleProducto.userId
            val intent = Intent(this, PerfilOtroUsuario::class.java)
            intent.putExtra("uid", detalleProducto.userId)
            startActivity(intent)
        }
        //Se consulta la localización del producto
        localizacionDetalleProducto.setOnClickListener {
            val intent = Intent(this, UbicacionProducto::class.java)
            intent.putExtra("esPublicarProducto", false)
            intent.putExtra("longitud", detalleProducto.longitud)
            intent.putExtra("latitud", detalleProducto.latitud)
            startActivity(intent)
        }
    }

    /**
     * Se le pregunta al usuario si quiere intercambiarlo por otro producto o  por dinero en efectivo
     */
    private fun mostrarMetodosPago() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una forma de intercambiar el producto")
        val opciones = arrayOf("Intercambiar por otro producto", "Dinero Efectivo")
        builder.setItems(opciones, DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                0 -> elegirProducto()
                1 -> registrarIntercambio()
            }
        })
        builder.show()
    }

    /**
     * Si elige intercambiarlo por otro producto, se redirige a la pantalla para elegir el producto
     */
    private fun elegirProducto(){
        esProductoIntercambiado=true
        val intent = Intent(this, ProductosAIntercambiar::class.java)
        intent.putExtra("productoId", detalleProducto.productoId)
        intent.putExtra("userId", detalleProducto.userId)
        startActivity(intent)
    }

    private fun registrarIntercambio(){
        Utilities().registrarIntercambio(detalleProducto.productoId, detalleProducto.userId, this, false)
        MenuPrincipal.selectedFragment = MenuPrincipalFragment()
        val intent = Intent(this, MenuPrincipal::class.java)
        startActivity(intent)
        finish()
    }
}