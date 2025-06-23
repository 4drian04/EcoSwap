package com.example.ecoswap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoswap.callbacks.ProductosCallBack
import com.example.ecoswap.listAdapter.ListAdapterProducto
import com.example.ecoswap.modelos.Categorias
import com.example.ecoswap.modelos.Producto
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MenuPrincipalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MenuPrincipalFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var buscadorLinearLayout: LinearLayout
    lateinit var productosMenuRecyclerView: RecyclerView
    lateinit var menuProgressBarLayout: LinearLayout
    lateinit var notifcacionImageButton: ImageButton
    lateinit var noHayProductosLinearLayout: LinearLayout
    lateinit var buscador: SearchView
    lateinit var categoryChipGroup: ChipGroup
    lateinit var searchIconImageView: ImageView
    var filtro = "";
    var categoriasSeleccionadas: ArrayList<String> = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu_principal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MainActivity.estaEnPerfil=false
        DetalleProducto.esProductoIntercambiado = false
        buscadorLinearLayout = view.findViewById(R.id.buscadorMenuLinearLayout)
        buscador = view.findViewById(R.id.search_view)
        searchIconImageView = view.findViewById(R.id.searchIcon)
        notifcacionImageButton = view.findViewById(R.id.notificacionesButton)
        noHayProductosLinearLayout = view.findViewById(R.id.noHayProductosLayout)
        categoryChipGroup = view.findViewById(R.id.category_chip_group) // Inicializar el ChipGroup
        productosMenuRecyclerView = view.findViewById(R.id.productosMenuRecyclerView)
        productosMenuRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        menuProgressBarLayout = view.findViewById(R.id.menuProgressBarLayout)
        categoryChipGroup.visibility = View.GONE
        buscadorLinearLayout.visibility = View.GONE
        productosMenuRecyclerView.visibility = View.GONE
        menuProgressBarLayout.visibility = View.VISIBLE

        fillCategoryChips() // Llena los chips con las categorías disponibles

        buscador.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                productosMenuRecyclerView.visibility = View.GONE
                categoryChipGroup.visibility = View.GONE
                menuProgressBarLayout.visibility = View.VISIBLE
                p0.let {
                    filtro = p0!!
                    //Si el usuario le da a buscar, hará una consulta a la base de datos, obteniendo los productos correspondientes
                    DatabaseService().obtenerProductosFiltro(MainActivity.sharedPref.getString("uid", "")!!, p0, categoriasSeleccionadas){productos ->
                        menuProgressBarLayout.visibility = View.GONE
                        if(productos.isEmpty()){ //Si la lista de productos está vacía (o es nulo), se muestra por pantalla un texto indicando que no hay productos ue mostrar
                            productosMenuRecyclerView.visibility = View.GONE
                            noHayProductosLinearLayout.visibility = View.VISIBLE
                        }else{
                            noHayProductosLinearLayout.visibility = View.GONE
                            val productosAdapter = ListAdapterProducto(productos, requireContext());
                            categoryChipGroup.visibility = View.VISIBLE
                            productosMenuRecyclerView.visibility = View.VISIBLE
                            productosMenuRecyclerView.adapter = productosAdapter
                        }
                    }
                }
                return true // Indica que se manejó el evento
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                var esCambiado = false
                if(p0.isNullOrEmpty()){
                    DatabaseService().obtenerProductosFiltro(MainActivity.sharedPref.getString("uid", "")!!, p0, categoriasSeleccionadas){productos ->
                        menuProgressBarLayout.visibility = View.GONE
                        if(productos.isEmpty()){ //Si la lista de productos está vacía (o es nulo), se muestra por pantalla un texto indicando que no hay productos ue mostrar
                            productosMenuRecyclerView.visibility = View.GONE
                            noHayProductosLinearLayout.visibility = View.VISIBLE
                        }else{
                            noHayProductosLinearLayout.visibility = View.GONE
                            val productosAdapter = ListAdapterProducto(productos, requireContext());
                            categoryChipGroup.visibility = View.VISIBLE
                            productosMenuRecyclerView.visibility = View.VISIBLE
                            productosMenuRecyclerView.adapter = productosAdapter
                            esCambiado = true
                            filtro = ""
                        }
                    }
                }else{
                    filtro = p0
                }
                return esCambiado
            }
        })

        searchIconImageView.setOnClickListener {
            productosMenuRecyclerView.visibility = View.GONE
            categoryChipGroup.visibility = View.GONE
            menuProgressBarLayout.visibility = View.VISIBLE
            //Si el usuario le da a buscar, hará una consulta a la base de datos, obteniendo los productos correspondientes
            DatabaseService().obtenerProductosFiltro(MainActivity.sharedPref.getString("uid", "")!!, filtro, categoriasSeleccionadas){productos ->
                menuProgressBarLayout.visibility = View.GONE
                if(productos.isEmpty()){ //Si la lista de productos está vacía (o es nulo), se muestra por pantalla un texto indicando que no hay productos ue mostrar
                    productosMenuRecyclerView.visibility = View.GONE
                    noHayProductosLinearLayout.visibility = View.VISIBLE
                }else{
                    noHayProductosLinearLayout.visibility = View.GONE
                    val productosAdapter = ListAdapterProducto(productos, requireContext());
                    categoryChipGroup.visibility = View.VISIBLE
                    productosMenuRecyclerView.visibility = View.VISIBLE
                    productosMenuRecyclerView.adapter = productosAdapter
                }
            }
        }
        notifcacionImageButton.setOnClickListener {
            val intent = Intent(requireContext(), Notificaciones::class.java) //Envia al usuario a la pantalla para visualizar las notificaciones
            startActivity(intent)
        }
        getProductos()
    }

    private fun getProductos(){
        //Se obtiene los productos de la base de datos que no sean del usuario ni tenga un intercambio pendiente con dicho producto
        DatabaseService().obtenerProductosMenuPrincipal(MainActivity.sharedPref.getString("uid", "")!!, object : ProductosCallBack{
            override fun onCallback(producto: List<Producto>?) {
                menuProgressBarLayout.visibility = View.GONE
                if(producto.isNullOrEmpty()){ //Si la lista de productos está vacía (o es nulo), se muestra por pantalla un texto indicando que no hay productos ue mostrar
                    productosMenuRecyclerView.visibility = View.GONE
                    noHayProductosLinearLayout.visibility = View.VISIBLE
                }else{
                    try{
                        val productosAdapter = ListAdapterProducto(producto, requireContext());
                        productosMenuRecyclerView.visibility = View.VISIBLE
                        productosMenuRecyclerView.adapter = productosAdapter
                        buscadorLinearLayout.visibility = View.VISIBLE
                        categoryChipGroup.visibility = View.VISIBLE
                    }catch(e: Exception){ //Es posible que haya alguna excepción, una de ellas es si cambias de pestañas muy rapido, en ese caso se pondría el texto de que no hay productos que mostrar. Pero si el usuario hace clikc de nuevo en esta pestaña, vuelven a cargar lo sproductos
                        productosMenuRecyclerView.visibility = View.GONE
                        noHayProductosLinearLayout.visibility = View.VISIBLE
                    }
                }
            }
        })

    }

    /**
     * Llena los chips con las categorías disponibles
     */
    private fun fillCategoryChips() {
        for (category in Categorias.categories) {
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true // Permitir selección múltiple
                setOnCheckedChangeListener { _, isChecked ->
                    if(isChecked){
                        //Si el usuario hace clic en alguna de las categorias, se añade a la lista de las categorias seleccionadas para poder buscar por dichas categorias
                        categoriasSeleccionadas.add(category)
                    }else{
                        //Si el usuario hace clic en alguna de las categorias, se elimina de la lista de las categorias seleccionadas para poder buscar por las categorias si seleccionadas
                        categoriasSeleccionadas.remove(category)
                    }
                    productosMenuRecyclerView.visibility = View.GONE
                    menuProgressBarLayout.visibility = View.VISIBLE
                    //Se obitene los productos con las categorias seleccionadas
                    var filtroAux: String? = "";
                    Log.d("FILTRO", filtro)
                    if(filtro.isNotEmpty()){
                        filtroAux = filtro
                    }else{
                        filtroAux = null
                    }
                    DatabaseService().obtenerProductosFiltro(MainActivity.sharedPref.getString("uid", "")!!, filtroAux, categoriasSeleccionadas){productos ->
                        menuProgressBarLayout.visibility = View.GONE
                        if(productos.isEmpty()){ //Si la lista de productos está vacía (o es nulo), se muestra por pantalla un texto indicando que no hay productos ue mostrar
                            productosMenuRecyclerView.visibility = View.GONE
                            noHayProductosLinearLayout.visibility = View.VISIBLE
                        }else{
                            noHayProductosLinearLayout.visibility = View.GONE
                            val productosAdapter = ListAdapterProducto(productos, requireContext());
                            productosMenuRecyclerView.visibility = View.VISIBLE
                            productosMenuRecyclerView.adapter = productosAdapter
                        }
                    }
                }
            }
            categoryChipGroup.addView(chip) // Agrega el chip al ChipGroup
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MenuPrincipalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MenuPrincipalFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}