package com.herry.libs.widget.view.spinner

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import androidx.annotation.IntRange
import com.herry.libs.R
import com.herry.libs.widget.extension.convertDpToPx
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToLong

@Suppress("unused", "MemberVisibilityCanBePrivate")
class SpinnerView : View {
    // drawing attributes
    private var textColor: ColorStateList
    private var overlayColor: ColorStateList
    @IntRange(from = 1, to = Long.MAX_VALUE)
    private var stepGapSize: Int
    private var stepMarginStart: Int
    private var stepMarginEnd: Int
    private var textSize: Float
    private var smallDotRadius: Float
    private var bigDotRadius: Float
    private var indicatorInset: Float
    private var indicatorWidth: Float
    private var maxValue: Float
    private var minValue: Float
    private var stepValue: Float
    private var stepBigValue: Float
    private var stepTextValue: Float
    private var stepDecimalPlaces: Int = 0
    private var textFormat: DecimalFormat?
    private var flingEnabled: Boolean
    private var showEdgeText: Boolean

    private var currentValue: Float = 0f

    private var scrollX = 0f
    private val paint = Paint()
    private val path = Path()
    private val fontMetrics = Paint.FontMetrics()
    private val scroller: OverScroller

    private var onValueChangeListener: OnChangedValueListener? = null
    private var scrolling = false
    private var fling = false

    private var overlayGradientLeft: LinearGradient? = null
    private var overlayGradientRight: LinearGradient? = null
    private var overlayGradientCacheWidth = 0

    // control view focus
    private var isStopScrollingOnLostViewFocus = false

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    @SuppressLint("CustomViewStyleable")
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int): super(context, attrs, defStyle) {
        gestureDetector = GestureDetector(context, onGestureDetectorListener).apply {
//            this.setTouchSlop(ViewConfiguration.get(context).scaledTouchSlop)
        }
        scroller = OverScroller(context)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.SpinnerView)

        textColor = attributes.getColorStateList(R.styleable.SpinnerView_sv_textColor) ?: ColorStateList.valueOf(Color.WHITE)
        overlayColor = attributes.getColorStateList(R.styleable.SpinnerView_sv_overlayColor) ?: ColorStateList.valueOf(Color.TRANSPARENT)
        stepGapSize = attributes.getDimensionPixelSize(R.styleable.SpinnerView_sv_stepGapSize, convertDpToPx(10f))
        stepMarginStart = attributes.getDimensionPixelSize(R.styleable.SpinnerView_sv_stepMarginStart, convertDpToPx(10f))
        stepMarginEnd = attributes.getDimensionPixelSize(R.styleable.SpinnerView_sv_stepMarginEnd, convertDpToPx(10f))
        smallDotRadius = attributes.getDimensionPixelSize(R.styleable.SpinnerView_sv_smallDotRadius, convertDpToPx(1f)).toFloat()
        bigDotRadius = attributes.getDimensionPixelSize(R.styleable.SpinnerView_sv_bigDotRadius, convertDpToPx(2f)).toFloat()
        indicatorInset = attributes.getDimensionPixelSize(R.styleable.SpinnerView_sv_indicatorInset, convertDpToPx(3f)).toFloat()
        indicatorWidth = attributes.getDimensionPixelSize(R.styleable.SpinnerView_sv_indicatorWidth, convertDpToPx(6f)).toFloat()
        textSize = attributes.getDimensionPixelSize(R.styleable.SpinnerView_sv_textSize, convertDpToPx(15f)).toFloat()
        val textFormatPattern = attributes.getString(R.styleable.SpinnerView_sv_textFormatPattern)?.ifBlank { "#.#" } ?: "#.#"
        textFormat = DecimalFormat(textFormatPattern).apply {
            this.decimalFormatSymbols = DecimalFormatSymbols(Locale.ENGLISH)
        }

        val max = attributes.getFloat(R.styleable.SpinnerView_sv_maxValue, Float.MAX_VALUE)
        val min = attributes.getFloat(R.styleable.SpinnerView_sv_minValue, 0f)
        val valueRange = listOf(min, max)
        maxValue = valueRange.maxOrNull() ?: Float.MAX_VALUE
        minValue = valueRange.minOrNull() ?: 0f
        stepValue = attributes.getFloat(R.styleable.SpinnerView_sv_stepValue, 1f)
        stepDecimalPlaces = getDecimalPlaces(stepValue)
        stepBigValue = attributes.getFloat(R.styleable.SpinnerView_sv_stepBigValue, 5f)
        stepTextValue = attributes.getFloat(R.styleable.SpinnerView_sv_textStepValue, stepBigValue)
        val startValue =attributes.getFloat(R.styleable.SpinnerView_sv_value, kotlin.math.abs(maxValue - minValue) / 2f)
        flingEnabled = attributes.getBoolean(R.styleable.SpinnerView_sv_flingEnabled, true)
        showEdgeText = attributes.getBoolean(R.styleable.SpinnerView_sv_showEdgeText, false)

        attributes.recycle()

        scrollToValue(startValue, false)
    }

    private fun getDecimalPlaces(value: Float): Int {
        return value.toString().substringAfter(".").length
    }

    class Attributes private constructor(
        internal val textColor: ColorStateList?,
        internal val shadeColor: ColorStateList?,
        internal val stepGapSize: Int?,
        internal val stepMarginStart: Int?,
        internal val stepMarginEnd: Int?,
        internal val textSize: Float?,
        internal val smallDotRadius: Float?,
        internal val bigDotRadius: Float?,
        internal val indicatorInset: Float?,
        internal val indicatorWidth: Float?,
        internal val maxValue: Float?,
        internal val minValue: Float?,
        internal val stepValue: Float?,
        internal val stepBigValue: Float?,
        internal val stepTextValue: Float?,
        internal val textFormat: DecimalFormat?,
        internal val textFormatPattern: String?,
        internal val flingEnabled: Boolean?,
        internal val showEdgeText: Boolean?
    ) {

        internal fun isSet(): Boolean {
            return this.textColor != null
                    || this.shadeColor != null
                    || this.stepGapSize != null
                    || this.stepMarginStart != null
                    || this.stepMarginEnd != null
                    || this.textSize != null
                    || this.smallDotRadius != null
                    || this.bigDotRadius != null
                    || this.indicatorInset != null
                    || this.indicatorWidth != null
                    || this.maxValue != null
                    || this.minValue != null
                    || this.stepValue != null
                    || this.stepBigValue != null
                    || this.stepTextValue != null
                    || this.textFormat != null
                    || this.textFormatPattern != null
                    || this.flingEnabled != null
                    || this.showEdgeText != null
        }

        @Suppress("unused")
        class Builder {
            private var textColor: ColorStateList? = null
            private var shadeColor: ColorStateList? = null
            private var stepGapSize: Int? = null
            private var stepMarginStart: Int? = null
            private var stepMarginEnd: Int? = null
            private var textSize: Float? = null
            private var smallDotRadius: Float? = null
            private var bigDotRadius: Float? = null
            private var indicatorInset: Float? = null
            private var indicatorWidth: Float? = null
            private var maxValue: Float? = null
            private var minValue: Float? = null
            private var stepValue: Float? = null
            private var stepBigValue: Float? = null
            private var stepTextValue: Float? = null
            private var textFormat: DecimalFormat? = null
            private var textFormatPattern: String? = null
            private var flingEnabled: Boolean? = null
            private var showEdgeText: Boolean? = null

            fun setTextColor(color: ColorStateList): Builder {
                this.textColor = color
                return this@Builder
            }

            fun setShadeColor(color: ColorStateList): Builder {
                this.shadeColor = color
                return this@Builder
            }

            /**
             * @param size pixel
             */
            fun setStepGapSize(size: Int): Builder {
                this.stepGapSize = size
                return this@Builder
            }
            /**
             * @param margin pixel
             */
            fun setStepMarginStart(margin: Int): Builder {
                this.stepMarginStart = margin
                return this@Builder
            }
            /**
             * @param margin pixel
             */
            fun setStepMarginEnd(margin: Int): Builder {
                this.stepMarginEnd = margin
                return this@Builder
            }
            fun setTextSize(size: Float): Builder {
                this.textSize = size
                return this@Builder
            }
            fun setSmallDotRadius(radius: Float): Builder {
                this.smallDotRadius = radius
                return this@Builder
            }
            fun setBigDotRadius(radius: Float): Builder {
                this.bigDotRadius = radius
                return this@Builder
            }
            fun setIndicatorInset(inset: Float): Builder {
                this.indicatorInset = inset
                return this@Builder
            }
            fun setIndicatorWidth(width: Float): Builder {
                this.indicatorWidth = width
                return this@Builder
            }
            fun setMaxValue(value: Float): Builder {
                this.maxValue = value
                return this@Builder
            }
            fun setMinValue(value: Float): Builder {
                this.minValue = value
                return this@Builder
            }
            fun setStepValue(value: Float): Builder {
                this.stepValue = value
                return this@Builder
            }
            fun setStepBigValue(value: Float): Builder {
                this.stepBigValue = value
                return this@Builder
            }
            fun setStepTextValue(value: Float): Builder {
                this.stepTextValue = value
                return this@Builder
            }
            fun setTextFormat(format: DecimalFormat): Builder {
                this.textFormat = format
                return this@Builder
            }
            fun setTextFormatPattern(pattern: String): Builder {
                this.textFormatPattern = pattern
                return this@Builder
            }
            fun setFlingEnabled(enabled: Boolean): Builder {
                this.flingEnabled = enabled
                return this@Builder
            }
            fun setShowEdgeText(show: Boolean): Builder {
                this.showEdgeText = show
                return this@Builder
            }

            fun build(): Attributes = Attributes(
                textColor = this.textColor,
                shadeColor = this.shadeColor,
                stepGapSize = this.stepGapSize,
                stepMarginStart = this.stepMarginStart,
                stepMarginEnd = this.stepMarginEnd,
                textSize = this.textSize,
                smallDotRadius = this.smallDotRadius,
                bigDotRadius = this.bigDotRadius,
                indicatorInset = this.indicatorInset,
                indicatorWidth = this.indicatorWidth,
                maxValue = this.maxValue,
                minValue = this.minValue,
                stepValue = this.stepValue,
                stepBigValue = this.stepBigValue,
                stepTextValue = this.stepTextValue,
                textFormat = this.textFormat,
                textFormatPattern = this.textFormatPattern,
                flingEnabled = this.flingEnabled,
                showEdgeText = this.showEdgeText
            )
        }
    }

    private val gestureDetector: GestureDetector
    private val onGestureDetectorListener = object : GestureDetector.OnGestureListener {
        override fun onDown(e: MotionEvent): Boolean {
            if (isStopScrollingOnLostViewFocus) {
                requestFocus()
            }

            scrolling = false
            fling = false

            // allows parent view scrolling
            parent.requestDisallowInterceptTouchEvent(false)
            return true
        }

        override fun onShowPress(e: MotionEvent) {}

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            singleTapUp()
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // prevents parent view scrolling while scrolling this view.
            parent.requestDisallowInterceptTouchEvent(true)
            scroll(distanceX, distanceY)
            return true
        }

        override fun onLongPress(e: MotionEvent) {}

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            fling(-velocityX, -velocityY)
            return true
        }
    }

    // CallBack Interface
    interface OnChangedValueListener {
        fun onChangedValue(value: Float, isScrolled: Boolean) {}
    }

    // CallBack Listener
    fun setOnValueChangeListener(listener: OnChangedValueListener?) {
        this.onValueChangeListener = listener
    }

    private fun singleTapUp() {
        scrolling = false
        fling = false

        val snapX: Float = getSnapX(scrollX)
        scroller.forceFinished(true)
        scroller.startScroll(snapX.toInt(), 0, 0, 0)
        scrollX = snapX

        if (!awakenScrollBars()) {
            // Keep on drawing until the animation has finished.
            postInvalidate()
        }
        setCurrentValueAndNotify(scrollX, true)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun scroll(distanceX: Float, distanceY: Float) {
        scrolling = true
        scrollX = kotlin.math.max(getMinScrollX().toFloat(), kotlin.math.min(getMaxScrollX().toFloat(), scrollX + distanceX))
        if (!awakenScrollBars()) {
            // Keep on drawing until the animation has finished.
            postInvalidate()
        }
        setCurrentValueAndNotify(getSnapX(scrollX), false)
    }

    private fun fling(velocityX: Float, velocityY: Float) {
        scrolling = false

        if (!flingEnabled) {
            return
        }

        val startX = scrollX.toInt()
        fling = true
        scroller.forceFinished(true)
        scroller.fling(
            startX,
            0, velocityX.toInt(), velocityY.toInt(),
            getMinScrollX(), getMaxScrollX(),
            0, 0
        )
        if (!awakenScrollBars()) {
            // Keep on drawing until the animation has finished.
            postInvalidate()
        }
    }

    private fun getSnapX(x: Float): Float {
        return convertValueToScrollX(convertScrollXToValue(x))
    }

    private fun scrollToValue(value: Float, animated: Boolean) {
        scrolling = false
        fling = false
        scroller.forceFinished(true)
        val scrollValue = if (value < minValue) minValue else if (value > maxValue) maxValue else value
        val scrollValueX: Float = convertValueToScrollX(scrollValue)

        if (animated) {
            scroller.startScroll(scrollX.toInt(), 0, (scrollValueX - scrollX).toInt(), 0, 100)
        } else {
            scroller.startScroll(scrollValueX.toInt(), 0, 0, 0, 100)
        }
        if (!awakenScrollBars()) {
            // Keep on drawing until the animation has finished.
            postInvalidate()
        }
        // updates current value
        currentValue = scrollValue
    }

    override fun computeScroll() {
        computeScroll(true)
    }

    private fun computeScroll(notify: Boolean) {
        if (scroller.computeScrollOffset()) {
            // The scroller isn't finished, meaning a fling or programmatic pan operation is
            // currently active.
            scrollX = kotlin.math.max(getMinScrollX(), kotlin.math.min(getMaxScrollX(), scroller.currX)).toFloat()
            if (!awakenScrollBars()) {
                // Keep on drawing until the animation has finished.
                postInvalidate()
            }
            val snapX = getSnapX(scrollX)
            if (notify) {
                setCurrentValueAndNotify(snapX, false)
            } else {
                setCurrentValue(snapX, false)
            }
        } else {
            if (fling && !scrolling) {
                scrollX = kotlin.math.max(getMinScrollX(), kotlin.math.min(getMaxScrollX(), scroller.currX)).toFloat()
                val currentSnapX = getSnapX(scrollX)
                val finalSnapX = getSnapX(scroller.finalX.toFloat())
                if (currentSnapX == finalSnapX) {
                    fling = false
                    // the scroller is finished already that it is set by the scroller.computeScrollOffset()
//                    scroller.forceFinished(true)
                    scroller.startScroll(currentSnapX.toInt(), 0, 0, 0)
                    if (!awakenScrollBars()) {
                        // Keep on drawing until the animation has finished.
                        postInvalidate()
                    }
                    if (notify) {
                        setCurrentValueAndNotify(currentSnapX, true)
                    } else {
                        setCurrentValue(currentSnapX, true)
                    }
                }
            }
        }
    }

    fun setValue(value: Float, animated: Boolean = false) {
        scrollToValue(value, animated)
    }

    fun getValue(): Float = currentValue

    private fun setCurrentValueAndNotify(scrollX: Float, done: Boolean) {
        setCurrentValue(scrollX, done)?.let { notifyValue ->
            onValueChangeListener?.onChangedValue(notifyValue, done)
        }
    }

    private fun setCurrentValue(scrollX: Float, force: Boolean): Float? {
        val notifyValue: Float = convertScrollXToValue(scrollX)
        if (kotlin.math.abs(currentValue - notifyValue) >= stepValue || force) {
            currentValue = notifyValue
            return notifyValue
        }

        return null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBackground(canvas)
        drawScale(canvas)
        drawOverlay(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        val background = background
        if (background != null) {
            background.setBounds(0, 0, width, height)
            background.draw(canvas)
        }
    }

    private fun drawScale(canvas: Canvas) {
        val viewWidth = width.toFloat() - (paddingStart + paddingEnd)
        val centerOfWidth = viewWidth / 2f

        if (scroller.computeScrollOffset()) { // computeScroll() true 를 돌려주는 경우, 애니메이션이 아직 끝나지 않았음, 스크롤러에 의해 자동 계산된 중간값
            postInvalidate()
            scrollX = kotlin.math.max(getMinScrollX(), kotlin.math.min(getMaxScrollX(), scroller.currX)).toFloat()
        }

        val currentValue: Float = convertScrollXToValue(scrollX)
        val currentValueX = convertValueToScrollX(currentValue)

        canvas.save()
        canvas.translate(centerOfWidth - scrollX, 0f)
        val dotAlpha = 20 * 255 / 100
        paint.isAntiAlias = true
        paint.color = textColor.getColorForState(drawableState, Color.WHITE)
        paint.style = Paint.Style.FILL
        paint.alpha = dotAlpha
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = textSize
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.getFontMetrics(fontMetrics)
        val topY = stepMarginStart
        val bottomY = height - stepMarginEnd
        val textY = height.toFloat() / 2 - fontMetrics.ascent * 0.8f / 2

        if (stepValue > 0) {
            val drawValueCounts = ((viewWidth / stepGapSize) + 0.5f).toInt()
            val drawMinValue = currentValue - (drawValueCounts / 2) * stepValue
            val drawMinValueX = currentValueX - (drawValueCounts / 2) * stepGapSize

            for (index in 0 until drawValueCounts) {
                val adjustValue = if (stepValue < 1f) 10.toDouble().pow(stepDecimalPlaces).toFloat() else 1f
                // calculate bar X coordinate
                val drawValue: Float = (((drawMinValue + stepValue * index) * adjustValue).roundToLong() / adjustValue)
                if (drawValue < minValue) continue // skip draw
                if (drawValue > maxValue) continue // skip draw

                val drawValueX: Float = drawMinValueX + index * stepGapSize
                val isStepBigValue = if (stepBigValue > 0) ((drawValue * adjustValue).roundToLong() % (stepBigValue * adjustValue).roundToLong()) == 0L else false
                val isStepTextValue = if (stepTextValue > 0) ((drawValue * adjustValue).roundToLong() % (stepTextValue * adjustValue).roundToLong()) == 0L else false

                // draw step top/bottom mark
                canvas.drawCircle(drawValueX, topY.toFloat(), if (isStepBigValue) bigDotRadius else smallDotRadius, paint)
                canvas.drawCircle(drawValueX, bottomY.toFloat(), if (isStepBigValue) bigDotRadius else smallDotRadius, paint)

                // draw the big step text
                if (isStepTextValue || showEdgeText && (drawValue == minValue || drawValue == maxValue)) {
                    val bigStepDrawText = textFormat?.format(drawValue) ?: String.format(Locale.ENGLISH, if (stepValue < 1f) "%0.${stepDecimalPlaces}f" else "%f", drawValue)
                    paint.textSize = textSize
                    // get a drawing text width
                    val textWidth = 0f //paint.measureText(bigStepDrawText)
                    val dist = (kotlin.math.abs(drawValueX - scrollX) / centerOfWidth * 200).toInt()
                    // set a drawing text alpha value
                    paint.alpha = 255 - kotlin.math.min(dist, 200)
                    // draw a text
                    canvas.drawText(bigStepDrawText, drawValueX - textWidth / 2, textY, paint)
                    paint.alpha = dotAlpha
                }
            }
        }
        canvas.restore()

        canvas.save()
        // draw indicator
        paint.alpha = 255
        path.rewind()
        path.moveTo(centerOfWidth, indicatorInset + indicatorWidth / 2f)
        path.lineTo(centerOfWidth - indicatorWidth / 2f, indicatorInset)
        path.lineTo(centerOfWidth + indicatorWidth / 2f, indicatorInset)
        path.close()
        canvas.drawPath(path, paint)
        path.rewind()
        path.moveTo(centerOfWidth, height - (indicatorInset + indicatorWidth / 2f))
        path.lineTo(centerOfWidth - indicatorWidth / 2f, height - indicatorInset)
        path.lineTo(centerOfWidth + indicatorWidth / 2f, height - indicatorInset)
        path.close()
        canvas.drawPath(path, paint)
        canvas.restore()
    }

    private fun drawOverlay(canvas: Canvas) {
        // draw overlay
        canvas.save()
        paint.style = Paint.Style.FILL
        if (isEnabled) {
            val overlayDrawingColor = overlayColor.getColorForState(drawableState, Color.TRANSPARENT)
            if (overlayDrawingColor != Color.TRANSPARENT) {
                if (overlayGradientCacheWidth != width || overlayGradientLeft == null || overlayGradientRight == null) {
                    val shadeColor = this.overlayColor.getColorForState(drawableState, Color.TRANSPARENT)
                    overlayGradientLeft = LinearGradient(0f, 0f, (width / 2f), 0f, shadeColor, 0x00000000, Shader.TileMode.CLAMP)
                    overlayGradientRight = LinearGradient((width / 2).toFloat(), 0f, width.toFloat(), 0f, 0x00000000, shadeColor, Shader.TileMode.CLAMP)
                    overlayGradientCacheWidth = width
                }
                paint.shader = overlayGradientLeft
                canvas.drawRect(0f, 0f, (width / 2).toFloat(), height.toFloat(), paint)
                paint.shader = overlayGradientRight
                canvas.drawRect((width / 2).toFloat(), 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null
            }
        } else {
            paint.color = overlayColor.getColorForState(drawableState, Color.TRANSPARENT)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }
        canvas.restore()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (!isEnabled) return true
        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val retVal = gestureDetector.onTouchEvent(event)

        val action = event.actionMasked
        // controls touch-up action when onFling() is not called after onScroll().
        if (action == MotionEvent.ACTION_UP) {
            if (scrolling) {
                scrolling = false
                val snapX = getSnapX(scrollX)
                scroller.forceFinished(true)
                scroller.startScroll(snapX.toInt(), 0, 0, 0)
                scrollX = snapX
                if (!awakenScrollBars()) {
                    // Keep on drawing until the animation has finished.
                    postInvalidate()
                }
                setCurrentValueAndNotify(scrollX, true)
            }
        }

        return retVal || super.onTouchEvent(event)
    }

    private fun getMinScrollX(): Int = 0

    private fun getMaxScrollX(): Int {
        return convertValueToScrollX(maxValue).toInt()
    }

    private fun convertScrollXToValue(scrollX: Float): Float {
        val adjustValue = if (stepValue < 1f) 10.toDouble().pow(stepDecimalPlaces).toFloat() else 1f
        val valueOfScrollX = kotlin.math.round((scrollX * stepValue * adjustValue) / stepGapSize) / adjustValue
        val targetValue: Float = minValue + valueOfScrollX
        return kotlin.math.max(minValue, kotlin.math.min(maxValue, targetValue))
    }

    private fun convertValueToScrollX(value: Float): Float {
        val validValue = kotlin.math.min(maxValue, value)
        return kotlin.math.round((((validValue - minValue) / stepValue) * stepGapSize).toDouble()).toFloat()
    }

    fun getMaxValue(): Float = maxValue

    fun getMinValue(): Float = minValue

    fun setAttributes(attributes: Attributes) {
        if (!attributes.isSet()) return

        attributes.textSize?.let { this.textSize = it }
        attributes.textColor?.let { this.textColor = it }
        attributes.shadeColor?.let { this.overlayColor = it }
        attributes.stepGapSize?.let { this.stepGapSize = it }
        attributes.stepMarginStart?.let { this.stepMarginStart = it }
        attributes.stepMarginEnd?.let { this.stepMarginEnd = it }
        attributes.textSize?.let { this.textSize = it }
        attributes.smallDotRadius?.let { this.smallDotRadius = it }
        attributes.bigDotRadius?.let { this.bigDotRadius = it }
        attributes.indicatorInset?.let { this.indicatorInset = it }
        attributes.indicatorWidth?.let { this.indicatorWidth = it }
        attributes.maxValue?.let { this.maxValue = it }
        attributes.minValue?.let { this.minValue = it }
        attributes.stepValue?.let {
            this.stepValue = it
            this.stepDecimalPlaces = getDecimalPlaces(it)
        }
        attributes.stepBigValue?.let { this.stepBigValue = it }
        attributes.stepTextValue?.let { this.stepTextValue = it }
        attributes.textFormat?.let { this.textFormat = it }
        attributes.textFormatPattern?.let {
            if (it.isNotBlank()) this.textFormat?.applyPattern(it)
        }
        attributes.flingEnabled?.let { this.flingEnabled = it }
        attributes.showEdgeText?.let { this.showEdgeText = it }

        postInvalidate()
    }

    // stop scrolling
    fun stop(notify: Boolean) {
        scrolling = false
        scroller.forceFinished(true)
        computeScroll(notify)
    }

    /**
     * To stop the scrolling when the view focus is lost.
     * The default action is that keeping the scrolling although the view focus is lost.
     */
    fun setStopScrollingOnLostViewFocus(stop: Boolean) {
        isStopScrollingOnLostViewFocus = stop
        if (stop) {
            isFocusable = true
            isFocusableInTouchMode = true
        } else {
            isFocusable = false
            isFocusableInTouchMode = false
        }
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)

        if (isStopScrollingOnLostViewFocus && !gainFocus && (scrolling || fling)) {
            // stop scrolling or fling and than setting it
            stop(true)
        }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)

        if (isStopScrollingOnLostViewFocus && !hasWindowFocus && (scrolling || fling)) {
            // stop scrolling or fling and than setting it
            stop(true)
        }
    }
}