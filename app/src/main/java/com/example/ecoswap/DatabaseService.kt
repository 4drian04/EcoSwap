package com.example.ecoswap

import com.example.ecoswap.modelos.Producto
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoswap.callbacks.ChatroomCallback
import com.example.ecoswap.callbacks.ChatsRecientesCallback
import com.example.ecoswap.callbacks.IntercambioCallback
import com.example.ecoswap.callbacks.NotificacionesCallBack
import com.example.ecoswap.callbacks.ProductoCallBack
import com.example.ecoswap.callbacks.ProductosCallBack
import com.example.ecoswap.callbacks.UsuarioCallBack
import com.example.ecoswap.callbacks.ValoracionCallback
import com.example.ecoswap.callbacks.ValoracionesCallback
import com.example.ecoswap.listAdapter.ListAdapterChat
import com.example.ecoswap.modelos.ChatMessage
import com.example.ecoswap.modelos.Chatroom
import com.example.ecoswap.modelos.Intercambio
import com.example.ecoswap.modelos.Notificacion
import com.example.ecoswap.modelos.Usuario
import com.example.ecoswap.modelos.Valoracion
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Locale

class DatabaseService {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    /**
     * Guarda el usuario en la base de datos
     */
    fun guardarUsuario(uid: String, nombreUsuario: String, urlFotoPerfil: String) {
        // Crea un objeto de usuario
        val usuario = Usuario(nombreUsuario, urlFotoPerfil, "")

        // Guarda el usuario en la base de datos bajo el nodo "usuarios" utilizando el uid como clave
        database.child("usuarios").child(uid).setValue(usuario)
    }

    /**
     * Obtiene el usuario con el uid pasado como parámetro
     */
    fun obtenerUsuario(uid: String, callback: UsuarioCallBack) {
        val usuariosRef = database.child("usuarios").child(uid)
        usuariosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Verificar si el usuario existe
                if (dataSnapshot.exists()) {
                    // Obtener los datos del usuario
                    val usuario = dataSnapshot.getValue(Usuario::class.java)
                    callback.onCallback(usuario) // Llama al callback con el usuario
                } else {
                    callback.onCallback(null) // Llama al callback con null
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onCallback(null) // Llama al callback con null en caso de error
            }
        })
    }

    /**
     * Actualiza el token del usuario que se ha logeado o registrado en la aplicación
     */
    fun actualizarTokenUsuario(token: String, uid: String){
        val tokenData = mapOf(
            "token" to token
        )
        database.child("usuarios").child(uid).updateChildren(tokenData);
    }

    /**
     * Cuando el usuario cierra sesión se elimina el token de la base de datos, ya que no tiene sentido que le sigan llegando notificaciones después de cerrar sesión
     */
    fun eliminarTokenUsuario(uid: String){
        val tokenData = mapOf(
            "token" to ""
        )
        database.child("usuarios").child(uid).updateChildren(tokenData);
    }

    /**
     * Guarda el producto en la base de datos
     */
    fun guardarProducto(url: String, nombreProducto: String, descripcionProducto: String, database:DatabaseReference, productId: String, latitud: Double, longitud: Double){
        val productData = mapOf(
            "nombre" to nombreProducto,
            "descripcion" to descripcionProducto,
            "fotoProducto" to url,
            "userId" to MainActivity.sharedPref.getString("uid", ""),
            "latitud" to latitud,
            "longitud" to longitud,
            "categorias" to PublicarProducto.categoriasSeleccionadas
        )
        database.child("Producto").child(productId).setValue(productData)
    }

    /**
     * Este método hace una operación asíncrona, en el que, cuando se llama en la clase correspondiente,
     * devuelve lo que le hayamos pasado como parámetro (en este caso devuelve una lista de productos)
     */
    fun obtenerProductosPorUsuario(userId: String, callback: (List<Producto>) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val productosRef = database.getReference("Producto")

        // Se hace la consulta filtrando por "userId"
        val query = productosRef.orderByChild("userId").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaProductos = mutableListOf<Producto>()
                for (productoSnapshot in snapshot.children) {
                    // Se parsea cada hijo a un objeto Producto
                    val producto = productoSnapshot.getValue(Producto::class.java)
                    if (producto != null) {
                        // Agrega el ID del producto al objeto Producto
                        producto.productoId = productoSnapshot.key ?: ""
                        listaProductos.add(producto)
                    }
                }
                // Retorna la lista filtrada por callback
                callback(listaProductos)
            }

            override fun onCancelled(error: DatabaseError) {
                //Si da algún error, se devuelve una lista vacía
                callback(emptyList())
            }
        })
    }

    /**
     * Se obtiene el producto de la base de datos con el id pasado como parámetro
     */
    fun obtenerProducto(productoId: String, callback: ProductoCallBack) {
        val productosRef = database.child("Producto").child(productoId)
        productosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Se obtiene los datos del producto
                    var producto = dataSnapshot.getValue(Producto::class.java)
                    producto!!.productoId = productoId
                    callback.onCallback(producto) // Llama al callback con el producto
                } else {
                    callback.onCallback(null) // Llama al callback con null si no existe
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onCallback(null) // Llama al callback con null en caso de error (se podría devolver una lista vacía como anteriormente)
            }
        })
    }

    /**
     * Se elimina el archivo según la ruta pasada como parámetro
     */
    fun eliminarArchivoFirebaseStorage(rutaArchivo: String) {
        // Se obtiene la instancia de Firebase Storage
        val storage = FirebaseStorage.getInstance()

        // Se obtiene la referencia al archivo que deseas eliminar
        // rutaArchivo es la ruta relativa dentro del storage, ej: "images/miimagen.jpg"
        val archivoRef = storage.reference.child(rutaArchivo)

        // Llama al método delete
        archivoRef.delete()
    }

    /**
     * Se elimina un producto según el id del producto pasado como parámetro
     */
    fun eliminarProductoFirebase(productoId: String) : Boolean {
        val ref = database.child("Producto").child(productoId)
        var esEliminadoCorrectamente = false
        ref.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) { //Si el producto es eliminado, se establece el valor booleano a true
                esEliminadoCorrectamente = true
                eliminarIntercambiosPorProducto(productoId)
            }
        }
        return  esEliminadoCorrectamente
    }

    /**
     * En caso de que se elimine un producto, también se eliminará los productos en lo que esté involucrado dicho producto
     */
    private fun eliminarIntercambiosPorProducto(productoId: String) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val intercambiosRef: DatabaseReference = database.getReference("Intercambios")
        intercambiosRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (intercambioSnapshot in snapshot.children) {
                    val estado = intercambioSnapshot.child("estado").getValue(String::class.java)
                    val productos = intercambioSnapshot.child("productos")
                    var productoEncontrado = false
                    // Verifica si el productoId está en los productos del intercambio
                    for (productoSnapshot in productos.children) {
                        val id = productoSnapshot.child("productoId").getValue(String::class.java)
                        if (id == productoId) {
                            productoEncontrado = true
                            break
                        }
                    }
                    // Si se encuentra el producto, eliminar el intercambio
                    if (productoEncontrado && estado == "pendiente") {
                        intercambiosRef.child(intercambioSnapshot.key!!).removeValue()
                    }
                }
            }
        }
    }

    /**
     * Se obtiene los productos que el usuario puede ver en el menú principal
     */
    fun obtenerProductosMenuPrincipal(apikey: String, callback: ProductosCallBack) {
        val productosRef = database.child("Producto")
        productosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productosFiltrados = mutableListOf<Producto>()
                var productosContados = 0
                val totalProductos = dataSnapshot.childrenCount
                for (productoSnapshot in dataSnapshot.children) {
                    val producto = productoSnapshot.getValue(Producto::class.java)
                    if (producto != null && producto.userId != apikey) {
                        producto.productoId = productoSnapshot.key ?: ""
                        //Esto nos sirve para mostrar los productos que no estén invlucrados en un intercambio con dicho usuario
                        buscarIntercambioPorProductoYUsuario(producto.productoId, apikey) { encontrado ->
                            if (!encontrado) {
                                productosFiltrados.add(producto)
                            }
                            productosContados++
                            // Verifica si se han procesado todos los productos
                            if (productosContados == totalProductos.toInt()) {
                                callback.onCallback(productosFiltrados)
                            }
                        }
                    } else {
                        productosContados++
                        // Verifica si se han procesado todos los productos
                        if (productosContados == totalProductos.toInt()) {
                            callback.onCallback(productosFiltrados)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onCallback(null) // Llama al callback con null en caso de error
            }
        })
    }

    /**
     * Busca si existe algún intercambio con el uid del producto pasado como parámetro, así como el uid o apikey del usuario
     */
    fun buscarIntercambioPorProductoYUsuario(
        productoId: String,
        apiKey: String,
        callback: (Boolean) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance()
        val intercambiosRef = database.getReference("Intercambios")
        intercambiosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var encontrado = false
                for (intercambioSnapshot in dataSnapshot.children) {
                    val comprador = intercambioSnapshot.child("comprador").getValue(String::class.java)
                    val vendedor = intercambioSnapshot.child("vendedor").getValue(String::class.java)
                    val productos = intercambioSnapshot.child("productos")
                    // Verifica si el producto está en el intercambio
                    for (productoSnapshot in productos.children) {
                        val idProducto = productoSnapshot.child("productoId").getValue(String::class.java)
                        if (idProducto == productoId) {
                            // Verificar si el comprador o vendedor es el usuario
                            if (comprador == apiKey || vendedor == apiKey) {
                                encontrado = true
                                break
                            }
                        }
                    }

                    if (encontrado) break
                }

                // Llamar al callback con el resultado
                callback(encontrado)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error al acceder a la base de datos: ${databaseError.message}")
                // Llamar al callback con false en caso de error
                callback(false)
            }
        })
    }


    /**
     * Obtiene los productos filtrados, según los filtros pasado como parámetros
     */
    fun obtenerProductosFiltro(apikey: String, filtro: String?, categorias: ArrayList<String>, callback: (List<Producto>) -> Unit) {
        val productosRef = database.child("Producto")
        productosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productosFiltrados = mutableListOf<Producto>()
                var productosContados = 0
                val totalProductos = dataSnapshot.childrenCount
                for (productoSnapshot in dataSnapshot.children) {
                    val producto = productoSnapshot.getValue(Producto::class.java)
                    //Comprueba si el filtro de busqueda es distinto de null, en caso de ser distinto de null, comprueba los productos que existen con dicho nombre.
                    //Luego se comprueba las categorías. Si existe algún producto con dichos filtros se añade a la lista
                    var filtroLower: String? = null
                    if(filtro!=null){
                        filtroLower = filtro.lowercase(Locale.getDefault())
                    }
                    if(producto!=null){
                        val nombreProductoLower = producto.nombre.lowercase()
                        if (producto.userId != apikey && ((((filtroLower!=null) && nombreProductoLower.contains(filtroLower)) || filtro == null) && ((contieneCategoria(categorias, producto.categorias))))) {
                            producto.productoId = productoSnapshot.key ?: ""
                            buscarIntercambioPorProductoYUsuario(producto.productoId, apikey){encontrado ->
                                if (!encontrado){
                                    productosFiltrados.add(producto)
                                }
                                productosContados++
                                // Verifica si se han procesado todos los productos
                                if (productosContados == totalProductos.toInt()) {
                                    callback(productosFiltrados)
                                }
                            }
                        }else{
                            productosContados++
                            if (productosContados == totalProductos.toInt()) {
                                callback(productosFiltrados)
                            }
                        }
                    }else{
                        productosContados++
                        if (productosContados == totalProductos.toInt()) {
                            callback(productosFiltrados)
                        }
                    }
                }
                // Llama al callback con la lista de productos filtrados
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de errores
                callback(emptyList()) // Llama al callback con una lista vacía en caso de error
            }
        })
    }

    /**
     * Comprueba si la categoría de los productos tienen alguna de las categorías seleccionadas por el usuario
     */
    private fun contieneCategoria(categoriasSeleccionadas: ArrayList<String>, categoriasProducto: ArrayList<String>): Boolean{
        var contieneCategoria = false
        if(categoriasSeleccionadas.isEmpty()){
            contieneCategoria = true
        }else{
            for(categoria in categoriasSeleccionadas){
                if(categoriasProducto.contains(categoria)){ //Si la categoría de los productos contiene alguna de las categorias seleccionadas por el usuario, se devuelve true
                    contieneCategoria = true
                }
            }
        }
        return contieneCategoria
    }

    /**
     * Registra el intercambio en la base de datos
     */
    fun registrarIntercambio(compradorUid: String, vendedorUid: String, productoComprador: String?, productoVendedor: String){
        val intercambioId = database.child("Intercambios").push().key
        val databaseReference = database.child("Intercambios").child(intercambioId!!)
        databaseReference.child("vendedor").setValue(vendedorUid)
        databaseReference.child("vendedorVistoBueno").setValue(false)
        databaseReference.child("comprador").setValue(compradorUid)
        databaseReference.child("compradorVistoBueno").setValue(false)
        databaseReference.child("estado").setValue("pendiente")
        if(productoComprador!=null){ //Si el producto del comprador es distinto de null, quiere decir que ha elegido un producto para intercambiar, por lo que obtenemos el producto y lo guardamos en el intercambio
            obtenerProducto(productoComprador, object : ProductoCallBack {
                override fun onCallback(producto: Producto?) {
                    producto!!.productoId = productoComprador
                    databaseReference.child("productos").child(productoComprador).setValue(producto)
                }
            })
        }
        //Guardamos el producto del vendedor en la base de datos (hay que tener en cuenta que el vendedor siempre tendrá un producto, ya que es el que publica el producto)
        obtenerProducto(productoVendedor, object: ProductoCallBack {
            override fun onCallback(producto: Producto?) {
                producto!!.productoId = productoVendedor
                databaseReference.child("productos").child(productoVendedor).setValue(producto)
            }
        })
    }

    /**
     * Se obtienen los intercambios del usuario pasado como parámetro y según el estado pasado como parámetro
     */
    fun obtenerIntercambiosPendientes(uid: String, estado: String, callback: IntercambioCallback){
        val intercambioRef = database.child("Intercambios")
        intercambioRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val intercambiosList = mutableListOf<Intercambio>()
                for (intercambioSnapshot in snapshot.children) {
                    val intercambio = intercambioSnapshot.getValue(Intercambio::class.java)
                    if (intercambio != null) {
                        intercambio.intercambioId = intercambioSnapshot.key ?: ""
                        if ((intercambio.comprador == uid || intercambio.vendedor == uid) && intercambio.estado == estado) { //Si el uid del comprador o vendedor coincide con el uid pasado como parámetro y además coincide con el estado pasado como parámetro, se añade a la lista
                            intercambiosList.add(intercambio)
                        }
                    }
                }
                callback.onCallBack(intercambiosList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onCallBack(emptyList())
            }
        })
    }

    /**
     * Se obtienen los intercambios del usuario pasado como parámetro y según el estado pasado como parámetro
     */
    fun obtenerIntercambiosPendientesEscucha(uid: String, estado: String, callback: IntercambioCallback) {
        val intercambioRef = database.child("Intercambios")

        // Usamos un ChildEventListener para escuchar cambios en tiempo real
        intercambioRef.addChildEventListener(object : ChildEventListener {
            private val intercambiosList = mutableListOf<Intercambio>()
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val intercambio = dataSnapshot.getValue(Intercambio::class.java)
                if (intercambio != null) {
                    intercambio.intercambioId = dataSnapshot.key ?: ""
                    if ((intercambio.comprador == uid || intercambio.vendedor == uid) && intercambio.estado == estado) {
                        intercambiosList.add(intercambio)
                    }
                }
                callback.onCallBack(intercambiosList)// Actualiza la lista al agregar
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val intercambio = dataSnapshot.getValue(Intercambio::class.java)
                if (intercambio != null) {
                    intercambio.intercambioId = dataSnapshot.key ?: ""
                    // Verifica si el estado ha cambiado a "realizado"
                    if (intercambio.estado == "realizado") {
                        // Elimina el intercambio de la lista si existe
                        val index = intercambiosList.indexOfFirst { it.intercambioId == intercambio.intercambioId }
                        if (index != -1) {
                            intercambiosList.removeAt(index) // Elimina el intercambio de la lista
                        }
                    } else {
                        // Actualiza el intercambio en la lista si ya existe
                        val index = intercambiosList.indexOfFirst { it.intercambioId == intercambio.intercambioId }
                        if (index != -1) {
                            intercambiosList[index] = intercambio
                        } else if ((intercambio.comprador == uid || intercambio.vendedor == uid) && intercambio.estado == estado) {
                            intercambiosList.add(intercambio)
                        }
                    }
                }
                callback.onCallBack(intercambiosList) // Actualiza la lista al cambiar
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val intercambioId = dataSnapshot.key
                // Elimina el intercambio de la lista si existe
                intercambiosList.removeAll { it.intercambioId == intercambioId }
                callback.onCallBack(intercambiosList) // Actualiza la lista al eliminar
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // Este método se puede dejar vacío si no necesitas manejar el movimiento de hijos
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onCallBack(emptyList()) // Maneja el error
            }
        })
    }


    /**
     * Se elimina el intercambio según el uid pasado como parámetro de la base de dato
     */
    fun eliminarIntercambio(intercambioId: String, callback: (Boolean) -> Unit) {
        val intercambiosRef = database.child("Intercambios").child(intercambioId)
        intercambiosRef.removeValue()
            .addOnSuccessListener {
                // El intercambio se eliminó correctamente
                callback(true) // Llama al callback con éxito
            }
            .addOnFailureListener { error ->
                callback(false) // Llama al callback con error
            }
    }

    /**
     * Se actualiza el visto bueno del vendedor o comprador
     * @param compradorVendedor Si el visto bueno lo hace el vendedor, será vendedorVistoBueno, en otro caso será compradorVistoBueno
     * @param intercambioId Es el uid del intercambio
     */
    fun actualizarVistoBueno(intercambioId: String, compradorVendedor: String, callback: (Boolean) -> Unit){
        val intercambiosRef = database.child("Intercambios").child(intercambioId)
        intercambiosRef.child(compradorVendedor).setValue(true)
            .addOnSuccessListener {
                callback(true) // Llama al callback con éxito
            }
            .addOnFailureListener { error ->
                callback(false) // Llama al callback con error (false)
            }
    }

    fun comprobarVistoBueno(uidIntercambio: String, esComprador: Boolean, callback: (Boolean) -> Unit){
        var vistoBueno = ""
        if(esComprador){
            vistoBueno = "vendedorVistoBueno"
        }else{
            vistoBueno = "compradorVistoBueno"
        }
        database.child("Intercambios").child(uidIntercambio).child(vistoBueno).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val compradorVistoBueno = dataSnapshot.getValue(Boolean::class.java)
                if (compradorVistoBueno != null) {
                    callback(compradorVistoBueno)
                } else {
                    callback(false)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }

    /**
     * En caso de que ambos hayan dado el visto bueno, se actualizará el estado general del intercambio, dandolo como realizado
     * @param intercambioId uid del intercambio
     */
    fun actualizarEstado(intercambioId: String, callback: (Boolean) -> Unit){
        val intercambiosRef = database.child("Intercambios").child(intercambioId)
        intercambiosRef.child("estado").setValue("realizado")
            .addOnSuccessListener {
                callback(true) // Llama al callback con éxito
            }
            .addOnFailureListener { error ->
                callback(false) // Llama al callback con error
            }
    }

    /**
     * Obtiene la referencia del chatroom, usando el id de ambos usuarios
     * @param chatroomId uid del chatroom
     * @param userId1 uid de uno de los usuarios
     * @param userId2 uid del otro usuario
     *
     */
    fun getChatroomReference(chatroomId: String, userId1: String, userId2: String, callback: ChatroomCallback){
        var chatroom: Chatroom? = null
        val chatroomRef = database.child("chatrooms").child(chatroomId)
        chatroomRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Obtener los datos del chatroom
                    chatroom = dataSnapshot.getValue(Chatroom::class.java)
                } else { //Si no existe el chatroom, se crea uno
                    chatroom = crearChatroom(chatroomId, userId1, userId2)
                }
                callback.onCallBack(chatroom!!)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    /**
     * Se obtiene el id del chatroom, de esta manera el id siempre será el mismo para un mismo chat
     * @param userId1 uid de un usuario
     * @param userId2 uid del otro usuario
     */
    fun getChatroomId(userId1: String, userId2: String): String{
        var chatroomId: String
        if(userId1.hashCode()<userId2.hashCode()){
            chatroomId = userId1 + "_" + userId2
        }else{
            chatroomId = userId2 + "_" + userId1
        }
        return chatroomId
    }

    /**
     * Se crea el chaatroom a partir del uid del chatroom y los uid de los usuarios
     * @param chatroomId uid del chatroom
     * @param userId1 uid de un usuario
     * @param userId2 uid del otro usuario
     */
    fun crearChatroom(chatroomId: String, userId1: String, userId2: String) : Chatroom {
        val lista: MutableList<String> = mutableListOf()
        lista.add(userId1)
        lista.add(userId2)
        var chatroom = Chatroom(chatroomId, lista, System.currentTimeMillis(), "", "")
        database.child("chatrooms").child(chatroomId).setValue(chatroom)
        return chatroom
    }

    /**
     * Permite enviar un mensaje a un usuario
     * @param chatroom Es el chatroom en el que se encuentra el usuario
     * @param mensaje Es el mensaje que el usuario va a envíar
     * @param uid es el uid del usuario que envía el mensaje
     */
    fun sendMessage(chatroom: Chatroom, mensaje: String, uid: String){
        chatroom.ultimoMensajeTimestamp = System.currentTimeMillis()
        chatroom.ultimoMensajeRemitenteId = uid
        //Se actualiza la hora del útlimo mensaje y el uid de la persona que envía el mensaje
        database.child("chatrooms").child(chatroom.chatroomId).updateChildren(mapOf("ultimoMensajeRemitenteId" to uid, "ultimoMensajeTimestamp" to System.currentTimeMillis()))
        val mensajeId = database.child("chatrooms").child(chatroom.chatroomId).child("chats").push().key
        val chatMessage = ChatMessage(mensajeId!!,mensaje, uid, System.currentTimeMillis()) //Se crea el objeto del mensaje
        database.child("chatrooms").child(chatroom.chatroomId).child("chats").child(mensajeId).setValue(chatMessage)
        database.child("chatrooms").child(chatroom.chatroomId).child("ultimoMensaje").setValue(mensaje)
    }

    /**
     * Se actualiza el chat cuando un usuario envía algún mensaje
     * @param adapterChat Es el ListAdapter del chat en el que se encuentra el usuario
     * @param chatroomId Es el uid del chatroom en el que se encuentra el usuario
     * @param recyclerView Es el recycler view del chat en el que se encuentra el usuario
     */
    fun actualizarChat(adapterChat: ListAdapterChat, chatroomId: String, recyclerView: RecyclerView){
        val chatMessagesRef = database.child("chatrooms").child(chatroomId).child("chats")
        chatMessagesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                chatMessage?.let { //Cuando se escribe un mensaje, se agrega a la base de datos y se añade en el listAdapter para que se muestre por pantalla
                    database.child("chatrooms").child(chatroomId).child("ultimoMensaje").setValue(it.mensaje)
                    adapterChat.addMensaje(it)
                    recyclerView.smoothScrollToPosition(0) // Desplazar hacia arriba
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }
        })
    }

    /**
     * Se obtiene los chats recientes del usuario pasado como parámetro
     * @param uid Es el uid del usuario que se quiere obtener los chats
     */
    fun getChatsRecientes(uid: String, callback: ChatsRecientesCallback){
        val chatsRecientesRef = database.child("chatrooms")
        chatsRecientesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val chatroomsList = mutableListOf<Chatroom>()
                for (snapshot in dataSnapshot.children) {
                    val chatroom = snapshot.getValue(Chatroom::class.java)
                    // Verifica si el usuario está en el chatroom
                    if (chatroom != null && chatroom.userIds.contains(uid)) {
                        chatroom.chatroomId = snapshot.key ?: ""
                        chatroomsList.add(chatroom)
                    }
                }
                // Ordena los chatrooms por el timestamp del último mensaje
                val sortedChatrooms = chatroomsList.sortedByDescending { it.ultimoMensajeTimestamp }
                callback.onCallBack(sortedChatrooms)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onCallBack(emptyList())
            }
        })
    }

    fun getChatsRecientesEscucha(uid: String, callback: ChatsRecientesCallback) {
        val chatsRecientesRef = database.child("chatrooms")

        // Usamos un ChildEventListener para escuchar cambios en tiempo real
        chatsRecientesRef.addChildEventListener(object : ChildEventListener {
            private val chatroomsList = mutableListOf<Chatroom>()

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val chatroom = dataSnapshot.getValue(Chatroom::class.java)
                // Verifica si el usuario está en el chatroom
                if (chatroom != null && chatroom.userIds.contains(uid)) {
                    chatroom.chatroomId = dataSnapshot.key ?: ""
                    chatroomsList.add(chatroom)
                }
                // Ordena los chatrooms por el timestamp del último mensaje y llama al callback
                val sortedChatrooms = chatroomsList.sortedByDescending { it.ultimoMensajeTimestamp }
                callback.onCallBack(sortedChatrooms)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val chatroom = dataSnapshot.getValue(Chatroom::class.java)
                if (chatroom != null) {
                    chatroom.chatroomId = dataSnapshot.key ?: ""
                    // Actualiza el chatroom en la lista si ya existe
                    val index = chatroomsList.indexOfFirst { it.chatroomId == chatroom.chatroomId }
                    if (index != -1) {
                        chatroomsList[index] = chatroom
                    } else if (chatroom.userIds.contains(uid)) {
                        chatroomsList.add(chatroom)
                    }
                }
                // Ordena los chatrooms por el timestamp del último mensaje y llama al callback
                val sortedChatrooms = chatroomsList.sortedByDescending { it.ultimoMensajeTimestamp }
                callback.onCallBack(sortedChatrooms)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val chatroomId = dataSnapshot.key
                // Elimina el chatroom de la lista si existe
                chatroomsList.removeAll { it.chatroomId == chatroomId }
                // Ordena los chatrooms por el timestamp del último mensaje y llama al callback
                val sortedChatrooms = chatroomsList.sortedByDescending { it.ultimoMensajeTimestamp }
                callback.onCallBack(sortedChatrooms)
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // Este método se puede dejar vacío si no necesitas manejar el movimiento de hijos
            }

            override fun onCancelled(error: DatabaseError) {
                // Si hay un error, llama al callback con una lista vacía
                callback.onCallBack(emptyList())
            }
        })
    }


    /**
     * Se envía la valoración del usuario y se guarda en la base de datos
     * @param uidUsuario uid del usuario que envía la valoración
     * @param uidOtroUsuario uid del usuario que recibe la valoración
     * @param descripcionValoracion Es la descripción de la valoración que escribe el usuario
     * @param estrellas Estrellas que le ha puesto al usuario
     */
    fun enviarValoracionUsuario(uidUsuario: String, uidOtroUsuario: String, descripcionValoracion: String, estrellas: Float): String{
        val valoracion = EnvioValoracion(descripcionValoracion, estrellas)
        var respuesta: String = ""
        //Se añade a la base de datos y se envía una respuesta u otra según el resultado (la respuesta es para mostrarla posteriormente en un Toast)
        database.child("usuarios").child(uidOtroUsuario).child("valoraciones").child(uidUsuario).setValue(valoracion).addOnSuccessListener {
            respuesta = "La valoracion se ha registrado correctamente. Muchas gracias por tu colaboración"
        }.addOnFailureListener {
            respuesta = "No se ha podido guardar la valoración. Vuelve a intentarlo"
        }
        return  respuesta
    }

    /**
     * Se obtiene las valoraciones del usuario indicado por parámetro
     * @param uid uid del usuario del que se obtiene las valoraciones
     */
    fun getValoracionesUsuario(uid: String, callback: ValoracionesCallback){
        val valoracionesRef = database.child("usuarios").child(uid).child("valoraciones")
        valoracionesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val valoracionesList = mutableListOf<Valoracion>()
                for (snapshot in dataSnapshot.children) {
                    val valoracion = snapshot.getValue(Valoracion::class.java)
                    if (valoracion != null) {
                        valoracion.uid = snapshot.key?: ""
                        valoracionesList.add(valoracion)
                    }
                }
                callback.onCallback(valoracionesList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onCallback(emptyList())
            }
        })
    }

    /**
     * Esté método nos sirve para comprobar si un usuario ha valorado a otro usuario, esto evita el famoso "Review Bombing", que es que un mismo usuario pueda valorar a un usuario muchas veces
     * @param uidUsuarioAValorar Es el usuario que recibiría la valoración
     * @param uidUsuarioRemitente Es el usuario que escribiría la valoración
     */
    fun comprobarValoracionUsuario(uidUsuarioAValorar: String, uidUsuarioRemitente: String, callback: ValoracionCallback){
        val valoracionesRef = database.child("usuarios").child(uidUsuarioAValorar).child("valoraciones").child((uidUsuarioRemitente))
        valoracionesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val valoracion = dataSnapshot.getValue(Valoracion::class.java)
                if (valoracion != null) {
                    valoracion.uid = dataSnapshot.key?: ""
                    callback.onCallback(valoracion)
                }else{
                    callback.onCallback(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onCallback(null)
            }
        })
    }

    /**
     * Cuando el usuario envia una notificación (ya sea envío de mensajes, cancelar un intercambio...) se guarda la notificación al otro usuario para que pueda ver las notificaciones en la aplicació
     * @param uid uid del usuario que recibe la notificación
     * @param titulo título de la notificación
     * @param cuerpo cuerpo de la notificación
     */
    fun guardarNotificacion(uid: String, titulo: String, cuerpo: String) {
        val notificacion = Notificacion(titulo, cuerpo)
        notificacion.notificacionId = database.child("usuarios").child(uid).child("notificaciones").push().key!!;
        database.child("usuarios").child(uid).child("notificaciones").child(notificacion.notificacionId).child("titulo").setValue(notificacion.titulo)
    }

    /**
     * El usuario puede eliminar sus notficaciones si desea
     * @param uidNotificacion Es el uid de la notificación a borrar
     * @param uidUsuario Es el uis del usuario que va a borrar la notificación
     */
    fun eliminarNotificacion(uidNotificacion: String, uidUsuario: String){
        database.child("usuarios").child(uidUsuario).child("notificaciones").child(uidNotificacion).removeValue()
    }

    /**
     * Se obtiene las notificaciones del usuario pasado como parámetro
     * @param uid uid del usuario para obtener las notificaciones
     */
    fun getNotificacionesUsuario(uid: String, callBack: NotificacionesCallBack){
        val valoracionesRef = database.child("usuarios").child(uid).child("notificaciones")
        valoracionesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val notificacionesList = ArrayList<Notificacion>()
                for (snapshot in dataSnapshot.children) {
                    val notificacion = snapshot.getValue(Notificacion::class.java)
                    if (notificacion != null) {
                        notificacion.notificacionId = snapshot.key?: ""
                        notificacionesList.add(notificacion)
                    }
                }
                callBack.onCallback(notificacionesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Valoracion", "Error al leer las valoraciones.", error.toException())
            }
        })
    }

    /**
     * El usuario tendrá la posibilidad de actualizar su foto de perfil, guardando en la base de datos la url de la foto
     * @param uid uid del usuario que quiere actualizar su perfil
     * @param urlFotoPerfil url de la foto que ha elegido el usuario
     */
    fun actualizarFotoPerfil(uid: String, urlFotoPerfil: String,  callback: (Boolean) -> Unit){
        database.child("usuarios").child(uid).child("urlFotoPerfil").setValue(urlFotoPerfil).addOnSuccessListener {
            callback(true)
        }.addOnFailureListener {
            callback(false)
        }
    }
}
