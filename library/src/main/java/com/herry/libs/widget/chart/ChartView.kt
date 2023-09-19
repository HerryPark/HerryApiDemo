package com.herry.libs.widget.chart

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.View

open class ChartView : View {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    protected enum class DrawDirection(val value: Int) {
        RIGHT(0), LEFT(1);

        companion object {
            fun generate(value: Int): DrawDirection = values().firstOrNull {it.value == value } ?: RIGHT
        }
    }

    enum class AxisStartLocation(val value: Int) {
        NORTH(0), EAST(1), SOUTH(2), WEST(3);

        companion object {
            fun generate(value: Int): AxisStartLocation = values().firstOrNull {it.value == value } ?: NORTH
        }
    }

    protected inner class AxisVertices internal constructor(// position
        val point: Point, val degree: Double
    )

    protected fun createPoint(radius: Float, radian: Double, x0: Float, y0: Float): Point {
        return Point((radius * StrictMath.cos(radian) + x0).toInt(), (radius * StrictMath.sin(radian) + y0).toInt())
    }

    protected fun convertDegreeToRadian(degree: Float): Double {
        return degree * StrictMath.PI / 180 - StrictMath.PI / 2
    }

    protected fun getDegree(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        var angle = StrictMath.atan2((y2 - y1).toDouble(), (x2 - x1).toDouble()) * 180 / StrictMath.PI
        if (0 > angle) {
            angle += 360.0
        }
        return angle
    }
}