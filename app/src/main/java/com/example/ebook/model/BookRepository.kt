package com.example.ebook.model

import android.content.Context
import android.os.Environment
import java.io.File

object BookRepository {

    fun getSampleBook(context: Context): Book {
        val files = context.assets.list("book")
            ?.filter { it.endsWith(".html") }
            ?.sorted()
            ?: emptyList()

        return Book(
            title = "The Adventures of Tom Sawyer",
            author = "Mark Twain",
            coverAsset = "book/page_00_cover.html",
            pages = files.map { "book/$it" },
            isAsset = true
        )
    }

    fun getBooksFromDocs(): List<Book> {
        val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (!docsDir.exists() || !docsDir.canRead()) return emptyList()

        val books = mutableListOf<Book>()

        // HTML files directly in Documents → one book
        val rootFiles = docsDir.listFiles { f -> f.isFile && f.name.lowercase().endsWith(".html") }
            ?.sortedBy { it.name } ?: emptyList()
        if (rootFiles.isNotEmpty()) {
            books.add(Book(
                title = "Documents",
                author = "",
                coverAsset = rootFiles.first().absolutePath,
                pages = rootFiles.map { it.absolutePath },
                isAsset = false
            ))
        }

        // Each subfolder containing HTML files → one book per folder
        docsDir.listFiles { f -> f.isDirectory }
            ?.sortedBy { it.name }
            ?.forEach { dir ->
                val htmlFiles = collectHtmlFiles(dir)
                if (htmlFiles.isNotEmpty()) {
                    books.add(Book(
                        title = dir.name.replace('_', ' ').replace('-', ' '),
                        author = "",
                        coverAsset = htmlFiles.first().absolutePath,
                        pages = htmlFiles.map { it.absolutePath },
                        isAsset = false
                    ))
                }
            }

        return books
    }

    private fun collectHtmlFiles(dir: File): List<File> =
        (dir.listFiles { f -> f.isFile && f.name.lowercase().endsWith(".html") } ?: emptyArray())
            .sortedBy { it.name }
}
