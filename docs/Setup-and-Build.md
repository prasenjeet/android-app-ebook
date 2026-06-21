# Setup and Build

## Prerequisites

| Tool | Minimum version |
|---|---|
| Android Studio | Hedgehog (2023.1) or newer |
| JDK | 17 |
| Android SDK | API 34 (compileSdk) |
| Android SDK Platform-Tools | Latest |
| Gradle | 8.4 (wrapper included) |

---

## Clone and open

```bash
git clone https://github.com/prasenjeet/android-app-ebook.git
```

Open the root folder in Android Studio. The IDE will sync Gradle automatically.

---

## Build variants

| Command | Output |
|---|---|
| `./gradlew assembleDebug` | `app/build/outputs/apk/debug/app-debug.apk` |
| `./gradlew assembleRelease` | Unsigned release APK (configure signing first) |
| `./gradlew installDebug` | Build + install on connected device/emulator |
| `./gradlew test` | Unit tests |
| `./gradlew connectedAndroidTest` | Instrumented tests |

---

## Key dependencies

```gradle
// Foldable support
implementation 'androidx.window:window:1.2.0'
implementation 'androidx.window:window-java:1.2.0'

// Lifecycle / ViewModel
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
implementation 'androidx.activity:activity-ktx:1.8.2'

// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
```

---

## Running on a Samsung Galaxy Z Fold 5

1. Enable **Developer Options** → **USB Debugging** on the device.
2. Connect via USB.
3. Run `./gradlew installDebug`.
4. Open the app, then unfold the device to trigger two-page spread mode.

## Running on an emulator

1. In Android Studio → **Device Manager**, create a **Resizable (Experimental)** AVD targeting API 33 or 34.
2. Start the AVD, then use **Extended Controls → Virtual sensors → Fold** to simulate fold/unfold.

---

## Signing a release build

Create `app/keystore.properties`:

```properties
storeFile=../my-release-key.jks
storePassword=yourStorePassword
keyAlias=yourKeyAlias
keyPassword=yourKeyPassword
```

Then add to `app/build.gradle`:

```gradle
android {
    signingConfigs {
        release {
            def props = new Properties()
            props.load(file('../app/keystore.properties').newDataInputStream())
            storeFile     file(props['storeFile'])
            storePassword props['storePassword']
            keyAlias      props['keyAlias']
            keyPassword   props['keyPassword']
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
```
