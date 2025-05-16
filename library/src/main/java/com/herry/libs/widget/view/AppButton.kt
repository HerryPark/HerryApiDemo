package com.herry.libs.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import com.herry.libs.R
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.setLayoutGravity
import com.herry.libs.widget.extension.setViewMargin
import com.herry.libs.widget.extension.setViewMarginBottom
import com.herry.libs.widget.extension.setViewMarginEnd
import com.herry.libs.widget.extension.setViewMarginStart
import com.herry.libs.widget.extension.setViewMarginTop
import com.herry.libs.widget.extension.setViewPadding
import com.herry.libs.widget.extension.setViewSize

@Suppress("SameParameterValue", "unused", "MemberVisibilityCanBePrivate")
class AppButton: FrameLayout {

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
    private var iconTextRelativeOf: RelativeOf = RelativeOf.START
    private var iconTextRelativeChainStyle: Int = ConstraintSet.CHAIN_PACKED

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        context.withStyledAttributes(attrs, R.styleable.AppButton) {
            retrieveAttributes(context, this, attrs, defStyleAttr)
        }
    }

    @SuppressLint("ResourceType")
    constructor(context: Context, @StyleRes styleResId: Int, width: Int = LayoutParams.WRAP_CONTENT, height: Int = LayoutParams.WRAP_CONTENT) : super(context, null, 0) {
        val sizeTypedArray = context.obtainStyledAttributes(styleResId, intArrayOf(
            android.R.attr.layout_width,  // 0
            android.R.attr.layout_height, // 1
            android.R.attr.minWidth // 2
        ))
        val layoutWidth = sizeTypedArray.getLayoutDimension(0, width)
        val layoutHeight = sizeTypedArray.getLayoutDimension(1, height)
        sizeTypedArray.recycle()

        setViewSize(layoutWidth, layoutHeight)

        context.withStyledAttributes(styleResId, R.styleable.AppButton) {
            retrieveAttributes(context, this)
        }
    }

    private fun retrieveAttributes(context: Context, attr: TypedArray, attrs: AttributeSet? = null, defStyleAttr: Int = 0) {
//        val typedArray = context.obtainStyledAttributes(attrs, intArrayOf(
//            android.R.attr.layout_width,  // 0
//            android.R.attr.layout_height, // 1
//            android.R.attr.minWidth // 2
//        ))
//        val layoutWidth = typedArray.getLayoutDimension(0, LayoutParams.WRAP_CONTENT)
//        val layoutHeight = typedArray.getLayoutDimension(1, LayoutParams.WRAP_CONTENT)
//        typedArray.recycle()
//        setViewSize(layoutWidth, layoutHeight)

        // sets focusable
        isFocusable = false
        isFocusableInTouchMode = false

        containerView = CardView(context, attrs, defStyleAttr).apply {
            this.setCardBackgroundColor(Color.TRANSPARENT)
        }.also { containerView ->
            containerView.visibility = VISIBLE
            this.addView(containerView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setContainerAttributes(containerView, attr)
        }

        buttonView = ConstraintLayout(context, attrs, defStyleAttr).also { buttonView ->
            buttonView.visibility = VISIBLE
            containerView?.addView(buttonView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setButtonAttributes(buttonView, attr)
        }

        iconView = AppCompatImageView(context, attrs, defStyleAttr).apply {
            id = R.id.icon
        }.also { iconView ->
            buttonView?.addView(iconView, ConstraintLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
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
            buttonView?.addView(textView, ConstraintLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
                topToTop = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
            })
            setTextAttributes(textView, attr)
        }

        iconTextRelativeMargin = attr.getDimensionPixelSize(R.styleable.AppButton_abIconTextRelativeMargin, 0)
        iconTextRelativeOf = parseRelativeOf(attr.getInt(R.styleable.AppButton_abIconRelativeOfText, RelativeOf.START.value), RelativeOf.START)
        iconTextRelativeChainStyle = parseChainStyleOf(attr.getInt(R.styleable.AppButton_abIconTextRelativeChainStyle, iconTextRelativeChainStyle), 0)
        updateIconAndTextRelative()
    }

    private fun updateIconAndTextRelative() {
        setIconAndTextRelative(
            buttonView,
            iconView,
            textView,
            iconTextRelativeOf,
            iconTextRelativeMargin,
            iconTextRelativeChainStyle
        )
    }

    private fun setContainerAttributes(view: CardView, attr: TypedArray) {
        view.clipToOutline = true
        view.elevation = 0f

        // sets focusable
        view.isFocusable = true
        view.isFocusableInTouchMode = false

        // sets button size
        val buttonWidth = attr.getLayoutDimension(R.styleable.AppButton_abWidth, LayoutParams.MATCH_PARENT)
        val buttonHeight = attr.getLayoutDimension(R.styleable.AppButton_abHeight, LayoutParams.MATCH_PARENT)
        view.setViewSize(buttonWidth, buttonHeight)
        val buttonMinWidth = attr.getDimensionPixelSize(R.styleable.AppButton_abMinWidth, 0)
        view.minimumWidth = buttonMinWidth
        val buttonMinHeight = attr.getDimensionPixelSize(R.styleable.AppButton_abMinHeight, 0)
        view.minimumHeight = buttonMinHeight

        // button radius
        view.radius = attr.getDimensionPixelSize(R.styleable.AppButton_abCornerRadius, 0).toFloat()

        // sets button gravity
        val buttonLayoutGravity = parseGravity(attr.getInt(R.styleable.AppButton_abLayoutGravity, DEFAULT_BUTTON_LAYOUT_GRAVITY))
        view.setLayoutGravity(buttonLayoutGravity)

        // sets button margins
        val buttonMargin = attr.getDimensionPixelSize(R.styleable.AppButton_abMargin, UNDEFINED_MARGIN)
        val buttonMarginHorizontal = attr.getDimensionPixelSize(R.styleable.AppButton_abMarginHorizontal, UNDEFINED_MARGIN)
        val buttonMarginVertical = attr.getDimensionPixelSize(R.styleable.AppButton_abMarginVertical, UNDEFINED_MARGIN)
        val buttonMarginStart = attr.getDimensionPixelSize(R.styleable.AppButton_abMarginStart, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (buttonMarginHorizontal != UNDEFINED_MARGIN) {
                buttonMarginHorizontal
            } else if (buttonMargin != UNDEFINED_MARGIN) {
                buttonMargin
            } else {
                view.marginStart
            }
        }
        val buttonMarginEnd = attr.getDimensionPixelSize(R.styleable.AppButton_abMarginEnd, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (buttonMarginHorizontal != UNDEFINED_MARGIN) {
                buttonMarginHorizontal
            } else if (buttonMargin != UNDEFINED_MARGIN) {
                buttonMargin
            } else {
                view.marginEnd
            }
        }
        val buttonMarginTop = attr.getDimensionPixelSize(R.styleable.AppButton_abMarginTop, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (buttonMarginVertical != UNDEFINED_MARGIN) {
                buttonMarginVertical
            } else if (buttonMargin != UNDEFINED_MARGIN) {
                buttonMargin
            } else {
                view.marginTop
            }
        }
        val buttonMarginBottom = attr.getDimensionPixelSize(R.styleable.AppButton_abMarginBottom, UNDEFINED_MARGIN).let { size ->
            if (size != UNDEFINED_MARGIN) {
                size
            } else if (buttonMarginVertical != UNDEFINED_MARGIN) {
                buttonMarginVertical
            } else if (buttonMargin != UNDEFINED_MARGIN) {
                buttonMargin
            } else {
                view.marginBottom
            }
        }
        view.setViewMargin(buttonMarginStart, buttonMarginTop, buttonMarginEnd, buttonMarginBottom)
    }

    private fun setButtonAttributes(view: ConstraintLayout, attr: TypedArray) {

        // sets button size
        val buttonWidth = attr.getLayoutDimension(R.styleable.AppButton_abWidth, LayoutParams.MATCH_PARENT)
        val buttonHeight = attr.getLayoutDimension(R.styleable.AppButton_abHeight, LayoutParams.MATCH_PARENT)
        view.setViewSize(buttonWidth, buttonHeight)
        val buttonMinWidth = attr.getDimensionPixelSize(R.styleable.AppButton_abMinWidth, 0)
        view.minWidth = buttonMinWidth
        val buttonMinHeight = attr.getDimensionPixelSize(R.styleable.AppButton_abMinHeight, 0)
        view.minHeight = buttonMinHeight

        // sets button paddings
        val buttonPadding = attr.getDimensionPixelSize(R.styleable.AppButton_abPadding, UNDEFINED_PADDING)
        val buttonPaddingHorizontal = attr.getDimensionPixelSize(R.styleable.AppButton_abPaddingHorizontal, UNDEFINED_PADDING)
        val buttonPaddingVertical = attr.getDimensionPixelSize(R.styleable.AppButton_abPaddingVertical, UNDEFINED_PADDING)
        val buttonPaddingStart = attr.getDimensionPixelSize(R.styleable.AppButton_abPaddingStart, UNDEFINED_PADDING).let { size ->
            if (size != UNDEFINED_PADDING) {
                size
            } else if (buttonPaddingHorizontal != UNDEFINED_PADDING) {
                buttonPaddingHorizontal
            } else if (buttonPadding != UNDEFINED_PADDING) {
                buttonPadding
            } else {
                view.paddingStart
            }
        }
        val buttonPaddingEnd = attr.getDimensionPixelSize(R.styleable.AppButton_abPaddingEnd, UNDEFINED_PADDING).let { size ->
            if (size != UNDEFINED_PADDING) {
                size
            } else if (buttonPaddingHorizontal != UNDEFINED_PADDING) {
                buttonPaddingHorizontal
            } else if (buttonPadding != UNDEFINED_PADDING) {
                buttonPadding
            } else {
                view.paddingEnd
            }
        }
        val buttonPaddingTop = attr.getDimensionPixelSize(R.styleable.AppButton_abPaddingTop, UNDEFINED_PADDING).let { size ->
            if (size != UNDEFINED_PADDING) {
                size
            } else if (buttonPaddingVertical != UNDEFINED_PADDING) {
                buttonPaddingVertical
            } else if (buttonPadding != UNDEFINED_PADDING) {
                buttonPadding
            } else {
                view.paddingTop
            }
        }
        val buttonPaddingBottom = attr.getDimensionPixelSize(R.styleable.AppButton_abPaddingBottom, UNDEFINED_PADDING).let { size ->
            if (size != UNDEFINED_PADDING) {
                size
            } else if (buttonPaddingVertical != UNDEFINED_PADDING) {
                buttonPaddingVertical
            } else if (buttonPadding != UNDEFINED_PADDING) {
                buttonPadding
            } else {
                view.paddingBottom
            }
        }
        view.setViewPadding(buttonPaddingStart, buttonPaddingTop, buttonPaddingEnd, buttonPaddingBottom)

        view.background = attr.getDrawable(R.styleable.AppButton_abBackground)
        view.backgroundTintList = attr.getColorStateList(R.styleable.AppButton_abBackgroundTint)
        view.backgroundTintMode = parseTintMode(attr.getInt(R.styleable.AppButton_abBackgroundTintMode, DEFAULT_TINT_MODE), null)

        // sets foreground
        view.foreground = attr.getDrawable(R.styleable.AppButton_abForeground)
        view.foregroundTintList  = attr.getColorStateList(R.styleable.AppButton_abForegroundTint)
        view.foregroundTintMode = parseTintMode(attr.getInt(R.styleable.AppButton_abForegroundTintMode, DEFAULT_TINT_MODE), null)
    }

    private fun setIconAttributes(view: AppCompatImageView, attr: TypedArray) {
        // sets icon size
        val iconWidth = attr.getLayoutDimension(R.styleable.AppButton_abIconWidth, LayoutParams.MATCH_PARENT)
        val iconHeight = attr.getLayoutDimension(R.styleable.AppButton_abIconHeight, LayoutParams.MATCH_PARENT)
        view.setViewSize(iconWidth, iconHeight)

        // sets icon adjust view bounds
        view.adjustViewBounds = attr.getBoolean(R.styleable.AppButton_abIconAdjustViewBounds, false)

        // sets icon scale type
        parseScaleType(attr.getInt(R.styleable.AppButton_abIconScaleType, -1))?.let { scaleType ->
            view.scaleType = scaleType
        }

        // sets icon margins
        val iconMargin = attr.getDimensionPixelSize(R.styleable.AppButton_abIconMargin, UNDEFINED_MARGIN)
        val iconMarginHorizontal = attr.getDimensionPixelSize(R.styleable.AppButton_abIconMarginHorizontal, UNDEFINED_MARGIN)
        val iconMarginVertical = attr.getDimensionPixelSize(R.styleable.AppButton_abIconMarginVertical, UNDEFINED_MARGIN)
        val iconMarginStart = attr.getDimensionPixelSize(R.styleable.AppButton_abIconMarginStart, UNDEFINED_MARGIN).let { size ->
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
        val iconMarginEnd = attr.getDimensionPixelSize(R.styleable.AppButton_abIconMarginEnd, UNDEFINED_MARGIN).let { size ->
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
        val iconMarginTop = attr.getDimensionPixelSize(R.styleable.AppButton_abIconMarginTop, UNDEFINED_MARGIN).let { size ->
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
        val iconMarginBottom = attr.getDimensionPixelSize(R.styleable.AppButton_abIconMarginBottom, UNDEFINED_MARGIN).let { size ->
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
        view.background = attr.getDrawable(R.styleable.AppButton_abIconBackground)
        view.backgroundTintList = attr.getColorStateList(R.styleable.AppButton_abIconBackgroundTint)
        view.backgroundTintMode = parseTintMode(attr.getInt(R.styleable.AppButton_abIconBackgroundTintMode, DEFAULT_TINT_MODE), null)

        // sets icon src
        view.setImageDrawable(attr.getDrawable(R.styleable.AppButton_abIconSrc))

        view.imageTintList = attr.getColorStateList(R.styleable.AppButton_abIconSrcTint)
        view.imageTintMode = parseTintMode(attr.getInt(R.styleable.AppButton_abIconSrcTintMode, DEFAULT_TINT_MODE), null)

        view.setLayoutGravity(Gravity.CENTER)
    }

    private fun setTextAttributes(view: AppCompatTextView, attr: TypedArray) {
        // sets text size
        val textWidth = attr.getLayoutDimension(R.styleable.AppButton_abTextWidth, 0)
        val textHeight = attr.getLayoutDimension(R.styleable.AppButton_abTextHeight, 0)
        view.setViewSize(textWidth, textHeight)

        // sets text
        view.text = attr.getText(R.styleable.AppButton_abText) ?: view.text ?: ""
        // sets text color
        val textColorStateList = attr.getColorStateList(R.styleable.AppButton_abTextColor) ?: view.textColors
        textColorStateList?.let { colorStateList ->
            view.setTextColor(colorStateList)
        }
        // sets text size
        val textSize = attr.getDimensionPixelSize(R.styleable.AppButton_abTextSize, 0)
        if (textSize > 0) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        }
        // sets text style
        val textStyle = attr.getInt(R.styleable.AppButton_abTextStyle, 0)
        if (textStyle > 0) {
            view.typeface = when (textStyle) {
                1 -> Typeface.DEFAULT_BOLD
                else -> Typeface.DEFAULT
            }
        }
        // sets text gravity
        val textGravity = parseGravity(attr.getInt(R.styleable.AppButton_abTextGravity, DEFAULT_TEXT_GRAVITY))
        if (textGravity > 0) {
            view.gravity = textGravity
        }
        // sets text ellipsize
        view.ellipsize = parseEllipsize(attr.getInt(R.styleable.AppButton_abTextEllipsize, -1))
        // sets text max lines
        val maxLines = attr.getInt(R.styleable.AppButton_abTextMaxLines, -1)
        if (maxLines > -1) {
            view.maxLines = maxLines
        }
        // sets text lines
        val textLines = attr.getInt(R.styleable.AppButton_abTextLines, -1)
        if (textLines > -1) {
            view.setLines(textLines)
        }

        val textLineSpace = attr.getDimensionPixelSize(R.styleable.AppButton_abTextLineSpacingExtra, 0).toFloat()
        val textLineSpacingMultiplier = attr.getFloat(R.styleable.AppButton_abTextLineSpacingMultiplier, 1.0f)
        view.setLineSpacing(textLineSpace, textLineSpacingMultiplier)

        // sets text margins
        val textMargin = attr.getDimensionPixelSize(R.styleable.AppButton_abTextMargin, UNDEFINED_MARGIN)
        val textMarginHorizontal = attr.getDimensionPixelSize(R.styleable.AppButton_abTextMarginHorizontal, UNDEFINED_MARGIN)
        val textMarginVertical = attr.getDimensionPixelSize(R.styleable.AppButton_abTextMarginVertical, UNDEFINED_MARGIN)
        val textMarginStart = attr.getDimensionPixelSize(R.styleable.AppButton_abTextMarginStart, UNDEFINED_MARGIN).let { size ->
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
        val textMarginEnd = attr.getDimensionPixelSize(R.styleable.AppButton_abTextMarginEnd, UNDEFINED_MARGIN).let { size ->
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
        val textMarginTop = attr.getDimensionPixelSize(R.styleable.AppButton_abTextMarginTop, UNDEFINED_MARGIN).let { size ->
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
        val textMarginBottom = attr.getDimensionPixelSize(R.styleable.AppButton_abTextMarginBottom, UNDEFINED_MARGIN).let { size ->
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

    private fun setIconAndTextRelative(
        buttonView: ConstraintLayout?,
        iconView: AppCompatImageView?,
        textView: AppCompatTextView?,
        relativeOf: RelativeOf,
        margin: Int,
        chainStyle: Int
    ) {
        if (buttonView == null || iconView == null || textView == null) {
            return
        }

        iconView.isVisible = iconView.drawable != null || iconView.background != null
        textView.isVisible = !textView.text.isNullOrEmpty()

        if (!iconView.isVisible || !textView.isVisible) {
            return
        }

        val constraintSet = ConstraintSet()
        when (relativeOf) {
            RelativeOf.TOP -> {
                iconView.bringToFront()

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
                    intArrayOf(iconView.id, textView.id), null, chainStyle
                )

                constraintSet.applyTo(buttonView)

                if (margin != 0) {
                    // sets top or bottom margin after applied
                    textView.setViewMarginTop(margin + textView.marginTop)
                }
            }

            RelativeOf.BOTTOM -> {
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
                    intArrayOf(textView.id, iconView.id), null, chainStyle
                )

                constraintSet.applyTo(buttonView)

                if (margin != 0) {
                    // sets top or bottom margin after applied
                    textView.setViewMarginBottom(margin + textView.marginBottom)
                }
            }

            RelativeOf.START -> {
                if (margin != 0) {
                    // sets start or end margin before applied
                    textView.setViewMarginStart(margin + textView.marginStart)
                }

                iconView.bringToFront()

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
                    intArrayOf(iconView.id, textView.id), null, chainStyle
                )

                constraintSet.applyTo(buttonView)
            }

            RelativeOf.END -> {
                if (margin != 0) {
                    // sets start or end margin before applied
                    textView.setViewMarginEnd(margin + textView.marginEnd)
                }

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
                    intArrayOf(textView.id, iconView.id), null, chainStyle
                )

                constraintSet.applyTo(buttonView)
            }

            RelativeOf.OVERLAP -> {
                // ignores the margin between the icon and the text
                // ignores the chain style

                constraintSet.clone(buttonView)

                constraintSet.connect(textView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(textView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(textView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                constraintSet.connect(textView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                constraintSet.connect(iconView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(iconView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(iconView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                constraintSet.connect(iconView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

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

    private enum class RelativeOf(val value: Int) {
        OVERLAP (0),
        TOP (1),
        BOTTOM (2),
        START (3),
        END (4);

        companion object {
            fun generate(value: Int): RelativeOf? = entries.firstOrNull { it.value == value }
        }
    }

    /**
     * name="overlap" value="0"
     * name="top" value="1"
     * name="bottom" value="2"
     * name="start" value="3"
     * name="end" value="4"
     */
    private fun parseRelativeOf(location: Int, default: RelativeOf): RelativeOf {
        return RelativeOf.generate(location) ?: default
    }

    /**
     * name="spread" value="0"
     * name="spread_inside" value="1"
     * name="packed" value="2"
     */
    private fun parseChainStyleOf(chain: Int, default: Int): Int {
        return when (chain) {
            0 -> ConstraintSet.CHAIN_SPREAD
            1 -> ConstraintSet.CHAIN_SPREAD_INSIDE
            2 -> ConstraintSet.CHAIN_PACKED
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
            0 -> VISIBLE
            1 -> INVISIBLE
            2 -> GONE
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

        context.withStyledAttributes(style, R.styleable.AppButton) {

            setContainerAttributes(containerView, this)
            setButtonAttributes(buttonView, this)
            setIconAttributes(iconView, this)
            setTextAttributes(textView, this)

            iconTextRelativeMargin = getDimensionPixelSize(R.styleable.AppButton_abIconTextRelativeMargin, 0)
            iconTextRelativeOf = parseRelativeOf(getInt(R.styleable.AppButton_abIconRelativeOfText, RelativeOf.START.value), RelativeOf.START)
            updateIconAndTextRelative()

        }
        requestLayout()
    }

    fun setIcon(@DrawableRes icon: Int?) {
        val context = this.iconView?.context ?: return

        setIcon(ViewUtil.getDrawable(context, icon ?: 0))
    }

    fun setIcon(icon: Drawable?) {
        val iconView = this.iconView ?: return

        iconView.setImageDrawable(icon)
        updateIconAndTextRelative()
    }

    fun getIcon(): Drawable? = this.iconView?.drawable

    @SuppressLint("SetTextI18n")
    fun setText(@StringRes text: Int?) {
        val textView = this.textView ?: return

        if (text != null && text != 0) {
            textView.setText(text)
        } else {
            textView.text = ""
        }
        updateIconAndTextRelative()
    }

    fun setText(text: String?) {
        val textView = this.textView ?: return

        textView.text = text ?: ""
        updateIconAndTextRelative()
    }

    fun setTextTypeface(typeface: Typeface) {
        val textView = this.textView ?: return

        textView.typeface = typeface
        updateIconAndTextRelative()
    }

    fun setTextColor(@ColorInt color: Int) {
        this.setTextColor(ColorStateList.valueOf(color))
    }

    fun setTextColor(colors: ColorStateList?) {
        colors ?: return
        val textView = this.textView ?: return

        textView.setTextColor(colors)
        updateIconAndTextRelative()
    }

    fun setButtonSize(width: Int, height: Int) {
        buttonView?.setViewSize(width, height)
        containerView?.setViewSize(width, height)
    }

    fun setButtonBackground(drawable: Drawable?) {
        buttonView?.background = drawable
    }

    fun setButtonPadding(padding: Int) {
        buttonView?.setPadding(/* left = */ padding, /* top = */ padding, /* right = */ padding, /* bottom = */ padding)
    }

    fun setButtonPadding(
        left: Int = buttonView?.paddingLeft ?: 0,
        top: Int = buttonView?.paddingTop ?: 0,
        right: Int = buttonView?.paddingRight ?: 0,
        bottom: Int = buttonView?.paddingBottom ?: 0
    ) {
        buttonView?.setPadding(/* left = */ left, /* top = */ top, /* right = */ right, /* bottom = */ bottom)
    }

    fun setIconSize(width: Int, height: Int) {
        iconView?.setViewSize(width, height)
    }

    fun setIconTintList(colors: ColorStateList?) {
        iconView?.imageTintList = colors
    }

    override fun setEnabled(enabled: Boolean) {
        ViewUtil.setViewEnabledWithChildView(containerView, enabled)
        super.setEnabled(enabled)
    }

    fun setCornerRadius(radius: Float) {
        containerView?.radius = radius
    }

    override fun setOnFocusChangeListener(l: OnFocusChangeListener?) {
        containerView?.onFocusChangeListener = l
    }

    private var onPressChangeListener: OnPressChangeListener? = null
    private var isDispatchPressed: Boolean = false

    fun setOnPressChangeListener(listener: OnPressChangeListener?) {
        this.onPressChangeListener = listener
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val onPressChangeListener = this.onPressChangeListener
        if (onPressChangeListener != null && this.isEnabled) {
            val previousDispatchPressed = isDispatchPressed
            var newDispatchPressed = previousDispatchPressed
            when (ev?.action) {
                MotionEvent.ACTION_DOWN -> newDispatchPressed = true
                MotionEvent.ACTION_MOVE -> {}
                MotionEvent.ACTION_UP -> newDispatchPressed = false
                MotionEvent.ACTION_CANCEL -> newDispatchPressed = false
            }

            if (newDispatchPressed != previousDispatchPressed) {
                isDispatchPressed = newDispatchPressed
                onPressChangeListener.onPressChange(this, newDispatchPressed)
            }
        }

        return super.dispatchTouchEvent(ev)
    }
}