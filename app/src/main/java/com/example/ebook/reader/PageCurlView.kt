package com.example.ebook.reader

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Custom view that renders a realistic page-curl animation.
 *
 * The host supplies three bitmaps and listens to [onPageFlipped]:
 *   - [prevPageBitmap]    — page that is revealed when curling backward (left edge)
 *   - [currentPageBitmap] — page currently displayed on top
 *   - [nextPageBitmap]    — page revealed when curling forward (right edge)
 *
 * Touch flow:
 *   RIGHT-half touch + left-drag → FORWARD curl (reveals nextPageBitmap)
 *   LEFT-half  touch + right-drag → BACKWARD curl (reveals prevPageBitmap)
 *   Release past threshold → flip completes; [onPageFlipped] fires with ±1.
 *   Release before threshold → snaps back.
 */
class PageCurlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var prevPageBitmap: Bitmap? = null
    var currentPageBitmap: Bitmap? = null
    var nextPageBitmap: Bitmap? = null

    /** Invoked when a full flip completes: delta = +1 (forward) or -1 (backward). */
    var onPageFlipped: ((delta: Int) -> Unit)? = null

    // ── Internal state ───────────────────────────────────────────────────────
    private enum class CurlDirection { NONE, FORWARD, BACKWARD }

    private var direction = CurlDirection.NONE
    private var touchX = 0f
    private var touchY = 0f
    private var startX = 0f

    private val flipThreshold = 0.35f

    // ── Paints ───────────────────────────────────────────────────────────────
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(40, 255, 255, 255)
    }

    // ── Touch ────────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                touchX = event.x
                touchY = event.y
                direction = when {
                    event.x > width * 0.5f -> CurlDirection.FORWARD
                    event.x < width * 0.5f -> CurlDirection.BACKWARD
                    else -> CurlDirection.NONE
                }
                // Disable if there is nothing to reveal in that direction
                if (direction == CurlDirection.FORWARD && nextPageBitmap == null) {
                    direction = CurlDirection.NONE
                }
                if (direction == CurlDirection.BACKWARD && prevPageBitmap == null) {
                    direction = CurlDirection.NONE
                }
                return direction != CurlDirection.NONE
            }

            MotionEvent.ACTION_MOVE -> {
                touchX = event.x
                touchY = event.y
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val delta = touchX - startX
                val threshold = width * flipThreshold
                when {
                    direction == CurlDirection.FORWARD && delta < -threshold ->
                        animateFlip(complete = true, flipDelta = +1)
                    direction == CurlDirection.BACKWARD && delta > threshold ->
                        animateFlip(complete = true, flipDelta = -1)
                    else ->
                        animateFlip(complete = false, flipDelta = 0)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    // ── Drawing ──────────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        if (direction == CurlDirection.NONE) {
            currentPageBitmap?.let { canvas.drawBitmap(it, null, RectF(0f, 0f, w, h), bitmapPaint) }
            return
        }

        val progress = when (direction) {
            CurlDirection.FORWARD  -> ((startX - touchX) / w).coerceIn(0f, 1f)
            CurlDirection.BACKWARD -> ((touchX - startX) / w).coerceIn(0f, 1f)
            else -> 0f
        }

        drawCurl(canvas, w, h, progress)
    }

    private fun drawCurl(canvas: Canvas, w: Float, h: Float, progress: Float) {
        // foldX: position of the crease line
        val foldX = when (direction) {
            CurlDirection.FORWARD  -> w * (1f - progress) // moves left as progress → 1
            CurlDirection.BACKWARD -> w * progress        // moves right as progress → 1
            else -> w
        }

        // ── Back layer (the page being revealed underneath) ──────────────────
        //   FORWARD:  next page is revealed
        //   BACKWARD: previous page is revealed
        val backBitmap = when (direction) {
            CurlDirection.FORWARD  -> nextPageBitmap ?: currentPageBitmap
            CurlDirection.BACKWARD -> prevPageBitmap ?: currentPageBitmap
            else -> null
        }
        backBitmap?.let { canvas.drawBitmap(it, null, RectF(0f, 0f, w, h), bitmapPaint) }

        // ── Front layer (the current page curling away) ──────────────────────
        //   Both directions curl the CURRENT page away.
        val frontBitmap = currentPageBitmap
        val frontClip = Path().apply {
            when (direction) {
                // Remaining visible part of the front page shrinks from right
                CurlDirection.FORWARD  -> addRect(0f, 0f, foldX, h, Path.Direction.CW)
                // Remaining visible part shrinks from left
                CurlDirection.BACKWARD -> addRect(foldX, 0f, w, h, Path.Direction.CW)
                else -> {}
            }
        }
        canvas.save()
        canvas.clipPath(frontClip)
        frontBitmap?.let { canvas.drawBitmap(it, null, RectF(0f, 0f, w, h), bitmapPaint) }
        canvas.restore()

        // ── Drop-shadow at the fold crease ───────────────────────────────────
        val shadowW = (w * 0.08f).coerceAtMost(60f)
        val sx1: Float
        val sx2: Float
        when (direction) {
            CurlDirection.FORWARD -> {
                sx1 = (foldX - shadowW).coerceAtLeast(0f)
                sx2 = foldX
            }
            CurlDirection.BACKWARD -> {
                sx1 = foldX
                sx2 = (foldX + shadowW).coerceAtMost(w)
            }
            else -> return
        }
        val shadowColors = when (direction) {
            CurlDirection.FORWARD  -> intArrayOf(Color.TRANSPARENT, Color.argb(120, 0, 0, 0))
            else                   -> intArrayOf(Color.argb(120, 0, 0, 0), Color.TRANSPARENT)
        }
        shadowPaint.shader = LinearGradient(sx1, 0f, sx2, 0f, shadowColors, null, Shader.TileMode.CLAMP)
        canvas.save()
        canvas.clipPath(frontClip)
        canvas.drawRect(sx1, 0f, sx2, h, shadowPaint)
        canvas.restore()

        // ── Specular highlight on the exposed fold flap ──────────────────────
        val flapW = (w * progress * 0.06f).coerceAtMost(30f)
        val highlightRect = when (direction) {
            CurlDirection.FORWARD  -> RectF(foldX, 0f, (foldX + flapW).coerceAtMost(w), h)
            else                   -> RectF((foldX - flapW).coerceAtLeast(0f), 0f, foldX, h)
        }
        canvas.drawRect(highlightRect, highlightPaint)
    }

    // ── Settle animation ─────────────────────────────────────────────────────

    private fun animateFlip(complete: Boolean, flipDelta: Int) {
        val w = width.toFloat()
        val startProgress = when (direction) {
            CurlDirection.FORWARD  -> ((startX - touchX) / w).coerceIn(0f, 1f)
            CurlDirection.BACKWARD -> ((touchX - startX) / w).coerceIn(0f, 1f)
            else -> 0f
        }
        val endProgress = if (complete) 1f else 0f
        val steps = 20
        val step_delta = (endProgress - startProgress) / steps
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        var step = 0
        val runnable = object : Runnable {
            override fun run() {
                step++
                val p = (startProgress + step_delta * step).coerceIn(0f, 1f)
                touchX = when (direction) {
                    CurlDirection.FORWARD  -> startX - p * w
                    CurlDirection.BACKWARD -> startX + p * w
                    else -> startX
                }
                invalidate()
                if (step < steps) {
                    handler.postDelayed(this, 8)
                } else {
                    direction = CurlDirection.NONE
                    invalidate()
                    if (complete) onPageFlipped?.invoke(flipDelta)
                }
            }
        }
        handler.post(runnable)
    }
}
