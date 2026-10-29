package com.example.ep2

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class DetailsActivity : AppCompatActivity() {
    private lateinit var tvResultado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)

        tvResultado = findViewById(R.id.tvResultado)

        mostrarInformacionReceta()
    }

    // METODO PARA MOSTRAR INFORMACION DE UNA RECETA
    private fun mostrarInformacionReceta() {
        val idReceta = intent.getIntExtra("ID_RECETA", 1)
        val nombreReceta = intent.getStringExtra("NOMBRE_RECETA")
        val tiempo = intent.getIntExtra("TIEMPO_RECETA", 0)
        val porciones = intent.getIntExtra("PORCIONES_RECETA", 0)
        val imagen = intent.getStringExtra("IMAGEN_RECETA")

        tvResultado.text = "NOMBRE DE LA RECETA: $nombreReceta"
    }
}
