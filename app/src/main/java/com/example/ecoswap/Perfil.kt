package com.example.ecoswap

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.ecoswap.activities.ViewPagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.io.InputStream
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var tabLayout: TabLayout
private lateinit var viewPager: ViewPager2
private lateinit var viewPagerAdapter: ViewPagerAdapter

/**
 * A simple [Fragment] subclass.
 * Use the [Perfil.newInstance] factory method to
 * create an instance of this fragment.
 */
class Perfil : Fragment() {
    lateinit var logoutButton: ImageButton
    lateinit var nombreUsuario: TextView
    lateinit var subirProductoButton: FloatingActionButton;
    lateinit var fotoPerfilUsuario: ImageView
    lateinit var cambiarContrasenhaButton: Button
    lateinit var cambiarFotoPerfilButton: Button
    val REQUEST_IMAGE_GALLERY = 1;
    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MainActivity.estaEnPerfil=true
        DetalleProducto.esProductoIntercambiado = false
        tabLayout = view.findViewById(R.id.tab_layout)
        viewPager = view.findViewById(R.id.view_pager)

        viewPagerAdapter = ViewPagerAdapter(this.requireActivity()) //Se establece el adapter del ViewPager para poder navegar entre las distintas pestañas
        viewPager.adapter = viewPagerAdapter

        //Establecemos el texto de cada pestaña
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Productos Publicados"
                1 -> "Valoraciones"
                else -> null
            }
        }.attach()

        logoutButton = view.findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            //Si el usuario cierra sesión, se elimina el token del dispositivo de la base de datos
            DatabaseService().eliminarTokenUsuario(MainActivity.sharedPref.getString("uid", "")!!)
            var sharedPreferencesEdit = MainActivity.sharedPref.edit();
            // Cierra la sesión del usuario actual en Firebase Authentication
            FirebaseAuth.getInstance().signOut()
            sharedPreferencesEdit.clear() //Se elimina los datos almacenados en el preferences
            sharedPreferencesEdit.apply()
            var intentLogin = Intent(activity, MainActivity::class.java) //Se vuelve a enviar al usuario a la pantalla de login
            startActivity(intentLogin)
            activity?.finishAffinity()
        }
        //Se carga los datos del usuario en la pantalla
        fotoPerfilUsuario = view.findViewById(R.id.fotoPerfilUsuario)
        Glide.with(requireContext())
            .load(MainActivity.sharedPref.getString("fotoPerfil", "")!!).placeholder(R.drawable.loading).error(R.drawable.ic_profile)
            .into(fotoPerfilUsuario)
        nombreUsuario = view.findViewById(R.id.nombreUsuario)
        nombreUsuario.setText(MainActivity.sharedPref.getString("nombreUsuario", "usuario"))
        subirProductoButton = view.findViewById(R.id.subirProductoButton)
        //Si el usuario hace clic en el botón para subir un producto, se redirige a dicha pantalla
        subirProductoButton.setOnClickListener {
            PublicarProducto.categoriasSeleccionadas = ArrayList<String>()
            var intentLogin = Intent(activity, PublicarProducto::class.java)
            startActivity(intentLogin)
        }
        if(MainActivity.sharedPref.getString("authType", "") == AuthProvider.EMAIL.toString()){
            cambiarContrasenhaButton = view.findViewById(R.id.cambiarContrasenhaButton)
            cambiarContrasenhaButton.visibility = View.VISIBLE
            cambiarContrasenhaButton.setOnClickListener {
                val intent = Intent(requireContext(), CambioContrasenha::class.java)
                startActivity(intent)
            }
        }
        cambiarFotoPerfilButton = view.findViewById(R.id.cambiarFotoPerfilButton)
        cambiarFotoPerfilButton.setOnClickListener {
            handleStoragePermission()
        }
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
                        requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED) {
                    // Pedir permiso
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        REQUEST_IMAGE_GALLERY
                    )
                } else {
                    // Permiso concedido
                    openGallery()
                }
            }else{ // Sin embargo, si la versión del Android es inferior a Android 13, se pide otro permiso
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED) {
                    // Pedir permiso
                    ActivityCompat.requestPermissions(
                        requireActivity(),
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_GALLERY -> {
                    var selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        var bitmap = Utilities().getResizedBitmapFromUri(selectedImageUri, 400, 400, requireActivity().contentResolver)
                        fotoPerfilUsuario.setImageBitmap(bitmap)
                        val nombreUsuario = MainActivity.sharedPref.getString("nombreUsuario", "")
                        DatabaseService().eliminarArchivoFirebaseStorage("images/$nombreUsuario.jpg")
                        Utilities().cambiarFotoPerfil(bitmap!!, true, requireContext(), nombreUsuario!!, storage)
                    }
                }
            }
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
                Toast.makeText(requireContext(),
                    "Permiso denegado para acceder a la galería",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Perfil.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Perfil().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}