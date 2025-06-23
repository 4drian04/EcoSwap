package com.example.ecoswap

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import com.example.ecoswap.ProductosAIntercambiar.Companion.productoElegido
import com.example.ecoswap.callbacks.UsuarioCallBack
import com.example.ecoswap.modelos.Usuario
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.io.InputStream

class Utilities {


    fun getResizedBitmapFromUri(uri: Uri, width: Int, height: Int, contentResolver: ContentResolver): Bitmap? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            Bitmap.createScaledBitmap(originalBitmap, width, height, true)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Se guarda la imagen en caso de que se haya redimensionado
     */
    fun convertirAData(bitmap: android.graphics.Bitmap): ByteArray {
        // Crea un ByteArrayOutputStream para convertir el Bitmap a un array de bytes
        val baos = java.io.ByteArrayOutputStream()
        // Inicia la tarea de subida de los bytes a Firebase Storage
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, baos)
        // Convierte el contenido del ByteArrayOutputStream a un array de bytes
        val data = baos.toByteArray()
        return data
    }

    fun cambiarFotoPerfil(bitmap: android.graphics.Bitmap, esActualizarFoto: Boolean, context: Context, nombreUsuario: String, storage: FirebaseStorage){
        val storageRef = storage.reference.child("images/${nombreUsuario}.jpg")
        val data = convertirAData(bitmap)
        // Inicia la tarea de subida de los bytes a Firebase Storage
        val uploadTask = storageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                // Se obtiene el url de la imagen para guardarlo en la base de datos
                val downloadUrl = uri.toString()
                try {
                    DatabaseService().actualizarFotoPerfil(MainActivity.sharedPref.getString("uid", "")!!, downloadUrl){actualizado ->
                        if(esActualizarFoto){
                            if(actualizado){
                                Toast.makeText(context, "Se ha actualizado la foto de perfil correctamente", Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(context, "No se ha podido actualizado la foto de perfil. Vuelve a intentarlo", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    if (!esActualizarFoto){
                        pantalla_registro().urlFotoPerfil = downloadUrl
                    }
                    MainActivity.sharedPref.edit().putString("fotoPerfil", downloadUrl).apply();
                }catch(e: Exception){

                }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Error subiendo imagen: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
    /**
     * En caso de que si esté seguro, se registrará el intercambio en la base de datos
     */
    fun registrarIntercambio(productoId: String, usuarioId: String, context: Context, esProductoElegido: Boolean){
        var productoElegidoId: String?
        if(esProductoElegido){
            productoElegidoId = productoElegido!!.productoId
        }else{
            productoElegidoId = null
        }
        //Se guarda el intercambio en la base de datos
        DatabaseService().registrarIntercambio(MainActivity.sharedPref.getString("uid", "")!!, usuarioId, productoElegidoId, productoId)
        var mensaje = ""
        if(esProductoElegido){
            mensaje = MainActivity.sharedPref.getString("nombreUsuario", "")!! + " quiere intercambiar su " + productoElegido!!.nombre + " por otro producto de tu perfil"
        }else{
            mensaje = MainActivity.sharedPref.getString("nombreUsuario", "")!! + " quiere un producto de tu perfil por dinero"
        }
        //Se obtiene el token del otro usuario para enviarle una notificación
        DatabaseService().obtenerUsuario(usuarioId, object : UsuarioCallBack {
            override fun onCallback(usuario: Usuario?) {
                FCMNotificationService().enviarNotificacion(mensaje, usuario!!.token)
                DatabaseService().guardarNotificacion(usuarioId, MainActivity.sharedPref.getString("nombreUsuario", "")!!, mensaje)
            }
        })
        Toast.makeText(context, "Se ha realizado el intercambio correctamente", Toast.LENGTH_SHORT).show()
    }

}