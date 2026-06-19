# Development Guide

## Build Commands

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing config)
./gradlew assembleRelease

# Clean build
./gradlew clean assembleDebug

# Run lint
./gradlew lintDebug
```

Output APK: `app/build/outputs/apk/debug/app-debug.apk`

## ADB Quick Reference

```bash
# Check connected devices
adb devices

# Install / reinstall APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Install on specific device
adb -s RFCWB0RECLM install -r app/build/outputs/apk/debug/app-debug.apk

# Push a folder of HTML files
adb shell mkdir -p /sdcard/Documents/MyBook
adb push ./my-html-files/ /sdcard/Documents/MyBook/

# View logcat (filter to this app)
adb logcat --pid=$(adb shell pidof com.example.ebook)

# Clear app data (reset permissions)
adb shell pm clear com.example.ebook
```

## Project Configuration

| Setting | Value |
|---------|-------|
| `applicationId` | `com.example.ebook` |
| `minSdk` | 26 (Android 8.0) |
| `targetSdk` | 34 (Android 14) |
| `compileSdk` | 34 |
| Kotlin | 1.9.22 |
| AGP | 8.2.2 |
| Gradle | 8.4 |

## Adding a New Book Source

Books are returned as a `List<Book>` from `BookRepository`. To add a new source (e.g. a specific folder or a cloud download):

```kotlin
// In BookRepository.kt
fun getBooksFromCustomPath(path: String): List<Book> {
    val dir = File(path)
    val htmlFiles = dir.listFiles { f -> f.name.endsWith(".html") }
        ?.sortedBy { it.name } ?: return emptyList()
    return listOf(
        Book(
            title = dir.name,
            author = "",
            coverAsset = htmlFiles.first().absolutePath,
            pages = htmlFiles.map { it.absolutePath },
            isAsset = false
        )
    )
}
```

Then call it from `MainActivity.loadBooks()`.

## Customising the Page Style

HTML pages loaded from `assets/` use `assets/css/ebook.css`. For external HTML files, include your own `<link rel="stylesheet">` tag or inline styles in the HTML.

Key CSS variables in `ebook.css`:
```css
:root {
    --page-bg:     #FFF8F0;   /* cream background */
    --text-color:  #1A1A1A;   /* near-black text */
    --accent:      #8B4513;   /* saddle brown */
    --font-body:   Georgia, serif;
    --line-height: 1.75;
}
```

## Customising the Curl Animation

`PageCurlView` exposes these tuneable values:

| Field | Default | Effect |
|-------|---------|--------|
| `flipThreshold` | `0.35f` | Fraction of screen width to drag before flip commits |
| Animation `duration` | `progress × 300ms` | Speed of the settle/flip animation |
| Shadow alpha | `140` | Darkness of the fold crease shadow |
| Highlight alpha | `40` | Brightness of the specular edge highlight |

## Extending to Other File Formats

The reader currently supports `.html` only (rendered via `WebView`). To support PDF or EPUB:
- PDF: use `PdfRenderer` (Android API 21+) and render pages to `Bitmap` — feed directly into `PageCurlView` bitmaps
- EPUB: unzip the `.epub`, extract HTML pages, pass paths the same way as local HTML

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Books not showing | Check storage permission is granted under Settings → Apps |
| White pages / blank curl | WebView hasn't finished loading — increase the 120 ms delay in `loadPageAsBitmap` |
| Buttons don't animate | Bitmaps not yet loaded; the flip is blocked — wait for the current page to finish rendering |
| Dual-page not activating | Open Z Fold 5 fully flat; check `WindowInfoTracker` output in logcat |
| `MANAGE_EXTERNAL_STORAGE` not available | Only available on API 30+; below API 30 the app uses `READ_EXTERNAL_STORAGE` automatically |
