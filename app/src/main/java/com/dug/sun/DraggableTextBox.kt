package com.dug.sun

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import kotlin.math.min

class DraggableTextBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val textView: TextView
    private val borderPaint = Paint().apply {
        color = Color.parseColor("#471A72")
        style = Paint.Style.STROKE
        strokeWidth = 4f // 2px equivalent roughly, or set exactly
    }
    private val handlePaint = Paint().apply {
        color = Color.parseColor("#471A72")
        style = Paint.Style.FILL
        textSize = resources.getDimension(com.intuit.ssp.R.dimen._12ssp)
        textAlign = Paint.Align.CENTER
    }

    private var downRawX = 0f
    private var downRawY = 0f
    private var startTranslationX = 0f
    private var startTranslationY = 0f
    private var startWidth = 0
    private var startHeight = 0
    private var isResizing = false
    private var isTouched = false

    private val minSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._30sdp)
    private val resizeArea = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._20sdp)

    init {
        setWillNotDraw(false)
        setBackgroundColor(Color.TRANSPARENT)
        val padding = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp)
        setPadding(padding, padding, padding, padding)

        textView = TextView(context).apply {
            setTextColor(Color.parseColor("#505050"))
            textSize = resources.getDimension(com.intuit.ssp.R.dimen._22ssp)
            gravity = Gravity.CENTER
            includeFontPadding = false
            letterSpacing = 0.25f
            isClickable = false
            isFocusable = false
            isFocusableInTouchMode = false

            typeface = try {
                Typeface.createFromAsset(context.assets, "fonts/digital-7.ttf")
            } catch (e: Exception) {
                Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            }

            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        addView(textView)
    }

    fun setText(text: String) {
        textView.text = text
        updateTextSize()
    }

    private fun updateTextSize() {
        if (width <= 0 || height <= 0) return
        val availableWidth = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom
        if (availableWidth <= 0 || availableHeight <= 0) return
        val size = min(availableWidth, availableHeight) * 0.6f
        textView.textSize = size.coerceIn(8f, 35f)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateTextSize()
    }

    private fun isResizeArea(x: Float, y: Float): Boolean {
        return x >= width - resizeArea && y >= height - resizeArea
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isResizing = isResizeArea(ev.x, ev.y)
                isTouched = true
                invalidate()
                if (isResizing) return true
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX
                downRawY = event.rawY
                startTranslationX = translationX
                startTranslationY = translationY
                startWidth = width
                startHeight = height
                isTouched = true
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downRawX
                val dy = event.rawY - downRawY

                if (isResizing) {
                    val newWidth = (startWidth + dx).toInt().coerceAtLeast(minSize)
                    val newHeight = (startHeight + dy).toInt().coerceAtLeast(minSize)
                    layoutParams = layoutParams.apply {
                        width = newWidth
                        height = newHeight
                    }
                } else {
                    val parent = parent as? View
                    if (parent != null) {
                        translationX = (startTranslationX + dx).coerceIn(-left.toFloat(), (parent.width - left - width).toFloat())
                        translationY = (startTranslationY + dy).coerceIn(-top.toFloat(), (parent.height - top - height).toFloat())
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isResizing = false
                isTouched = false
                invalidate()
                performClick()
                return true
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isTouched) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), borderPaint)
            canvas.drawText("+", width.toFloat() - 25f, height.toFloat() - 10f, handlePaint)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}