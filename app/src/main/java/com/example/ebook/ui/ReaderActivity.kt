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

    companion object {
        const val EXTRA_PAGES = "extra_pages"
        const val EXTRA_IS_ASSET = "extra_is_asset"
        const val EXTRA_TITLE = "extra_title"
    }

    private lateinit var binding: ActivityReaderBinding
    private val vm: ReaderViewModel by viewModels()

    private lateinit var offscreenPrev: EbookWebView
    private lateinit var offscreenCurrent: EbookWebView
    private lateinit var offscreenNext: EbookWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialise ViewModel with pages passed from MainActivity
        val pages = intent.getStringArrayListExtra(EXTRA_PAGES) ?: emptyList<String>()
        val isAsset = intent.getBooleanExtra(EXTRA_IS_ASSET, true)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        if (pages.isNotEmpty()) vm.initPages(pages, isAsset)
        if (title.isNotEmpty()) supportActionBar?.title = title

        setupOffscreenWebViews()
        setupCurlView()
        setupNavigation()
        observeFoldingState()

        vm.currentIndex.observe(this) { idx -> updateReader(idx) }
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
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also {
            webView.draw(Canvas(it))
        }
    }

    private fun loadPageAsBitmap(path: String, target: EbookWebView, onReady: (Bitmap) -> Unit) {
        target.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.postDelayed({ onReady(snapshotWebView(target)) }, 120)
            }
        }
        target.loadPage(path)
    }

    // ── PageCurlView ─────────────────────────────────────────────────────────

    private fun setupCurlView() {
        binding.pageCurlView.onPageFlipped = { delta -> vm.advance(delta) }
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    private fun setupNavigation() {
        // Route button presses through the curl animation
        binding.btnPrev.setOnClickListener { binding.pageCurlView.flipBackward() }
        binding.btnNext.setOnClickListener { binding.pageCurlView.flipForward() }
    }

    private fun updateReader(pageIndex: Int) {
        val pages = vm.pages
        if (pages.isEmpty()) return

        binding.pageIndicator.text = "${pageIndex + 1} / ${pages.size}"
        binding.btnPrev.isEnabled = pageIndex > 0
        binding.btnNext.isEnabled = pageIndex < pages.size - 1

        binding.mainWebView.loadPage(pages[pageIndex])

        val curl = binding.pageCurlView

        loadPageAsBitmap(pages[pageIndex], offscreenCurrent) { bmp ->
            curl.currentPageBitmap = bmp
            curl.invalidate()
        }

        if (pageIndex > 0) {
            loadPageAsBitmap(pages[pageIndex - 1], offscreenPrev) { bmp ->
                curl.prevPageBitmap = bmp
                curl.invalidate()
            }
        } else {
            curl.prevPageBitmap = null
        }

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
            fold != null &&
                    fold.state == FoldingFeature.State.FLAT &&
                    fold.orientation == FoldingFeature.Orientation.VERTICAL -> {
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
        val pages = vm.pages
        binding.mainWebView.loadPage(pages[idx])
        val nextIdx = (idx + 1).coerceAtMost(pages.size - 1)
        if (nextIdx != idx) binding.secondaryWebView.loadPage(pages[nextIdx])
    }
}
