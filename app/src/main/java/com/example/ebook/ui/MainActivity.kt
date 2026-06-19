package com.example.ebook.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ebook.databinding.ActivityMainBinding
import com.example.ebook.model.Book
import com.example.ebook.model.BookRepository

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val REQUEST_STORAGE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bookList.layoutManager = LinearLayoutManager(this)
        checkPermissionAndLoad()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            binding.permissionCard.visibility = View.GONE
            loadBooks()
        }
    }

    private fun checkPermissionAndLoad() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                loadBooks()
            } else {
                showPermissionCard()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                loadBooks()
            } else {
                showPermissionCard()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_STORAGE
                )
            }
        }
    }

    private fun showPermissionCard() {
        binding.permissionCard.visibility = View.VISIBLE
        binding.btnGrantPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_STORAGE
                )
            }
        }
        // Still show the sample book even without permission
        loadBooks()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            binding.permissionCard.visibility = View.GONE
            loadBooks()
        }
    }

    private fun loadBooks() {
        val books = mutableListOf<Book>()
        books.addAll(BookRepository.getBooksFromDocs())
        books.add(BookRepository.getSampleBook(this))
        binding.bookList.adapter = BookAdapter(books) { book -> openBook(book) }
    }

    private fun openBook(book: Book) {
        startActivity(
            Intent(this, ReaderActivity::class.java).apply {
                putStringArrayListExtra(ReaderActivity.EXTRA_PAGES, ArrayList(book.pages))
                putExtra(ReaderActivity.EXTRA_IS_ASSET, book.isAsset)
                putExtra(ReaderActivity.EXTRA_TITLE, book.title)
            }
        )
    }
}
