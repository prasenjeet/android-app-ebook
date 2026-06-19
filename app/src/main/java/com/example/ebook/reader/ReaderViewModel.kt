package com.example.ebook.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.ebook.model.Book
import com.example.ebook.model.BookRepository

class ReaderViewModel(app: Application) : AndroidViewModel(app) {

    val book: Book by lazy { BookRepository.getSampleBook(app) }

    val currentIndex = MutableLiveData(0)

    val totalPages: Int get() = book.pages.size

    fun currentPage(): String = book.pages[currentIndex.value ?: 0]

    fun nextPage(): String? {
        val idx = currentIndex.value ?: 0
        return if (idx + 1 < totalPages) book.pages[idx + 1] else null
    }

    fun prevPage(): String? {
        val idx = currentIndex.value ?: 0
        return if (idx - 1 >= 0) book.pages[idx - 1] else null
    }

    fun goTo(index: Int) {
        val clamped = index.coerceIn(0, totalPages - 1)
        currentIndex.value = clamped
    }

    fun advance(delta: Int) {
        goTo((currentIndex.value ?: 0) + delta)
    }
}
