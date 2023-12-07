package com.example.reciepekotlinapp

data class Recipe(
    val id: String = "",  // Changed to String
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val ingredients: List<String> = listOf(),
    val estimatedTime: String = ""
) {

}
