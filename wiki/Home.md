# Ebook Reader — Wiki Home

Welcome to the Ebook Reader wiki. This app renders local HTML files as a paginated ebook with a realistic page-curl animation, optimised for the Samsung Galaxy Z Fold 5.

## Pages

| Page | Description |
|------|-------------|
| [Installation & Setup](Installation.md) | Build the APK, install on device, push HTML files |
| [Usage Guide](Usage.md) | How to use the app, permissions, file layout |
| [Architecture](Architecture.md) | Code structure, key classes, data flow |
| [Development Guide](Development.md) | How to build, run, and extend the app |

## At a Glance

```
HTML files in Documents/
         │
         ▼
  BookRepository  ──► scans folder tree
         │
         ▼
   MainActivity  ──► shows book list (RecyclerView)
         │
         ▼
  ReaderActivity  ──► loads pages into EbookWebView
         │
         ▼
   PageCurlView  ──► renders curl animation from page bitmaps
```

## Key Capabilities

- Reads `.html` files from `Documents/` on the device — no internet needed
- Each subfolder in `Documents/` becomes a separate book
- Swipe or tap Prev/Next to turn pages with curl animation
- Automatically shows a two-page spread on Z Fold 5 when fully unfolded
- Ships with a built-in sample book (*The Adventures of Tom Sawyer*)
