# Architecture

## Overview

The app follows **MVVM** with a thin repository layer. All UI state lives in `ReaderViewModel`; the Activities only observe and dispatch.

```
┌─────────────────────────────────────────────────────┐
│  MainActivity                                       │
│   BookAdapter (RecyclerView)                        │
│   └── taps → openBook(book) → ReaderActivity        │
└─────────────────────────────────────────────────────┘
                        │ Intent (pages[], isAsset)
                        ▼
┌─────────────────────────────────────────────────────┐
│  ReaderActivity                                     │
│   ├── ReaderViewModel  (currentIndex: LiveData)     │
│   ├── EbookWebView     (visible reading pane)       │
│   ├── PageCurlView     (curl overlay + touch)       │
│   └── WindowInfoTracker (fold state → dual/single)  │
└─────────────────────────────────────────────────────┘
```

## Key Classes

### `BookRepository`
- `getSampleBook(context)` — loads HTML pages from `assets/book/`
- `getBooksFromDocs()` — scans `Environment.DIRECTORY_DOCUMENTS`; root files → "Documents" book; each subfolder → one book

### `Book`
```kotlin
data class Book(
    val title: String,
    val author: String,
    val coverAsset: String,
    val pages: List<String>,   // asset paths OR absolute file paths
    val isAsset: Boolean = true
)
```

### `ReaderViewModel`
Holds the single source of truth for reader state.

| Property / Method | Purpose |
|-------------------|---------|
| `pages: List<String>` | Current book's page paths (custom or sample) |
| `currentIndex: MutableLiveData<Int>` | Active page index |
| `initPages(list, fromAsset)` | Called by ReaderActivity on launch |
| `advance(delta)` | Navigate ±1 pages |
| `goTo(index)` | Jump to specific page |

### `EbookWebView`
Custom `WebView` subclass with consistent ebook settings:
- JavaScript enabled
- File access + cross-file URL access enabled (for local HTML → CSS/image refs)
- No zoom controls, no scroll bars
- `loadPage(path)` handles three path formats:
  - `file://…` — used as-is
  - `/storage/…` — prepended with `file://`
  - `book/page.html` — prefixed with `file:///android_asset/`

### `PageCurlView`
Custom `View` that renders the page-curl animation using three bitmaps.

**State machine:**
```
NONE ──(touch/flip)──► FORWARD or BACKWARD
                              │
                    (ValueAnimator runs)
                              │
                         progress 0→1
                              │
                         NONE + onPageFlipped(±1)
```

**Drawing layers (per frame):**
1. Back bitmap (next/prev page) — full canvas
2. Front bitmap (current page) — clipped to remaining visible region
3. Drop-shadow gradient at the fold crease
4. Specular highlight on the fold edge

**Public API:**
- `flipForward()` — programmatic forward flip (called by Next button)
- `flipBackward()` — programmatic backward flip (called by Prev button)
- `onPageFlipped: ((Int) -> Unit)?` — callback when flip completes

### `ReaderActivity` — bitmap pipeline

```
updateReader(pageIndex)
    │
    ├── mainWebView.loadPage(pages[idx])        ← visible pane
    │
    ├── offscreenCurrent.loadPage(current)
    │       └── onPageFinished → snapshotWebView → curl.currentPageBitmap
    │
    ├── offscreenPrev.loadPage(prev)
    │       └── onPageFinished → snapshotWebView → curl.prevPageBitmap
    │
    └── offscreenNext.loadPage(next)
            └── onPageFinished → snapshotWebView → curl.nextPageBitmap
```

Three off-screen `EbookWebView` instances render pages to `Bitmap` objects used by the curl animation.

## Z Fold 5 — Foldable Support

`WindowInfoTracker` emits `WindowLayoutInfo` whenever the fold state changes.

| Fold state | Orientation | UI mode |
|-----------|-------------|---------|
| `FLAT` | `VERTICAL` | Two-page spread (mainWebView + secondaryWebView) |
| Any other | — | Single panel (secondaryWebView hidden) |

The fold divider `View` (2dp wide) is shown/hidden alongside `secondaryWebView`.

## Permissions

| Permission | When used |
|-----------|-----------|
| `READ_EXTERNAL_STORAGE` (max API 32) | Android 8–12: read Documents folder |
| `MANAGE_EXTERNAL_STORAGE` | Android 13+: full file access for HTML reading |
| `INTERNET` | WebView (not actively used for local files) |
