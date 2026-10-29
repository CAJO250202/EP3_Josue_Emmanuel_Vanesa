package com.example.ep2.model

import com.google.gson.annotations.SerializedName

data class Recipe(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String?,

    @SerializedName("image")
    val image: String?,

    @SerializedName("imageType")
    val imageType: String?,

    @SerializedName("readyInMinutes")
    val readyInMinutes: Int?,

    @SerializedName("servings")
    val servings: Int?,

    @SerializedName("summary")
    val summary: String?
)
