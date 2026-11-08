package com.example.ep2

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class DetailsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DataBaseHelper
    private lateinit var imgDetalleReceta: ImageView
    private lateinit var tvTituloDetalle: TextView
    private lateinit var tvTiempoDetalle: TextView
    private lateinit var tvPorcionesDetalle: TextView
    private lateinit var tvResumenDetalle: TextView
    private lateinit var btnFavorito: Button

    private var idReceta: Int = 0
    private var nombreReceta: String = ""
    private var tiempo: Int = 0
    private var porciones: Int = 0
    private var imagen: String? = null
    private var esFavorito: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)

        dbHelper = DataBaseHelper(this)

        imgDetalleReceta = findViewById(R.id.imgDetalleReceta)
        tvTituloDetalle = findViewById(R.id.tvTituloDetalle)
        tvTiempoDetalle = findViewById(R.id.tvTiempoDetalle)
        tvPorcionesDetalle = findViewById(R.id.tvPorcionesDetalle)
        tvResumenDetalle = findViewById(R.id.tvResumenDetalle)
        btnFavorito = findViewById(R.id.btnFavorito)

        recibirDatosIntent()
        configurarBotonFavorito()
    }

    private fun recibirDatosIntent() {
        idReceta = intent.getIntExtra("ID_RECETA", 0)
        nombreReceta = intent.getStringExtra("NOMBRE_RECETA") ?: "Receta sin nombre"
        tiempo = intent.getIntExtra("TIEMPO_RECETA", 0)
        porciones = intent.getIntExtra("PORCIONES_RECETA", 0)
        imagen = intent.getStringExtra("IMAGEN_RECETA")

        // Mostrar datos en las vistas
        tvTituloDetalle.text = nombreReceta
        tvTiempoDetalle.text = "Tiempo: $tiempo min"
        tvPorcionesDetalle.text = "Porciones: $porciones"
        tvResumenDetalle.text = "Receta seleccionada del API de Spoonacular con ID: $idReceta.\nDisfruta preparando este delicioso platillo siguiendo los ingredientes y pasos recomendados."

        if (!imagen.isNullOrEmpty()) {
            Glide.with(this)
                .load(imagen)
                .into(imgDetalleReceta)
        }

        // Comprobar estado de favoritos en SQLite
        esFavorito = dbHelper.esFavorito(idReceta)
        actualizarTextoBotonFavorito()
    }

    private fun configurarBotonFavorito() {
        btnFavorito.setOnClickListener {
            if (esFavorito) {
                val eliminado = dbHelper.eliminarFavorito(idReceta)
                if (eliminado) {
                    esFavorito = false
                    Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al eliminar de favoritos", Toast.LENGTH_SHORT).show()
                }
            } else {
                val guardado = dbHelper.agregarFavorito(idReceta, nombreReceta, tiempo, porciones, imagen)
                if (guardado) {
                    esFavorito = true
                    Toast.makeText(this, "Guardado en favoritos con éxito", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al guardar en favoritos", Toast.LENGTH_SHORT).show()
                }
            }
            actualizarTextoBotonFavorito()
        }
    }

    private fun actualizarTextoBotonFavorito() {
        if (esFavorito) {
            btnFavorito.text = "Eliminar de Favoritos"
            btnFavorito.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D32F2F"))
        } else {
            btnFavorito.text = "Guardar en Favoritos"
            btnFavorito.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF5722"))
        }
    }
}
