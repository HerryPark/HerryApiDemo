package com.herry.libs.draw

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import com.herry.libs.util.ColorUtil


@Suppress("unused")
class ColorPickerView : FrameLayout {
    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0): super(context, attrs, defStyleAttr) {
        init()
    }

    private var radius = 0f
    private var centerX = 0f
    private var centerY = 0f

    private var pointerRadiusPx = ColorConstant.COLOR_POINTER_RADIUS_DP * resources.displayMetrics.density

    private var currentColor = Color.MAGENTA
    private val currentPoint = PointF()

    private var colorPointer: ColorPointer? = null
    private var colorListener: ColorListener? = null

    private fun init() {
        val layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val palette = ColorPalette(context)
        val padding = pointerRadiusPx.toInt()
        palette.setPadding(padding, padding, padding, padding)
        addView(palette, layoutParams)

        colorPointer = ColorPointer(context).apply {
            this.setPointerRadius(pointerRadiusPx)
            addView(this, layoutParams)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxWidth = MeasureSpec.getSize(widthMeasureSpec)
        val maxHeight = MeasureSpec.getSize(heightMeasureSpec)

        val size = maxWidth.coerceAtMost(maxHeight)
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val netWidth = w - paddingLeft - paddingRight
        val netHeight = h - paddingTop - paddingBottom
        radius = netWidth.coerceAtMost(netHeight) * 0.5f - pointerRadiusPx

        if (radius < 0) return

        centerX = netWidth * 0.5f
        centerY = netHeight * 0.5f

        setColor(currentColor)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP -> {
                update(event)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun update(event: MotionEvent) {
        val x = event.x
        val y = event.y

        currentColor = getColorAtPoint(x, y)
        pickColor(currentColor)
        updateSelector(x, y)
    }

    private fun getColorAtPoint(eventX: Float, eventY: Float): Int {
        val x = eventX - centerX
        val y = eventY - centerY
        val r = kotlin.math.sqrt(x * x + y * y.toDouble())
        val hsv = floatArrayOf(0f, 0f, 1f)
        hsv[0] = (kotlin.math.atan2(y.toDouble(), -x.toDouble()) / Math.PI * 180f).toFloat() + 180
        hsv[1] = 0f.coerceAtLeast(1f.coerceAtMost((r / radius).toFloat()))
        return Color.HSVToColor(hsv)
    }

    fun getColor() = currentColor

    fun setColor(color: Int) {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        val r = hsv[1] * radius
        val radian = (hsv[0] / 180f * Math.PI).toFloat()

        updateSelector(
            (r * kotlin.math.cos(radian.toDouble()) + centerX).toFloat(),
            (-r * kotlin.math.sin(radian.toDouble()) + centerY).toFloat()
        )

        currentColor = color
    }

    private fun updateSelector(eventX: Float, eventY: Float) {
        var x = eventX - centerX
        var y = eventY - centerY
        val r = kotlin.math.sqrt(x * x + y * y.toDouble())
        if (r > radius) {
            x *= radius / r.toFloat()
            y *= radius / r.toFloat()
        }
        currentPoint.x = x + centerX
        currentPoint.y = y + centerY
        colorPointer?.setCurrentPoint(currentPoint)
    }

    private fun pickColor(color: Int) {
        colorListener?.onColorSelected(color, ColorUtil.formatColor(color))
    }

    fun setColorListener(listener: (Int, String) -> Unit) {
        this.colorListener = object : ColorListener {
            override fun onColorSelected(color: Int, colorHex: String) {
                listener(color, colorHex)
            }
        }
    }
}