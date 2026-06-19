package com.example.ebook.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook.databinding.ItemBookBinding
import com.example.ebook.model.Book

class BookAdapter(
    private val books: List<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        with(holder.binding) {
            itemTitle.text = book.title
            itemMeta.text = buildString {
                if (book.author.isNotEmpty()) append("${book.author}  ·  ")
                append("${book.pages.size} pages")
                if (!book.isAsset) append("  ·  From Documents")
            }
            root.setOnClickListener { onBookClick(book) }
        }
    }

    override fun getItemCount() = books.size
}
