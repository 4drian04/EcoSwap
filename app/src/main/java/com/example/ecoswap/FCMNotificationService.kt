package com.example.ecoswap

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class FCMNotificationService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "my_channel_id"
        const val NOTIFICATION_ID = 1
    }

    /**
     * Se gestiona cuando se recibe un mensaje para enviar al usuario la notificación
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Manejar la notificación
        remoteMessage.notification?.let {
            sendNotification(it.title, it.body)
        }
    }

    /**
     * Se envia la notificación al usuario
     */
    private fun sendNotification(title: String?, messageBody: String?) {
        // Crear el canal de notificación si es necesario
        createNotificationChannel()
        // Verificar si se tiene el permiso para enviar notificaciones
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setSmallIcon(R.drawable.ic_notifications)
                .setAutoCancel(true)
            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        } else {
            // Aquí puedes manejar el caso en que no se tiene el permiso
            Log.w("MyFirebaseMessagingService", "No se tiene permiso para enviar notificaciones.")
        }
    }

    /**
     * Crear el canal de notificación si es necesario
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My Channel"
            val descriptionText = "Channel for notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                val description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Envia una notificación remotaa al otro usuario cuando la aplicación está cerrada
     */
    fun enviarNotificacion(mensaje: String, token: String){
        val client = OkHttpClient()
        //Se construye el JSON para enviarlo a la función
        val json = JSONObject().apply {
            put("titulo", MainActivity.sharedPref.getString("nombreUsuario", "")!!)
            put("cuerpo", mensaje)
            put("token", token)
        }

        val body = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
        //Se envia la petición a la URL de la función
        val request = Request.Builder()
            .url("https://enviarnotificacion-apcd4ctfwq-uc.a.run.app\n")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FCM", "Error al enviar notificación", e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("FCM", "Respuesta: ${response.body?.string()}")
            }
        })
    }
}