package com.herry.libs.widget.chart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import com.herry.libs.R
import com.herry.libs.util.ViewUtil

/**
 * Created by herry.park
 */
@Suppress("unused")
class ProgressBarChartView : View {
    enum class Gravity(private val value: Int) {
        LEFT(1), RIGHT(2), TOP(3), BOTTOM(4);

        companion object {
            fun generate(value: Int): Gravity = values().firstOrNull { it.value == value } ?: LEFT
        }
    }

    private var maxValue = 100f
    private var value = 0f
    private var barPaint: Paint? = null
    private var gravity: Gravity? = null
    private var barEdgeTopStart = 0f
    private var barEdgeTopEnd = 0f
    private var barEdgeBottomStart = 0f
    private var barEdgeBottomEnd = 0f
    private var labelText: String? = ""

    //@NonNull
    //private LabelTextStyle labelTextStyle = LabelTextStyle.NORMAL;
    private var labelLocation = LabelLocation.END
    private var labelInnerTextColor = -0x1 // black
    private var labelOuterTextColor = -0x1 // black

    //    private float labelTextSize = 0;
    private var labelTextMargin = 0
    private var labelPaint: TextPaint? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.ProgressBarChartView, defStyleAttr, 0)

        val barPaint = Paint()
        // sets label paint
        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        val metrics = resources.displayMetrics
        labelPaint.density = metrics.density

        gravity = Gravity.generate(attr.getInt(R.styleable.ProgressBarChartView_bpcv_direction, 1))
        maxValue = attr.getFloat(R.styleable.ProgressBarChartView_bpcv_max, 100f)
        value = attr.getFloat(R.styleable.ProgressBarChartView_bpcv_value, 0f)

        val barColor = attr.getColor(R.styleable.ProgressBarChartView_bpcv_barColor, Color.TRANSPARENT)
        barPaint.color = barColor

        barEdgeTopStart = attr.getDimensionPixelSize(R.styleable.ProgressBarChartView_bpcv_roundEdgeTopStart, 0).toFloat()
        barEdgeTopEnd = attr.getDimensionPixelSize(R.styleable.ProgressBarChartView_bpcv_roundEdgeTopEnd, 0).toFloat()
        barEdgeBottomStart = attr.getDimensionPixelSize(R.styleable.ProgressBarChartView_bpcv_roundEdgeBottomStart, 0).toFloat()
        barEdgeBottomEnd = attr.getDimensionPixelSize(R.styleable.ProgressBarChartView_bpcv_roundEdgeBottomEnd, 0).toFloat()

        val label = attr.getString(R.styleable.ProgressBarChartView_bpcv_labelText)
        labelText = if (!TextUtils.isEmpty(label)) label else ""

        labelLocation = LabelLocation.generate(attr.getInt(R.styleable.ProgressBarChartView_bpcv_labelLocation, 1))
        val labelTextTypeface: Typeface = when (LabelTextStyle.generate(attr.getInt(R.styleable.ProgressBarChartView_bpcv_labelTextStyle, 0))) {
            LabelTextStyle.BOLD -> {
                Typeface.DEFAULT_BOLD
            }
            LabelTextStyle.NORMAL -> {
                Typeface.DEFAULT
            }
        }
        labelInnerTextColor = attr.getColor(R.styleable.ProgressBarChartView_bpcv_labelInnerTextColor, Color.TRANSPARENT)
        labelOuterTextColor = attr.getColor(R.styleable.ProgressBarChartView_bpcv_labelOuterTextColor, Color.TRANSPARENT)
        labelTextMargin = attr.getDimensionPixelSize(R.styleable.ProgressBarChartView_bpcv_labelMargin, 0)
        val labelTextSize = attr.getDimensionPixelSize(R.styleable.ProgressBarChartView_bpcv_labelTextSize, 0)
        // sets label text size
        labelPaint.textSize = labelTextSize.toFloat()
        labelPaint.typeface = labelTextTypeface
        attr.recycle()

        this.barPaint = barPaint
        this.labelPaint = labelPaint
    }

    fun setGravity(gravity: Gravity?) {
        this.gravity = gravity
        invalidate()
    }

    fun setMaxValue(maxValue: Float) {
        this.maxValue = maxValue
        invalidate()
    }

    fun setColor(color: Int) {
        val barPaint = this.barPaint ?: return
        barPaint.color = color
        invalidate()
    }

    fun setValue(value: Float) {
        this.value = value
        invalidate()
    }

    enum class LabelLocation(private val value: Int) {
        START(1), END(2);

        companion object {
            fun generate(value: Int): LabelLocation = values().firstOrNull{ it.value == value } ?: END
        }
    }

    enum class LabelTextStyle(private val value: Int) {
        NORMAL(0), BOLD(1);

        companion object {
            fun generate(value: Int): LabelTextStyle = values().firstOrNull { it.value == value } ?: NORMAL
        }
    }

    /**
     * Sets label text
     * @param label label text
     */
    fun setLabel(label: String?) {
        labelText = label
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (barPaint == null || gravity == null) {
            return
        }
        var ratio = value / maxValue
        if (ratio < 0) {
            ratio = 0f
        } else if (ratio > 1) {
            ratio = 1f
        }
        val viewW = width
        val viewH = height
        var progressX = 0f
        var progressY = 0f
        var progressW = 0f
        var progressH = 0f
        val roundedEdgeTopStart = barEdgeTopStart
        val roundedEdgeTopEnd = barEdgeTopEnd
        val roundedEdgeBottomStart = barEdgeBottomStart
        val roundedEdgeBottomEnd = barEdgeBottomEnd
        when (gravity) {
            Gravity.LEFT -> {
                progressX = 0f
                progressY = 0f
                progressW = viewW * ratio
                progressH = viewH.toFloat()
            }
            Gravity.RIGHT -> {
                progressX = viewW - viewW * ratio
                progressY = 0f
                progressW = viewW.toFloat()
                progressH = viewH.toFloat()
            }
            Gravity.TOP -> {
                progressX = 0f
                progressY = 0f
                progressW = viewW.toFloat()
                progressH = viewH * ratio
            }
            Gravity.BOTTOM -> {
                progressX = 0f
                progressY = viewH - viewH * ratio
                progressW = viewW.toFloat()
                progressH = viewH.toFloat()
            }
            else -> {}
        }
        barPaint?.let { barPaint ->
            if (0 < roundedEdgeTopStart || 0 < roundedEdgeTopEnd || 0 < roundedEdgeBottomStart || 0 < roundedEdgeBottomEnd) {
                val barRect = RectF(progressX, progressY, progressX + progressW, progressY + progressH)
                val path: Path = ViewUtil.composeRoundedRectPath(barRect, roundedEdgeTopStart, roundedEdgeTopEnd, roundedEdgeBottomStart, roundedEdgeBottomEnd)
                canvas.drawPath(path, barPaint)
            } else {
                canvas.drawRect(progressX, progressY, progressW, progressH, barPaint)
            }
        }

        // draws label
        val labelPaint = this.labelPaint
        val labelText = this.labelText
        if (labelText?.isNotEmpty() == true && null != labelPaint) {
            // sets label text bounds
            val labelBound = Rect()
            labelPaint.getTextBounds(labelText, 0, labelText.length, labelBound)
            val labelAreaWidth = labelPaint.measureText(labelText)
            val labelAreaHeight = (kotlin.math.abs(labelBound.top) + kotlin.math.abs(labelBound.bottom)).toFloat()
            val labelBaseLineY = kotlin.math.abs(labelBound.top).toFloat()

            // sets label range
            val labelW = labelAreaWidth +
                    if (gravity == Gravity.LEFT || gravity == Gravity.RIGHT) labelTextMargin else 0
            val labelH = labelAreaHeight +
                    if (gravity == Gravity.TOP || gravity == Gravity.BOTTOM) labelTextMargin else 0

            // Checks label is inset on progress bar.
            var isInnerLabel = false
            when (gravity) {
                Gravity.LEFT, Gravity.RIGHT -> {
                    isInnerLabel = progressW - progressX > labelW
                }
                Gravity.TOP, Gravity.BOTTOM -> {
                    isInnerLabel = progressH - progressY > labelH
                }
                else -> {}
            }

            // sets text color
            labelPaint.color = if (isInnerLabel) labelInnerTextColor else labelOuterTextColor

            // sets label position
            var x = 0f
            var y = 0f
            when (gravity) {
                Gravity.LEFT -> {
                    if (isInnerLabel) {
                        if (LabelLocation.START == labelLocation) {
                            x = labelTextMargin.toFloat()
                        } else if (LabelLocation.END == labelLocation) {
                            x = progressW - labelW
                        }
                    } else {
                        x = progressW + labelTextMargin
                    }
                    y = progressY + (progressH - labelH) / 2
                }
                Gravity.RIGHT -> {
                    if (isInnerLabel) {
                        if (LabelLocation.START == labelLocation) {
                            x = progressW - labelW
                        } else if (LabelLocation.END == labelLocation) {
                            x = progressX + labelTextMargin
                        }
                    } else {
                        x = progressX - labelW
                    }
                    y = progressY + (progressH - labelH) / 2
                }
                Gravity.TOP -> {
                    x = (progressW - labelW) / 2
                    if (isInnerLabel) {
                        if (LabelLocation.START == labelLocation) {
                            y = progressY + labelTextMargin
                        } else if (LabelLocation.END == labelLocation) {
                            y = progressH - labelH
                        }
                    } else {
                        y = progressH + labelTextMargin
                    }
                }
                Gravity.BOTTOM -> {
                    x = (progressW - labelW) / 2
                    if (isInnerLabel) {
                        if (LabelLocation.START == labelLocation) {
                            y = progressH - labelH
                        } else if (LabelLocation.END == labelLocation) {
                            y = progressY + labelTextMargin
                        }
                    } else {
                        y = progressY - labelH
                    }
                }
                else -> {}
            }

            // adds label base line position
            y += labelBaseLineY

//            Trace.d("BPCV", "labelText = " +labelText+ ", x = " +x+ " y = "+y);
            canvas.drawText(labelText, x, y, labelPaint)
        }
    }
}