package com.example.ebook.model

/**
 * Represents a single ebook loaded from assets.
 * [pages] is an ordered list of asset paths relative to assets/book/.
 */
data class Book(
    val title: String,
    val author: String,
    val coverAsset: String,
    val pages: List<String>
)
