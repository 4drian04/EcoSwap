package com.example.ecoswap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoswap.callbacks.ValoracionesCallback
import com.example.ecoswap.listAdapter.ListAdapterValoracion
import com.example.ecoswap.modelos.Valoracion

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ValoracionUsuario.newInstance] factory method to
 * create an instance of this fragment.
 */
class ValoracionUsuario : Fragment() {
    lateinit var valoracionesUsuarioRecyclerView: RecyclerView
    lateinit var valoracionProgressBarLinearLayout: LinearLayout
    lateinit var noHayValoracionLinearLayout: LinearLayout
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
        return inflater.inflate(R.layout.fragment_valoracion_usuario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noHayValoracionLinearLayout = view.findViewById(R.id.noHayValoracionLayout)
        valoracionesUsuarioRecyclerView = view.findViewById(R.id.valoracionUsuario)
        valoracionesUsuarioRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        valoracionProgressBarLinearLayout = view.findViewById(R.id.progressBarValoracion)
        valoracionesUsuarioRecyclerView.visibility = View.GONE
        valoracionProgressBarLinearLayout.visibility = View.VISIBLE
        //Se obtiene las valoraciones del usuario con el uid correspondiente y se muestra por pantalla
        DatabaseService().getValoracionesUsuario(uid, object : ValoracionesCallback {
            override fun onCallback(valoraciones: List<Valoracion>) {
                if(valoraciones.isEmpty()){
                    valoracionesUsuarioRecyclerView.visibility = View.GONE
                    noHayValoracionLinearLayout.visibility = View.VISIBLE
                }else{
                    try{
                        val valoracionAdapter = ListAdapterValoracion(valoraciones, requireContext())
                        valoracionesUsuarioRecyclerView.adapter = valoracionAdapter
                        valoracionesUsuarioRecyclerView.visibility = View.VISIBLE
                    }catch (e: Exception){
                        valoracionesUsuarioRecyclerView.visibility = View.GONE
                        noHayValoracionLinearLayout.visibility = View.VISIBLE
                    }
                }
                valoracionProgressBarLinearLayout.visibility = View.GONE

            }
        })

    }

    companion object {
        lateinit var uid: String
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ValoracionUsuario.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ValoracionUsuario().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}