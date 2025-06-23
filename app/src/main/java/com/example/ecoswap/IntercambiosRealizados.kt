package com.example.ecoswap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoswap.callbacks.IntercambioCallback
import com.example.ecoswap.listAdapter.ListAdapterIntercambioRealizado
import com.example.ecoswap.modelos.Intercambio

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [IntercambiosRealizados.newInstance] factory method to
 * create an instance of this fragment.
 */
class IntercambiosRealizados : Fragment() {
    lateinit var intercambiosRealizadosLinearLayout: LinearLayout
    lateinit var intercambiosRealizadosProgressBarLinearLayout: LinearLayout
    lateinit var intercambiosRealizadosRecyclerView: RecyclerView
    lateinit var noHayIntercambiosRealizadosLinearLayout: LinearLayout
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_intercambios_realizados, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intercambiosRealizadosLinearLayout = view.findViewById(R.id.intercambiosRealizadosLinearLayout)
        noHayIntercambiosRealizadosLinearLayout = view.findViewById(R.id.noHayIntercambiosRealizadosLayout)
        intercambiosRealizadosProgressBarLinearLayout = view.findViewById(R.id.intercambiosRealizadosProgressBarLinearLayout)
        intercambiosRealizadosRecyclerView = view.findViewById(R.id.intercambiosRealizadosRecyclerView)
        intercambiosRealizadosRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        intercambiosRealizadosLinearLayout.visibility = View.GONE
        intercambiosRealizadosProgressBarLinearLayout.visibility = View.VISIBLE
        //Obtenemos los intercambios realizados del usuario actual
        DatabaseService().obtenerIntercambiosPendientes(MainActivity.sharedPref.getString("uid", "")!!, "realizado", object:
            IntercambioCallback {
            override fun onCallBack(intercambios: List<Intercambio>) {
                intercambiosRealizadosProgressBarLinearLayout.visibility = View.GONE
                if(intercambios.isEmpty()){ //Si la lista de intercambios está vacía (o es nulo), se muestra por pantalla un texto indicando que no hay intercambios que mostrar
                    intercambiosRealizadosRecyclerView.visibility = View.GONE
                    noHayIntercambiosRealizadosLinearLayout.visibility=View.VISIBLE
                }else{
                    try{
                        val intercambioAdapter = ListAdapterIntercambioRealizado(intercambios, requireContext())
                        intercambiosRealizadosRecyclerView.adapter = intercambioAdapter
                    }catch (e: Exception){ //Es posible que haya alguna excepción, una de ellas es si cambias de pestañas muy rapido, en ese caso se pondría el texto de que no hay intercambios que mostrar. Pero si el usuario hace clikc de nuevo en esta pestaña, vuelven a cargar los intercambios
                        intercambiosRealizadosRecyclerView.visibility = View.GONE
                        noHayIntercambiosRealizadosLinearLayout.visibility=View.VISIBLE
                    }
                }
                intercambiosRealizadosLinearLayout.visibility = View.VISIBLE
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment IntercambiosRealizados.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IntercambiosRealizados().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}