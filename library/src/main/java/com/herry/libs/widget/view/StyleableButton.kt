package com.herry.libs.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import com.herry.libs.R
import com.herry.libs.widget.extension.setLayoutGravity
import com.herry.libs.widget.extension.setViewMargin
import com.herry.libs.widget.extension.setViewMarginBottom
import com.herry.libs.widget.extension.setViewMarginEnd
import com.herry.libs.widget.extension.setViewMarginStart
import com.herry.libs.widget.extension.setViewMarginTop
import com.herry.libs.widget.extension.setViewPadding
import com.herry.libs.widget.extension.setViewSize

@Suppress("SameParameterValue", "unused")
class StyleableButton: FrameLayout {

    companion object {
        private const val UNDEFINED_PADDING = Int.MIN_VALUE
        private const val UNDEFINED_MARGIN = Int.MIN_VALUE
        private const val DEFAULT_BUTTON_LAYOUT_GRAVITY = 0x30 // center
        private const val DEFAULT_TINT_MODE = 5 // PorterDuff.Mode.SRC_IN.nativeInt
        private const val DEFAULT_TEXT_GRAVITY = 0x04 or 0x10 // start | center_vertical
    }

    /*
     * view layers:
     *  3: iconView | textView
     *  2: buttonView - button size, button padding, button foreground drawable, button background drawable
     *  1: containerView - button corner radius, button layout gravity, button margin
     *  0: baseView (this)
     */
    private var containerView: CardView? = null
    private var buttonView: ConstraintLayout? = null
    private var iconView: AppCompatImageView? = null
    private var textView: AppCompatTextView? = null
    private var iconTextRelativeMargin: Int = 0
    private var iconTextRelativeOf: Int = 0

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.StyleableButton)
        retrieveAttributes(context, typedArray, attrs, defStyleAttr)
        typedArray.recycle()
    }

    @SuppressLint("ResourceType")
    constructor(context: Context, @StyleRes styleResId: Int, width: Int = ViewGroup.LayoutParams.WRAP_CONTENT, height: Int = ViewGroup.LayoutParams.WRAP_CONTENT) : super(context, null, 0) {
        val sizeTypedArray = context.obtainStyledAttributes(styleResId, intArrayOf(
            android.R.attr.layout_width,  // 0
            android.R.attr.layout_height, // 1
            android.R.attr.minWidth // 2
        ))
        val layoutWidth = sizeTypedArray.getLayoutDimension(0, width)
        val layoutHeight = sizeTypedArray.getLayoutDimension(1, height)
        sizeTypedArray.recycle()

        setViewSize(layoutWidth, layoutHeight)

        val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.StyleableButton)
        retrieveAttributes(context, typedArray)
        typedArray.recycle()
    }

    private fun retrieveAttributes(context: Context, attr: TypedArray, attrs: AttributeSet? = null, defStyleAttr: Int = 0) {
//        val typedArray = context.obtainStyledAttributes(attrs, intArrayOf(
//            android.R.attr.layout_width,  // 0
//            android.R.attr.layout_height, // 1
//            android.R.attr.minWidth // 2
//        ))
//        val layoutWidth = typedArray.getLayoutDimension(0, ViewGroup.LayoutParams.WRAP_CONTENT)
//        val layoutHeight = typedArray.getLayoutDimension(1, ViewGroup.LayoutParams.WRAP_CONTENT)
//        typedArray.recycle()
//        setViewSize(layoutWidth, layoutHeight)

        // sets focusable
        isFocusable = false
        isFocusableInTouchMode = false

        containerView = CardView(context, attrs, defStyleAttr).apply {
            this.setCardBackgroundColor(Color.TRANSPARENT)
        }.also { containerView ->
            containerView.visibility = View.VISIBLE
            this.addView(containerView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setContainerAttributes(containerView, attr)
        }

        buttonView = ConstraintLayout(context, attrs, defStyleAttr).also { buttonView ->
            buttonView.visibility = View.VISIBLE
            containerView?.addView(buttonView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setButtonAttributes(buttonView, attr)
        }

        iconView = AppCompatImageView(context, attrs, defStyleAttr).apply {
            id = R.id.icon
        }.also { iconView ->
            buttonView?.addView(iconView, ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
                topToTop = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
            })
            setIconAttributes(iconView, attr)
        }

        textView = AppCompatTextView(context, attrs, defStyleAttr).apply {
            id = R.id.text
        }.also { textView ->
            buttonView?.addView(textView, ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
                topToTop = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
            })
            setTextAttributes(textView, attr)
        }

        iconTextRelativeMargin = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbIconTextRelativeMargin, 0)
        iconTextRelativeOf = parseRelativeOf(attr.getInt(R.styleable.StyleableButton_sbIconRelativeOfText, Gravity.START), 0)
        updateIconAndTextRelative()
    }

    private fun updateIconAndTextRelative() {
        setIconAndTextRelative(
            buttonView,
            iconView,
            textView,
            iconTextRelativeOf,
            iconTextRelativeMargin
        )
    }

    private fun setContainerAttributes(view: CardView, attr: TypedArray) {
        view.clipToOutline = true
        view.elevation = 0f

        // sets focusable
        view.isFocusable = true
        view.isFocusableInTouchMode = false

        // sets button size
        val buttonWidth = attr.getLayoutDimension(R.styleable.StyleableButton_sbWidth, ViewGroup.LayoutParams.MATCH_PARENT)
        val buttonHeight = attr.getLayoutDimension(R.styleable.StyleableButton_sbHeight, ViewGroup.LayoutParams.MATCH_PARENT)
        view.setViewSize(buttonWidth, buttonHeight)

        // button radius
        view.radius = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbCornerRadius, 0).toFloat()

        // sets button gravity
        val buttonLayoutGravity = parseGravity(attr.getInt(R.styleable.StyleableButton_sbLayoutGravity, DEFAULT_BUTTON_LAYOUT_GRAVITY))
        view.setLayoutGravity(buttonLayoutGravity)

        // sets button margins
        val buttonMargin = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbMargin, UNDEFINED_MARGIN)
        val buttonMarginHorizontal = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbMarginHorizontal, UNDEFINED_MARGIN)
        val buttonMarginVertical = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbMarginVertical, UNDEFINED_MARGIN)
        val buttonMarginStart = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbMarginStart, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (buttonMarginHorizontal != UNDEFINED_MARGIN) {
                buttonMarginHorizontal
            } else if (buttonMargin != UNDEFINED_MARGIN) {
                buttonMargin
            } else {
                0
            }
        }
        val buttonMarginEnd = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbMarginEnd, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (buttonMarginHorizontal != UNDEFINED_MARGIN) {
                buttonMarginHorizontal
            } else if (buttonMargin != UNDEFINED_MARGIN) {
                buttonMargin
            } else {
                0
            }
        }
        val buttonMarginTop = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbMarginTop, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (buttonMarginVertical != UNDEFINED_MARGIN) {
                buttonMarginVertical
            } else if (buttonMargin != UNDEFINED_MARGIN) {
                buttonMargin
            } else {
                0
            }
        }
        val buttonMarginBottom = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbMarginBottom, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (buttonMarginVertical != UNDEFINED_MARGIN) {
                buttonMarginVertical
            } else if (buttonMargin != UNDEFINED_MARGIN) {
                buttonMargin
            } else {
                0
            }
        }
        view.setViewMargin(buttonMarginStart, buttonMarginTop, buttonMarginEnd, buttonMarginBottom)
    }

    private fun setButtonAttributes(view: ConstraintLayout, attr: TypedArray) {

        // sets button size
        val buttonWidth = attr.getLayoutDimension(R.styleable.StyleableButton_sbWidth, ViewGroup.LayoutParams.MATCH_PARENT)
        val buttonHeight = attr.getLayoutDimension(R.styleable.StyleableButton_sbHeight, ViewGroup.LayoutParams.MATCH_PARENT)
        view.setViewSize(buttonWidth, buttonHeight)

        // sets button paddings
        val buttonPadding = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbPadding, UNDEFINED_PADDING)
        val buttonPaddingHorizontal = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbPaddingHorizontal, UNDEFINED_PADDING)
        val buttonPaddingVertical = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbPaddingVertical, UNDEFINED_PADDING)
        val buttonPaddingStart = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbPaddingStart, UNDEFINED_PADDING).let { size ->
            if (size != UNDEFINED_PADDING) {
                size
            } else if (buttonPaddingHorizontal != UNDEFINED_PADDING) {
                buttonPaddingHorizontal
            } else if (buttonPadding != UNDEFINED_PADDING) {
                buttonPadding
            } else {
                0
            }
        }
        val buttonPaddingEnd = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbPaddingEnd, UNDEFINED_PADDING).let { size ->
            if (size != UNDEFINED_PADDING) {
                size
            } else if (buttonPaddingHorizontal != UNDEFINED_PADDING) {
                buttonPaddingHorizontal
            } else if (buttonPadding != UNDEFINED_PADDING) {
                buttonPadding
            } else {
                0
            }
        }
        val buttonPaddingTop = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbPaddingTop, UNDEFINED_PADDING).let { size ->
            if (size != UNDEFINED_PADDING) {
                size
            } else if (buttonPaddingVertical != UNDEFINED_PADDING) {
                buttonPaddingVertical
            } else if (buttonPadding != UNDEFINED_PADDING) {
                buttonPadding
            } else {
                0
            }
        }
        val buttonPaddingBottom = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbPaddingBottom, UNDEFINED_PADDING).let { size ->
            if (size != UNDEFINED_PADDING) {
                size
            } else if (buttonPaddingVertical != UNDEFINED_PADDING) {
                buttonPaddingVertical
            } else if (buttonPadding != UNDEFINED_PADDING) {
                buttonPadding
            } else {
                0
            }
        }
        view.setViewPadding(buttonPaddingStart, buttonPaddingTop, buttonPaddingEnd, buttonPaddingBottom)

        attr.getDrawable(R.styleable.StyleableButton_sbBackground)?.let { background ->
            view.background = background
        }
        attr.getColorStateList(R.styleable.StyleableButton_sbBackgroundTint)?.let { tintList ->
            this.backgroundTintList = tintList
        }
        this.backgroundTintMode = parseTintMode(attr.getInt(R.styleable.StyleableButton_sbBackgroundTintMode, DEFAULT_TINT_MODE), null)

        // sets foreground
        attr.getDrawable(R.styleable.StyleableButton_sbForeground)?.let { foreground ->
            view.foreground = foreground
        }
        attr.getColorStateList(R.styleable.StyleableButton_sbForegroundTint)?.let { tintList ->
            view.foregroundTintList = tintList
        }
        view.foregroundTintMode = parseTintMode(attr.getInt(R.styleable.StyleableButton_sbForegroundTintMode, DEFAULT_TINT_MODE), null)
    }

    private fun setIconAttributes(view: AppCompatImageView, attr: TypedArray) {
        // sets icon size
        val iconWidth = attr.getLayoutDimension(R.styleable.StyleableButton_sbIconWidth, ViewGroup.LayoutParams.MATCH_PARENT)
        val iconHeight = attr.getLayoutDimension(R.styleable.StyleableButton_sbIconHeight, ViewGroup.LayoutParams.MATCH_PARENT)
        view.setViewSize(iconWidth, iconHeight)

        // sets icon adjust view bounds
        view.adjustViewBounds = attr.getBoolean(R.styleable.StyleableButton_sbIconAdjustViewBounds, false)

        // sets icon scale type
        parseScaleType(attr.getInt(R.styleable.StyleableButton_sbIconScaleType, -1))?.let { scaleType ->
            view.scaleType = scaleType
        }

        // sets icon margins
        val iconMargin = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbIconMargin, UNDEFINED_MARGIN)
        val iconMarginHorizontal = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbIconMarginHorizontal, UNDEFINED_MARGIN)
        val iconMarginVertical = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbIconMarginVertical, UNDEFINED_MARGIN)
        val iconMarginStart = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbIconMarginStart, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (iconMarginHorizontal != UNDEFINED_MARGIN) {
                iconMarginHorizontal
            } else if (iconMargin != UNDEFINED_MARGIN) {
                iconMargin
            } else {
                0
            }
        }
        val iconMarginEnd = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbIconMarginEnd, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (iconMarginHorizontal != UNDEFINED_MARGIN) {
                iconMarginHorizontal
            } else if (iconMargin != UNDEFINED_MARGIN) {
                iconMargin
            } else {
                0
            }
        }
        val iconMarginTop = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbIconMarginTop, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (iconMarginVertical != UNDEFINED_MARGIN) {
                iconMarginVertical
            } else if (iconMargin != UNDEFINED_MARGIN) {
                iconMargin
            } else {
                0
            }
        }
        val iconMarginBottom = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbIconMarginBottom, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (iconMarginVertical != UNDEFINED_MARGIN) {
                iconMarginVertical
            } else if (iconMargin != UNDEFINED_MARGIN) {
                iconMargin
            } else {
                0
            }
        }
        view.setViewMargin(iconMarginStart, iconMarginTop, iconMarginEnd, iconMarginBottom)

        // sets icon background
        attr.getDrawable(R.styleable.StyleableButton_sbIconBackground)?.let { background ->
            view.background = background
        }
        attr.getColorStateList(R.styleable.StyleableButton_sbIconBackgroundTint)?.let { tintList ->
            view.backgroundTintList = tintList
        }
        view.backgroundTintMode = parseTintMode(attr.getInt(R.styleable.StyleableButton_sbIconBackgroundTintMode, DEFAULT_TINT_MODE), null)

        // sets icon src
        attr.getDrawable(R.styleable.StyleableButton_sbIconSrc)?.let { drawable ->
            view.setImageDrawable(drawable)
        }
        attr.getColorStateList(R.styleable.StyleableButton_sbIconSrcTint)?.let { tintList ->
            view.imageTintList = tintList
        }
        view.imageTintMode = parseTintMode(attr.getInt(R.styleable.StyleableButton_sbIconSrcTintMode, DEFAULT_TINT_MODE), null)

        view.setLayoutGravity(Gravity.CENTER)
    }

    private fun setTextAttributes(view: AppCompatTextView, attr: TypedArray) {
        // sets text size
        val textWidth = attr.getLayoutDimension(R.styleable.StyleableButton_sbTextWidth, 0)
        val textHeight = attr.getLayoutDimension(R.styleable.StyleableButton_sbTextHeight, 0)
        view.setViewSize(textWidth, textHeight)

        // sets text
        attr.getText(R.styleable.StyleableButton_sbText)?.let { text ->
            view.text = text
        }
        // sets text color
        attr.getColorStateList(R.styleable.StyleableButton_sbTextColor)?.let { colorStateList ->
            view.setTextColor(colorStateList)
        }
        // sets text size
        val textSize = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbTextSize, 0)
        if (textSize > 0) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        }
        // sets text style
        val textStyle = attr.getInt(R.styleable.StyleableButton_sbTextStyle, 0)
        if (textStyle > 0) {
            view.typeface = when (textStyle) {
                1 -> Typeface.DEFAULT_BOLD
                else -> Typeface.DEFAULT
            }
        }
        // sets text gravity
        val textGravity = parseGravity(attr.getInt(R.styleable.StyleableButton_sbTextGravity, DEFAULT_TEXT_GRAVITY))
        if (textGravity > 0) {
            view.gravity = textGravity
        }
        // sets text ellipsize
        view.ellipsize = parseEllipsize(attr.getInt(R.styleable.StyleableButton_sbTextEllipsize, -1))
        // sets text lines
        view.setLines(attr.getInt(R.styleable.StyleableButton_sbTextLines, 1))
        // sets text max lines
        view.maxLines = attr.getInt(R.styleable.StyleableButton_sbTextMaxLines, Integer.MAX_VALUE)

        // sets text margins
        val textMargin = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbTextMargin, UNDEFINED_MARGIN)
        val textMarginHorizontal = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbTextMarginHorizontal, UNDEFINED_MARGIN)
        val textMarginVertical = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbTextMarginVertical, UNDEFINED_MARGIN)
        val textMarginStart = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbTextMarginStart, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (textMarginHorizontal != UNDEFINED_MARGIN) {
                textMarginHorizontal
            } else if (textMargin != UNDEFINED_MARGIN) {
                textMargin
            } else {
                0
            }
        }
        val textMarginEnd = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbTextMarginEnd, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (textMarginHorizontal != UNDEFINED_MARGIN) {
                textMarginHorizontal
            } else if (textMargin != UNDEFINED_MARGIN) {
                textMargin
            } else {
                0
            }
        }
        val textMarginTop = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbTextMarginTop, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (textMarginVertical != UNDEFINED_MARGIN) {
                textMarginVertical
            } else if (textMargin != UNDEFINED_MARGIN) {
                textMargin
            } else {
                0
            }
        }
        val textMarginBottom = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbTextMarginBottom, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (textMarginVertical != UNDEFINED_MARGIN) {
                textMarginVertical
            } else if (textMargin != UNDEFINED_MARGIN) {
                textMargin
            } else {
                0
            }
        }
        view.setViewMargin(textMarginStart, textMarginTop, textMarginEnd, textMarginBottom)
    }

    private fun setIconAndTextRelative(buttonView: ConstraintLayout?, iconView: AppCompatImageView?, textView: AppCompatTextView?, relativeOf: Int, margin: Int) {
        if (buttonView == null || iconView == null || textView == null) {
            return
        }

        iconView.isVisible = iconView.drawable != null || this.background != null
        textView.isVisible = !textView.text.isNullOrEmpty()

        if (!iconView.isVisible || !textView.isVisible) {
            return
        }

        val constraintSet = ConstraintSet()
        when (relativeOf) {
            Gravity.TOP -> {
                constraintSet.clone(buttonView)

                constraintSet.connect(iconView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(iconView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(iconView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                constraintSet.connect(iconView.id, ConstraintSet.BOTTOM, textView.id, ConstraintSet.TOP)

                constraintSet.connect(textView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(textView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(textView.id, ConstraintSet.TOP, iconView.id, ConstraintSet.BOTTOM)
                constraintSet.connect(textView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                constraintSet.createVerticalChain(ConstraintSet.PARENT_ID, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,
                    intArrayOf(iconView.id, textView.id), null, ConstraintSet.CHAIN_PACKED
                )
                constraintSet.applyTo(buttonView)

                // sets top or bottom margin after applied
                textView.setViewMarginTop(margin + textView.marginTop)
            }

            Gravity.BOTTOM -> {
                constraintSet.clone(buttonView)

                textView.bringToFront()

                constraintSet.connect(textView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(textView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(textView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                constraintSet.connect(textView.id, ConstraintSet.BOTTOM, textView.id, ConstraintSet.TOP)

                constraintSet.connect(iconView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(iconView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(iconView.id, ConstraintSet.TOP, iconView.id, ConstraintSet.BOTTOM)
                constraintSet.connect(iconView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                constraintSet.createVerticalChain(ConstraintSet.PARENT_ID, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,
                    intArrayOf(textView.id, iconView.id), null, ConstraintSet.CHAIN_PACKED
                )

                constraintSet.applyTo(buttonView)

                // sets top or bottom margin after applied
                textView.setViewMarginBottom(margin + textView.marginBottom)
            }

            Gravity.START -> {
                // sets start or end margin before applied
                textView.setViewMarginStart(margin + textView.marginStart)

                constraintSet.clone(buttonView)

                constraintSet.connect(iconView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(iconView.id, ConstraintSet.END, textView.id, ConstraintSet.START)
                constraintSet.connect(iconView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                constraintSet.connect(iconView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                constraintSet.connect(textView.id, ConstraintSet.START, iconView.id, ConstraintSet.END)
                constraintSet.connect(textView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(textView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                constraintSet.connect(textView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                constraintSet.createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,
                    intArrayOf(iconView.id, textView.id), null, ConstraintSet.CHAIN_PACKED
                )

                constraintSet.applyTo(buttonView)
            }

            Gravity.END -> {
                // sets start or end margin before applied
                textView.setViewMarginEnd(margin + textView.marginEnd)

                constraintSet.clone(buttonView)

                textView.bringToFront()

                constraintSet.connect(textView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(textView.id, ConstraintSet.END, iconView.id, ConstraintSet.START)
                constraintSet.connect(textView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                constraintSet.connect(textView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                constraintSet.connect(iconView.id, ConstraintSet.START, textView.id, ConstraintSet.END)
                constraintSet.connect(iconView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(iconView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                constraintSet.connect(iconView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                constraintSet.createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,
                    intArrayOf(textView.id, iconView.id), null, ConstraintSet.CHAIN_PACKED
                )

                constraintSet.applyTo(buttonView)
            }
        }
    }

    /**
     * The tint is drawn of the drawable.
     * name="src_over" value="3"
     * name="src_in" value="5"
     * name="src_atop" value="9"
     * name="multiply" value="14"
     * name="screen" value="15"
     * name="add" value="16"
     */
    private fun parseTintMode(value: Int, defaultMode: PorterDuff.Mode?): PorterDuff.Mode? {
        return when (value) {
            3 -> PorterDuff.Mode.SRC_OVER
            5 -> PorterDuff.Mode.SRC_IN
            9 -> PorterDuff.Mode.SRC_ATOP
            14 -> PorterDuff.Mode.MULTIPLY
            15 -> PorterDuff.Mode.SCREEN
            16 -> PorterDuff.Mode.ADD
            else -> defaultMode
        }
    }

    /**
     * name="top" value="0x01"
     * name="bottom" value="0x02"
     * name="start" value="0x04"
     * name="end" value="0x08"
     * name="center_vertical" value="0x10"
     * name="center_horizontal" value="0x20"
     * name="center" value="0x30"
     */
    private fun parseGravity(attrsValue: Int): Int {
        var gravity = 0
        if (attrsValue and 0x30 == 0x30) {
            gravity = Gravity.CENTER
            return gravity
        }
        if (attrsValue and 0x01 == 0x01) { // top
            gravity = /*gravity or */Gravity.TOP
        }
        if (attrsValue and 0x02 == 0x02) { // bottom
            gravity = gravity or Gravity.BOTTOM
        }
        if (attrsValue and 0x04 == 0x04) { // start
            gravity = gravity or Gravity.START
        }
        if (attrsValue and 0x08 == 0x08) { // end
            gravity = gravity or Gravity.END
        }
        if (attrsValue and 0x10 == 0x10) { // center vertical
            gravity = gravity or Gravity.CENTER_VERTICAL
        }
        if (attrsValue and 0x20 == 0x20) { // center horizontal
            gravity = gravity or Gravity.CENTER_HORIZONTAL
        }
        return gravity
    }

    /**
     * Scale using the image matrix when drawing.
     * See {@link android.widget.ImageView#ScaleType }
     * name="matrix" value="0"
     * name="fitXY" value="1"
     * name="fitStart" value="2"
     * name="fitCenter" value="3"
     * name="fitEnd" value="4"
     * name="center" value="5"
     * name="centerCrop" value="6"
     * name="centerInside" value="7"
     */
    private fun parseScaleType(scaleType: Int): ImageView.ScaleType? {
        return when (scaleType) {
            0 -> ImageView.ScaleType.MATRIX
            1 -> ImageView.ScaleType.FIT_XY
            2 -> ImageView.ScaleType.FIT_START
            3 -> ImageView.ScaleType.FIT_CENTER
            4 -> ImageView.ScaleType.FIT_END
            5 -> ImageView.ScaleType.CENTER
            6 -> ImageView.ScaleType.CENTER_CROP
            7 -> ImageView.ScaleType.CENTER_INSIDE
            else -> null
        }
    }

    /**
     *  ELLIPSIZE_NOT_SET = -1;
     *  ELLIPSIZE_NONE = 0;
     *  ELLIPSIZE_START = 1;
     *  ELLIPSIZE_MIDDLE = 2;
     *  ELLIPSIZE_END = 3;
     *  ELLIPSIZE_MARQUEE = 4;
     */
    private fun parseEllipsize(ellipsize: Int): TextUtils.TruncateAt? {
        return when (ellipsize) {
            1 -> // ELLIPSIZE_START
                TextUtils.TruncateAt.START
            2 -> // ELLIPSIZE_MIDDLE
                TextUtils.TruncateAt.MIDDLE
            3 -> //ELLIPSIZE_END
                TextUtils.TruncateAt.END
            4 -> // ELLIPSIZE_MARQUEE
                TextUtils.TruncateAt.MARQUEE
            else -> // ELLIPSIZE_NOT_SET
                null
        }
    }

    /**
     * name="top" value="0x01"
     * name="bottom" value="0x02"
     * name="start" value="0x04"
     * name="end" value="0x08"
     */
    private fun parseRelativeOf(location: Int, default: Int): Int {
        return when (location) {
            1 -> Gravity.TOP
            2 -> Gravity.BOTTOM
            3 -> Gravity.START
            4 -> Gravity.END
            else -> default
        }
    }

    /**
     * <!-- Visible on screen; the default value. -->
     *  name="visible" value="0"
     * <!-- Not displayed, but taken into account during layout (space is left for it). -->
     * name="invisible" value="1"
     * <!-- Completely hidden, as if the view had not been added. -->
     *  name="gone" value="2
     */
    private fun parseVisibility(visibility: Int, default: Int): Int {
        return when (visibility) {
            0 -> View.VISIBLE
            1 -> View.INVISIBLE
            2 -> View.GONE
            else -> default
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        containerView?.setOnClickListener(l)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        containerView?.setOnLongClickListener(l)
    }

    fun setStyle(@StyleRes style: Int) {
        val context = this.context ?: return
        val containerView = this.containerView ?: return
        val buttonView = this.buttonView ?: return
        val iconView = this.iconView ?: return
        val textView = this.textView ?: return

        val attr = context.obtainStyledAttributes(style, R.styleable.StyleableButton)

        setContainerAttributes(containerView, attr)
        setButtonAttributes(buttonView, attr)
        setIconAttributes(iconView, attr)
        setTextAttributes(textView, attr)

        iconTextRelativeMargin = attr.getDimensionPixelSize(R.styleable.StyleableButton_sbIconTextRelativeMargin, 0)
        iconTextRelativeOf = parseRelativeOf(attr.getInt(R.styleable.StyleableButton_sbIconRelativeOfText, Gravity.START), 0)
        updateIconAndTextRelative()

        attr.recycle()
        requestLayout()
    }

    fun setIcon(@DrawableRes icon: Int?) {
        val iconView = this.iconView ?: return

        if (icon != null) {
            iconView.setImageResource(icon)
        } else {
            iconView.setImageDrawable(null)
        }
        updateIconAndTextRelative()
    }

    fun setIcon(icon: Drawable?) {
        val iconView = this.iconView ?: return

        iconView.setImageDrawable(icon)
        updateIconAndTextRelative()
    }

    fun setText(@StringRes text: Int?) {
        val textView = this.textView ?: return

        if (text != null) {
            textView.setText(text)
        } else {
            textView.text = ""
        }
        updateIconAndTextRelative()
    }

    fun setText(text: String?) {
        val textView = this.textView ?: return

        textView.text = text
        updateIconAndTextRelative()
    }
}