package com.example.ecoswap

import com.example.ecoswap.modelos.Producto
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
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
import com.example.ecoswap.callbacks.UsuarioCallBack
import com.example.ecoswap.listAdapter.ListAdapterProducto
import com.example.ecoswap.modelos.Usuario

class ProductosAIntercambiar : AppCompatActivity() {
    companion object{
        var productoElegido: Producto? = null; //Probar que si yo intercambio un producto y luego otro, y en el segundo no elijo producto, me da error
    }
    lateinit var productosRecyclerView: RecyclerView
    lateinit var progressBarLinearLayout: LinearLayout
    lateinit var tituloProductosAIntercambiar: TextView
    lateinit var datosProductosLinearLayout: LinearLayout
    lateinit var intercambiarProductoButton: Button
    lateinit var sinProductosTextView: TextView
    lateinit var sinProductosLinearLayout: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_productos_aintercambiar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        productoElegido = null
        datosProductosLinearLayout = findViewById(R.id.datosProductosAIntercambiar)
        productosRecyclerView = findViewById(R.id.productosAIntercambiar)
        productosRecyclerView.setLayoutManager(LinearLayoutManager(this))
        progressBarLinearLayout = findViewById(R.id.productosAIntercambiarProgressBar)
        sinProductosTextView = findViewById(R.id.sinProductosParaIntercambiar)
        tituloProductosAIntercambiar = findViewById(R.id.tituloProductosAIntercambiar)
        intercambiarProductoButton = findViewById(R.id.intercambiarProductoElegidoButton)
        sinProductosLinearLayout = findViewById(R.id.sinProductosParaIntercambiarLinearLayout)
        datosProductosLinearLayout.visibility = View.GONE
        progressBarLinearLayout.visibility = View.VISIBLE
        //Se obtiene los productos del usuario actual para que eliga uno de ellos
        DatabaseService().obtenerProductosPorUsuario(MainActivity.sharedPref.getString("uid", "")!!) { productos ->
            if(productos.isEmpty()){ //Si no hay ningún producto, se muestra por pantalla que no hay ningún producto para elegir
                datosProductosLinearLayout.visibility = View.GONE
                sinProductosLinearLayout.visibility = View.VISIBLE
            }else{ //En caso de que si haya, se mostrarán los productos obtenidos
                val productoAdapter = ListAdapterProducto(productos, this)
                productosRecyclerView.adapter = productoAdapter
                sinProductosLinearLayout.visibility = View.GONE
                datosProductosLinearLayout.visibility = View.VISIBLE
            }
            progressBarLinearLayout.visibility = View.GONE

        }
        intercambiarProductoButton.setOnClickListener {
            if(productoElegido == null){ //Si el usuario le da al botón de intercambiar pero no ha elegido ninguno, se informará de ello al usuario
                Toast.makeText(this, "Tienes que elegir un producto", Toast.LENGTH_LONG).show()
            }else{
                showImageSourceDialog()
            }
        }
    }

    /**
     * Cuando el usuario elige un producto, se le pregunta de que si está seguro del producto elegido
     */
    private fun showImageSourceDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("¿Estás seguro que quieres seleccionar este producto?")
        val opciones = arrayOf("Si", "No")
        builder.setItems(opciones, DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                0 -> registrarIntercambio()
                1 -> null
            }
        })
        builder.show()
    }

    /**
     * En caso de que si esté seguro, se registrará el intercambio en la base de datos
     */
    private fun registrarIntercambio(){
        val productoId = intent.getStringExtra("productoId")
        val usuarioId = intent.getStringExtra("userId")
        Utilities().registrarIntercambio(productoId!!, usuarioId!!, this, true)
        MenuPrincipal.selectedFragment = MenuPrincipalFragment()
        val intent = Intent(this, MenuPrincipal::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        DetalleProducto.esProductoIntercambiado = false
    }
}