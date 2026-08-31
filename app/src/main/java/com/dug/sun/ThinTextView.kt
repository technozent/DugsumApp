package com.dug.sun


import android.graphics.drawable.ColorDrawable
import android.text.TextPaint
import androidx.compose.foundation.style.Style
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
class ThinTextView(context: Context) : androidx.appcompat.widget.AppCompatTextView(context) {
    var erosionWidth = 1.5f // increase to thin more, decrease for less effect

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val erodePaint = TextPaint(paint).apply {
            style = Paint.Style.STROKE
            strokeWidth = erosionWidth
            color = (background as? ColorDrawable)?.color ?: Color.BLACK // match your actual bg
        }
        canvas.drawText(text.toString(), 0f, baseline.toFloat(), erodePaint)
    }
}