package com.herry.libs.draw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class ColorPalette : View {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0, defStyleRes: Int = 0) : super(context, attrs, defStyleAttr, defStyleRes)

    private var radius = 0f
    private var centerX = 0f
    private var centerY = 0f

    private var huePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var saturationPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var hueColors = intArrayOf(
        Color.RED,
        Color.MAGENTA,
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.YELLOW,
        Color.RED
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val netWidth = w - paddingLeft - paddingRight
        val netHeight = h - paddingTop - paddingBottom
        radius = netWidth.coerceAtMost(netHeight) * 0.5f

        if (radius < 0) return

        centerX = w * 0.5f
        centerY = h * 0.5f

        huePaint.shader = SweepGradient(centerX, centerY, hueColors, null)

        saturationPaint.shader = RadialGradient(
            centerX, centerY, radius,
            Color.WHITE, 0x00FFFFFF, Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, radius, huePaint)
        canvas.drawCircle(centerX, centerY, radius, saturationPaint)
    }
}