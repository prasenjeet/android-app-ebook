package com.example.ebook.model

data class Book(
    val title: String,
    val author: String,
    val coverAsset: String,
    val pages: List<String>,
    val isAsset: Boolean = true
)
