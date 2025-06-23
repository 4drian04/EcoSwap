package com.example.ecoswap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoswap.listAdapter.ListAdapterProducto

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProductosUsuario.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProductosUsuario : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var productosRecyclerView: RecyclerView
    lateinit var progressBarLinearLayout: LinearLayout
    lateinit var noHayProductosUsuarioLinearLayout: LinearLayout

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
        return inflater.inflate(R.layout.fragment_productos_usuario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productosRecyclerView = view.findViewById(R.id.productosUsuario)
        productosRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        progressBarLinearLayout = view.findViewById((R.id.productosUsuarioProgressBar))
        noHayProductosUsuarioLinearLayout = view.findViewById(R.id.noHayProductosUsuariosLayout)
        productosRecyclerView.visibility = View.GONE
        progressBarLinearLayout.visibility = View.VISIBLE
        //Obtenemos los productos del usuario mediante el uid y se muestra por pantalla
        DatabaseService().obtenerProductosPorUsuario(uid) { productos ->
            progressBarLinearLayout.visibility = View.GONE
            if(productos.isEmpty()){
                noHayProductosUsuarioLinearLayout.visibility = View.VISIBLE
            }else{
                val productoAdapter = ListAdapterProducto(productos, requireContext())
                productosRecyclerView.adapter = productoAdapter
                productosRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        lateinit var uid: String
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProductosUsuario.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProductosUsuario().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}