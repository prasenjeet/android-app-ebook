# Ebook Reader — Android App

A native Android ebook reader that renders HTML-based books with a **realistic page-curl animation** and full support for the **Samsung Galaxy Z Fold 5** foldable display.

---

## Feature Overview

| Feature | Details |
|---|---|
| HTML rendering | WebView-based, reads from `assets/book/` |
| Page-curl animation | Custom `PageCurlView` with forward & backward curl, drop-shadow, specular highlight, and settle animation |
| Z Fold 5 support | Two-page spread when fully open; single-panel in phone or half-open mode |
| Navigation | Swipe gesture + Prev/Next buttons + live page indicator |
| Minimum SDK | API 26 (Android 8.0) |
| Target SDK | API 34 (Android 14) |

---

## Quick Start

```bash
# Clone
git clone https://github.com/prasenjeet/android-app-ebook.git
cd android-app-ebook

# Build (requires Android SDK 34 + JDK 17)
./gradlew assembleDebug

# Install on a connected device / emulator
./gradlew installDebug
```

---

## Wiki Pages

- [Architecture](Architecture.md)
- [Page-Curl Animation](Page-Curl-Animation.md)
- [Z Fold 5 Foldable Support](ZFold5-Foldable-Support.md)
- [Adding Books](Adding-Books.md)
- [Setup and Build](Setup-and-Build.md)
