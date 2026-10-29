package com.example.ep2.network

import com.example.ep2.model.Recipe
import com.example.ep2.model.RecipeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SpoonacularApi {

    @GET("recipes/complexSearch")
    fun obtenerRecetasBusqueda(
        @Query("query") query: String,
        @Query("apiKey") apiKey: String,
        @Query("number") limite: Int = 10
    ): Call<RecipeResponse>

    @GET("recipes/random")
    fun obtenerRecetasAleatorias(
        @Query("apiKey") apiKey: String,
        @Query("number") limite: Int = 10
    ): Call<RecipeResponse>

    @GET("recipes/{id}/information")
    fun obtenerDetalleReceta(
        @Path("id") id: Int,
        @Query("apiKey") apiKey: String
    ): Call<Recipe>
}
