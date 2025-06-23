package com.example.ecoswap.listAdapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoswap.PublicarProducto
import com.example.ecoswap.R

// Clase ListAdapterCategorias que extiende RecyclerView.Adapter para manejar una lista de categorías
class ListAdapterCategorias(
    private var listaCategorias: List<String>?, // Lista de categorías a mostrar
    private var esPublicarProducto: Boolean // Indica si proviene de la pantalla de publicar producto o no
) : RecyclerView.Adapter<ListAdapterCategorias.CategoriaViewHolder?>(){

    // Método que devuelve el número total de elementos en la lista de categorías
    override fun getItemCount(): Int {
        return listaCategorias!!.size
    }
    // Método que crea un nuevo ViewHolder para un elemento de la lista
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoriaViewHolder {
        val resource: Int
        // Selecciona el layout a utilizar según si se está en modo de publicación de producto
        if(esPublicarProducto){
            resource = R.layout.item_checkbox
        }else{
            resource = R.layout.activity_list_adapter_categorias
        }
        val view = LayoutInflater.from(parent.context)
            .inflate(resource, parent, false)
        return CategoriaViewHolder(view)
    }

    // Método que vincula los datos del elemento en la posición dada
    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoriaActual = listaCategorias!![position]
        holder.bindData(categoriaActual, esPublicarProducto)
    }
    class CategoriaViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val textoCategoira: TextView = itemView.findViewById(R.id.categoriaText)

        // Método que da valor al texto de la categoría y gestiona el comportamiento del checkbox si proviene de la pantalla de publicar producto
        fun bindData(item: String, esPublicarProducto: Boolean) {
            textoCategoira.setText(item)
            if(esPublicarProducto){
                val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_item)
                checkBox.setOnCheckedChangeListener {buttonView, isChecked ->
                    if(isChecked){
                        // Si el checkBox está marcado, añade la categoría a la lista de categorías seleccionadas
                        PublicarProducto.categoriasSeleccionadas.add(item)
                    }else{
                        // Si el CheckBox está desmarcado, elimina la categoría de la lista de categorías seleccionadas
                        PublicarProducto.categoriasSeleccionadas.remove(item)
                    }
                }
            }

        }
    }
}