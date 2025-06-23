package com.example.ecoswap

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoswap.callbacks.IntercambioCallback
import com.example.ecoswap.listAdapter.ListAdapterIntercambio
import com.example.ecoswap.modelos.Intercambio

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [IntercambiosPendientes.newInstance] factory method to
 * create an instance of this fragment.
 */
class IntercambiosPendientes : Fragment() {

    lateinit var intercambiosPendientesRecyclerView: RecyclerView
    lateinit var intercambiosPendientesLinearLayout: LinearLayout
    lateinit var intercambiosPendientesProgressBarLinearLayout: LinearLayout
    lateinit var noHayIntercambiosPendientesLinearLayout: LinearLayout
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
        return inflater.inflate(R.layout.fragment_intercambios_pendientes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intercambiosPendientesLinearLayout = view.findViewById(R.id.intercambiosPendientesLinearLayout)
        noHayIntercambiosPendientesLinearLayout = view.findViewById(R.id.noHayIntercambiosPendientesLayout)
        intercambiosPendientesProgressBarLinearLayout = view.findViewById(R.id.intercambiosPendientesProgressBarLinearLayout)
        intercambiosPendientesRecyclerView = view.findViewById(R.id.intercambiosPendientesRecyclerView)
        intercambiosPendientesRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        intercambiosPendientesLinearLayout.visibility = View.GONE
        intercambiosPendientesProgressBarLinearLayout.visibility = View.VISIBLE
        //Obtenemos los intercambios pendientes del usuario actual
        obtenerIntercambio()
    }

    private fun obtenerIntercambio(){
        DatabaseService().obtenerIntercambiosPendientesEscucha(MainActivity.sharedPref.getString("uid", "")!!, "pendiente", object:
            IntercambioCallback {
            override fun onCallBack(intercambios: List<Intercambio>) {
                intercambiosPendientesProgressBarLinearLayout.visibility = View.GONE
                if(intercambios.isEmpty()){ //Si la lista de intercambios está vacía (o es nulo), se muestra por pantalla un texto indicando que no hay intercambios que mostrar
                    intercambiosPendientesRecyclerView.visibility = View.GONE
                    noHayIntercambiosPendientesLinearLayout.visibility = View.VISIBLE
                }else{
                    try{
                        noHayIntercambiosPendientesLinearLayout.visibility = View.GONE
                        intercambiosPendientesRecyclerView.visibility = View.VISIBLE
                        val adapterIntercambio = ListAdapterIntercambio(intercambios, requireContext())
                        intercambiosPendientesRecyclerView.adapter = adapterIntercambio
                    }catch (e: Exception){ //Es posible que haya alguna excepción, una de ellas es si cambias de pestañas muy rapido, en ese caso se pondría el texto de que no hay intercambios que mostrar. Pero si el usuario hace clikc de nuevo en esta pestaña, vuelven a cargar los intercambios
                        intercambiosPendientesRecyclerView.visibility = View.GONE
                        noHayIntercambiosPendientesLinearLayout.visibility = View.VISIBLE
                    }
                }
                intercambiosPendientesLinearLayout.visibility = View.VISIBLE
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
         * @return A new instance of fragment IntercambiosPendientes.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IntercambiosPendientes().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}