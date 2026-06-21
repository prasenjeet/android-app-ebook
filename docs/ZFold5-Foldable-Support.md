# Z Fold 5 Foldable Support

The app uses the **Jetpack WindowManager** library (`androidx.window:window:1.2.0`) to adapt its layout dynamically to the three physical states of the Samsung Galaxy Z Fold 5.

---

## Fold States and App Behavior

| Physical state | `FoldingFeature.state` | `orientation` | App behavior |
|---|---|---|---|
| Fully open (flat) | `FLAT` | `VERTICAL` | Two-page spread — left + right WebView panes |
| Half-open (tabletop/book) | `HALF_OPENED` | either | Single-pane (relaxed reading) |
| Folded (phone mode) | `null` (no feature) | — | Single-pane |

---

## Implementation

### Layout (`activity_reader.xml`)

Three views sit in a horizontal ConstraintLayout chain:

```
[mainWebView 0dp] — [divider 2dp] — [secondaryWebView 0dp]
```

- `divider` and `secondaryWebView` start as `visibility="gone"`.
- When both are `GONE`, ConstraintLayout collapses them to 0-size anchors at the right edge, so `mainWebView` fills 100 % of the width automatically.
- When both become `VISIBLE`, the chain splits the width equally (each WebView gets ~50 %).

### Observer (`ReaderActivity.observeFoldingState`)

```kotlin
WindowInfoTracker.getOrCreate(this)
    .windowLayoutInfo(this)          // returns Flow<WindowLayoutInfo>
    .collectLatest { info ->
        val fold = info.displayFeatures
            .filterIsInstance<FoldingFeature>()
            .firstOrNull()
        handleFoldingFeature(fold)
    }
```

The coroutine is launched inside `lifecycle.repeatOnLifecycle(STARTED)` so it is automatically cancelled in the background and restarted in the foreground.

### Manifest flags

```xml
android:resizeableActivity="true"
android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize|..."
```

`configChanges` prevents the activity from being recreated on fold/unfold; instead, `handleFoldingFeature()` updates the layout in-place.

---

## Testing on Z Fold 5

1. Install the APK on a physical Z Fold 5.
2. Open the app in phone mode (folded) → single-page view.
3. Fully unfold the device → two-page spread appears automatically.
4. Hold the device in tabletop/book half-open position → reverts to single-page.

### Testing without hardware

Use the **Resizable (Experimental)** AVD in Android Studio (API 33+) or the **Foldable** AVD (Pixel Fold profile). You can toggle fold state from the Extended Controls panel.
