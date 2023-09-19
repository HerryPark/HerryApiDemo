package com.herry.libs.widget.chart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet

/**
 * Created by herry.park
 */
class HalfPieChartView : PieChartView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val paint = paint ?: return

        val width = width
        val height = height

        // Width should equal to Height, find the min value to setup the circle
        val minValue = kotlin.math.min(width, height)

        // Calc the Offset if needed
        val xOffset = width - minValue
        val yOffset = height - minValue

        // Add the offset
        val paddingTop = this.paddingTop + yOffset / 2
        val paddingBottom = this.paddingBottom + yOffset / 2
        val paddingLeft = this.paddingLeft + xOffset / 2
        val paddingRight = this.paddingRight + xOffset / 2
        val bounds = RectF(
            paddingLeft.toFloat(),
            (paddingTop + minValue / 2).toFloat(),
            (width - paddingRight).toFloat(),
            (height - paddingBottom + minValue / 2).toFloat()
        )
        val innerBoundsWidth = bounds.width() * chartWidthRatio
        val innerBoundsHeight = bounds.height() * chartWidthRatio
        val innerBounds = RectF(
            (bounds.width() - innerBoundsWidth) / 2,
            bounds.top + (bounds.height() - innerBoundsHeight) / 2,
            (bounds.width() + innerBoundsWidth) / 2,
            bounds.top + (bounds.height() + innerBoundsHeight) / 2
        )

        val total = getTotal()
        val counts = values.size
        var currentPosition = -180f
        for (index in 0 until counts) {
            paint.color = colors[index]
            val thita: Float = if (0f == total) 0f else 180 * values[index] / total
            canvas.drawArc(bounds, currentPosition, thita, true, paint)
            currentPosition += thita
        }
        innerFillPaint?.let { innerFillPaint ->
            canvas.drawArc(innerBounds, -180f, 360f, true, innerFillPaint)
        }
        innerStrokePaint?.let { innerStrokePaint ->
            currentPosition = -180f
            for (index in 0 until counts) {
                innerStrokePaint.color = colors[index]
                val thita: Float = if (0f == total) 0f else 180 * values[index] / total
                canvas.drawArc(innerBounds, currentPosition, thita, false, innerStrokePaint)
                currentPosition += thita
            }
        }
    }
}