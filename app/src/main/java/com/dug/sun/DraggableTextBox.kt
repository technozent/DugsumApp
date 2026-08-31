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

class DraggableTextBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val textView: TextView
    private val borderPaint = Paint().apply {
        color = Color.parseColor("#471A72")
        style = Paint.Style.STROKE
        strokeWidth = 2.4f // Reduced by 40% from 4f
    }
    private val handlePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        textSize = resources.getDimension(com.intuit.ssp.R.dimen._24ssp)
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val handleBgPaint = Paint().apply {
        color = Color.parseColor("#471A72")
        style = Paint.Style.FILL
    }
    private val rotateBgPaint = Paint().apply {
        color = Color.parseColor("#0080d0")
        style = Paint.Style.FILL
    }

    private var downRawX = 0f
    private var downRawY = 0f
    private var startTranslationX = 0f
    private var startTranslationY = 0f
    private var startWidth = 0
    private var startHeight = 0
    private var initialRotation = 0f
    private var startAngle = 0f
    private var isResizing = false
    private var isRotating = false
    
    var isHandleVisible = false
        set(value) {
            field = value
            invalidate()
        }

    private val minSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._30sdp)
    private val handleArea = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._25sdp)

    init {
        setWillNotDraw(false)
        setBackgroundColor(Color.TRANSPARENT)
        
        // Add padding to ensure the textView stays within the border and 
        // doesn't overlap with the "outside" handles
        val radius = handlePaint.textSize * 0.6f
        val padding = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp)
        setPadding(padding, (padding + radius).toInt(), (padding + radius).toInt(), (padding + radius).toInt())

        textView = TextView(context).apply {
            setTextColor(Color.parseColor("#9E9E9E"))
            textSize = resources.getDimension(com.intuit.ssp.R.dimen._22ssp)
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            includeFontPadding = false
            letterSpacing = 0.06f
            isClickable = false
            isFocusable = false
            isFocusableInTouchMode = false

            val baseTypeface = try {
                Typeface.createFromAsset(context.assets, "fonts/ds_digital.ttf")
            } catch (e: Exception) {
                Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            }
            typeface = baseTypeface
            // Removed extra creation logic to keep it as regular as possible

            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        addView(textView)
    }

    fun setText(text: String) {
        textView.text = text
        updateTextSize()
    }

    fun setTextBlackness(intensity: Float) {
        // intensity 0.0 (lightest) to 1.0 (darkest)
        // Map to RGB values 200 (light grey) down to 0 (black)
        val grey = (200 * (1f - intensity)).toInt()
        textView.setTextColor(Color.rgb(grey, grey, grey))
    }

    private fun updateTextSize() {
        if (width <= 0 || height <= 0) return
        val availableWidth = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom
        if (availableWidth <= 0 || availableHeight <= 0) return
        
        // Use height as the primary scaling factor for meter-like digits
        val size = availableHeight * 0.73f
        textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, size)
        textView.textScaleX = 0.8f // 1.0 = normal, lower = narrower
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateTextSize()
    }

    private fun isResizeArea(x: Float, y: Float): Boolean {
        return x >= width - handleArea && y >= height - handleArea
    }

    private fun isRotationArea(x: Float, y: Float): Boolean {
        return x >= width - handleArea && y <= handleArea
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isResizing = isResizeArea(ev.x, ev.y)
                isRotating = isRotationArea(ev.x, ev.y)
                isHandleVisible = true
                if (isResizing || isRotating) return true
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
                
                initialRotation = rotation
                startAngle = calculateAngle(event.rawX, event.rawY)
                
                isHandleVisible = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isRotating) {
                    val currentAngle = calculateAngle(event.rawX, event.rawY)
                    rotation = (initialRotation + (currentAngle - startAngle)).coerceIn(-30f, 30f)
                } else if (isResizing) {
                    val dx = event.rawX - downRawX
                    val dy = event.rawY - downRawY
                    
                    val newWidth = (startWidth + dx).toInt().coerceAtLeast(minSize)
                    val newHeight = (startHeight + dy).toInt().coerceAtLeast(minSize)
                    
                    layoutParams = layoutParams.apply {
                        width = newWidth
                        height = newHeight
                    }
                } else {
                    val dx = event.rawX - downRawX
                    val dy = event.rawY - downRawY
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
                isRotating = false
                performClick()
                return true
            }
        }
        return true
    }

    private fun calculateAngle(rawX: Float, rawY: Float): Float {
        val location = IntArray(2)
        getLocationOnScreen(location)
        val centerX = location[0] + width / 2f
        val centerY = location[1] + height / 2f
        return Math.toDegrees(kotlin.math.atan2((rawY - centerY).toDouble(), (rawX - centerX).toDouble())).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isHandleVisible) {
            val radius = handlePaint.textSize * 0.6f
            val rotateRadius = radius * 0.7f // 30% smaller
            
            val rectRight = width.toFloat() - radius
            val rectBottom = height.toFloat() - radius
            val rectTop = radius

            // Draw border rectangle
            canvas.drawRect(0f, rectTop, rectRight, rectBottom, borderPaint)

            // Draw resize handle (bottom-right)
            canvas.drawCircle(rectRight, rectBottom, radius, handleBgPaint)
            canvas.drawText("+", rectRight, rectBottom + (handlePaint.textSize / 3f), handlePaint)

            // Draw rotation handle (top-right) - 30% smaller
            canvas.drawCircle(rectRight, rectTop, rotateRadius, rotateBgPaint)
            
            // Adjust text size for "R" to fit the smaller circle
            val originalTextSize = handlePaint.textSize
            handlePaint.textSize = originalTextSize * 0.7f
            canvas.drawText("R", rectRight, rectTop + (handlePaint.textSize / 3f), handlePaint)
            handlePaint.textSize = originalTextSize // Restore for next frame
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}