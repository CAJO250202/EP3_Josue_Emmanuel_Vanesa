package com.example.ep2

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide

class DetailsActivity : AppCompatActivity() {

    private lateinit var imgDetalleReceta: ImageView
    private lateinit var btnFavorito: ImageButton
    private lateinit var tvTituloDetalle: TextView
    private lateinit var tvTiempoDetalle: TextView
    private lateinit var tvPorcionesDetalle: TextView
    private lateinit var tvResumenDetalle: TextView

    private lateinit var db: DataBaseHelper
    private var recetaFavorita = false
    private val CHANNEL_ID_FAV = "ep2_favoritos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)

        imgDetalleReceta = findViewById(R.id.imgDetalleReceta)
        btnFavorito = findViewById(R.id.btnFavorito)
        tvTituloDetalle = findViewById(R.id.tvTituloDetalle)
        tvTiempoDetalle = findViewById(R.id.tvTiempoDetalle)
        tvPorcionesDetalle = findViewById(R.id.tvPorcionesDetalle)
        tvResumenDetalle = findViewById(R.id.tvResumenDetalle)
        db = DataBaseHelper(this)

        crearCanalNotificacionesFavoritos()
        mostrarInformacionReceta()
    }

    // Método para mostrar la información de una receta
    private fun mostrarInformacionReceta() {
        val idReceta = intent.getIntExtra("ID_RECETA", 0)
        val nombreReceta = intent.getStringExtra("NOMBRE_RECETA") ?: "Receta sin nombre"
        val tiempo = intent.getIntExtra("TIEMPO_RECETA", 0)
        val porciones = intent.getIntExtra("PORCIONES_RECETA", 0)
        val imagen = intent.getStringExtra("IMAGEN_RECETA") ?: ""

        tvTituloDetalle.text = nombreReceta
        tvTiempoDetalle.text = "Tiempo: $tiempo min"
        tvPorcionesDetalle.text = "Porciones: $porciones"
        tvResumenDetalle.text = "Receta seleccionada del API de Spoonacular con ID: $idReceta.\nDisfruta preparando este delicioso platillo siguiendo los ingredientes y pasos recomendados."

        if (imagen.isNotEmpty()) {
            Glide.with(this)
                .load(imagen)
                .into(imgDetalleReceta)
        }

        // Verificar si la receta existe como favorita
        if (idReceta != 0) {
            recetaFavorita = db.esFavorito(idReceta)
        }

        if (recetaFavorita) {
            btnFavorito.setImageResource(R.drawable.favorito)
        } else {
            btnFavorito.setImageResource(R.drawable.no_favorito)
        }

        btnFavorito.setOnClickListener {
            if (recetaFavorita) {
                val eliminar = db.eliminarFavorito(idReceta)

                if (eliminar) {
                    recetaFavorita = !recetaFavorita
                    btnFavorito.setImageResource(R.drawable.no_favorito)
                    Toast.makeText(this, "Receta $nombreReceta eliminada de favoritos", Toast.LENGTH_SHORT).show()
                }
            } else {
                val guardado = db.guardarRecetaFavorita(
                    recetaId = idReceta,
                    titulo = nombreReceta,
                    tiempo = tiempo,
                    porciones = porciones,
                    imagen = imagen
                )

                if (guardado) {
                    recetaFavorita = !recetaFavorita
                    btnFavorito.setImageResource(R.drawable.favorito)
                    Toast.makeText(this, "Receta $nombreReceta agregada a favoritos", Toast.LENGTH_SHORT).show()
                    verificarPermisosYNotificar(nombreReceta)
                } else {
                    Toast.makeText(this, "Receta $nombreReceta ya se encuentra agregada a favoritos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // MÉTODOS PARA NOTIFICACIÓN DE FAVORITOS
    private fun crearCanalNotificacionesFavoritos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Favoritos"
            val descriptionText = "Notificaciones al agregar recetas a favoritos"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID_FAV, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun verificarPermisosYNotificar(nombreReceta: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 102)
            } else {
                mostrarNotificacionFavorito(nombreReceta)
            }
        } else {
            mostrarNotificacionFavorito(nombreReceta)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Como la receta ya fue agregada, si el usuario da permiso justo ahora, le mostramos la notificación
        if (requestCode == 102 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val nombreReceta = intent.getStringExtra("NOMBRE_RECETA") ?: "Tu receta"
            mostrarNotificacionFavorito(nombreReceta)
        }
    }

    private fun mostrarNotificacionFavorito(nombreReceta: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID_FAV)
            .setSmallIcon(R.drawable.favorito) // Icono específico de favoritos
            .setContentTitle("¡Receta guardada!")
            .setContentText("$nombreReceta se ha añadido a tu lista.")
            .setStyle(NotificationCompat.InboxStyle()
                .addLine("Receta: $nombreReceta")
                .addLine("Puedes verla en la sección de Favoritos")
                .setBigContentTitle("¡Guardada con éxito!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            try {
                notify(2, builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}
