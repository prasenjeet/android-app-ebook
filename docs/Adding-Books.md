# Adding Books

The app discovers books by scanning the `assets/book/` directory for HTML files at startup. No code changes are needed to add new content.

---

## Step 1 — Write your HTML pages

Each page is a standalone HTML file. Link the shared stylesheet from `assets/css/ebook.css`:

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
  <title>Chapter 1</title>
  <link rel="stylesheet" href="../css/ebook.css">
</head>
<body>
<div class="page">
  <div class="chapter-heading">
    <span class="chapter-number">Chapter I</span>
    <h2 class="chapter-title">Your Title Here</h2>
    <hr class="chapter-rule">
  </div>
  <p class="drop-cap">First paragraph with a decorative drop cap…</p>
  <p>Subsequent paragraphs…</p>
</div>
</body>
</html>
```

---

## Step 2 — Name files in reading order

`BookRepository` sorts filenames alphabetically, so use a zero-padded numeric prefix:

```
assets/book/
  page_00_cover.html
  page_01_preface.html
  page_02_chapter1.html
  page_03_chapter1b.html
  page_04_chapter2.html
  ...
```

---

## Step 3 — Available CSS classes

| Class | Purpose |
|---|---|
| `.page` | Outer content wrapper (max-width, padding) |
| `.chapter-heading` | Centred heading block |
| `.chapter-number` | Small-caps chapter label |
| `.chapter-title` | Italic heading |
| `.chapter-rule` | Decorative horizontal rule |
| `.drop-cap` | First-letter large drop capital |
| `.section-break` | Centred ornament between sections |
| `.cover-page` | Full-screen gradient cover layout |

---

## Step 4 — Update book metadata

Edit `BookRepository.kt` to set the correct title and author:

```kotlin
return Book(
    title  = "My Book Title",
    author = "Author Name",
    coverAsset = "book/page_00_cover.html",
    pages  = files.map { "book/$it" }
)
```

---

## Multiple books (future extension)

To support a library of books:
1. Give each book its own subfolder: `assets/books/my-book/`.
2. Extend `BookRepository` to enumerate subfolders.
3. Pass the selected book title/path through the `Intent` that launches `ReaderActivity`.
