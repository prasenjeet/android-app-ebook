package com.example.ebook.model

import android.content.Context

object BookRepository {

    fun getSampleBook(context: Context): Book {
        // List HTML pages stored in assets/book/
        val assetManager = context.assets
        val files = assetManager.list("book")
            ?.filter { it.endsWith(".html") }
            ?.sorted()
            ?: emptyList()

        return Book(
            title = "The Adventures of Tom Sawyer",
            author = "Mark Twain",
            coverAsset = "book/page_00_cover.html",
            pages = files.map { "book/$it" }
        )
    }
}
