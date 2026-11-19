package com.example.ep2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.ep2.model.FavoriteRecipe

class FavoritesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DataBaseHelper
    private lateinit var gridLayoutFavoritos: GridLayout
    private lateinit var tvSinFavoritos: TextView
    private lateinit var toolbarFavoritos: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favorites)

        dbHelper = DataBaseHelper(this)

        toolbarFavoritos = findViewById(R.id.toolbarFavoritos)
        setSupportActionBar(toolbarFavoritos)
        toolbarFavoritos.setNavigationOnClickListener {
            finish()
        }

        tvSinFavoritos = findViewById(R.id.tvSinFavoritos)
        gridLayoutFavoritos = findViewById(R.id.gridLayoutFavoritos)
    }

    override fun onResume() {
        super.onResume()
        cargarFavoritos()
    }

    // MÉTODO PARA CONSULTAR LAS RECETAS FAVORITAS DESDE LA BD Y MOSTRARLAS EN GRIDLAYOUT
    private fun cargarFavoritos() {
        val listaFavoritos: List<FavoriteRecipe> = dbHelper.obtenerRecetasFavoritas()

        if (listaFavoritos.isEmpty()) {
            tvSinFavoritos.visibility = View.VISIBLE
            gridLayoutFavoritos.removeAllViews()
        } else {
            tvSinFavoritos.visibility = View.GONE
            gridLayoutFavoritos.removeAllViews()

            for (favorito in listaFavoritos) {
                val vistaReceta = layoutInflater.inflate(
                    R.layout.item_recipe,
                    gridLayoutFavoritos,
                    false
                )

                val imgPortada = vistaReceta.findViewById<ImageView>(R.id.imgPortadaReceta)
                val tvNombreReceta = vistaReceta.findViewById<TextView>(R.id.tvNombreReceta)
                val tvDetalleReceta = vistaReceta.findViewById<TextView>(R.id.tvDetalleReceta)

                tvNombreReceta.text = favorito.titulo
                tvDetalleReceta.text = if (favorito.tiempo > 0) "${favorito.tiempo} min" else "Porciones: ${favorito.porciones}"

                if (!favorito.imagen.isNullOrEmpty()) {
                    Glide.with(this@FavoritesActivity)
                        .load(favorito.imagen)
                        .into(imgPortada)
                }

                vistaReceta.setOnClickListener {
                    val intent = Intent(this@FavoritesActivity, DetailsActivity::class.java).apply {
                        putExtra("ID_RECETA", favorito.recetaId)
                        putExtra("NOMBRE_RECETA", favorito.titulo)
                        putExtra("TIEMPO_RECETA", favorito.tiempo)
                        putExtra("PORCIONES_RECETA", favorito.porciones)
                        putExtra("IMAGEN_RECETA", favorito.imagen)
                    }
                    startActivity(intent)
                }

                gridLayoutFavoritos.addView(vistaReceta)
            }
        }
    }
}
