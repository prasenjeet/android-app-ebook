# Installation & Setup

## Prerequisites

| Tool | Version |
|------|---------|
| Android Studio | Hedgehog (2023.1) or newer |
| JDK | 17 |
| Android SDK | API 34 (compileSdk) |
| Gradle | 8.4 (bundled via wrapper) |
| ADB | Platform Tools (included with Android SDK) |

## 1. Clone / Open the Project

```bash
cd /Users/prasenjeet/android-app-ebook
```

Open the folder in Android Studio or build from the terminal.

## 2. Build the Debug APK

```bash
# Using the Gradle wrapper (recommended)
./gradlew assembleDebug

# Or using the cached Gradle binary directly
~/.gradle/wrapper/dists/gradle-8.4-bin/*/gradle-8.4/bin/gradle assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## 3. Enable USB Debugging on Your Z Fold 5

1. Go to **Settings → About phone → Software information**
2. Tap **Build number** 7 times to unlock Developer options
3. Go to **Settings → Developer options → USB debugging** → toggle ON
4. Connect phone via USB and tap **Allow** on the "Allow USB debugging?" dialog

## 4. Install the APK

```bash
# Verify device is detected
adb devices

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 5. Push HTML Files to the Device

The app reads from `Documents/` on the phone. Each subfolder becomes one book.

```bash
# Single book (all files in one folder)
adb shell mkdir -p /sdcard/Documents/MyBook
adb push /path/to/my-html-files/ /sdcard/Documents/MyBook/

# Multiple books
adb push /path/to/Insurance/ /sdcard/Documents/Insurance/
adb push /path/to/Reports/   /sdcard/Documents/Reports/
```

Files in the **root** of `Documents/` (not in any subfolder) are grouped into a single "Documents" book.

## 6. Grant Storage Permission

On first launch:
1. Tap **Grant Access** in the permission card
2. You are taken to **Settings → Apps → Ebook Reader → Permissions**
3. Enable **Files and media → Allow access to manage all files**
4. Return to the app — your books appear in the list

## Supported File Format

- `.html` files only
- Pages are sorted **alphabetically** by filename — prefix filenames with numbers to control order:
  ```
  01_cover.html
  02_chapter1.html
  03_chapter2.html
  ```
- Linked CSS/images in the same folder are loaded automatically
