package com.example.ep2.model

// CLASE DE DATOS PARA LAS RECETAS FAVORITAS GUARDADAS DE LA API EN LA BD
data class FavoriteRecipe(
    val id: Int = 0,
    val recetaId: Int,
    val titulo: String,
    val tiempo: Int,
    val porciones: Int,
    val imagen: String?
)
