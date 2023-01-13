package com.herry.libs.draw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View

class ColorPointer : View {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0, defStyleRes: Int = 0) : super(context, attrs, defStyleAttr, defStyleRes)

    private var pointerRadius = ColorConstant.COLOR_POINTER_RADIUS_DP
    private var point = PointF()

    init {
        alpha = 0.5f
    }

    private var selectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0x22, 0x22, 0x22)
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(point.x, point.y, pointerRadius * 0.66f, selectorPaint)
    }

    fun setPointerRadius(pointerRadius: Float) {
        this.pointerRadius = pointerRadius
    }

    fun setCurrentPoint(point: PointF) {
        this.point = point
        invalidate()
    }
}