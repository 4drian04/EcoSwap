package com.example.ecoswap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.ecoswap.PublicarProducto
import com.example.ecoswap.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class UbicacionProducto : FragmentActivity(), OnMapReadyCallback {
    var latitud: Double? = null
    var longitud: Double? = null
    private var mMap: GoogleMap? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    lateinit var confirmarUbicacionProductoButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubicacion_producto)

        // Instancia de FusedLocationProviderClient, que es la API de Google para obtener ubicaciones de forma eficiente.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Se gestiona el fragmento de pantalla en el que se ubicará nuestro mapa.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        confirmarUbicacionProductoButton = findViewById(R.id.confirmarUbicacion)
        if(intent.getBooleanExtra("esPublicarProducto", false)){
            confirmarUbicacionProductoButton.setOnClickListener { //Cuando se confirme la ubicación, se obtiene la latitud y longitud, y se vuelve a la anterior pantalla
                if(latitud != null && longitud != null){
                    PublicarProducto.latitud = latitud;
                    PublicarProducto.longitud = longitud
                    //PROBAR LO DE LA LOCALIZACION QUITANDO ESTO
                    //latitud = null
                    //longitud = null
                    finish()
                }else{
                    Toast.makeText(this, "Debes seleccionar un lugar de preferencia para el producto", Toast.LENGTH_LONG).show()
                }
            }
        }else{ //Si proviene de ver el producto de otro usuario, se obtiene la latitud y longitud del prodcuto
            latitud = intent.getDoubleExtra("latitud", 0.0);
            longitud = intent.getDoubleExtra("longitud", 0.0);
            confirmarUbicacionProductoButton.visibility = View.GONE
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Nuestro mapa será el mapa generado por Google
        mMap = googleMap

        // Configurar tipo de mapa
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Añadir marcador con la posición del usuario.
        addUserPositionMarker()

        val esPublicarProducto = intent.getBooleanExtra("esPublicarProducto", false);
        // Habilitar clics en los marcadores
        if(!esPublicarProducto){
            addMarkers()
            mMap!!.setOnMarkerClickListener { marker ->
                // Acción al seleccionar un marcador
                marker.showInfoWindow()
                false // Si es false, mantiene el comportamiento predeterminado
            }
        }else{
            if(longitud!= null && latitud != null){
                addMarkers()
            }
            // Habilitar clics en el mapa para añadir marcadores
            mMap!!.setOnMapClickListener { latLng ->
                // Añadir un marcador en la ubicación clicada
                mMap!!.clear()
                mMap!!.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Marcador añadido")
                )
                latitud = latLng.latitude
                longitud = latLng.longitude
            }
        }
    }

    private fun addMarkers() {
        val ubicacion = LatLng(latitud!!, longitud!!)
        mMap!!.addMarker(MarkerOptions().position(ubicacion).title("Ubicacion Preferente"))
    }

    private fun addUserPositionMarker() {
        // Solicitud de permisos en tiempo de ejecución
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }

        // Listener de actualización de posición: cada 2 segundos
        val locationRequest: LocationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build()

        // Gestión de la respuesta del servicio de ubicación.
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                if (p0 == null) {
                    return
                }
                for (location in p0.getLocations()) {
                    if (location != null) {
                        // Si todo va correctamente, posicionamos el avatar sobre nuestra posición en el mapa
                        // y hacemos un zoom medio.
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        mMap!!.addMarker(
                            MarkerOptions()
                                .position(userLatLng)
                                .title("¡Aquí estás!")
                                .icon(
                                    bitmapDescriptorFromVector(
                                        this@UbicacionProducto,
                                        R.drawable.ic_profile
                                    )
                                )
                        )

                        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 8f))
                        fusedLocationClient!!.removeLocationUpdates(this) // Detener actualizaciones para ahorrar batería
                        break
                    }
                }
            }
        }

        // Se establece la petición de ubicación, la gestión de la respuesta y se define la ejecución automática.
        // fusedLocationClient → Es una instancia de FusedLocationProviderClient, que es la API de Google para obtener ubicaciones de forma eficiente.
        // requestLocationUpdates(...) → Es el método que solicita actualizaciones de ubicación basadas en los parámetros que le pasamos.
        fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    /**
     * Gestión de la respuesta de los permisos solicitados en tiempo de ejecución.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @param deviceId
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Verifica si la respuesta es para el permiso de ubicación..
        if (requestCode == LOCATION_REQUEST_CODE) {
            // Comprueba si el permiso fue concedido.
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el permiso fue concedido, añade el marcador del usuario.
                addUserPositionMarker()
            } else {
                // Si el permiso fue denegado, muestra un mensaje informativo al usuario.
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Este método transforma un .png en un bitmap
     *
     * @param context
     * @param vectorResId
     * @return
     */
    private fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorResId: Int
    ): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return null

        vectorDrawable.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    companion object {
        private const val LOCATION_REQUEST_CODE = 1001
    }
}