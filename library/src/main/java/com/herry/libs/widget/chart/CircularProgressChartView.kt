package com.herry.libs.widget.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.RectF
import android.text.TextUtils
import android.text.TextUtils.SimpleStringSplitter
import android.text.TextUtils.StringSplitter
import android.util.AttributeSet
import androidx.annotation.FloatRange
import com.herry.libs.R
import com.herry.libs.util.ViewUtil.createPaint
import com.herry.libs.util.ViewUtil.mutatePaint

@Suppress("unused", "MemberVisibilityCanBePrivate")
class CircularProgressChartView : ChartView {
    private var centerX = 0
    private var centerY = 0
    private val paint: Paint

    // chart axis max value
    private var axisMaxValue: Int
    private val axisThickness: Int

    // axis radius size
    private val chartRadius: Float
    private val chartBGColor: Int

    @FloatRange(from = 360.0, to = 360.0)
    private val chartMaxAngle: Float
    private val chartAxisGap: Int

    // axis start location of chart
    private val chartAxisStartLocation: AxisStartLocation

    // direction of to draw axis on chart.
    private val chartDrawDirection: DrawDirection
    private val chartStartEdgeStyle: EdgeStyle
    private val chartEndEdgeStyle: EdgeStyle

    private enum class EdgeStyle(val value: Int) {
        BUTT(0), ROUND(1);

        companion object {
            fun generate(value: Int): EdgeStyle = values().firstOrNull { it.value == value } ?: BUTT
        }
    }

    private val axises: MutableList<Axis>

    class AxisDrawAttr {
        var radius = 0f
        var thickness = 0
        var barColor = 0
        var barBaseColor = 0
    }

    class Axis(val axisDrawAttr: AxisDrawAttr? = null, val axisValue: Float)

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressChartView, defStyleAttr, 0)
        chartAxisStartLocation = AxisStartLocation.generate(attr.getInt(R.styleable.CircularProgressChartView_ccv_startLocation, AxisStartLocation.NORTH.value))
        chartDrawDirection = DrawDirection.generate(attr.getInt(R.styleable.CircularProgressChartView_ccv_direction, DrawDirection.RIGHT.value))
        chartRadius = attr.getDimensionPixelSize(R.styleable.CircularProgressChartView_ccv_radius, 0).toFloat()
        chartBGColor = attr.getColor(R.styleable.CircularProgressChartView_ccv_baseColor, Color.WHITE)
        chartMaxAngle = attr.getFloat(R.styleable.CircularProgressChartView_ccv_maxAngle, 360.0f)
        chartAxisGap = attr.getDimensionPixelSize(R.styleable.CircularProgressChartView_ccv_axisGap, 0)
        axisMaxValue = attr.getInt(R.styleable.CircularProgressChartView_ccv_maxValue, 100)
        if (0 == axisMaxValue) {
            axisMaxValue = 1
        }
        chartStartEdgeStyle = EdgeStyle.generate(attr.getInt(R.styleable.CircularProgressChartView_ccv_startEdge, EdgeStyle.BUTT.value))
        chartEndEdgeStyle = EdgeStyle.generate(attr.getInt(R.styleable.CircularProgressChartView_ccv_endEdge, EdgeStyle.BUTT.value))
        axisThickness = attr.getDimensionPixelSize(R.styleable.CircularProgressChartView_ccv_barThickness, 0)

        // initialize values
        paint = createPaint()
        axises = ArrayList()
        // sets ticks
        val bars = attr.getString(R.styleable.CircularProgressChartView_ccv_bars)
        if (!TextUtils.isEmpty(bars)) {
            val splitter: StringSplitter = SimpleStringSplitter(',')
            splitter.setString(bars)
            val axises: MutableList<Axis> = ArrayList()
            for (s in splitter) {
                if (TextUtils.isEmpty(s) || TextUtils.isEmpty(s.trim { it <= ' ' })) {
                    continue
                }
                try {
                    val drawAttr = AxisDrawAttr()
                    drawAttr.barBaseColor = -0xd0d0e
                    drawAttr.barColor = -0x19fff1
                    val value = java.lang.Float.valueOf(s.trim { it <= ' ' })
                    axises.add(Axis(drawAttr, value))
                } catch (ignore: NumberFormatException) {
                }
            }
            setAxises(axises)
        }
        attr.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // The first thing that happen is that we call the superclass
        // implementation of onMeasure. The reason for that is that measuring
        // can be quite a complex process and calling the super method is a
        // convenient way to get most of this complexity handled.
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // We can’t use getWidth() or getHeight() here. During the measuring
        // pass the view has not gotten its final size yet (this happens first
        // at the start of the layout pass) so we have to use getMeasuredWidth()
        // and getMeasuredHeight().
        val size: Int
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
        size = if (heightMode != MeasureSpec.UNSPECIFIED && widthMode != MeasureSpec.UNSPECIFIED) {
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        calculateCenter()
    }

    private fun calculateCenter() {
        centerX = (measuredWidth shr 1) + paddingLeft - paddingRight
        centerY = (measuredHeight shr 1) + paddingTop - paddingBottom
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBase(canvas)
        drawAxises(canvas)
    }

    private fun drawBase(canvas: Canvas) {
        val radius = chartRadius
        mutatePaint(paint, chartBGColor, 0f, Paint.Style.FILL)
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), radius, paint)
    }

    private fun drawAxises(canvas: Canvas) {
        for (axisIndex in axises.indices) {
            val axis = axises[axisIndex]
            val axisDrawAttr = axis.axisDrawAttr ?: continue


            // draws base bar
            val barBaseColor = axisDrawAttr.barBaseColor
            val barColor = axisDrawAttr.barColor
            val barMax = chartMaxAngle
            val barValue = axis.axisValue
            val thickness = axisThickness.toFloat()
            val gap = chartAxisGap
            val radius = chartRadius - thickness / 2 - thickness * axisIndex - gap * axisIndex
            val startAngle = getChartStartAngle()
            val baseBarSweepAngle = chartMaxAngle
            val barSweepAngle = getSweepAngle(barMax * barValue / axisMaxValue)

            // sets drawing bounds
            val bound = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
            var startEdgeRound = false
            var endEdgeRound = false
            if (EdgeStyle.ROUND == chartStartEdgeStyle && EdgeStyle.ROUND == chartEndEdgeStyle) {
                startEdgeRound = true
                endEdgeRound = true
            } else {
                if (EdgeStyle.BUTT == chartStartEdgeStyle && EdgeStyle.ROUND == chartEndEdgeStyle) {
                    endEdgeRound = true
                } else if (EdgeStyle.ROUND == chartStartEdgeStyle && EdgeStyle.BUTT == chartEndEdgeStyle) {
                    startEdgeRound = true
                }
            }
            var edgeCap = Cap.BUTT
            if (startEdgeRound && endEdgeRound) {
                edgeCap = Cap.ROUND
            }

            // draws base bar
            mutatePaint(paint, barBaseColor, thickness, Paint.Style.STROKE)
            paint.strokeCap = edgeCap
            canvas.drawArc(bound, startAngle, baseBarSweepAngle, false, paint)
            if (!startEdgeRound && endEdgeRound) {
                // draws end point
                drawEdgePoint(canvas, paint, radius, thickness, barBaseColor, baseBarSweepAngle)
            }

            // draws bar
            mutatePaint(paint, barColor, thickness, Paint.Style.STROKE)
            paint.strokeCap = edgeCap
            canvas.drawArc(bound, startAngle, barSweepAngle, false, paint)
            if (!startEdgeRound && endEdgeRound) {
                // draws end point
                drawEdgePoint(canvas, paint, radius, thickness, barColor, barSweepAngle)
            }
        }
    }

    private fun drawEdgePoint(canvas: Canvas, paint: Paint, radius: Float, thickness: Float, color: Int, sweepAngle: Float) {
        mutatePaint(paint, color, 0f, Paint.Style.FILL)
        var pointDegree = if (1 < sweepAngle) sweepAngle - 1 else sweepAngle
        pointDegree += when (chartAxisStartLocation) {
            AxisStartLocation.NORTH -> {
                0f // + 0;
            }
            AxisStartLocation.EAST -> {
                90f
            }
            AxisStartLocation.SOUTH -> {
                180f
            }
            AxisStartLocation.WEST -> {
                270f
            }
        }

        // gets end point
        val endPoint = createPoint(radius, convertDegreeToRadian(pointDegree), centerX.toFloat(), centerY.toFloat())
        val pointSweepAngle = getSweepAngle(180f)
        val pointStartAngle = getDegree(centerX.toFloat(), centerY.toFloat(), endPoint.x.toFloat(), endPoint.y.toFloat()).toFloat()
        val pointBound = RectF(
            endPoint.x - thickness / 2,
            endPoint.y - thickness / 2,
            endPoint.x + thickness / 2,
            endPoint.y + thickness / 2
        )
        canvas.drawArc(pointBound, pointStartAngle, pointSweepAngle, true, paint)
    }

    private fun getChartStartAngle(): Float {
        /* start angle values:
        *         270 (3pi /2)
        *  180 (pi)         0 or 360 (0 or 2pi)
        *          90 (pi / 2)
        */
        val startAngle: Float = when (chartAxisStartLocation) {
            AxisStartLocation.NORTH -> {
                270f
            }
            AxisStartLocation.EAST -> {
                0f
            }
            AxisStartLocation.SOUTH -> {
                90f
            }
            AxisStartLocation.WEST -> {
                180f
            }
        }
        return startAngle
    }

    private fun getSweepAngle(angle: Float): Float {
        return when (chartDrawDirection) {
            DrawDirection.LEFT -> -1 * angle
            else -> angle
        }
    }

    fun setAxises(axises: List<Axis>) {
        this.axises.clear()
        this.axises.addAll(axises)
    }

    fun setDataChanged() {
        invalidate()
    }
}