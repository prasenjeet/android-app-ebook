package com.example.ebook.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ebook.databinding.ActivityMainBinding
import com.example.ebook.model.BookRepository

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val book = BookRepository.getSampleBook(this)

        binding.bookTitle.text = book.title
        binding.bookAuthor.text = "by ${book.author}"
        binding.pageCount.text = "${book.pages.size} pages"

        binding.btnRead.setOnClickListener {
            startActivity(Intent(this, ReaderActivity::class.java))
        }
    }
}
