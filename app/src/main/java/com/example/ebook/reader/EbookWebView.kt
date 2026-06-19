package com.example.ebook.reader

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebSettings
import android.webkit.WebView

@SuppressLint("SetJavaScriptEnabled")
class EbookWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : WebView(context, attrs) {

    init {
        with(settings) {
            javaScriptEnabled = true
            allowFileAccess = true
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = true
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            textZoom = 100
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(false)
            builtInZoomControls = false
        }
        setBackgroundColor(0x00000000)
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
    }

    fun loadPage(path: String) {
        when {
            path.startsWith("file://") -> loadUrl(path)
            path.startsWith("/") -> loadUrl("file://$path")
            else -> loadUrl("file:///android_asset/$path")
        }
    }
}
