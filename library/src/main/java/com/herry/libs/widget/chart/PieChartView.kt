package com.herry.libs.widget.chart

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.herry.libs.R


/**
 * Created by herry.park
 */
@Suppress("unused")
open class PieChartView : View {
    private var chartLineWidth = 2f
    private var bgColor = -0x1
    private var charStyle = 0 // 0: line, 1: fill
    protected var chartWidthRatio = 0.5f
    protected var innerFillPaint: Paint? = null
    protected var innerStrokePaint: Paint? = null
    protected var values: IntArray = intArrayOf(50, 50)

    protected var colors: IntArray = intArrayOf(0xffff0000.toInt(), 0xff00ff00.toInt())
    protected var paint: Paint? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        // parse attrs
        if (null != attrs) {
            val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.PieChartView)
            chartLineWidth = typedArray.getDimension(R.styleable.PieChartView_pieChartViewLineWidth, chartLineWidth)
            bgColor = typedArray.getColor(R.styleable.PieChartView_pieChartViewBackgroundColor, bgColor)
            charStyle = typedArray.getInt(R.styleable.PieChartView_pieChartViewStyle, charStyle)
            chartWidthRatio = typedArray.getFloat(R.styleable.PieChartView_pieChartViewWidthRatio, chartWidthRatio)
            typedArray.recycle()
        }

        paint = Paint().apply {
            when (charStyle) {
                1 -> this.style = Paint.Style.FILL
                0 -> this.style = Paint.Style.STROKE
                else -> this.style = Paint.Style.STROKE
            }
            this.strokeWidth = chartLineWidth
            this.isAntiAlias = true
        }

        innerFillPaint = Paint().apply {
            this.style = Paint.Style.FILL
            this.color = bgColor
            this.isAntiAlias = true
        }

        innerStrokePaint = Paint().apply {
            this.style = Paint.Style.STROKE
            this.isAntiAlias = true
            this.strokeWidth = chartLineWidth
        }
    }

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
            paddingTop.toFloat(),
            (width - paddingRight).toFloat(),
            (height - paddingBottom).toFloat()
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
        var currentPosition = -90f
        for (index in 0 until counts) {
            paint.color = colors[index]
            val thita: Float = if (0f == total) 0f else 360f * values[index] / total
            canvas.drawArc(bounds, currentPosition, thita, true, paint)
            currentPosition += thita
        }
        innerFillPaint?.let {  innerFillPaint ->
            canvas.drawArc(innerBounds, -90f, 360f, true, innerFillPaint)
        }
        currentPosition = -90f
        for (index in 0 until counts) {
            innerStrokePaint?.color = colors[index]
            val thita: Float = if (0f == total) 0f else 360 * values[index] / total
            innerStrokePaint?.let {  innerStrokePaint ->
                canvas.drawArc(innerBounds, currentPosition, thita, false, innerStrokePaint)
            }
            currentPosition += thita
        }
    }

    /**
     * Use onSizeChanged instead of onAttachedToWindow to get the dimensions of the view,
     * because this method is called after measuring the dimensions of MATCH_PARENT & WRAP_CONTENT.
     * Use this dimensions to setup the bounds and paints.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // The first thing that happen is that we call the superclass
        // implementation of onMeasure. The reason for that is that measuring
        // can be quite a complex process and calling the super method is a
        // convenient way to get most of this complexity handled.
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // We can’t use getWidth() or getHight() here. During the measuring
        // pass the view has not gotten its final size yet (this happens first
        // at the start of the layout pass) so we have to use getMeasuredWidth()
        // and getMeasuredHeight().
        val width = measuredWidth
        val height = measuredHeight
        val widthWithoutPadding = width - paddingLeft - paddingRight
        val heightWithoutPadding = height - paddingTop - paddingBottom

        // Finally we have some simple logic that calculates the size of the view
        // and calls setMeasuredDimension() to set that size.
        // Before we compare the width and height of the view, we remove the padding,
        // and when we set the dimension we add it back again. Now the actual content
        // of the view will be square, but, depending on the padding, the total dimensions
        // of the view might not be.
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val size: Int = if (heightMode != MeasureSpec.UNSPECIFIED && widthMode != MeasureSpec.UNSPECIFIED) {
            if (widthWithoutPadding > heightWithoutPadding) {
                heightWithoutPadding
            } else {
                widthWithoutPadding
            }
        } else {
            kotlin.math.max(heightWithoutPadding, widthWithoutPadding)
        }

        // If you override onMeasure() you have to call setMeasuredDimension().
        // This is how you report back the measured size.  If you don’t call
        // setMeasuredDimension() the parent will throw an exception and your
        // application will crash.
        // We are calling the onMeasure() method of the superclass so we don’t
        // actually need to call setMeasuredDimension() since that takes care
        // of that. However, the purpose with overriding onMeasure() was to
        // change the default behaviour and to do that we need to call
        // setMeasuredDimension() with our own values.
        setMeasuredDimension(
            size + paddingLeft + paddingRight,
            size + paddingTop + paddingBottom
        )
    }

    fun setData(values: IntArray, colors: IntArray) {
        this.values = values
        this.colors = colors
        invalidate()
    }

    protected fun getTotal(): Float {
        var total = 0
        for (index in values.indices) {
            total += values[index]
        }
        return total.toFloat()
    }
}