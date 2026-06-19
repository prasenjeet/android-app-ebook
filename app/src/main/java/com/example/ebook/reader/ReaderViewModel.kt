package com.example.ebook.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.ebook.model.BookRepository

class ReaderViewModel(app: Application) : AndroidViewModel(app) {

    private val samplePages: List<String> by lazy {
        BookRepository.getSampleBook(app).pages
    }

    private var _customPages: List<String>? = null
    private var _isAsset = true

    val pages: List<String> get() = _customPages ?: samplePages
    val isAsset: Boolean get() = _isAsset

    val currentIndex = MutableLiveData(0)
    val totalPages: Int get() = pages.size

    fun initPages(pageList: List<String>, fromAsset: Boolean) {
        _customPages = pageList
        _isAsset = fromAsset
        currentIndex.value = 0
    }

    fun goTo(index: Int) {
        currentIndex.value = index.coerceIn(0, totalPages - 1)
    }

    fun advance(delta: Int) {
        goTo((currentIndex.value ?: 0) + delta)
    }
}
