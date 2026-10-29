package com.example.ep2.model

import com.google.gson.annotations.SerializedName

data class RecipeResponse(
    @SerializedName("results")
    val results: List<Recipe> = emptyList(),

    @SerializedName("recipes")
    val recipes: List<Recipe> = emptyList(),

    @SerializedName("totalResults")
    val totalResults: Int? = null
)
