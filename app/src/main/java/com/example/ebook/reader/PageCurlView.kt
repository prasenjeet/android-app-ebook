package com.example.ebook.reader

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator

/**
 * Renders a realistic page-curl animation using three bitmaps.
 *
 * Visual layers (back to front):
 *   1. Back page  — the page being revealed underneath
 *   2. Front page — the current page, clipped to its remaining visible strip
 *   3. Shadow     — cast by the curl onto the revealed back page
 *   4. Curl flap  — the peeling portion of the current page (mirrored + lit)
 *   5. Crease     — bright line at the fold axis
 *
 * Touch:
 *   Right-half drag left  → FORWARD curl (reveals nextPageBitmap)
 *   Left-half  drag right → BACKWARD curl (reveals prevPageBitmap)
 *   Release past 35% threshold → flip completes, onPageFlipped fires ±1
 *
 * Programmatic:
 *   flipForward() / flipBackward() — animated flip from buttons
 */
class PageCurlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var prevPageBitmap: Bitmap? = null
    var currentPageBitmap: Bitmap? = null
    var nextPageBitmap: Bitmap? = null
    var onPageFlipped: ((delta: Int) -> Unit)? = null

    private enum class CurlDirection { NONE, FORWARD, BACKWARD }

    private var direction = CurlDirection.NONE
    private var touchX = 0f
    private var touchY = 0f
    private var startX = 0f
    private val flipThreshold = 0.35f
    private var animator: ValueAnimator? = null

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val generalPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // ── Programmatic flip API ────────────────────────────────────────────────

    fun flipForward() {
        if (nextPageBitmap == null || direction != CurlDirection.NONE) return
        direction = CurlDirection.FORWARD
        startX = width.toFloat()
        touchX = startX
        touchY = height / 2f
        runAnimator(0f, 1f, complete = true, flipDelta = +1)
    }

    fun flipBackward() {
        if (prevPageBitmap == null || direction != CurlDirection.NONE) return
        direction = CurlDirection.BACKWARD
        startX = 0f
        touchX = startX
        touchY = height / 2f
        runAnimator(0f, 1f, complete = true, flipDelta = -1)
    }

    // ── Touch ────────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (animator?.isRunning == true) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                touchX = event.x
                touchY = event.y
                direction = if (event.x > width * 0.5f) CurlDirection.FORWARD else CurlDirection.BACKWARD
                if (direction == CurlDirection.FORWARD && nextPageBitmap == null) direction = CurlDirection.NONE
                if (direction == CurlDirection.BACKWARD && prevPageBitmap == null) direction = CurlDirection.NONE
                return direction != CurlDirection.NONE
            }
            MotionEvent.ACTION_MOVE -> {
                touchX = event.x
                touchY = event.y
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val moved = touchX - startX
                val threshold = width * flipThreshold
                val shouldFlip = (direction == CurlDirection.FORWARD && moved < -threshold) ||
                        (direction == CurlDirection.BACKWARD && moved > threshold)
                val curr = currentTouchProgress()
                val delta = if (direction == CurlDirection.FORWARD) +1 else -1
                if (shouldFlip) runAnimator(curr, 1f, true, delta)
                else runAnimator(curr, 0f, false, 0)
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
        if (w == 0f || h == 0f) return

        if (direction == CurlDirection.NONE) {
            currentPageBitmap?.let { canvas.drawBitmap(it, null, RectF(0f, 0f, w, h), bitmapPaint) }
            return
        }

        val progress = currentTouchProgress()
        if (progress <= 0f) {
            currentPageBitmap?.let { canvas.drawBitmap(it, null, RectF(0f, 0f, w, h), bitmapPaint) }
            return
        }

        drawCurl(canvas, w, h, progress)
    }

    private fun currentTouchProgress(): Float = when (direction) {
        CurlDirection.FORWARD  -> ((startX - touchX) / width).coerceIn(0f, 1f)
        CurlDirection.BACKWARD -> ((touchX - startX) / width).coerceIn(0f, 1f)
        else -> 0f
    }

    private fun drawCurl(canvas: Canvas, w: Float, h: Float, progress: Float) {
        val foldX = when (direction) {
            CurlDirection.FORWARD  -> w * (1f - progress)   // sweeps right → left
            CurlDirection.BACKWARD -> w * progress           // sweeps left → right
            else -> w
        }

        // ── Layer 1: back page (revealed underneath) ─────────────────────────
        val backBmp = if (direction == CurlDirection.FORWARD) nextPageBitmap else prevPageBitmap
        (backBmp ?: currentPageBitmap)?.let {
            canvas.drawBitmap(it, null, RectF(0f, 0f, w, h), bitmapPaint)
        }

        // ── Layer 2: remaining front page (the part that hasn't curled yet) ──
        val frontClip = Path().also {
            when (direction) {
                CurlDirection.FORWARD  -> it.addRect(0f, 0f, foldX, h, Path.Direction.CW)
                CurlDirection.BACKWARD -> it.addRect(foldX, 0f, w, h, Path.Direction.CW)
                else -> {}
            }
        }
        canvas.save()
        canvas.clipPath(frontClip)
        currentPageBitmap?.let { canvas.drawBitmap(it, null, RectF(0f, 0f, w, h), bitmapPaint) }
        canvas.restore()

        // ── Layer 3: shadow cast onto the revealed back page ──────────────────
        drawRevealedShadow(canvas, w, h, foldX)

        // ── Layer 4: curl flap — the peeling portion of the current page ──────
        currentPageBitmap?.let { drawCurlFlap(canvas, it, w, h, foldX, progress) }

        // ── Layer 5: bright crease line at the fold axis ──────────────────────
        drawCreaseLine(canvas, h, foldX)
    }

    /**
     * Draws the "back face" of the peeling page — a perspective-narrowed strip
     * of the current page, mirrored and tinted to look like the reverse side of
     * thin paper, with a lighting gradient (bright at crease, dim at free edge).
     */
    private fun drawCurlFlap(canvas: Canvas, bmp: Bitmap, w: Float, h: Float, foldX: Float, progress: Float) {
        // Flap width: proportional to the revealed area, narrows with perspective as progress → 1
        val revealedW = if (direction == CurlDirection.FORWARD) w - foldX else foldX
        val flapW = revealedW * (0.08f + (1f - progress) * 0.52f)
        if (flapW < 2f) return

        val flapLeft: Float
        val flapRight: Float
        when (direction) {
            CurlDirection.FORWARD -> {
                flapLeft  = foldX
                flapRight = (foldX + flapW).coerceAtMost(w)
            }
            CurlDirection.BACKWARD -> {
                flapLeft  = (foldX - flapW).coerceAtLeast(0f)
                flapRight = foldX
            }
            else -> return
        }
        if (flapRight <= flapLeft) return

        // Source strip of the bitmap that corresponds to the peeling portion
        val bmpFoldX = (foldX / w * bmp.width).toInt().coerceIn(0, bmp.width)
        val srcLeft: Int
        val srcRight: Int
        when (direction) {
            CurlDirection.FORWARD  -> { srcLeft = bmpFoldX;  srcRight = bmp.width }
            CurlDirection.BACKWARD -> { srcLeft = 0;         srcRight = bmpFoldX  }
            else -> return
        }
        if (srcRight <= srcLeft) return

        canvas.save()
        canvas.clipRect(RectF(flapLeft, 0f, flapRight, h))

        // Map source strip → flap destination rect, then flip horizontally
        // (horizontal mirror = "back face" of the page)
        val matrix = Matrix()
        matrix.setRectToRect(
            RectF(srcLeft.toFloat(), 0f, srcRight.toFloat(), bmp.height.toFloat()),
            RectF(flapLeft, 0f, flapRight, h),
            Matrix.ScaleToFit.FILL
        )
        matrix.postScale(-1f, 1f, (flapLeft + flapRight) / 2f, 0f)

        // Desaturate + brighten  →  "thin paper" back-face appearance
        val cm = ColorMatrix().also { it.setSaturation(0.08f) }
        cm.postConcat(ColorMatrix().also { it.setScale(1.6f, 1.6f, 1.6f, 1f) })

        canvas.drawBitmap(bmp, matrix, Paint(bitmapPaint).apply {
            colorFilter = ColorMatrixColorFilter(cm)
        })

        // Lighting gradient: bright highlight at the crease → shadow at free edge
        val (gx1, gx2) = if (direction == CurlDirection.FORWARD)
            flapLeft to flapRight else flapRight to flapLeft

        generalPaint.shader = LinearGradient(
            gx1, 0f, gx2, 0f,
            intArrayOf(Color.argb(200, 255, 255, 255), Color.argb(130, 0, 0, 0)),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(flapLeft, 0f, flapRight, h, generalPaint)
        generalPaint.shader = null

        canvas.restore()
    }

    /**
     * Gradient shadow cast by the curling flap onto the revealed back page.
     * Darkens the back page near the fold crease, fades to transparent away from it.
     */
    private fun drawRevealedShadow(canvas: Canvas, w: Float, h: Float, foldX: Float) {
        val shadowW = (w * 0.14f).coerceAtMost(90f)

        val x1: Float; val x2: Float
        val colors: IntArray
        val clipRect: RectF

        when (direction) {
            CurlDirection.FORWARD -> {
                x1 = foldX;  x2 = (foldX + shadowW).coerceAtMost(w)
                colors   = intArrayOf(Color.argb(170, 0, 0, 0), Color.TRANSPARENT)
                clipRect = RectF(foldX, 0f, w, h)
            }
            CurlDirection.BACKWARD -> {
                x1 = (foldX - shadowW).coerceAtLeast(0f);  x2 = foldX
                colors   = intArrayOf(Color.TRANSPARENT, Color.argb(170, 0, 0, 0))
                clipRect = RectF(0f, 0f, foldX, h)
            }
            else -> return
        }

        generalPaint.shader = LinearGradient(x1, 0f, x2, 0f, colors, null, Shader.TileMode.CLAMP)
        canvas.save()
        canvas.clipRect(clipRect)
        canvas.drawRect(x1, 0f, x2, h, generalPaint)
        canvas.restore()
        generalPaint.shader = null
    }

    /** Thin bright line at the fold axis — the crease of the bend. */
    private fun drawCreaseLine(canvas: Canvas, h: Float, foldX: Float) {
        generalPaint.apply {
            shader = null
            style  = Paint.Style.STROKE
            strokeWidth = 1.5f
            color  = Color.argb(180, 255, 252, 230)
        }
        canvas.drawLine(foldX, 0f, foldX, h, generalPaint)
        generalPaint.style = Paint.Style.FILL
    }

    // ── ValueAnimator settle / flip ──────────────────────────────────────────

    private fun runAnimator(from: Float, to: Float, complete: Boolean, flipDelta: Int) {
        animator?.cancel()
        val dir = direction
        val w   = width.toFloat()
        animator = ValueAnimator.ofFloat(from, to).apply {
            duration     = (kotlin.math.abs(to - from) * 320).toLong().coerceAtLeast(80)
            interpolator = DecelerateInterpolator()
            addUpdateListener { va ->
                val p = va.animatedValue as Float
                touchX = when (dir) {
                    CurlDirection.FORWARD  -> startX - p * w
                    CurlDirection.BACKWARD -> startX + p * w
                    else -> startX
                }
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    direction = CurlDirection.NONE
                    invalidate()
                    if (complete) onPageFlipped?.invoke(flipDelta)
                }
            })
            start()
        }
    }
}
