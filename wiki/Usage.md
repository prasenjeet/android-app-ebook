# Usage Guide

## Library Screen

When you open the app you land on the **book library**.

- Books found in `Documents/` subfolders appear at the top
- The built-in sample book (*The Adventures of Tom Sawyer*) is always at the bottom
- If the permission card is visible, tap **Grant Access** to allow reading from storage

Tap any book card to open it in the reader.

## Reader Screen

### Turning Pages

| Action | Result |
|--------|--------|
| Swipe left | Go to next page (forward curl) |
| Swipe right | Go to previous page (backward curl) |
| Tap **›** (Next) button | Forward page-curl animation |
| Tap **‹** (Prev) button | Backward page-curl animation |

The page indicator at the bottom shows `current / total` pages.

### Page-Curl Animation

- Drag from the **right half** of the screen leftward to preview the next page curling in
- Drag from the **left half** rightward to preview the previous page curling back
- Release past ~35% of screen width to complete the flip
- Release before that threshold to snap back

### Z Fold 5 — Dual-Page Mode

When you fully unfold the Z Fold 5 flat:
- The app automatically switches to a **two-page spread** (left + right panels)
- The left panel shows the current page, the right panel shows the next page
- Folding the phone back returns to single-panel mode

No action required — the app detects the fold state automatically.

## Organising Your HTML Files

```
/sdcard/Documents/
├── Insurance/               ← shows as "Insurance" book (3 pages)
│   ├── 01_lic_policy.html
│   ├── 02_icici_ulip.html
│   └── 03_retirement.html
├── Travel/                  ← shows as "Travel" book
│   ├── 01_itinerary.html
│   └── 02_hotels.html
└── notes.html               ← grouped into "Documents" book
```

- Folder names become book titles (underscores and dashes replaced with spaces)
- Pages are sorted alphabetically — use numeric prefixes for ordered reading
- Nested subfolders are not scanned (only one level deep)

## Tips

- HTML files can reference local CSS/images in the same folder — they load correctly
- The app uses a `WebView` internally so standard HTML5 + CSS3 is fully supported
- JavaScript is enabled — interactive HTML content works
- There is no zoom — the page fills the screen width at 100% text zoom
