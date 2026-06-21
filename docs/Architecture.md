# Architecture

## Package Structure

```
com.example.ebook
├── model/
│   ├── Book.kt             — Data class: title, author, ordered page list
│   └── BookRepository.kt   — Discovers HTML pages from assets/book/
├── reader/
│   ├── EbookWebView.kt     — Preconfigured WebView (JS on, no zoom, asset access)
│   ├── PageCurlView.kt     — Custom View: page-curl drawing + touch + animation
│   └── ReaderViewModel.kt  — AndroidViewModel: current page index, navigation
└── ui/
    ├── MainActivity.kt     — Library screen: book card + "Start Reading" button
    └── ReaderActivity.kt   — Reader screen: WebView pane + curl overlay + fold handling
```

## Data Flow

```
BookRepository
    └─ returns Book (pages: List<String> of asset paths)
            │
            ▼
    ReaderViewModel
        currentIndex: LiveData<Int>
            │  observe
            ▼
    ReaderActivity
        ├─ mainWebView.loadPage(currentAsset)        ← visible reading pane
        ├─ offscreenPrev/Current/Next WebViews       ← snapshot bitmaps for curl
        └─ pageCurlView.prevPageBitmap / currentPageBitmap / nextPageBitmap
```

## Key Design Decisions

### Off-screen WebView snapshots
The curl animation needs `Bitmap` objects to draw during the transition. Three off-screen `EbookWebView` instances are kept alive and sized to match screen dimensions. After each page load (`onPageFinished`) a 120 ms delay lets CSS/JS render, then `draw(Canvas)` snapshots the view into a `Bitmap`.

### PageCurlView is stateless about page index
`PageCurlView` knows only three bitmaps (prev / current / next). `ReaderActivity` is responsible for wiring the right assets to those slots whenever the page changes.

### WindowManager Jetpack for foldables
`WindowInfoTracker.windowLayoutInfo()` emits a `Flow<WindowLayoutInfo>` that carries `FoldingFeature` descriptors. The activity observes this within the `STARTED` lifecycle state so the subscription is automatically paused when the app goes to the background.
