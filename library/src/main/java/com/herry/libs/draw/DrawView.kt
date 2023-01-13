package com.herry.libs.draw

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils

class DrawView : View {

    private var paths = LinkedHashMap<DrawPath, PaintOption>()
    private var lastPaths = LinkedHashMap<DrawPath, PaintOption>()
    private var undonePaths =LinkedHashMap<DrawPath, PaintOption>()

    private val paint = Paint()
    private var path = DrawPath()
    private var paintOption = PaintOption()

    private var currentX = 0f
    private var currentY = 0f
    private var startX = 0f
    private var startY = 0f
    private var isSaving = false
    private var isStrokeWidthBarEnabled = false
    private var isEraserOn = false

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0): super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        paint.apply {
            this.color = paintOption.color
            this.style = Paint.Style.STROKE
            this.strokeJoin = Paint.Join.ROUND
            this.strokeCap = Paint.Cap.ROUND
            this.strokeWidth = paintOption.strokeWidth
            this.isAntiAlias = true
        }
    }

    fun canUndo(): Boolean {
        if (paths.isEmpty() && lastPaths.isNotEmpty()) {
            return true
        }
        if (paths.isNotEmpty()) {
            return true
        }
        return false
    }

    fun undo() {
        if (paths.isEmpty() && lastPaths.isNotEmpty()) {
            @Suppress("UNCHECKED_CAST")
            paths = lastPaths.clone() as LinkedHashMap<DrawPath, PaintOption>
            lastPaths.clear()
            invalidate()
            return
        }

        if (paths.isEmpty()) {
            return
        }

        val lastPath = paths.values.lastOrNull()
        val lastKey = paths.keys.lastOrNull()

        paths.remove(lastKey)
        if (lastPath != null && lastKey != null) {
            undonePaths[lastKey] = lastPath
        }
        invalidate()
    }

    fun canRedo(): Boolean {
        return undonePaths.keys.isNotEmpty()
    }

    fun redo() {
        if (!canRedo()) {
            return
        }

        val lastKey = undonePaths.keys.last()
        addPath(lastKey, undonePaths.values.last())
        undonePaths.remove(lastKey)
        invalidate()
    }

    fun hasDrawing(): Boolean {
        return paths.isNotEmpty()
    }

    fun setColor(@ColorInt color: Int) {
        paintOption.color = ColorUtils.setAlphaComponent(color, paintOption.alpha)
        if (isStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    @ColorInt fun getColor(): Int = paintOption.color

    fun setAlpha(alpha: Int) {
        paintOption.alpha = (alpha * 255) / 100
        setColor(paintOption.color)
    }

    fun getStrokeWidth(): Float = paintOption.strokeWidth
    fun setStrokeWidth(width: Float) {
        paintOption.strokeWidth = width
        if (isStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    fun getBitmap(): Bitmap? {
        if (!hasDrawing()) {
            return null
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        isSaving = true
        draw(canvas)
        isSaving = false
        return bitmap
    }

    fun addPath(path: DrawPath, option: PaintOption) {
        paths[path] = option
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return
        for ((key, value) in paths) {
            changePaint(value)
            canvas.drawPath(key, paint)
        }

        changePaint(paintOption)
        canvas.drawPath(path, paint)
    }

    private fun changePaint(paintOption: PaintOption) {
        paint.color = if (paintOption.isEraserOn) Color.WHITE else paintOption.color
        paint.strokeWidth = paintOption.strokeWidth
    }

    fun clear() {
        @Suppress("UNCHECKED_CAST")
        lastPaths = paths.clone() as LinkedHashMap<DrawPath, PaintOption>
        path.reset()
        paths.clear()
        invalidate()
    }

    private fun actionDown(x: Float, y: Float) {
        path.reset()
        path.moveTo(x, y)
        currentX = x
        currentY = y
    }

    private fun actionMove(x: Float, y: Float) {
        path.quadTo(currentX, currentY, (x + currentX) / 2, (y + currentY) / 2)
        currentX = x
        currentY = y
    }

    private fun actionUp() {
        path.lineTo(currentX, currentY)

        // draw a dot on click
        if (startX == currentX && startY == currentY) {
            path.lineTo(currentX, currentY + 2)
            path.lineTo(currentX + 1, currentY + 2)
            path.lineTo(currentX + 1, currentY)
        }

        paths[path] = paintOption
        path = DrawPath()
        paintOption = PaintOption(
            color = paintOption.color,
            strokeWidth = paintOption.strokeWidth,
            alpha = paintOption.alpha,
            isEraserOn = paintOption.isEraserOn
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = x
                startY = y
                actionDown(x, y)
                undonePaths.clear()
            }
            MotionEvent.ACTION_MOVE -> {
                actionMove(x, y)
            }
            MotionEvent.ACTION_UP -> {
                actionUp()
            }
        }

        invalidate()
        return true
    }

    fun toggleEraser() {
        isEraserOn = !isEraserOn
        paintOption.isEraserOn = isEraserOn
        invalidate()
    }

    fun isEraseOn() = this.isEraserOn
}