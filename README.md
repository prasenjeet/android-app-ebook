# Ebook Reader — Samsung Z Fold 5

An Android ebook reader app built for the Samsung Galaxy Z Fold 5. Renders local HTML files with a realistic page-curl animation and automatically switches to a two-page spread when the phone is fully unfolded.

## Features

- **Page-curl animation** — swipe or tap Prev/Next to flip pages with a smooth curl effect
- **Z Fold 5 dual-page mode** — opens a two-column spread when the device is fully unfolded flat
- **Local HTML reading** — scans your phone's `Documents/` folder for HTML files automatically
- **Folder = book** — each subfolder in `Documents/` becomes a separate book in the library
- **Built-in sample book** — ships with *The Adventures of Tom Sawyer* as a fallback

## Screenshots

| Library | Reading (single) | Reading (dual — Z Fold 5 open) |
|---------|-----------------|-------------------------------|
| Book list from Documents folder | Page-curl animation | Two-page spread on unfolded display |

## Requirements

- Android 8.0+ (API 26+)
- Samsung Galaxy Z Fold 5 (or any Android device for single-panel mode)
- HTML files stored in `Documents/` on the device

## Quick Start

### 1. Build & Install

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 2. Push your HTML files

```bash
# Create a book folder and push files
adb shell mkdir -p /sdcard/Documents/MyBook
adb push ./my-book/ /sdcard/Documents/MyBook/
```

### 3. Grant storage permission

On first launch tap **Grant Access** and allow "All files access" in Settings.

## Project Structure

```
android-app-ebook/
├── app/
│   └── src/main/
│       ├── java/com/example/ebook/
│       │   ├── model/
│       │   │   ├── Book.kt              # Book data model
│       │   │   └── BookRepository.kt   # Scans assets + Documents folder
│       │   ├── reader/
│       │   │   ├── EbookWebView.kt     # WebView for HTML rendering
│       │   │   ├── PageCurlView.kt     # Custom page-curl animation view
│       │   │   └── ReaderViewModel.kt  # Page navigation state
│       │   └── ui/
│       │       ├── MainActivity.kt     # Book library screen
│       │       ├── ReaderActivity.kt   # Reader screen
│       │       └── BookAdapter.kt      # RecyclerView adapter
│       ├── res/
│       │   ├── layout/
│       │   │   ├── activity_main.xml
│       │   │   ├── activity_reader.xml
│       │   │   └── item_book.xml
│       │   └── assets/
│       │       ├── book/               # Sample HTML pages
│       │       └── css/ebook.css       # Stylesheet for HTML pages
└── wiki/                               # Documentation
```

## Tech Stack

| Component | Library |
|-----------|---------|
| UI | Material Design 3, ConstraintLayout |
| Architecture | MVVM, AndroidViewModel, LiveData |
| WebView | Custom EbookWebView with file:// support |
| Foldable support | Jetpack WindowManager 1.2.0 |
| Concurrency | Kotlin Coroutines |
| Build | Gradle 8.4, Kotlin 1.9.22, AGP 8.2.2 |

## Wiki

- [Home](wiki/Home.md)
- [Installation & Setup](wiki/Installation.md)
- [Usage Guide](wiki/Usage.md)
- [Architecture](wiki/Architecture.md)
- [Development Guide](wiki/Development.md)

## License

MIT
