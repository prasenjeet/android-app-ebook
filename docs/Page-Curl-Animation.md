# Page-Curl Animation

`PageCurlView` (`reader/PageCurlView.kt`) is a custom `View` that draws a realistic book-page curl using the Canvas 2-D API.

---

## Touch Gesture Mapping

| Touch area | Drag direction | Effect |
|---|---|---|
| Right half of screen | Left | Forward curl — reveals **next** page |
| Left half of screen | Right | Backward curl — reveals **previous** page |
| Any | Release < 35 % of width | Snaps back to current page |
| Any | Release ≥ 35 % of width | Completes flip; `onPageFlipped(±1)` fires |

---

## Drawing Pipeline (per frame)

```
onDraw(canvas)
  │
  ├─ direction == NONE  →  draw currentPageBitmap full-screen, return
  │
  └─ direction == FORWARD or BACKWARD
       │
       ├── 1. Draw back layer (full-screen)
       │       FORWARD:  nextPageBitmap   (page being revealed)
       │       BACKWARD: prevPageBitmap   (page being revealed)
       │
       ├── 2. Clip-draw front layer
       │       FORWARD:  clip [0 .. foldX]     → draw currentPageBitmap
       │       BACKWARD: clip [foldX .. width]  → draw currentPageBitmap
       │
       ├── 3. Drop-shadow at crease (LinearGradient, 8 % of width)
       │       Clipped to same region as front layer
       │
       └── 4. Specular highlight on exposed flap (semi-transparent white rect)
```

### foldX calculation

```
FORWARD:  foldX = width × (1 − progress)   // crease moves left as progress → 1
BACKWARD: foldX = width × progress          // crease moves right as progress → 1

progress = (drag distance) / width  clamped to [0, 1]
```

---

## Settle Animation

When the user lifts their finger `animateFlip()` runs a 20-step handler loop at ~8 ms/frame (~120 fps headroom) to interpolate `touchX` from the release position to either the fully-flipped or fully-reset position. No `ObjectAnimator` or `ValueAnimator` dependency is required.

---

## Adding New Visual Effects

- **Page texture**: replace `bitmapPaint` with a `BitmapShader` tiled over the page rect.
- **Curved crease**: replace the straight clip `Path` with a quadratic Bezier that bows toward the touch Y coordinate.
- **Backside texture**: draw a separate "paper back" bitmap on the flap rect between steps 2 and 3.
