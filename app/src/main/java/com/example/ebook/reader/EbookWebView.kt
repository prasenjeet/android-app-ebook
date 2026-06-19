package com.example.ebook.reader

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebSettings
import android.webkit.WebView

/**
 * Preconfigured WebView for rendering ebook HTML pages from assets.
 */
@SuppressLint("SetJavaScriptEnabled")
class EbookWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : WebView(context, attrs) {

    init {
        with(settings) {
            javaScriptEnabled = true
            allowFileAccess = true
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

    fun loadPage(assetPath: String) {
        loadUrl("file:///android_asset/$assetPath")
    }
}
