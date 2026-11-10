package com.example.ep2

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
                } else {
                    Toast.makeText(this, "Receta $nombreReceta ya se encuentra agregada a favoritos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
