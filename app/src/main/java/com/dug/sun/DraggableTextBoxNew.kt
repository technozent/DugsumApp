package com.dug.sun
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import kotlin.math.min

class DraggableTextBoxNew @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val textView: TextView

    private var downRawX = 0f
    private var downRawY = 0f

    private var startTranslationX = 0f
    private var startTranslationY = 0f

    private var isDragging = false

    // Size limits
    private val minSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._100sdp)
    private val maxSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._500sdp)

    init {

        // ---------------------------------------------------------
        // Meter display background
        // ---------------------------------------------------------

        setBackgroundColor(
            Color.parseColor("#5EA079")
        )

        val padding = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._16sdp)
        setPadding(padding, padding, padding, padding)

        // ---------------------------------------------------------
        // Digital text
        // ---------------------------------------------------------

        textView = TextView(context).apply {

            setTextColor(Color.BLACK)

            textSize = resources.getDimension(com.intuit.ssp.R.dimen._22ssp)

            gravity = Gravity.CENTER

            includeFontPadding = false

            isClickable = false
            isFocusable = false

            typeface = try {

                Typeface.createFromAsset(
                    context.assets,
                    "fonts/desg7_classic_regular.ttf"
                )

            } catch (e: Exception) {

                Typeface.create(
                    Typeface.MONOSPACE,
                    Typeface.BOLD
                )
            }

            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }

        addView(textView)

        // ---------------------------------------------------------
        // Drag
        // ---------------------------------------------------------

        setupDrag()
    }

    // -------------------------------------------------------------
    // Set Text
    // -------------------------------------------------------------

    fun setText(text: String) {

        textView.text = text

        updateTextSize()
    }

    // -------------------------------------------------------------
    // Get Current Text
    // -------------------------------------------------------------

    fun getText(): String {

        return textView.text.toString()
    }

    // -------------------------------------------------------------
    // Automatically adjust text size
    // -------------------------------------------------------------

    private fun updateTextSize() {

        if (width <= 0 || height <= 0) {
            return
        }

        val availableWidth =
            width - paddingLeft - paddingRight

        val availableHeight =
            height - paddingTop - paddingBottom

        if (availableWidth <= 0 || availableHeight <= 0) {
            return
        }

        val size = min(
            availableWidth,
            availableHeight
        ) * 0.30f

        textView.textSize =
            size.coerceIn(
                18f,
                42f
            )
    }

    // -------------------------------------------------------------
    // Called when rectangle size changes
    // -------------------------------------------------------------

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        super.onSizeChanged(
            w,
            h,
            oldw,
            oldh
        )

        updateTextSize()
    }

    // -------------------------------------------------------------
    // Drag rectangle
    // -------------------------------------------------------------

    private fun setupDrag() {

        setOnTouchListener { view, event ->

            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {

                    downRawX = event.rawX
                    downRawY = event.rawY

                    startTranslationX =
                        view.translationX

                    startTranslationY =
                        view.translationY

                    isDragging = false

                    true
                }

                MotionEvent.ACTION_MOVE -> {

                    val dx =
                        event.rawX - downRawX

                    val dy =
                        event.rawY - downRawY

                    if (
                        kotlin.math.abs(dx) > 5 ||
                        kotlin.math.abs(dy) > 5
                    ) {
                        isDragging = true
                    }

                    val parent =
                        view.parent as? View

                    var newTx =
                        startTranslationX + dx

                    var newTy =
                        startTranslationY + dy

                    if (parent != null) {

                        val minTx =
                            -view.left.toFloat()

                        val maxTx =
                            (
                                    parent.width -
                                            view.left -
                                            view.width
                                    ).toFloat()

                        val minTy =
                            -view.top.toFloat()

                        val maxTy =
                            (
                                    parent.height -
                                            view.top -
                                            view.height
                                    ).toFloat()

                        newTx = newTx.coerceIn(
                            minTx,
                            maxTx
                        )

                        newTy = newTy.coerceIn(
                            minTy,
                            maxTy
                        )
                    }

                    view.translationX = newTx

                    view.translationY = newTy

                    true
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {

                    true
                }

                else -> false
            }
        }
    }

    // -------------------------------------------------------------
    // Increase rectangle size
    // -------------------------------------------------------------

    fun increaseSize(step: Int = 20) {

        resize(
            step
        )
    }

    // -------------------------------------------------------------
    // Decrease rectangle size
    // -------------------------------------------------------------

    fun decreaseSize(step: Int = 20) {

        resize(
            -step
        )
    }

    // -------------------------------------------------------------
    // Resize
    // -------------------------------------------------------------

    private fun resize(
        amount: Int
    ) {

        val currentWidth =
            width

        val currentHeight =
            height

        val newWidth =
            (currentWidth + amount)
                .coerceIn(
                    minSize,
                    maxSize
                )

        val newHeight =
            (currentHeight + amount)
                .coerceIn(
                    minSize,
                    maxSize
                )

        val lp =
            layoutParams

        lp.width = newWidth
        lp.height = newHeight

        layoutParams = lp

        requestLayout()

        updateTextSize()
    }
}