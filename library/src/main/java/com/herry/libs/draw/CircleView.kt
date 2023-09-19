package com.herry.libs.draw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

@Suppress("unused")
class CircleView : View {

    private val paint = Paint()
    private var radius = 8f

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0): super(context, attrs, defStyleAttr) {
        paint.apply {
            this.color = Color.BLACK
            this.style = Paint.Style.FILL
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height / 2

        canvas.drawCircle(centerX, centerY, radius / 2, paint)
    }

    fun setRadius(radius: Float) {
        this.radius = radius
        invalidate()
    }

    fun setAlpha(alpha: Int) {
        paint.alpha = (alpha * 255) / 100
        invalidate()
    }

    fun setColor(color: Int) {
        paint.color = color
        invalidate()
    }
}