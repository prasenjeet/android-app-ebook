package com.example.ebook.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.example.ebook.databinding.ActivityReaderBinding
import com.example.ebook.reader.EbookWebView
import com.example.ebook.reader.ReaderViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReaderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReaderBinding
    private val vm: ReaderViewModel by viewModels()

    // Off-screen WebViews used to snapshot pages into Bitmaps for the curl overlay
    private lateinit var offscreenPrev: EbookWebView
    private lateinit var offscreenCurrent: EbookWebView
    private lateinit var offscreenNext: EbookWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupOffscreenWebViews()
        setupCurlView()
        setupNavigation()
        observeFoldingState()

        vm.currentIndex.observe(this) { idx ->
            updateReader(idx)
        }
    }

    // ── Off-screen rendering ─────────────────────────────────────────────────

    private fun setupOffscreenWebViews() {
        val w = resources.displayMetrics.widthPixels
        val h = resources.displayMetrics.heightPixels

        fun makeOffscreen() = EbookWebView(this).apply {
            measure(
                View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY)
            )
            layout(0, 0, w, h)
        }

        offscreenPrev = makeOffscreen()
        offscreenCurrent = makeOffscreen()
        offscreenNext = makeOffscreen()
    }

    private fun snapshotWebView(webView: EbookWebView): Bitmap {
        val w = webView.width.coerceAtLeast(1)
        val h = webView.height.coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        webView.draw(Canvas(bmp))
        return bmp
    }

    private fun loadPageAsBitmap(
        assetPath: String,
        target: EbookWebView,
        onReady: (Bitmap) -> Unit
    ) {
        target.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.postDelayed({ onReady(snapshotWebView(target)) }, 120)
            }
        }
        target.loadPage(assetPath)
    }

    // ── PageCurlView setup ───────────────────────────────────────────────────

    private fun setupCurlView() {
        binding.pageCurlView.onPageFlipped = { delta -> vm.advance(delta) }
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    private fun setupNavigation() {
        binding.btnPrev.setOnClickListener { vm.advance(-1) }
        binding.btnNext.setOnClickListener { vm.advance(+1) }
    }

    private fun updateReader(pageIndex: Int) {
        val pages = vm.book.pages
        if (pages.isEmpty()) return

        binding.pageIndicator.text = "${pageIndex + 1} / ${pages.size}"
        binding.btnPrev.isEnabled = pageIndex > 0
        binding.btnNext.isEnabled = pageIndex < pages.size - 1

        // Primary WebView — visible reading pane
        binding.mainWebView.loadPage(pages[pageIndex])

        val curl = binding.pageCurlView

        // Current page bitmap (required for both curl directions)
        loadPageAsBitmap(pages[pageIndex], offscreenCurrent) { bmp ->
            curl.currentPageBitmap = bmp
            curl.invalidate()
        }

        // Previous page bitmap (revealed when curling backward)
        if (pageIndex > 0) {
            loadPageAsBitmap(pages[pageIndex - 1], offscreenPrev) { bmp ->
                curl.prevPageBitmap = bmp
                curl.invalidate()
            }
        } else {
            curl.prevPageBitmap = null
        }

        // Next page bitmap (revealed when curling forward)
        if (pageIndex + 1 < pages.size) {
            loadPageAsBitmap(pages[pageIndex + 1], offscreenNext) { bmp ->
                curl.nextPageBitmap = bmp
                curl.invalidate()
            }
        } else {
            curl.nextPageBitmap = null
        }
    }

    // ── Foldable / Z Fold 5 support ──────────────────────────────────────────

    private fun observeFoldingState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                WindowInfoTracker.getOrCreate(this@ReaderActivity)
                    .windowLayoutInfo(this@ReaderActivity)
                    .collectLatest { info ->
                        val fold = info.displayFeatures
                            .filterIsInstance<FoldingFeature>()
                            .firstOrNull()
                        handleFoldingFeature(fold)
                    }
            }
        }
    }

    private fun handleFoldingFeature(fold: FoldingFeature?) {
        when {
            fold == null -> setSinglePanel()

            fold.state == FoldingFeature.State.FLAT &&
                    fold.orientation == FoldingFeature.Orientation.VERTICAL -> {
                // Fully open Z Fold 5 in book mode: two-page spread
                binding.secondaryWebView.visibility = View.VISIBLE
                binding.divider.visibility = View.VISIBLE
                loadDualPageLayout()
            }

            else -> setSinglePanel()
        }
    }

    private fun setSinglePanel() {
        binding.secondaryWebView.visibility = View.GONE
        binding.divider.visibility = View.GONE
    }

    private fun loadDualPageLayout() {
        val idx = vm.currentIndex.value ?: 0
        val pages = vm.book.pages
        binding.mainWebView.loadPage(pages[idx])
        val nextIdx = (idx + 1).coerceAtMost(pages.size - 1)
        if (nextIdx != idx) {
            binding.secondaryWebView.loadPage(pages[nextIdx])
        }
    }
}
