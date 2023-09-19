package com.herry.libs.widget.view.recyclerview.scroller

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.SectionIndexer
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRect
import androidx.customview.view.AbsSavedState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.herry.libs.R


@Suppress("MemberVisibilityCanBePrivate", "unused")
class RecyclerViewAlphabetIndexerScrollerView : FrameLayout {

    private var savedState: SavedState? = null

    private var recyclerView: RecyclerView? = null

    private var adapter: RecyclerView.Adapter<*>? = null
    private val adapterObserver: RecyclerView.AdapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            refresh()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            refresh()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            refresh()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            refresh()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            refresh()
        }

        private fun refresh() {
            updateSections()
            requestLayout()
        }
    }

    private var sectionIndexer: SectionIndexer? = null
    private var sections: Array<String>? = null
    private var currentSectionIndex: Int = RecyclerView.NO_POSITION

    private var sectionBarRect: RectF = RectF()
    private var sectionBarBackgroundDrawable: Drawable? = null
    private var sectionBarPaddingStart: Int = 0
    private var sectionBarPaddingEnd: Int = 0
    private var sectionBarPaddingTop: Int = 0
    private var sectionBarPaddingBottom: Int = 0
    private var sectionBarMarginStart: Int = 0
    private var sectionBarMarginEnd: Int = 0
    private var sectionBarMarginTop: Int = 0
    private var sectionBarMarginBottom: Int = 0
    private var sectionBarWidth: Int = 0
    private var sectionTextSize = 0
    private var sectionTextTypeface = Typeface.DEFAULT
    @ColorInt
    private var sectionTextColor = Color.WHITE

    private var isHighlightEnable = false
    private var sectionHighlightTextSize = 0
    private var sectionHighlightTextTypeface = Typeface.DEFAULT_BOLD
    @ColorInt
    private var sectionHighlightTextColor = Color.WHITE

    private var isSectionPreviewEnable = true
    private var sectionPreviewViewWidth: Int = 0
    private var sectionPreviewViewHeight: Int = 0
    private var sectionPreviewBackgroundDrawable: Drawable? = null
    private var sectionPreviewTextTypeface = Typeface.DEFAULT
    private var sectionPreviewTextSize = 0
    @ColorInt
    private var sectionPreviewTextColor = Color.WHITE

    private var sectionPreviewHideDelay = 0
    private val sectionPreviewHideRunnable = Runnable {
        invalidate()
    }

    private val sectionTextPaint = Paint()
    private val sectionPreviewTextPaint = Paint()

    private var isIndexing = false

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    @SuppressLint("CustomViewStyleable")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        val attr = context.obtainStyledAttributes(attrs, R.styleable.RecyclerViewAlphabetIndexerScrollerView)

        sectionBarBackgroundDrawable = attr.getDrawable(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionBarBackground)
        if (sectionBarBackgroundDrawable == null) {
            sectionBarBackgroundDrawable = ColorDrawable(Color.argb(0x80, 0, 0, 0))
        }
        sectionBarWidth = attr.getDimensionPixelSize(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionBarWidth, convertDpToPx(context, 30f))
        sectionBarPaddingStart = attr.getDimensionPixelSize(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionBarPaddingStart, convertDpToPx(context, 0f))
        sectionBarPaddingEnd = attr.getDimensionPixelSize(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionBarPaddingEnd, convertDpToPx(context, 0f))
        sectionBarPaddingTop = attr.getDimensionPixelSize(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionBarPaddingTop, convertDpToPx(context, 0f))
        sectionBarPaddingBottom = attr.getDimensionPixelSize(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionBarPaddingBottom, convertDpToPx(context, 0f))
        sectionBarMarginStart = attr.getDimensionPixelSize(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionBarMarginStart, convertDpToPx(context, 0f))
        sectionBarMarginEnd = attr.getDimensionPixelSize(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionBarMarginEnd, convertDpToPx(context, 0f))
        sectionBarMarginTop = attr.getDimensionPixelSize(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionBarMarginTop, convertDpToPx(context, 0f))
        sectionBarMarginBottom = attr.getDimensionPixelSize(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionBarMarginBottom, convertDpToPx(context, 0f))

        val sectionTextStyle = attr.getInt(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionTextStyle, 0)
        sectionTextTypeface = convertAttrTextStyleToTypeface(sectionTextStyle)
        sectionTextColor = attr.getColor(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionTextColor, Color.WHITE)
        sectionTextSize = attr.getDimensionPixelSize(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionTextSize, convertDpToPx(context, 10f))

        sectionHighlightTextSize = attr.getDimensionPixelSize(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionHighlightTextSize, convertDpToPx(context, 10f))
        val sectionHighlightTextStyle = attr.getInt(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionHighlightTextStyle, 1)
        sectionHighlightTextTypeface = convertAttrTextStyleToTypeface(sectionHighlightTextStyle)
        sectionHighlightTextColor = attr.getColor(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionHighlightTextColor, Color.WHITE)
        isHighlightEnable = attr.getBoolean(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionHighlightEnable, false)

        sectionPreviewViewWidth = attr.getDimensionPixelSize(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionPreviewWidth, convertDpToPx(context, 56f))
        sectionPreviewViewHeight = attr.getDimensionPixelSize(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionPreviewHeight, convertDpToPx(context, 56f))
        sectionPreviewBackgroundDrawable = attr.getDrawable(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionPreviewBackground)
        if (sectionPreviewBackgroundDrawable == null) {
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.argb(0x80, 0, 0, 0))
                val radius = convertDpToPx(context, 5f).toFloat()
                cornerRadii = floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
            }

            sectionPreviewBackgroundDrawable = drawable
        }
        val sectionPreviewTextStyle = attr.getInt(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionPreviewTextStyle, 0)
        sectionPreviewTextTypeface = convertAttrTextStyleToTypeface(sectionPreviewTextStyle)
        sectionPreviewTextSize = attr.getDimensionPixelSize(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionPreviewTextSize, convertDpToPx(context, 40f))
        sectionPreviewTextColor = attr.getColor(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionPreviewTextColor, Color.WHITE)
        isSectionPreviewEnable = attr.getBoolean(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionPreviewEnable, true)
        sectionPreviewHideDelay = attr.getInt(R.styleable.RecyclerViewAlphabetIndexerScrollerView_rvaisv_sectionPreviewHideDelay, 300)

        attr.recycle()

        if (background == null) {
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        }

        requestLayout()
    }

    private fun convertAttrTextStyleToTypeface(textStyle: Int): Typeface {
        return when (textStyle) {
            1 -> Typeface.DEFAULT_BOLD
            else -> Typeface.DEFAULT
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (isSectionsRegionPoint(event.x, event.y)) {
                    recyclerView?.stopScroll()

                    isIndexing = true

                    // detects which section the point is in, and move the list to that section
                    val sectionIndex = getSectionIndexByPoint(event.y)
                    setCurrentSectionIndex(sectionIndex)
                    scrollToSection(sectionIndex)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isIndexing) {
                    if (isSectionsRegionPoint(event.x, event.y)) {
                        // detects which section the point is in, and move the list to that section
                        val sectionIndex = getSectionIndexByPoint(event.y)
                        setCurrentSectionIndex(sectionIndex)
                        scrollToSection(sectionIndex)
                        return true
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isIndexing) {
                    isIndexing = false
                }
            }
        }
        return false
    }

    private fun isSectionsRegionPoint(x: Float, y: Float): Boolean {
        // detects if the point is in index bar region, which includes the right margin of the bar
        return (x >= sectionBarRect.left && x <= sectionBarRect.left + sectionBarRect.width()
                && y >= sectionBarRect.top && y <= sectionBarRect.top + sectionBarRect.height())
    }

    fun setSectionBarWidth(width: Int) {
        this.sectionBarWidth = width

        invalidate()
    }

    fun setSectionBarBackground(drawable: Drawable?) {
        sectionBarBackgroundDrawable = drawable

        invalidate()
    }

    fun setCurrentSectionIndex(index: Int) {
        currentSectionIndex = index
        invalidate()
    }

    fun getCurrentSectionIndex(): Int {
        return currentSectionIndex
    }

    private fun getCurrentSection(): String {
        if (0 <= currentSectionIndex && currentSectionIndex < (sections?.size ?: 0)) {
            return sections?.get(currentSectionIndex) ?: ""
        }
        return ""
    }

    fun setSectionPreviewSize(width: Int, height: Int) {
        this.sectionPreviewViewWidth = width
        this.sectionPreviewViewHeight = height

        if (isSectionPreviewEnable) {
            invalidate()
        }
    }

    fun setSectionPreviewBackground(drawable: Drawable?) {
        sectionPreviewBackgroundDrawable = drawable

        if (isSectionPreviewEnable) {
            invalidate()
        }
    }

    fun setSectionPreviewEnable(enable: Boolean) {
        isSectionPreviewEnable = enable

        invalidate()
    }

//    private fun getSectionByRecyclerView(recyclerView: RecyclerView?): Int {
//        recyclerView ?: return RecyclerView.NO_POSITION
//
//        // updates current section
//        val barHeight = sectionBarRect.height()
//        if (barHeight <= 0) {
//            return RecyclerView.NO_POSITION
//        }
//
//        val offset = recyclerView.computeVerticalScrollOffset()
//        val extent = recyclerView.computeVerticalScrollExtent()
//        val range = recyclerView.computeVerticalScrollRange()
//
////        val percentage = (100.0f * offset / (range - extent).toFloat())
////        val y = barHeight * (percentage / 100.0f)
//
//        val y = (offset.toFloat() / (range - extent - extent)) * barHeight
//        return getSectionIndexByPoint(y)
//    }

    fun attachRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        this.recyclerView?.run {
//            addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    if (!isIndexing) {
//                        setCurrentSectionIndex(getSectionByRecyclerView(recyclerView))
//                    }
//                }
//            })
            recyclerView.adapter?.run {
                attachAdapter(this)
            }
        }
    }

    fun attachAdapter(adapter: RecyclerView.Adapter<*>?) {
        if (this.adapter == adapter) return
        this.adapter?.unregisterAdapterDataObserver(adapterObserver)
        adapter?.registerAdapterDataObserver(adapterObserver)
        this.adapter = adapter

        if (adapter is SectionIndexer) {
            sectionIndexer = adapter
            updateSections()
        }
    }

    private fun updateSections() {
        @Suppress("UNCHECKED_CAST")
        sections = sectionIndexer?.sections as? Array<String>
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        savedState?.run {
            setCurrentSectionIndex(this.currentSectionIndex)
            savedState = null
        }

        sectionBarRect = getSectionBarRect()
    }

    private fun getSectionBarRect(): RectF {
        val left = if (isRTL()) {
            (sectionBarMarginEnd).toFloat()
        } else {
            (width - sectionBarWidth - sectionBarMarginEnd).toFloat()
        }
        val right = if (isRTL()) {
            (sectionBarMarginEnd + sectionBarWidth).toFloat()
        } else {
            (width - sectionBarMarginEnd).toFloat()
        }
        val top = sectionBarMarginTop.toFloat()
        val bottom = this.height.toFloat() - sectionBarMarginBottom

        return RectF(left, top, right, bottom)
    }

    private fun getSectionIndexByPoint(y: Float): Int {
        val sections = this.sections
        if (sections.isNullOrEmpty()) return RecyclerView.NO_POSITION

        return when {
            y < sectionBarRect.top + sectionBarPaddingTop -> {
                0
            }
            y >= sectionBarRect.top + sectionBarRect.height() - sectionBarPaddingBottom -> {
                sections.size - 1
            }
            else -> {
                ((y - sectionBarRect.top - sectionBarPaddingTop) / ((sectionBarRect.height() - (sectionBarPaddingTop + sectionBarPaddingBottom)) / sections.size)).toInt()
            }
        }
    }

    private fun scrollToSection(sectionIndex: Int) {
        try {
            val position = sectionIndexer?.getPositionForSection(sectionIndex) ?: return
            val layoutManager: RecyclerView.LayoutManager = recyclerView?.layoutManager ?: return
            if (layoutManager is LinearLayoutManager) {
                layoutManager.scrollToPositionWithOffset(position, 0)
            } else {
                layoutManager.scrollToPosition(position)
            }
        } catch (e: Exception) {
            Log.d("", "Data size returns null")
        }
    }

    override fun onDraw(canvas: Canvas) {

        // draws FrameLayout attributes
        super.onDraw(canvas)

        if (sections.isNullOrEmpty()) {
            return
        }
        drawSectionBar(canvas)
        drawSections(canvas)
        drawSectionPreview(canvas)
    }

    private fun drawSectionBar(canvas: Canvas) {
        canvas.save()

        sectionBarBackgroundDrawable?.bounds = sectionBarRect.toRect()
        sectionBarBackgroundDrawable?.draw(canvas)

        canvas.restore()
    }

    private fun drawSections(canvas: Canvas) {
        val currentSectionIndex = getCurrentSectionIndex()
        val sections = sections ?: return
        if (sections.isEmpty()) {
            return
        }

        sectionTextPaint.reset()
        sectionTextPaint.isAntiAlias = true

        if (isHighlightEnable) {
            sectionTextPaint.textSize = sectionHighlightTextSize.toFloat()
            sectionTextPaint.typeface = sectionHighlightTextTypeface
        } else {
            sectionTextPaint.textSize = sectionTextSize.toFloat()
            sectionTextPaint.typeface = sectionTextTypeface
        }

        val sectionTextHeight = sectionTextPaint.descent() - sectionTextPaint.ascent()
        val sectionBarHeight = sectionBarRect.height() - (sectionBarPaddingTop + sectionBarPaddingBottom)
        val sectionHeight: Float = sectionBarHeight / sections.size

        canvas.save()

//        val drawSectionCounts = (sectionBarHeight / sectionTextHeight).toInt()
//        if (drawSectionCounts <= 0) {
//            return
//        }
//        var step: Float = sections.size / drawSectionCounts.toFloat()
//        if (step < 1f) {
//            step = 1f
//        }
//
//        val drawSectionHeight = if (step <= 1f) {
//            sectionBarHeight / sections.size
//        } else {
//            sectionBarHeight / drawSectionCounts
//        }
//        var lastDrawSection: String? = null
//        for (index in sections.indices) {
//            val drawIndex = (index * step).toInt()
//            if (0 > drawIndex || drawIndex >= sections.size) {
//                continue
//            }
//            val section = sections[drawIndex]
//            if (lastDrawSection != null && lastDrawSection == section) {
//                continue
//            }
//
//            lastDrawSection = section
//
//            val sectionTextTop = sectionBarRect.top + sectionBarPaddingTop + index * drawSectionHeight
//
//            sectionTextPaint.textSize = sectionTextSize.toFloat()
//            sectionTextPaint.typeface = sectionTextTypeface
//            sectionTextPaint.color = sectionTextColor
//
//            val sectionTextStart: Float = (sectionBarWidth - sectionTextPaint.measureText(section)) / 2f
//            val sectionTextX = (if (!isRTL()) sectionBarRect.left else sectionBarRect.right) + sectionTextStart
//            val sectionTextY = sectionTextTop + (drawSectionHeight - sectionTextHeight) / 2f + sectionTextHeight - sectionTextPaint.descent()
//            canvas.drawText(section, sectionTextX, sectionTextY, sectionTextPaint)
//        }
//
        var lastSectionTextY = -1f
        sections.forEachIndexed { index, section ->
            val isCurrentSection = currentSectionIndex > -1 && index == currentSectionIndex
            if (isHighlightEnable && isCurrentSection) {
                sectionTextPaint.textSize = sectionHighlightTextSize.toFloat()
                sectionTextPaint.typeface = sectionHighlightTextTypeface
                sectionTextPaint.color = sectionHighlightTextColor
            } else {
                sectionTextPaint.textSize = sectionTextSize.toFloat()
                sectionTextPaint.typeface = sectionTextTypeface
                sectionTextPaint.color = sectionTextColor
            }

            val sectionTextTop: Float = sectionBarRect.top + sectionBarPaddingTop + index * sectionHeight
            val sectionTextY = sectionTextTop + (sectionHeight - sectionTextHeight) / 2f - sectionTextPaint.ascent()

            if (lastSectionTextY == -1f || sectionTextY - lastSectionTextY > sectionTextPaint.textSize) {
                val sectionTextX = sectionBarRect.left + (sectionBarWidth - sectionTextPaint.measureText(section)) / 2f

                canvas.drawText(section, sectionTextX, sectionTextY, sectionTextPaint)
                lastSectionTextY = sectionTextY
            }
        }
        canvas.restore()
    }

    private fun drawSectionPreview(canvas: Canvas) {
        canvas.save()

        val section = getCurrentSection()
        if (isIndexing && isSectionPreviewEnable && section.isNotBlank()) {
            // draws section preview text
            sectionPreviewTextPaint.reset()
            sectionPreviewTextPaint.color = sectionPreviewTextColor
            sectionPreviewTextPaint.isAntiAlias = true
            sectionPreviewTextPaint.textSize = sectionPreviewTextSize.toFloat()
            sectionPreviewTextPaint.typeface = sectionPreviewTextTypeface

            val sectionPreviewTextWidth: Float = sectionPreviewTextPaint.measureText(section)
            val sectionPreviewTextHeight: Float = (sectionPreviewTextPaint.descent() - sectionPreviewTextPaint.ascent())

            val previewWidth: Float = kotlin.math.max(sectionPreviewTextWidth, sectionPreviewViewWidth.toFloat())
            val previewHeight: Float = kotlin.math.max(sectionPreviewTextHeight, sectionPreviewViewHeight.toFloat())

            // draws background
            val viewWidth = width - paddingStart - paddingEnd
            val viewHeight = height - paddingTop - paddingBottom
            val sectionPreviewRect = RectF(
                (viewWidth - previewWidth) / 2f + paddingStart,
                (viewHeight - previewHeight) / 2f + paddingTop,
                (viewWidth - previewWidth) / 2f + previewWidth + paddingStart,
                (viewHeight - previewHeight) / 2 + previewHeight + paddingTop
            )

            sectionPreviewBackgroundDrawable?.bounds = sectionPreviewRect.toRect()
            sectionPreviewBackgroundDrawable?.draw(canvas)

            val textLeft = ((if (isRTL()) sectionPreviewRect.right else sectionPreviewRect.left) + ((sectionPreviewRect.width() - sectionPreviewTextWidth) / 2f) + 0.5f).toInt().toFloat()
            val textTop = (sectionPreviewRect.top + ((sectionPreviewRect.height() - sectionPreviewTextHeight) / 2f) + 0.5f).toInt().toFloat()

            canvas.drawText(
                section,
                textLeft,
                textTop - sectionPreviewTextPaint.ascent(),
                sectionPreviewTextPaint
            )

            hideSectionPreview()
        }

        canvas.restore()
    }

    private fun hideSectionPreview(delay: Long = sectionPreviewHideDelay.toLong()) {
        if (isSectionPreviewEnable) {
            removeCallbacks(sectionPreviewHideRunnable)
            postDelayed(sectionPreviewHideRunnable, delay)
        }
    }

    private fun isRTL(): Boolean {
        context ?: return false

        return context.resources.configuration.layoutDirection == LAYOUT_DIRECTION_RTL
    }

    private fun convertDpToPx(context: Context?, dp: Float): Int {
        return if (null == context) {
            0
        } else (dp * context.resources.displayMetrics.density + 0.5f).toInt()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState: Parcelable? = super.onSaveInstanceState()
        return superState?.let {
            SavedState(superState).apply {
                this.currentSectionIndex = this@RecyclerViewAlphabetIndexerScrollerView.currentSectionIndex
            }
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                this.savedState = state
                requestLayout()
            }
            else -> {
                super.onRestoreInstanceState(state)
            }
        }
    }

    internal class SavedState : AbsSavedState {
        var currentSectionIndex = 0

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            currentSectionIndex = source.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(currentSectionIndex)
        }

        override fun toString(): String {
            return ("SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " currentSectionIndex=" + currentSectionIndex + "}")
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.ClassLoaderCreator<SavedState> = object : Parcelable.ClassLoaderCreator<SavedState> {
                override fun createFromParcel(source: Parcel, loader: ClassLoader): SavedState {
                    return SavedState(source, loader)
                }

                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source, null)
                }

                override fun newArray(size: Int): Array<SavedState> {
                    return newArray(size)
                }
            }
        }
    }
}