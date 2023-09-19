package com.herry.libs.widget.chart

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import android.text.TextUtils.SimpleStringSplitter
import android.text.TextUtils.StringSplitter
import android.util.AttributeSet
import com.herry.libs.R
import com.herry.libs.util.ViewUtil.createPaint
import com.herry.libs.util.ViewUtil.mutatePaint
import java.util.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
class RadarChartView : ChartView {
    // base chart out line color
    private val chartOutLineColor: Int

    // base chart out line width
    private val chartOutLineWidth: Float

    // base chart out line style
    private val chartOutLineStyle: LineStyle

    // base chart out line dot dash
    private val chartOutLineDotDash: Float

    // base chart out line dot gap
    private val chartOutLineDotGap: Float

    // base chart in line (axis) color
    private val chartInLineColor: Int

    // base chart in line (axis) width
    private val chartInLineWidth: Float

    // base chart in line style
    private val chartInLineStyle: LineStyle

    // base chart in line dot dash
    private val chartInLineDotDash: Float

    // base chart in line dot gap
    private val chartInLineDotGap: Float

    // base chart tick line (axis) color
    private val chartTickLineColor: Int

    // base chart tick line (axis) width
    private val chartTickLineWidth: Float

    // base chart tick line style
    private val chartTickLineStyle: LineStyle

    // base chart tick line dot dash
    private val chartTickLineDotDash: Float

    // base chart tick line dot gap
    private val chartTickLineDotGap: Float

    // base chart tick lines
    private val baseAxisTicks: MutableList<Float>

    // chart axis max value
    private val axisMaxValue: Int

    // axis radius size
    private var axisRadius: Float

    // axis title text color
    private val axisTitleColor: Int

    // margin between title and axis end
    private val axisTitleMargin: Float

    // axis title text size
    private val axisTitleSize: Float

    // direction of to draw axis on chart.
    private val chartDrawDirection: DrawDirection

    enum class LineStyle(val value: Int) {
        LINE(0), DOT(1);

        companion object {
            fun generate(value: Int): LineStyle = values().firstOrNull { it.value == value } ?: LINE
        }
    }

    // axis start location of chart
    private var chartAxisStartLocation: AxisStartLocation

    // chart shape. if total axis counts is even and chartShapeReverse is true, draw to reverse shape chart.
    private var chartShapeReverse = false
    private val axisDrawAttrs: MutableList<AxisDrawAttr>

    class AxisDrawAttr {
        // axis item point color
        var pointColor = 0

        // axis item point circle radius
        var pointRadius = 0f

        // axises link area color when chartStyle is fill.
        var linkAreaColor = 0

        // axises link line color
        var linkLineColor = 0

        // axises link line width
        var linkLineWidth = 0f

        // axises link style (stoke, fill or stroke and fill)
        var linkLineStyle: LineStyle = LineStyle.LINE

        // axises link line dash value of dot
        var linkLineDotDash = 0f

        // axises link line gap value of dot
        var linkLineDotGap = 0f

    }

    class Axis(val axisDrawAttr: AxisDrawAttr? = null, val axises: List<Float>)

    // Integer value is axis id. String value is axis title
    private val baseAxisTitles: MutableList<String>
    private val axises: MutableList<Axis>
    private val textRect: Rect
    private val path: Path
    private val textPaint: TextPaint
    private val paint: Paint
    private var centerX = 0
    private var centerY = 0
    private val baseAxisVertices: MutableList<AxisVertices>
    private var ratio = 0f
    private val axisVertices: MutableList<List<AxisVertices>>

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        // base
        baseAxisTitles = ArrayList()
        baseAxisTicks = ArrayList()
        baseAxisVertices = ArrayList()

        // itmes
        axises = ArrayList()
        axisVertices = ArrayList()
        axisDrawAttrs = ArrayList()
        val metrics = resources.displayMetrics

        // draw path
        path = Path()

        // text paint
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        textPaint.density = metrics.density
        // text rect
        textRect = Rect()
        // draw paint
        paint = createPaint()
        val attr = context.obtainStyledAttributes(attrs, R.styleable.RadarChartView, defStyleAttr, 0)
        chartAxisStartLocation = AxisStartLocation.generate(attr.getInt(R.styleable.RadarChartView_rcv_startLocation, AxisStartLocation.NORTH.value))
        chartDrawDirection = DrawDirection.generate(attr.getInt(R.styleable.RadarChartView_rcv_direction, DrawDirection.RIGHT.value))
        chartOutLineColor = attr.getColor(R.styleable.RadarChartView_rcv_outLineColor, Color.BLACK)
        chartOutLineWidth = attr.getDimensionPixelSize(R.styleable.RadarChartView_rcv_outLineWidth, 0).toFloat()
        chartOutLineStyle = LineStyle.generate(attr.getInt(R.styleable.RadarChartView_rcv_outLineStyle, LineStyle.LINE.value))
        chartOutLineDotDash = attr.getDimensionPixelSize(R.styleable.RadarChartView_rcv_outLineDotDash, 0).toFloat()
        chartOutLineDotGap = attr.getDimensionPixelSize(R.styleable.RadarChartView_rcv_outLineDotGap, 0).toFloat()
        chartInLineColor = attr.getColor(R.styleable.RadarChartView_rcv_inLineColor, Color.GRAY)
        chartInLineWidth = attr.getDimensionPixelSize(R.styleable.RadarChartView_rcv_inLineWidth, 0).toFloat()
        chartInLineStyle = LineStyle.generate(attr.getInt(R.styleable.RadarChartView_rcv_inLineStyle, LineStyle.DOT.value))
        chartInLineDotDash = attr.getDimensionPixelSize(R.styleable.RadarChartView_rcv_inLineDotDash, 0).toFloat()
        chartInLineDotGap = attr.getDimensionPixelSize(R.styleable.RadarChartView_rcv_inLineDotGap, 0).toFloat()
        chartTickLineColor = attr.getColor(R.styleable.RadarChartView_rcv_tickLineColor, Color.GRAY)
        chartTickLineWidth = attr.getDimensionPixelSize(R.styleable.RadarChartView_rcv_tickLineWidth, 0).toFloat()
        chartTickLineStyle = LineStyle.generate(attr.getInt(R.styleable.RadarChartView_rcv_tickLineStyle, LineStyle.LINE.value))
        chartTickLineDotDash = attr.getDimensionPixelSize(R.styleable.RadarChartView_rcv_tickLineDotDash, 0).toFloat()
        chartTickLineDotGap = attr.getDimensionPixelSize(R.styleable.RadarChartView_rcv_tickLineDotGap, 0).toFloat()
        axisRadius = attr.getDimensionPixelSize(R.styleable.RadarChartView_rcv_radius, 0).toFloat()
        axisMaxValue = attr.getInt(R.styleable.RadarChartView_rcv_maxValue, 100)
        axisTitleColor = attr.getColor(R.styleable.RadarChartView_rcv_titlesColor, Color.BLACK)
        axisTitleMargin = attr.getDimensionPixelSize(R.styleable.RadarChartView_rcv_titlesMargin, 0).toFloat()
        axisTitleSize = attr.getDimensionPixelSize(R.styleable.RadarChartView_rcv_titlesSize, 0).toFloat()

        // sets titles
        val titles = attr.getString(R.styleable.RadarChartView_rcv_titles)
        if (!TextUtils.isEmpty(titles)) {
            val splitter: StringSplitter = SimpleStringSplitter(',')
            splitter.setString(titles)
            val baseAxisTitles: MutableList<String> = ArrayList()
            for (s in splitter) {
                baseAxisTitles.add(s)
            }
            setTitles(baseAxisTitles)
        }

        // sets ticks
        val ticks = attr.getString(R.styleable.RadarChartView_rcv_ticks)
        if (!TextUtils.isEmpty(ticks)) {
            val splitter: StringSplitter = SimpleStringSplitter(',')
            splitter.setString(ticks)
            val baseTicks: MutableList<Float> = ArrayList()
            for (s in splitter) {
                if (TextUtils.isEmpty(s) || TextUtils.isEmpty(s.trim { it <= ' ' })) {
                    continue
                }
                try {
                    val tick = java.lang.Float.valueOf(s.trim { it <= ' ' })
                    baseTicks.add(tick)
                } catch (ignore: NumberFormatException) {
                }
            }
            setTicks(baseTicks)
        }

        // sets items
        /*String items = attr.getString(R.styleable.RadarChartView_rcv_items);
        if (!TextUtils.isEmpty(items)) {
            try {
                JSONArray itemsArray =
            } catch (JSONException ignore) {
            }
            TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(',');
            splitter.setString(titles);
            final List<String> baseAxisTitles = new ArrayList<>();
            for (String s : splitter) {
                baseAxisTitles.add(s);
            }

            setTitles(baseAxisTitles);
        }*/attr.recycle()
    }

    /**
     * Sets axis draw attributes.
     */
    private fun setAxisDrawAttrs(axisDrawAttrs: List<AxisDrawAttr>?) {
        this.axisDrawAttrs.clear()
        if (null != axisDrawAttrs) {
            this.axisDrawAttrs.addAll(axisDrawAttrs)
        }
    }

    /*    public final void clearTitles() {
        baseAxisTitles.clear();
        onChangedBaseAxises();
    }*/
    fun setTitles(titles: List<String>) {
        baseAxisTitles.clear()
        baseAxisTitles.addAll(titles)
        onChangedBaseAxises()
    }

    fun setTicks(ticks: List<Float>) {
        baseAxisTicks.clear()
        baseAxisTicks.addAll(ticks)
        onChangedBaseAxises()
    }

    fun setAxises(axises: List<Axis>) {
        this.axises.clear()
        this.axises.addAll(axises)
    }

    fun setDataChanged() {
        invalidate()
    }

    fun setChartAxisStartLocation(chartAxisStartLocation: AxisStartLocation) {
        this.chartAxisStartLocation = chartAxisStartLocation
        onChangedBaseAxises()
    }

    fun setChartShapeReverse(chartShapeReverse: Boolean) {
        this.chartShapeReverse = chartShapeReverse
        onChangedBaseAxises()
    }

    fun setTextSize(textSize: Float) {
        textPaint.textSize = textSize
        invalidate()
    }

    fun setAxisRadius(axisRadius: Float) {
        this.axisRadius = axisRadius
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        calculateCenter()
        calcRatio()
        buildVertices()
        buildItemVertices()
    }

    override fun onDraw(canvas: Canvas) {
        drawBase(canvas)
        drawTicks(canvas)
        drawTitles(canvas)
        drawAxises(canvas)
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

    private fun buildVertices() {
        baseAxisVertices.clear()
        val count = baseAxisTitles.size
        baseAxisVertices.addAll(createPoints(count, axisRadius, centerX.toFloat(), centerY.toFloat()))
    }

    private fun buildItemVertices() {
        axisVertices.clear()
        for (axis in axises) {
            val itemAxisVertices: MutableList<AxisVertices> = ArrayList()
            val itemAxises = axis.axises
            for (index in baseAxisVertices.indices) {
                var radius = 0f
                val axisVertices = baseAxisVertices[index]
                // sets degree from baseAxis
                val degree = axisVertices.degree
                if (-1.0 != degree && index < itemAxises.size) {
                    val itemAxisValue = itemAxises[index]
                    radius = itemAxisValue * ratio
                }
                val point = createPoint(radius, degree, centerX.toFloat(), centerY.toFloat())
                itemAxisVertices.add(AxisVertices(point, degree))
            }
            axisVertices.add(itemAxisVertices)
        }
    }

    private fun calcRatio() {
        ratio = if (axisMaxValue > 0) axisRadius / axisMaxValue else 1f
    }

    private fun calculateCenter() {
        centerX = (measuredWidth shr 1) + paddingLeft - paddingRight
        centerY = (measuredHeight shr 1) + paddingTop - paddingBottom
    }

    // gets axis draw attributes from input axises data.
    private fun getAxisDraw(index: Int): AxisDrawAttr? {
        var axisDrawAttr: AxisDrawAttr? = null
        val count = axises.size
        var axis: Axis? = null
        if (index in 0 until count) {
            axis = axises[index]
        } else if (0 < count) {
            axis = axises[count - 1]
        }
        if (null != axis) {
            axisDrawAttr = axis.axisDrawAttr
        } else {
            val axisAttrsCount = axisDrawAttrs.size
            if (index in 0 until axisAttrsCount) {
                axisDrawAttr = axisDrawAttrs[index]
            } else if (0 < axisAttrsCount) {
                axisDrawAttr = axisDrawAttrs[count - 1]
            }
        }
        return axisDrawAttr
    }

    private fun drawAxises(canvas: Canvas) {
        for (axisGroupIndex in axisVertices.indices) {
            val axisVerticesList = axisVertices[axisGroupIndex]
            val axisDrawAttr = getAxisDraw(axisGroupIndex) ?: continue
            val count = axisVerticesList.size
            path.reset()
            for (index in 0 until count) {
                val axisVertices = axisVerticesList[index]
                val point = axisVertices.point
                val pointX = point.x.toFloat()
                val pointY = point.y.toFloat()

                // draws point
                mutatePaint(paint, axisDrawAttr.pointColor, 0f, Paint.Style.FILL)
                canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), axisDrawAttr.pointRadius, paint)

                // checks counts
                if (1 == count) {
                    // draw line from center
                    path.moveTo(centerX.toFloat(), centerY.toFloat())
                    path.lineTo(pointX, pointY)
                } else {
                    // sets link path
                    if (0 == index) {
                        // sets start position of drawing
                        path.moveTo(pointX, pointY)
                    }
                    if (index + 1 < count) {
                        val nextAxisVertices = axisVerticesList[index + 1]
                        val nextPoint = nextAxisVertices.point
                        path.lineTo(nextPoint.x.toFloat(), nextPoint.y.toFloat())
                    } else {
                        val firstAxisVertices = axisVerticesList[0]
                        val firstPoint = firstAxisVertices.point
                        path.lineTo(firstPoint.x.toFloat(), firstPoint.y.toFloat())
                    }
                }
            }
            if (!path.isEmpty) {
                path.close()
            }

            // draws link area
            mutatePaint(paint, axisDrawAttr.linkAreaColor, 0f, Paint.Style.FILL)
            canvas.drawPath(path, paint)

            // draws link line
            mutatePaint(paint, axisDrawAttr.linkLineColor, axisDrawAttr.linkLineWidth, Paint.Style.STROKE)
            if (LineStyle.DOT == axisDrawAttr.linkLineStyle && 0 < axisDrawAttr.linkLineDotDash) {
                setPathEffect(paint, axisDrawAttr.linkLineDotDash, axisDrawAttr.linkLineDotGap)
            }
            canvas.drawPath(path, paint)
            clearPathEffect(paint)
        }
    }

    private fun drawBase(canvas: Canvas) {
        val count = baseAxisVertices.size

        // draws in line
        if (0 < count) {
            path.reset()
            for (axisVertices in baseAxisVertices) {
                val point = axisVertices.point

                // draws out line
                val pointX = point.x.toFloat()
                val pointY = point.y.toFloat()

                // draws in line
                path.moveTo(centerX.toFloat(), centerY.toFloat())
                path.lineTo(pointX, pointY)
            }
            path.moveTo(centerX.toFloat(), centerY.toFloat())
            path.close()

            // sets paint attributes
            mutatePaint(paint, chartInLineColor, chartInLineWidth, Paint.Style.STROKE)
            if (LineStyle.DOT == chartInLineStyle && 0 < chartInLineDotDash) {
                setPathEffect(paint, chartInLineDotDash, chartInLineDotGap)
            }
            canvas.drawPath(path, paint)
            clearPathEffect(paint)
        }

        // draws base out lines
        // axis total counts less than 3, draw to circle chart
        if (count in 1..2) {
            // draws out line
            mutatePaint(paint, chartOutLineColor, chartOutLineWidth, Paint.Style.STROKE)
            if (LineStyle.DOT == chartOutLineStyle && 0 < chartOutLineDotDash) {
                setPathEffect(paint, chartOutLineDotDash, chartOutLineDotGap)
            }
            canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), axisRadius, paint)
            clearPathEffect(paint)
        } else {
            path.reset()
            for (index in 0 until count) {
                val axisVertices = baseAxisVertices[index]
                val point = axisVertices.point

                // draws out line
                val pointX = point.x.toFloat()
                val pointY = point.y.toFloat()

                // sets out line path
                if (0 == index) {
                    // sets start position of drawing
                    path.moveTo(pointX, pointY)
                }
                if (index + 1 < count) {
                    val nextAxisVertices = baseAxisVertices[index + 1]
                    val nextPoint = nextAxisVertices.point
                    path.lineTo(nextPoint.x.toFloat(), nextPoint.y.toFloat())
                } else {
                    val firstAxisVertices = baseAxisVertices[0]
                    val firstPoint = firstAxisVertices.point
                    path.lineTo(firstPoint.x.toFloat(), firstPoint.y.toFloat())
                }
            }
            if (!path.isEmpty) {
                path.close()
            }
            mutatePaint(paint, chartOutLineColor, chartOutLineWidth, Paint.Style.STROKE)
            if (LineStyle.DOT == chartOutLineStyle && 0 < chartOutLineDotDash) {
                setPathEffect(paint, chartOutLineDotDash, chartOutLineDotGap)
            }
            canvas.drawPath(path, paint)
            clearPathEffect(paint)
        }
    }

    private fun setPathEffect(paint: Paint?, dash: Float, gap: Float) {
        if (null == paint) {
            return
        }
        paint.pathEffect = DashPathEffect(floatArrayOf(dash, gap), 0f)
    }

    private fun clearPathEffect(paint: Paint?) {
        if (null == paint) {
            return
        }
        paint.pathEffect = null
    }

    private fun drawTitles(canvas: Canvas) {
        val count = baseAxisVertices.size

        // draws titles
        for (index in 0 until count) {
            val axisVertices = baseAxisVertices[index]
            val point = axisVertices.point
            val textPoint = createPoint(axisTitleMargin + chartOutLineWidth, axisVertices.degree, point.x.toFloat(), point.y.toFloat())
            val pointX = textPoint.x.toFloat()
            val pointY = textPoint.y.toFloat()
            val axisTitle = baseAxisTitles[index]

            // sets title position
            textPaint.color = axisTitleColor
            textPaint.textSize = axisTitleSize
            textPaint.getTextBounds(axisTitle, 0, axisTitle.length, textRect)
            val x: Float = if (pointX == centerX.toFloat()) {
                pointX - textRect.width() / 2
            } else if (pointX > centerX) {
                pointX
            } else {
                pointX - textRect.width()
            }
            val y: Float = if (pointY == centerY.toFloat()) {
                pointY + textRect.height() / 2
            } else if (pointY > centerY) {
                pointY + textRect.height()
            } else {
                pointY
            }
            canvas.drawText(axisTitle, x, y, textPaint)
        }
    }

    private fun drawTicks(canvas: Canvas) {
        val tickCount = baseAxisTicks.size
        for (tickIndex in 0 until tickCount) {
            val tick = baseAxisTicks[tickIndex]
            var tickValue = tick
            if (0 >= tickValue) {
                tickValue = 0f
            } else if (tickValue >= axisMaxValue) {
                tickValue = axisMaxValue.toFloat()
            }
            val radius = tickValue * ratio
            val count = baseAxisVertices.size

            // draws base tick lines
            // axis total counts less than 3, draw to circle chart
            if (count in 1..2) {
                // draws out line
                mutatePaint(paint, chartTickLineColor, chartTickLineWidth, Paint.Style.STROKE)
                if (LineStyle.DOT == chartTickLineStyle && 0 < chartTickLineDotDash) {
                    setPathEffect(paint, chartTickLineDotDash, chartTickLineDotGap)
                }
                canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), radius, paint)
                clearPathEffect(paint)
            } else {
                path.reset()
                for (index in 0 until count) {
                    val axisVertices = baseAxisVertices[index]
                    // sets degree from baseAxis
                    val degree = axisVertices.degree
                    val point: Point = createPoint(radius, degree, centerX.toFloat(), centerY.toFloat())

                    // draws out line
                    val pointX = point.x.toFloat()
                    val pointY = point.y.toFloat()

                    // sets out line path
                    if (0 == index) {
                        // sets start position of drawing
                        path.moveTo(pointX, pointY)
                    }
                    if (index + 1 < count) {
                        val nextAxisVertices = baseAxisVertices[index + 1]

                        // sets degree from baseAxis
                        val nextDegree = nextAxisVertices.degree
                        val nextPoint = createPoint(radius, nextDegree, centerX.toFloat(), centerY.toFloat())
                        path.lineTo(nextPoint.x.toFloat(), nextPoint.y.toFloat())
                    } else {
                        val firstAxisVertices = baseAxisVertices[0]

                        // sets degree from baseAxis
                        val firstDegree = firstAxisVertices.degree
                        val firstPoint = createPoint(radius, firstDegree, centerX.toFloat(), centerY.toFloat())
                        path.lineTo(firstPoint.x.toFloat(), firstPoint.y.toFloat())
                    }
                }
                if (!path.isEmpty) {
                    path.close()
                }
                mutatePaint(paint, chartTickLineColor, chartTickLineWidth, Paint.Style.STROKE)
                if (LineStyle.DOT == chartTickLineStyle && 0 < chartTickLineDotDash) {
                    setPathEffect(paint, chartTickLineDotDash, chartTickLineDotGap)
                }
                canvas.drawPath(path, paint)
                clearPathEffect(paint)
            }
        }
    }

    private fun onChangedBaseAxises() {
        calcRatio()
        buildVertices()
        invalidate()
    }

    private fun createPoints(count: Int, radius: Float, x0: Float, y0: Float): List<AxisVertices> {
        val axisVerticesList = LinkedList<AxisVertices>()
        if (0 == count) {
            return axisVerticesList
        }
        val angle = 2 * StrictMath.PI / count
        val startAngle: Double = when (chartAxisStartLocation) {
            AxisStartLocation.NORTH -> {
                0.0 // 0 degree
            }
            AxisStartLocation.EAST -> {
                StrictMath.PI / 2 // 90 degree
            }
            AxisStartLocation.SOUTH -> {
                StrictMath.PI // 180 degree
            }
            AxisStartLocation.WEST -> {
                3 * StrictMath.PI / 2 // 270 degree
            }
        }
        val isEventCounts = count % 2 == 0
        val startAlpha: Double = ((if (isEventCounts || chartShapeReverse) angle / 2.toDouble() else 0.toDouble()) // sets additional degree
                + startAngle) // adds degree of start location
        for (index in 0 until count) {
            val alpha = startAlpha + (angle * index - StrictMath.PI) / 2
            val point = createPoint(radius, alpha, x0, y0)
            if (DrawDirection.RIGHT === chartDrawDirection) {
                axisVerticesList.add(AxisVertices(point, alpha))
            } else {
                axisVerticesList.addFirst(AxisVertices(point, alpha))
            }
        }
        return axisVerticesList
    }
}