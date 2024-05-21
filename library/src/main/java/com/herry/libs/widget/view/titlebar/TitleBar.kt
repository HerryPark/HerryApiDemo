package com.herry.libs.widget.view.titlebar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.View.OnClickListener
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import com.herry.libs.R
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.*

/**
 * Created by herry.park
 */
@Suppress("NAME_SHADOWING", "MemberVisibilityCanBePrivate", "unused")
class TitleBar : FrameLayout {
    private var mMenuActionView: TextView? = null
    private var mEndActionsContainer: View? = null
    private var mFirstActionViewContainer: ViewGroup? = null
    private var mFirstActionDefaultView: TextView? = null
    private var mFirstActionBadgeView: TextView? = null
    private var mSecondActionViewContainer: ViewGroup? = null
    private var mSecondActionDefaultView: TextView? = null
    private var mSecondActionBadgeView: TextView? = null
    private var mThirdActionViewContainer: ViewGroup? = null
    private var mThirdActionDefaultView: TextView? = null
    private var mThirdActionBadgeView: TextView? = null
    private var mFourthActionViewContainer: ViewGroup? = null
    private var mFourthActionDefaultView: TextView? = null
    private var mFourthActionBadgeView: TextView? = null

    //private View mMainTitleContainer = null;
    private var mMenuAnchorView: View? = null
    private var mTitleTextView: TextView? = null
    private var mTitleImageView: ImageView? = null
    private var mTitleShadowView: View? = null
    private var mRoot: View? = null
    private var mContainerView: View? = null
    private var mTitleGravity: Int = Gravity.CENTER
    private var mTitleResource = 0
    private var mSubtitleResource = 0
    private var mMenuBackgroundDrawable: Drawable? = null
    private var mMenuItemLayoutRes = 0
    private var mMenuWindowWidth = 0
    private var mMenuWindowAnimation = 0
    private var mHasFirstActionBadge = false
    private var mHasSecondActionBadge = false
    private var mHasThirdActionBadge = false
    private var mHasFourthActionBadge = false
    private val mTitleTextPadding: Rect = Rect()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    @SuppressLint("ClickableViewAccessibility")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val a = context.obtainStyledAttributes(
            null, intArrayOf(
                R.attr.titleTextStyle,
                R.attr.subtitleTextStyle
            ), R.attr.actionBarStyle, 0
        )

        try {
            mTitleResource = a.getResourceId(a.getIndex(0 /* titleTextStyle */), 0)
            mSubtitleResource = a.getResourceId(a.getIndex(1 /* subtitleTextStyle */), 0)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        a.recycle()

        if (null != attrs) {
            val layoutResId: Int
            val attr = context.obtainStyledAttributes(attrs, R.styleable.TitleBar)
            layoutResId = attr.getResourceId(R.styleable.TitleBar_customLayout, R.layout.hcs_titlebar)

            if (0 == childCount) {
                mRoot = LayoutInflater.from(context).inflate(layoutResId, this, true)
            }
            mRoot?.let { rootView ->
                mContainerView = rootView.findViewById(R.id.hcs_title_container)
                mFirstActionViewContainer = mContainerView?.findViewById(R.id.hcs_title_first_actionview)
                mFirstActionViewContainer?.let { firstActionViewContainer ->
                    mFirstActionDefaultView = TextView(context).apply {
                        this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        firstActionViewContainer.addView(this)
                    }

                    mFirstActionBadgeView = TextView(context).apply {
                        this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        firstActionViewContainer.addView(this)
                    }
                }

                mEndActionsContainer = mContainerView?.findViewById(R.id.hcs_title_end_actions_container)
                mEndActionsContainer?.let { endActionsContainer ->
                    mSecondActionViewContainer = endActionsContainer.findViewById(R.id.hcs_title_second_actionview)
                    mSecondActionViewContainer?.let { secondActionViewContainer ->
                        secondActionViewContainer.removeAllViews()
                        mSecondActionDefaultView = TextView(context).apply {
                            this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            secondActionViewContainer.addView(this)
                        }
                        mSecondActionBadgeView = TextView(context).apply {
                            this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            secondActionViewContainer.addView(this)
                        }
                    }

                    mThirdActionViewContainer = endActionsContainer.findViewById(R.id.hcs_title_third_actionview)
                    mThirdActionViewContainer?.let { thirdActionViewContainer ->
                        mThirdActionDefaultView = TextView(context).apply {
                            this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            thirdActionViewContainer.addView(this)
                        }
                        mThirdActionBadgeView = TextView(context).apply {
                            this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            thirdActionViewContainer.addView(this)
                        }
                    }

                    mFourthActionViewContainer = endActionsContainer.findViewById(R.id.hcs_title_fourth_actionview)
                    mFourthActionViewContainer?.let { fourthActionViewContainer ->
                        mFourthActionDefaultView = TextView(context).apply {
                            this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            fourthActionViewContainer.addView(this)
                        }
                        mFourthActionBadgeView = TextView(context).apply {
                            this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            fourthActionViewContainer.addView(this)
                        }
                    }
                    mMenuActionView = endActionsContainer.findViewById(R.id.hcs_title_menu_actionview)
                }

                mMenuAnchorView = mContainerView?.findViewById(R.id.hcs_title_menu_anchor_view)
                mTitleTextView = mContainerView?.findViewById(R.id.hcs_title_textview)
                mTitleImageView = mContainerView?.findViewById(R.id.hcs_title_imageview)
                mTitleShadowView = rootView.findViewById(R.id.hcs_title_shadow)
            }

            // set title-bar background
            mContainerView?.let { containerView ->
                var set = false
                while (!set) {
                    val d = attr.getDrawable(R.styleable.TitleBar_titleBarBackground)
                    if (d != null) {
                        containerView.background = d
                        set = true
                        continue
                    }
                    val baseAttr = context.obtainStyledAttributes(attrs, intArrayOf(R.attr.background))
                    try {
                        val drawable = baseAttr.getDrawable(0)
                        if (null != drawable) {
                            containerView.background = drawable
                            set = true
                            continue
                        }
                        val drawableResource = baseAttr.getResourceId(0, 0)
                        if (0 < drawableResource) {
                            containerView.setBackgroundResource(drawableResource)
                            set = true
                            continue
                        }
                        val backgroundColor = baseAttr.getColor(0, -0x1000000)
                        containerView.setBackgroundColor(backgroundColor)
                    } finally {
                        baseAttr.recycle()
                    }
                    set = true
                }
            }

            // sets badge
            // sets action badge
            mHasFirstActionBadge = attr.getBoolean(R.styleable.TitleBar_hasFirstActionBadge, false)
            mHasSecondActionBadge = attr.getBoolean(R.styleable.TitleBar_hasSecondActionBadge, false)
            mHasThirdActionBadge = attr.getBoolean(R.styleable.TitleBar_hasThirdActionBadge, false)
            mHasFourthActionBadge = attr.getBoolean(R.styleable.TitleBar_hasFourthActionBadge, false)

            // badge background
            val bg = attr.getDrawable(R.styleable.TitleBar_actionBadgeBackground)
            val width = attr.getDimensionPixelSize(R.styleable.TitleBar_actionBadgeWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            val height = attr.getDimensionPixelSize(R.styleable.TitleBar_actionBadgeHeight, ViewGroup.LayoutParams.WRAP_CONTENT)
            val marginTop = attr.getDimensionPixelSize(R.styleable.TitleBar_actionBadgeMarginTop, 0)
            val marginBottom = attr.getDimensionPixelSize(R.styleable.TitleBar_actionBadgeMarginBottom, 0)
            val marginEnd = attr.getDimensionPixelSize(R.styleable.TitleBar_actionBadgeMarginEnd, 0)
            val marginStart = attr.getDimensionPixelSize(R.styleable.TitleBar_actionBadgeMarginStart, 0)
            val paddingTop = attr.getDimensionPixelSize(R.styleable.TitleBar_actionBadgePaddingTop, 0)
            val paddingBottom = attr.getDimensionPixelSize(R.styleable.TitleBar_actionBadgePaddingBottom, 0)
            val paddingEnd = attr.getDimensionPixelSize(R.styleable.TitleBar_actionBadgePaddingEnd, 0)
            val paddingStart = attr.getDimensionPixelSize(R.styleable.TitleBar_actionBadgePaddingStart, 0)

            // text color
            val textColor = attr.getColorStateList(R.styleable.TitleBar_actionBadgeTextColor)
            var textSize = attr.getDimensionPixelSize(R.styleable.TitleBar_actionBadgeTextSize, -1)
            if (-1 == textSize) {
                textSize = ViewUtil.convertDpToPx(10f).toInt()
            }

            // text style
            val textStyle: Typeface = when (attr.getInt(R.styleable.TitleBar_actionBadgeTextStyle, -1)) {
                1 -> Typeface.DEFAULT_BOLD
                0 -> Typeface.DEFAULT
                else -> Typeface.DEFAULT
            } //Typeface.DEFAULT;

            // set text gravity
            val textGravity = getGravityValue(0x30)

            // set layout gravity
            val layoutGravity = getGravityValue(attr.getInt(R.styleable.TitleBar_actionBadgeLayoutGravity, 0x09))
            mFirstActionBadgeView?.let { firstActionBadgeView ->
                firstActionBadgeView.background = bg
                firstActionBadgeView.setViewSize(width, height)
                firstActionBadgeView.setViewMargin(marginStart, marginTop, marginEnd, marginBottom)
                firstActionBadgeView.setViewPadding(paddingStart, paddingTop, paddingEnd, paddingBottom)
                firstActionBadgeView.setTextColor(textColor ?: ViewUtil.getColorStateList(context, R.color.hcs_titlebar_title_text))
                firstActionBadgeView.typeface = textStyle
                if (0 <= textSize) {
                    firstActionBadgeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
                }
                if (0 < textGravity) {
                    firstActionBadgeView.gravity = textGravity
                }
                if (0 < layoutGravity) {
                    firstActionBadgeView.setLayoutGravity(layoutGravity)
                }
                firstActionBadgeView.visibility = if (mHasFirstActionBadge) if (!TextUtils.isEmpty(firstActionBadgeView.text.toString())) VISIBLE else GONE else GONE
            }

            mSecondActionBadgeView?.let { secondActionBadgeView ->
                secondActionBadgeView.visibility = if (mHasFirstActionBadge) VISIBLE else GONE
                secondActionBadgeView.background = bg
                secondActionBadgeView.setViewSize(width, height)
                secondActionBadgeView.setViewMargin(marginStart, marginTop, marginEnd, marginBottom)
                secondActionBadgeView.setViewPadding(paddingStart, paddingTop, paddingEnd, paddingBottom)
                secondActionBadgeView.setTextColor(textColor ?: ViewUtil.getColorStateList(context, R.color.hcs_titlebar_title_text))
                secondActionBadgeView.typeface = textStyle
                if (0 <= textSize) {
                    secondActionBadgeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
                }
                if (0 < textGravity) {
                    secondActionBadgeView.gravity = textGravity
                }
                if (0 < layoutGravity) {
                    secondActionBadgeView.setLayoutGravity(layoutGravity)
                }
                secondActionBadgeView.visibility = if (mHasSecondActionBadge) if (!TextUtils.isEmpty(secondActionBadgeView.text.toString())) VISIBLE else GONE else GONE
            }

            mThirdActionBadgeView?.let { thirdActionBadgeView ->
                thirdActionBadgeView.background = bg
                thirdActionBadgeView.setViewSize(width, height)
                thirdActionBadgeView.setViewMargin(marginStart, marginTop, marginEnd, marginBottom)
                thirdActionBadgeView.setViewPadding(paddingStart, paddingTop, paddingEnd, paddingBottom)
                thirdActionBadgeView.setTextColor(textColor ?: ViewUtil.getColorStateList(context, R.color.hcs_titlebar_title_text))
                thirdActionBadgeView.typeface = textStyle
                if (0 <= textSize) {
                    thirdActionBadgeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
                }
                if (0 < textGravity) {
                    thirdActionBadgeView.gravity = textGravity
                }
                if (0 < layoutGravity) {
                    thirdActionBadgeView.setLayoutGravity(layoutGravity)
                }
                thirdActionBadgeView.visibility = if (mHasThirdActionBadge) if (!TextUtils.isEmpty(thirdActionBadgeView.text.toString())) VISIBLE else GONE else GONE
            }

            mFourthActionBadgeView?.let { fourthActionBadgeView ->
                fourthActionBadgeView.background = bg
                fourthActionBadgeView.setViewSize(width, height)
                fourthActionBadgeView.setViewMargin(marginStart, marginTop, marginEnd, marginBottom)
                fourthActionBadgeView.setViewPadding(paddingStart, paddingTop, paddingEnd, paddingBottom)
                fourthActionBadgeView.setTextColor(textColor ?: ViewUtil.getColorStateList(context, R.color.hcs_titlebar_title_text))
                fourthActionBadgeView.typeface = textStyle
                if (0 <= textSize) {
                    fourthActionBadgeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
                }
                if (0 < textGravity) {
                    fourthActionBadgeView.gravity = textGravity
                }
                if (0 < layoutGravity) {
                    fourthActionBadgeView.setLayoutGravity(layoutGravity)
                }
                fourthActionBadgeView.visibility = if (mHasFourthActionBadge) if (!TextUtils.isEmpty(fourthActionBadgeView.text.toString())) VISIBLE else GONE else GONE
            }

            // sets sub action view min width
            val subActionViewMinWidth = kotlin.math.round(attr.getDimension(R.styleable.TitleBar_subActionViewMinWidth, 0f))

            // set first image view with attributes
            mFirstActionDefaultView?.let { firstActionDefaultView ->
                // set image
                val d = attr.getDrawable(R.styleable.TitleBar_firstActionDrawableSrc)
                if (d != null) {
                    mFirstImageDrawable = d
                    firstActionDefaultView.background = d
                }
                setFirstActionView(firstActionDefaultView)
            }

            // set second image view with attributes
            mSecondActionDefaultView?.let { secondActionDefaultView ->
                // set image
                val d = attr.getDrawable(R.styleable.TitleBar_secondActionDrawableSrc)
                if (d != null) {
                    mSecondImageDrawable = d
                    secondActionDefaultView.background = d
                }
                val text = attr.getText(R.styleable.TitleBar_secondActionText)
                if (null != text) {
                    // title color
                    secondActionDefaultView.text = text
                }
                val textColor = attr.getColorStateList(R.styleable.TitleBar_secondActionViewTextColor)
                if (textColor != null) {
                    secondActionDefaultView.setTextColor(textColor)
                } else {
                    val typedValue = TypedValue()
                    val a: TypedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.colorAccent))
                    val color = a.getColor(0, 0)
                    a.recycle()
                    secondActionDefaultView.setTextColor(color)
                }

                // title size
                var titleSize = attr.getDimensionPixelSize(R.styleable.TitleBar_subActionTextSize, -1)
                if (0 > titleSize) {
                    titleSize = ViewUtil.convertDpToPx(18f).toInt()
                }
                secondActionDefaultView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize.toFloat())

                // title style
                val textStyle: Typeface = when (attr.getInt(R.styleable.TitleBar_subActionViewTextStyle, -1)) {
                    1 -> Typeface.DEFAULT_BOLD
                    0 -> Typeface.DEFAULT
                    else -> Typeface.DEFAULT
                } // = Typeface.DEFAULT;
                secondActionDefaultView.typeface = textStyle
                val paddingEnd = kotlin.math.round(attr.getDimension(R.styleable.TitleBar_subActionViewPaddingEnd, 0f))
                secondActionDefaultView.setPadding(0, 0, paddingEnd.toInt(), 0)
                secondActionDefaultView.minWidth = subActionViewMinWidth.toInt()
                setSecondActionView(secondActionDefaultView)
            }

            // set third image view with attributes
            mThirdActionDefaultView?.let { thirdActionDefaultView ->
                // set image
                val d = attr.getDrawable(R.styleable.TitleBar_thirdActionDrawableSrc)
                if (d != null) {
                    mThirdImageDrawable = d
                    thirdActionDefaultView.background = d
                }
                val text = attr.getText(R.styleable.TitleBar_thirdActionText)
                if (null != text) {
                    // title color
                    thirdActionDefaultView.text = text
                }
                val textColor = attr.getColorStateList(R.styleable.TitleBar_thirdActionViewTextColor)
                if (textColor != null) {
                    thirdActionDefaultView.setTextColor(textColor)
                } else {
                    val typedValue = TypedValue()
                    val a: TypedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.colorAccent))
                    val color = a.getColor(0, 0)
                    a.recycle()
                    thirdActionDefaultView.setTextColor(color)
                }

                // title size
                var titleSize = attr.getDimensionPixelSize(R.styleable.TitleBar_subActionTextSize, -1)
                if (-1 == titleSize) {
                    titleSize = ViewUtil.convertDpToPx(18f).toInt()
                }
                if (0 <= titleSize) {
                    thirdActionDefaultView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize.toFloat())
                }

                // title style
                val textStyle: Typeface = when (attr.getInt(R.styleable.TitleBar_subActionViewTextStyle, -1)) {
                    1 -> Typeface.DEFAULT_BOLD
                    0 -> Typeface.DEFAULT
                    else -> Typeface.DEFAULT
                } // = Typeface.DEFAULT;
                thirdActionDefaultView.typeface = textStyle
                val paddingEnd = kotlin.math.round(attr.getDimension(R.styleable.TitleBar_subActionViewPaddingEnd, 0f))
                thirdActionDefaultView.setPadding(0, 0, paddingEnd.toInt(), 0)
                thirdActionDefaultView.minWidth = subActionViewMinWidth.toInt()
                setThirdActionView(thirdActionDefaultView)
            }

            // set fourth image view with attributes
            mFourthActionDefaultView?.let { fourthActionDefaultView ->
                // set image
                val d = attr.getDrawable(R.styleable.TitleBar_fourthActionDrawableSrc)
                if (d != null) {
                    mFourthImageDrawable = d
                    fourthActionDefaultView.background = d
                }
                val text = attr.getText(R.styleable.TitleBar_fourthActionText)
                if (null != text) {
                    // title color
                    fourthActionDefaultView.text = text
                }
                val textColor = attr.getColorStateList(R.styleable.TitleBar_fourthActionViewTextColor)
                if (textColor != null) {
                    fourthActionDefaultView.setTextColor(textColor)
                } else {
                    val typedValue = TypedValue()
                    val a: TypedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.colorAccent))
                    val color = a.getColor(0, 0)
                    a.recycle()
                    fourthActionDefaultView.setTextColor(color)
                }

                // title size
                var titleSize = attr.getDimensionPixelSize(R.styleable.TitleBar_subActionTextSize, -1)
                if (-1 == titleSize) {
                    titleSize = ViewUtil.convertDpToPx(18f).toInt()
                }
                if (0 <= titleSize) {
                    fourthActionDefaultView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize.toFloat())
                }

                // title style
                val textStyle: Typeface = when (attr.getInt(R.styleable.TitleBar_subActionViewTextStyle, -1)) {
                    1 -> Typeface.DEFAULT_BOLD
                    0 -> Typeface.DEFAULT
                    else -> Typeface.DEFAULT
                } // = Typeface.DEFAULT;
                fourthActionDefaultView.typeface = textStyle
                val paddingEnd = kotlin.math.round(attr.getDimension(R.styleable.TitleBar_subActionViewPaddingEnd, 0f))
                fourthActionDefaultView.setPadding(0, 0, paddingEnd.toInt(), 0)
                fourthActionDefaultView.minWidth = subActionViewMinWidth.toInt()
                setFourthActionView(fourthActionDefaultView)
            }

            // set menu image view with attributes
            mMenuActionView?.let { menuActionView ->
                // set image
                val d = attr.getDrawable(R.styleable.TitleBar_menuActionDrawableSrc)
                if (d != null) {
                    menuActionView.background = d
                }
                val text = attr.getText(R.styleable.TitleBar_menuActionText)
                if (null != text) {
                    // title color
                    menuActionView.text = text
                }
                val textColor = attr.getColorStateList(R.styleable.TitleBar_menuActionViewTextColor)
                menuActionView.setTextColor(textColor ?: ViewUtil.getColorStateList(context, R.color.hcs_titlebar_action_menu_text))

                // title size
                var titleSize = attr.getDimensionPixelSize(R.styleable.TitleBar_menuActionTextSize, -1)
                if (-1 == titleSize) {
                    titleSize = ViewUtil.convertDpToPx(14f).toInt()
                }
                if (0 <= titleSize) {
                    menuActionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize.toFloat())
                }

                // title style
                val textStyle: Typeface = when (attr.getInt(R.styleable.TitleBar_menuActionViewTextStyle, -1)) {
                    1 -> Typeface.DEFAULT_BOLD
                    0 -> Typeface.DEFAULT
                    else -> Typeface.DEFAULT
                } // = Typeface.DEFAULT;
                menuActionView.typeface = textStyle
                val paddingEnd = kotlin.math.round(attr.getDimension(R.styleable.TitleBar_menuActionViewPaddingEnd, 0f))
                menuActionView.setPadding(0, 0, paddingEnd.toInt(), 0)
            }

            mMenuAnchorView?.let { mMenuAnchorView ->
                val marginTop = kotlin.math.round(attr.getDimension(R.styleable.TitleBar_menuActionViewMarginTop, 0f))
                val marginEnd = kotlin.math.round(attr.getDimension(R.styleable.TitleBar_menuActionViewMarginEnd, 0f))
                mMenuAnchorView.setViewMargin(0, (marginTop - mMenuAnchorView.height).toInt(), marginEnd.toInt(), 0)
            }

            // set menu window
            mMenuBackgroundDrawable = attr.getDrawable(R.styleable.TitleBar_menuWindowBackground)
            if (null == mMenuBackgroundDrawable) {
                mMenuBackgroundDrawable = ColorDrawable(Color.WHITE)
            }
            mMenuItemLayoutRes = attr.getResourceId(R.styleable.TitleBar_menuWindowItemLayout, R.layout.hcs_titlebar_sub_menu_list_item)
            var menuWindowWidth = attr.getLayoutDimension(R.styleable.TitleBar_menuWindowWidth, 0)
            if (0 == menuWindowWidth) {
                menuWindowWidth = ViewUtil.convertDpToPx(143f).toInt()
            }
            if (0 > menuWindowWidth) {
                if (ViewGroup.LayoutParams.MATCH_PARENT != menuWindowWidth
                    && ViewGroup.LayoutParams.WRAP_CONTENT != menuWindowWidth
                ) {
                    mMenuWindowWidth = ViewGroup.LayoutParams.WRAP_CONTENT
                }
            } else {
                mMenuWindowWidth = kotlin.math.round(menuWindowWidth.toFloat()).toInt()
            }
            val menuWindowAnimation = attr.getResourceId(R.styleable.TitleBar_menuWindowAnimation, 0)
            if (0 < menuWindowAnimation) {
                mMenuWindowAnimation = menuWindowAnimation
            }

            // set title image view with attributes
            if (null != mTitleImageView) {
                // set image
                val d = attr.getDrawable(R.styleable.TitleBar_mainTitleDrawableSrc)

                // set image size
                var titleImageWidth = attr.getDimensionPixelSize(R.styleable.TitleBar_mainTitleDrawableSrcWidth, 0)
                if (0 == titleImageWidth) {
                    titleImageWidth = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                if (0 > titleImageWidth) {
                    if (ViewGroup.LayoutParams.MATCH_PARENT != titleImageWidth
                        && ViewGroup.LayoutParams.WRAP_CONTENT != titleImageWidth
                    ) {
                        titleImageWidth = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                }
                var titleImageHeight = attr.getDimensionPixelSize(R.styleable.TitleBar_mainTitleDrawableSrcHeight, 0)
                if (0 == menuWindowWidth) {
                    titleImageHeight = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                if (0 > titleImageHeight) {
                    if (ViewGroup.LayoutParams.MATCH_PARENT != titleImageHeight
                        && ViewGroup.LayoutParams.WRAP_CONTENT != titleImageHeight
                    ) {
                        titleImageHeight = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                }
                setTitleImage(d, titleImageWidth, titleImageHeight)
            }

            // set title text view with attributes
            mTitleTextView?.let { titleTextView ->
                titleTextView.setSingleLine()
                val titleTruncateAt = attr.getInt(R.styleable.TitleBar_mainTitleEllipsize, 0)
                val truncateAt = getTruncateAt(titleTruncateAt)
                if (null != truncateAt) {
                    titleTextView.ellipsize = truncateAt
                }
                if (0 != mTitleResource) {
                    setTextStyle(titleTextView, mTitleResource)
                }

                // title
                val text = attr.getText(R.styleable.TitleBar_mainTitleText)
                if (!TextUtils.isEmpty(text)) {
                    titleTextView.text = text
                }

                // title color
                val textColor = attr.getColorStateList(R.styleable.TitleBar_mainTitleTextColor)
                titleTextView.setTextColor(textColor ?: ViewUtil.getColorStateList(context, R.color.hcs_titlebar_title_text))

                // title style
                val textStyle: Typeface = when (attr.getInt(R.styleable.TitleBar_mainTitleTextStyle, -1)) {
                    1 -> Typeface.DEFAULT_BOLD
                    0 -> Typeface.DEFAULT
                    else -> Typeface.DEFAULT
                } //Typeface.DEFAULT;
                titleTextView.typeface = textStyle

                // title size
                var titleSize = attr.getDimensionPixelSize(R.styleable.TitleBar_mainTitleTextSize, -1)
                if (-1 == titleSize) {
                    titleSize = ViewUtil.convertDpToPx(20f).toInt()
                }
                if (0 <= titleSize) {
                    titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize.toFloat())
                }

                // set title gravity
                val titleTextGravity = attr.getInt(R.styleable.TitleBar_mainTitleTextGravity, 0x30)
                mTitleGravity = getGravityValue(titleTextGravity)
                if (0 < mTitleGravity) {
                    titleTextView.gravity = mTitleGravity
                }
                mTitleTextPadding.left = kotlin.math.round(attr.getDimension(R.styleable.TitleBar_mainTitleTextPaddingStart, 0f)).toInt()
                mTitleTextPadding.right = kotlin.math.round(attr.getDimension(R.styleable.TitleBar_mainTitleTextPaddingEnd, 0f)).toInt()
                mTitleTextPadding.top = kotlin.math.round(attr.getDimension(R.styleable.TitleBar_mainTitleTextPaddingTop, 0f)).toInt()
                mTitleTextPadding.bottom = kotlin.math.round(attr.getDimension(R.styleable.TitleBar_mainTitleTextPaddingBottom, 0f)).toInt()
            }

            // set shadow
            mTitleShadowView?.let { titleShadowView ->
                val showShadow = attr.getInt(R.styleable.TitleBar_showShadow, 1)
                titleShadowView.visibility = if (1 == showShadow) VISIBLE else GONE

                // set shadow height
                var shadowHeight = attr.getDimensionPixelSize(R.styleable.TitleBar_shadowHeight, -1)
                if (-1 == shadowHeight) {
                    shadowHeight = ViewUtil.convertDpToPx(1f).toInt()
                }
                if (0 <= shadowHeight) {
                    val layoutParams = titleShadowView.layoutParams
                    if (null != layoutParams) {
                        layoutParams.height = shadowHeight
                        titleShadowView.layoutParams = layoutParams
                    }
                }
                var d = attr.getDrawable(R.styleable.TitleBar_shadowBackground)
                if (null == d) {
                    d = ViewUtil.getColorDrawable(context, R.color.hcs_titlebar_shadow_line)
                }
                titleShadowView.background = d
            }
            attr.recycle() // ensure this is always called
        }
    }

    fun setShadowViewVisible(visible: Boolean) {
        mTitleShadowView?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (-1 != mFirstImageResID) {
            setFirstActionViewImage(mFirstImageResID)
        }
        if (-1 != mSecondImageResID) {
            setSecondActionViewImage(mSecondImageResID)
        } else if (null != mSecondImageDrawable) {
            setSecondActionViewDrawable(mSecondImageDrawable)
        }
        if (-1 != mThirdImageResID) {
            setThirdActionViewImage(mThirdImageResID)
        }
        if (-1 != mFourthImageResID) {
            setFourthActionViewImage(mFourthImageResID)
        }
    }

    private fun setTextStyle(view: TextView, style: Int) {
        if (style > 0) {
            TextViewCompat.setTextAppearance(view, style)
        }
    }

    //    @Override
    //    public void setBackground(Drawable background) {
    //        if (null != mContainerView) {
    //            mContainerView.setBackground(background);
    //        }
    //    }
    override fun setBackgroundColor(color: Int) {
        mContainerView?.setBackgroundColor(color)
    }

    fun setTitleImage(drawableResID: Int) {
        if (0 < drawableResID) {
            setTitleImage(ViewUtil.getDrawable(context, drawableResID))
        }
    }

    fun setTitleImage(@DrawableRes drawableResID: Int, width: Int, height: Int) {
        if (0 != drawableResID) {
            setTitleImage(ViewUtil.getDrawable(context, drawableResID), width, height)
        }
    }

    fun setTitleImage(drawable: Drawable?) {
        // set title image view with attributes
        mTitleImageView?.setImageDrawable(drawable)
    }

    fun setTitleImage(drawable: Drawable?, width: Int, height: Int) {
        // set title image view with attributes
        mTitleImageView?.let { mTitleImageView ->
            mTitleImageView.setImageDrawable(drawable)
            var layoutParams = mTitleImageView.layoutParams as? RelativeLayout.LayoutParams
            if (null != layoutParams) {
                layoutParams.width = width
                layoutParams.height = height
            } else {
                layoutParams = RelativeLayout.LayoutParams(width, height)
            }
            mTitleImageView.layoutParams = layoutParams
        }
    }

    fun setSubActionViewPaddingEnd(paddingEnd: Int) {
        mSecondActionDefaultView?.setViewPaddingEnd(paddingEnd)
        mThirdActionDefaultView?.setViewPaddingEnd(paddingEnd)
        mFourthActionDefaultView?.setViewPaddingEnd(paddingEnd)
    }

    /**
     * Sets the title string value.
     */
    fun setTitle(title: String?) {
        this.mTitleTextView?.let { textView ->
            textView.text = title
            updateTitleLocation()
        }
    }

    fun setTitleAlpha(alpha: Float) {
        this.mTitleTextView?.alpha = alpha
    }

    /**
     * Sets the title string value.
     */
    fun setTitle(title: CharSequence?) {
        this.mTitleTextView?.let { textView ->
            textView.text = title
            updateTitleLocation()
        }
    }

    /**
     * Sets the title string value.
     */
    fun setTitle(@StringRes resID: Int) {
        this.mTitleTextView?.let { textView ->
            textView.setText(resID)
            updateTitleLocation()
        }
    }

    /**
     * Sets the title text color for all the states (normal, selected,
     * focused) to be this color.
     */
    fun setTitleColor(@ColorInt color: Int) {
        this.mTitleTextView?.setTextColor(color)
    }

    fun setTitleGravity(gravity: Int) {
        this.mTitleTextView?.let { textView ->
            mTitleGravity = gravity
            textView.gravity = gravity
            updateTitleLocation()
        }
    }

    fun setTitlePadding(start: Int, top: Int, end: Int, bottom: Int) {
        this.mTitleTextView?.let {
            mTitleTextPadding.set(start, top, end, bottom)
            updateTitleLocation()
        }
    }

    private var mFirstImageResID: Int = -1
    private var mFirstImageDrawable: Drawable? = null
    private var mSecondImageResID: Int = -1
    private var mSecondImageDrawable: Drawable? = null
    private var mThirdImageResID: Int = -1
    private var mThirdImageDrawable: Drawable? = null
    private var mFourthImageResID: Int = -1
    private var mFourthImageDrawable: Drawable? = null

    /* --------------------------------------------------------------------------------
    Sets first action view
    -------------------------------------------------------------------------------- */
    fun getFirstActionView(): View? {
        val firstActionViewContainer = this.mFirstActionViewContainer
        if (null != firstActionViewContainer) {
            if (0 < firstActionViewContainer.childCount) {
                return firstActionViewContainer.getChildAt(0)
            }
        }

        return null
    }

    fun setFirstActionViewEnabled(enabled: Boolean) {
        ViewUtil.setViewGroupEnabled(mFirstActionViewContainer, enabled)
    }

    /**
     * Set the visibility state of this view.
     *
     * @param visibility One of [.VISIBLE], [.INVISIBLE], or [.GONE].
     */
    fun setFirstActionViewVisibility(visibility: Int) {
        mFirstActionViewContainer?.visibility = visibility
    }

    fun setFirstActionViewVisible(isVisible: Boolean) {
        mFirstActionViewContainer?.isVisible = isVisible
    }

    private fun setFirstActionView(view: View?) {
        if (null == view) {
            return
        }
        mFirstActionViewContainer?.let { firstActionViewContainer ->
            if (0 < firstActionViewContainer.childCount) {
                val childView = firstActionViewContainer.getChildAt(0)
                if (childView === view) {
                    return
                }
                firstActionViewContainer.removeAllViews()
            }
            firstActionViewContainer.addView(view)
            if (null != mFirstActionBadgeView && mHasFirstActionBadge) {
                firstActionViewContainer.addView(mFirstActionBadgeView)
            }
        }
    }

    fun setFirstActionViewOnClickListener(listener: OnClickListener?) {
        mFirstActionViewContainer?.setOnClickListener(listener)
    }

    fun setFirstActionViewText(resId: Int) {
        mFirstActionDefaultView?.let { firstActionDefaultView ->
            mFirstImageResID = 0
            mFirstImageDrawable = null
            firstActionDefaultView.background = null
            firstActionDefaultView.setText(resId)
        }
        setFirstActionView(mFirstActionDefaultView)
    }

    fun setFirstActionViewText(resId: Int, listener: OnClickListener?) {
        setFirstActionViewText(resId)
        setFirstActionViewOnClickListener(listener)
    }

    fun setFirstActionViewText(text: String?) {
        mFirstActionDefaultView?.let { firstActionDefaultView ->
            mFirstImageResID = 0
            mFirstImageDrawable = null
            firstActionDefaultView.background = null
            firstActionDefaultView.text = text
        }
        setFirstActionView(mFirstActionDefaultView)
    }

    fun setFirstActionViewText(text: String?, listener: OnClickListener?) {
        setFirstActionViewText(text)
        setFirstActionViewOnClickListener(listener)
    }

    fun setFirstActionViewImage(resId: Int) {
        if (0 >= resId) {
            return
        }
        mFirstActionDefaultView?.let { firstActionDefaultView ->
            mFirstImageResID = resId
            mFirstImageDrawable = null
            firstActionDefaultView.setBackgroundResource(resId)
            firstActionDefaultView.text = ""
        }
        setFirstActionView(mFirstActionDefaultView)
    }

    fun getFirstActionViewImageResource(): Int = mFirstImageResID

    fun setFirstActionViewImage(resId: Int, listener: OnClickListener?) {
        setFirstActionViewImage(resId)
        setFirstActionViewOnClickListener(listener)
    }

    fun setFirstActionViewDrawable(drawable: Drawable?) {
        mFirstActionDefaultView?.let { firstActionDefaultView ->
            mFirstImageResID = 0
            mFirstImageDrawable = drawable
            firstActionDefaultView.background = drawable
            firstActionDefaultView.text = ""
        }
        setFirstActionView(mFirstActionDefaultView)
    }

    fun setFirstActionCustomView(customView: View?) {
        mFirstImageResID = 0
        mFirstImageDrawable = null

        setFirstActionView(customView)
    }

    fun setFirstActionCustomView(customView: View?, listener: OnClickListener?) {
        setFirstActionCustomView(customView)
        setFirstActionViewOnClickListener(listener)
    }

    fun setFirstActionBadge(badge: String?) {
        mFirstActionBadgeView?.let { firstActionBadgeView ->
            if (mHasFirstActionBadge && !TextUtils.isEmpty(badge)) {
                firstActionBadgeView.text = badge
                firstActionBadgeView.visibility = View.VISIBLE
            } else {
                firstActionBadgeView.visibility = View.GONE
            }
        }
    }

    fun setHasFirstActionBadge(has: Boolean) {
        mHasFirstActionBadge = has
    }

    /* --------------------------------------------------------------------------------
        Sets second action view
        -------------------------------------------------------------------------------- */
    fun getSecondActionView(): View? {
        val secondActionViewContainer = this.mSecondActionViewContainer
        if (null != secondActionViewContainer) {
            if (0 < secondActionViewContainer.childCount) {
                return secondActionViewContainer.getChildAt(0)
            }
        }
        return null
    }

    fun setSecondActionViewEnabled(enabled: Boolean) {
        ViewUtil.setViewGroupEnabled(mSecondActionViewContainer, enabled)
    }

    /**
     * Set the visibility state of this view.
     *
     * @param visibility One of [.VISIBLE], [.INVISIBLE], or [.GONE].
     */
    fun setSecondActionViewVisibility(visibility: Int) {
        mSecondActionViewContainer?.visibility = visibility
    }

    private fun setSecondActionView(view: View?) {
        if (null == view) {
            return
        }
        mSecondActionViewContainer?.let { secondActionView ->
            if (0 < secondActionView.childCount) {
                val childView = secondActionView.getChildAt(0)
                if (childView === view) {
                    return
                }
                secondActionView.removeAllViews()
            }
            secondActionView.addView(view)
            if (null != mSecondActionBadgeView && mHasSecondActionBadge) {
                secondActionView.addView(mSecondActionBadgeView)
            }
        }
    }

    fun setSecondActionViewOnClickListener(listener: OnClickListener?) {
        mSecondActionViewContainer?.setOnClickListener(listener)
    }

    fun setSecondActionViewText(resId: Int) {
        mSecondActionDefaultView?.let { secondActionDefaultView ->
            mSecondImageResID = 0
            mSecondImageDrawable = null
            secondActionDefaultView.background = null
            secondActionDefaultView.setText(resId)
        }
        setSecondActionView(mSecondActionDefaultView)
    }

    fun setSecondActionViewText(resId: Int, listener: OnClickListener?) {
        setSecondActionViewText(resId)
        setSecondActionViewOnClickListener(listener)
    }

    fun setSecondActionViewText(text: String?) {
        mSecondActionDefaultView?.let { secondActionDefaultView ->
            mSecondImageResID = 0
            mSecondImageDrawable = null
            secondActionDefaultView.background = null
            secondActionDefaultView.text = text
        }
        setSecondActionView(mSecondActionDefaultView)
    }

    fun setSecondActionViewText(text: String?, listener: OnClickListener?) {
        setSecondActionViewText(text)
        setSecondActionViewOnClickListener(listener)
    }

    fun setSecondActionViewImage(resId: Int) {
        if (0 >= resId) {
            return
        }
        mSecondActionDefaultView?.let { secondActionDefaultView ->
            mSecondImageResID = resId
            mSecondImageDrawable = null
            secondActionDefaultView.setBackgroundResource(resId)
            secondActionDefaultView.text = ""
        }
        setSecondActionView(mSecondActionDefaultView)
    }

    fun getSecondActionViewImageResource(): Int = mSecondImageResID

    fun setSecondActionViewImage(resId: Int, listener: OnClickListener?) {
        setSecondActionViewImage(resId)
        setSecondActionViewOnClickListener(listener)
    }

    fun setSecondActionViewDrawable(drawable: Drawable?) {
        mSecondActionDefaultView?.let { secondActionDefaultView ->
            mSecondImageResID = 0
            mSecondImageDrawable = drawable
            secondActionDefaultView.background = drawable
            secondActionDefaultView.text = ""
        }
        setSecondActionView(mSecondActionDefaultView)
    }

    fun setSecondActionCustomView(customView: View?) {
        mSecondImageResID = 0
        mSecondImageDrawable = null

        setSecondActionView(customView)
    }

    fun setSecondActionCustomView(customView: View?, listener: OnClickListener?) {
        setSecondActionCustomView(customView)
        setSecondActionViewOnClickListener(listener)
    }

    fun setSecondActionBadge(badge: String?) {
        mSecondActionBadgeView?.let { secondActionBadgeView ->
            if (mHasSecondActionBadge && !TextUtils.isEmpty(badge)) {
                secondActionBadgeView.text = badge
                secondActionBadgeView.visibility = View.VISIBLE
            } else {
                secondActionBadgeView.visibility = View.GONE
            }
        }
    }

    fun setHasSecondActionBadge(has: Boolean) {
        mHasSecondActionBadge = has
    }

    /* --------------------------------------------------------------------------------
        Sets third action view
        -------------------------------------------------------------------------------- */
    fun getThirdActionView(): View? {
        val thirdActionViewContainer = this.mThirdActionViewContainer
        if (null != thirdActionViewContainer) {
            if (0 < thirdActionViewContainer.childCount) {
                return thirdActionViewContainer.getChildAt(0)
            }
        }
        return null
    }

    fun setThirdActionViewEnabled(enabled: Boolean) {
        ViewUtil.setViewGroupEnabled(mThirdActionViewContainer, enabled)
    }

    /**
     * Set the visibility state of this view.
     *
     * @param visibility One of [.VISIBLE], [.INVISIBLE], or [.GONE].
     */
    fun setThirdActionViewVisibility(visibility: Int) {
        mThirdActionViewContainer?.visibility = visibility
    }

    private fun setThirdActionView(view: View?) {
        if (null == view) {
            return
        }
        mThirdActionViewContainer?.let { thirdActionView ->
            if (0 < thirdActionView.childCount) {
                val childView = thirdActionView.getChildAt(0)
                if (childView === view) {
                    return
                }
                thirdActionView.removeAllViews()
            }
            thirdActionView.addView(view)
            if (null != mThirdActionBadgeView && mHasThirdActionBadge) {
                thirdActionView.addView(mThirdActionBadgeView)
            }
        }
    }

    fun setThirdActionViewOnClickListener(listener: OnClickListener?) {
        mThirdActionViewContainer?.setOnClickListener(listener)
    }

    fun setThirdActionViewText(resId: Int) {
        mThirdActionDefaultView?.let { thirdActionDefaultView ->
            mThirdImageResID = 0
            mThirdImageDrawable = null
            thirdActionDefaultView.background = null
            thirdActionDefaultView.setText(resId)
        }
        setThirdActionView(mThirdActionDefaultView)
    }

    fun setThirdActionViewText(resId: Int, listener: OnClickListener?) {
        setThirdActionViewText(resId)
        setThirdActionViewOnClickListener(listener)
    }

    fun setThirdActionViewText(text: String?) {
        mThirdActionDefaultView?.let { thirdActionDefaultView ->
            mThirdImageResID = 0
            mThirdImageDrawable = null
            thirdActionDefaultView.background = null
            thirdActionDefaultView.text = text
        }
        setThirdActionView(mThirdActionDefaultView)
    }

    fun setThirdActionViewText(text: String?, listener: OnClickListener?) {
        setThirdActionViewText(text)
        setThirdActionViewOnClickListener(listener)
    }

    fun setThirdActionViewImage(resId: Int) {
        if (0 >= resId) {
            return
        }
        mThirdActionDefaultView?.let { thirdActionDefaultView ->
            mThirdImageResID = resId
            mThirdImageDrawable = null
            thirdActionDefaultView.setBackgroundResource(resId)
            thirdActionDefaultView.text = ""
        }
        setThirdActionView(mThirdActionDefaultView)
    }

    fun getThirdActionViewImageResource(): Int = mThirdImageResID

    fun setThirdActionViewImage(resId: Int, listener: OnClickListener?) {
        setThirdActionViewImage(resId)
        setThirdActionViewOnClickListener(listener)
    }

    fun setThirdActionViewDrawable(drawable: Drawable?) {
        mThirdActionDefaultView?.let { thirdActionDefaultView ->
            mThirdImageResID = 0
            mThirdImageDrawable = drawable
            thirdActionDefaultView.background = drawable
            thirdActionDefaultView.text = ""
        }
        setThirdActionView(mThirdActionDefaultView)
    }

    fun setThirdActionCustomView(customView: View?) {
        mThirdImageResID = 0
        mThirdImageDrawable = null

        setThirdActionView(customView)
    }

    fun setThirdActionCustomView(customView: View?, listener: OnClickListener?) {
        setThirdActionCustomView(customView)
        setThirdActionViewOnClickListener(listener)
    }

    fun setThirdActionBadge(badge: String?) {
        mThirdActionBadgeView?.let { thirdActionBadgeView ->
            if (mHasThirdActionBadge && !TextUtils.isEmpty(badge)) {
                thirdActionBadgeView.text = badge
                thirdActionBadgeView.visibility = View.VISIBLE
            } else {
                thirdActionBadgeView.visibility = View.GONE
            }
        }
    }

    fun setHasThirdActionBadge(has: Boolean) {
        mHasThirdActionBadge = has
    }

    /* --------------------------------------------------------------------------------
    Sets fourth action view
    -------------------------------------------------------------------------------- */
    fun getFourthActionView(): View? {
        val fourthActionViewContainer = this.mFourthActionViewContainer
        if (null != fourthActionViewContainer) {
            if (0 < fourthActionViewContainer.childCount) {
                return fourthActionViewContainer.getChildAt(0)
            }
        }
        return null
    }

    fun setFourthActionViewEnabled(enabled: Boolean) {
        ViewUtil.setViewGroupEnabled(mFourthActionViewContainer, enabled)
    }

    /**
     * Set the visibility state of this view.
     *
     * @param visibility One of [.VISIBLE], [.INVISIBLE], or [.GONE].
     */
    fun setFourthActionViewVisibility(visibility: Int) {
        mFourthActionViewContainer?.visibility = visibility
    }

    private fun setFourthActionView(view: View?) {
        if (null == view) {
            return
        }
        mFourthActionViewContainer?.let { fourthActionView ->
            if (0 < fourthActionView.childCount) {
                val childView = fourthActionView.getChildAt(0)
                if (childView === view) {
                    return
                }
                fourthActionView.removeAllViews()
            }
            fourthActionView.addView(view)
            if (null != mFourthActionBadgeView && mHasFourthActionBadge) {
                fourthActionView.addView(mFourthActionBadgeView)
            }
        }
    }

    fun setFourthActionViewOnClickListener(listener: OnClickListener?) {
        mFourthActionViewContainer?.setOnClickListener(listener)
    }

    fun setFourthActionViewText(resId: Int) {
        mFourthActionDefaultView?.let { fourthActionDefaultView ->
            mFourthImageResID = 0
            mFourthImageDrawable = null
            fourthActionDefaultView.background = null
            fourthActionDefaultView.setText(resId)
        }
        setFourthActionView(mFourthActionDefaultView)
    }

    fun setFourthActionViewText(resId: Int, listener: OnClickListener?) {
        setFourthActionViewText(resId)
        setFourthActionViewOnClickListener(listener)
    }

    fun setFourthActionViewText(text: String?) {
        mFourthActionDefaultView?.let { fourthActionDefaultView ->
            mFourthImageResID = 0
            mFourthImageDrawable = null
            fourthActionDefaultView.background = null
            fourthActionDefaultView.text = text
        }
        setFourthActionView(mFourthActionDefaultView)
    }

    fun setFourthActionViewText(text: String?, listener: OnClickListener?) {
        setFourthActionViewText(text)
        setFourthActionViewOnClickListener(listener)
    }

    fun setFourthActionViewImage(resId: Int) {
        if (0 >= resId) {
            return
        }
        mFourthActionDefaultView?.let { fourthActionDefaultView ->
            mFourthImageResID = resId
            mFourthImageDrawable = null
            fourthActionDefaultView.setBackgroundResource(resId)
            fourthActionDefaultView.text = ""
        }
        setFourthActionView(mFourthActionDefaultView)
    }

    fun getFourthActionViewImageResource(): Int = mFourthImageResID

    fun setFourthActionViewImage(resId: Int, listener: OnClickListener?) {
        setFourthActionViewImage(resId)
        setFourthActionViewOnClickListener(listener)
    }

    fun setFourthActionViewDrawable(drawable: Drawable?) {
        mFourthActionDefaultView?.let { fourthActionDefaultView ->
            mFourthImageResID = 0
            mFourthImageDrawable = drawable
            fourthActionDefaultView.background = drawable
            fourthActionDefaultView.text = ""
        }
        setFourthActionView(mFourthActionDefaultView)
    }

    fun setFourthActionCustomView(customView: View?) {
        mFourthImageResID = 0
        mFourthImageDrawable = null

        setFourthActionView(customView)
    }

    fun setFourthActionCustomView(customView: View?, listener: OnClickListener?) {
        setFourthActionCustomView(customView)
        setFourthActionViewOnClickListener(listener)
    }

    fun setFourthActionBadge(badge: String?) {
        mFourthActionBadgeView?.let { fourthActionBadgeView ->
            if (mHasFourthActionBadge && !TextUtils.isEmpty(badge)) {
                fourthActionBadgeView.text = badge
                fourthActionBadgeView.visibility = View.VISIBLE
            } else {
                fourthActionBadgeView.visibility = View.GONE
            }
        }
    }

    fun setHasFourthActionBadge(has: Boolean) {
        mHasFourthActionBadge = has
    }

    fun setMenuActionViewOnClickListener(@MenuRes menu: Int, menuItemClickListener: OnMenuItemClickListener?) {
        setMenuActionViewOnClickListener(0, menu, menuItemClickListener)
    }

    interface OnMenuItemClickListener {
        fun onMenuItemClick(menuItem: MenuItem?)
    }

    fun setMenuActionViewOnClickListener(@DrawableRes resId: Int, @MenuRes menuRes: Int, menuItemClickListener: OnMenuItemClickListener?) {
        val mMenuActionView = this.mMenuActionView
        val mMenuAnchorView = this.mMenuAnchorView

        if (null != mMenuActionView && null != mMenuAnchorView) {
            if (0 != resId) {
                mMenuActionView.setBackgroundResource(resId)
            }
            if (0 != menuRes) {
                mMenuAnchorView.visibility = View.VISIBLE
                val popupMenu = PopupMenu(context, null)
                popupMenu.inflate(menuRes)
                val menu: Menu? = popupMenu.menu
                val menuItems: ArrayList<MenuItem> = ArrayList()
                if (null != menu) {
                    for (index in 0 until menu.size()) {
                        val menuItem: MenuItem = menu.getItem(index)
                        menuItems.add(menuItem)
                    }
                }
                val popupWindowWidth =
                    if (0 < mMenuWindowWidth) mMenuWindowWidth else if (WindowManager.LayoutParams.MATCH_PARENT == mMenuWindowWidth) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT

                // the drop down list is a list view
                val listView = ListView(context)
                // set our adapter and pass our pop up window contents
                listView.adapter = object : ArrayAdapter<MenuItem>(
                    context,
                    if (0 < mMenuItemLayoutRes) mMenuItemLayoutRes else android.R.layout.simple_dropdown_item_1line,
                    menuItems
                ) {
                    override fun hasStableIds(): Boolean {
                        return true
                    }

                    override fun getItemId(position: Int): Long {
                        return position.toLong()
                    }
                }
                listView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val popupWindow = PopupWindow(
                    popupWindowWidth,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )

                // set on item selected
                listView.onItemClickListener = object : AdapterView.OnItemClickListener {
                    override fun onItemClick(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
                        if (null == menuItemClickListener) {
                            return
                        }
                        val item: MenuItem? = adapterView.getItemAtPosition(position) as? MenuItem
                        if (null != item) {
                            menuItemClickListener.onMenuItemClick(item)
                        }
                        popupWindow.dismiss()
                    }
                }
                popupWindow.setBackgroundDrawable(mMenuBackgroundDrawable)
                popupWindow.isTouchable = true
                popupWindow.isFocusable = true
                popupWindow.isOutsideTouchable = true
                if (0 < mMenuWindowAnimation) {
                    popupWindow.animationStyle = mMenuWindowAnimation
                } else {
                    popupWindow.animationStyle = android.R.style.Animation_Dialog
                }

                // set the listview as popup content
                popupWindow.contentView = listView
                mMenuActionView.setOnClickListener(OnClickListener {
                    popupWindow.showAsDropDown(mMenuAnchorView, 0, 0, Gravity.END)
                })
            }
        }
    }

    fun setMenuActionViewVisibility(visibility: Int) {
        mMenuActionView?.visibility = visibility
    }

    /**
     * name="none" value = "0"
     * name="start" value = '"1";
     * name="middle" value = '"2";
     * name="end" value = '"3";
     * name="marquee" value = '"4";
     */
    private fun getTruncateAt(attrsValue: Int): TextUtils.TruncateAt? {
        return when (attrsValue) {
            1 -> {
                TextUtils.TruncateAt.START
            }
            2 -> {
                TextUtils.TruncateAt.MIDDLE
            }
            3 -> {
                TextUtils.TruncateAt.END
            }
            4 -> {
                TextUtils.TruncateAt.MARQUEE
            }
            else -> null
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
    private fun getGravityValue(attrsValue: Int): Int {
        var gravity = 0
        if (attrsValue and 0x30 == 0x30) {
            gravity = Gravity.CENTER
            return gravity
        }
        if (attrsValue and 0x01 == 0x01) { // top
            gravity = gravity or Gravity.TOP
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

    private fun updateTitleLocation() {
        // gets titlebar width
        var containerWidth = 0
        var startActionsWidth = 3
        var endActionsWidth = 3
        var titleWidth = 0

        containerWidth += mContainerView?.measuredWidth ?: 0

        mFirstActionViewContainer?.let { firstActionViewContainer ->
            startActionsWidth += firstActionViewContainer.measuredWidth
            val margins: Rect = firstActionViewContainer.getViewMargins()
            startActionsWidth += margins.left + margins.right
        }

        mEndActionsContainer?.let { endActionsContainer ->
            endActionsWidth += endActionsContainer.measuredWidth
            val margins: Rect = endActionsContainer.getViewMargins()
            endActionsWidth += margins.left + margins.right
        }

        // sets title location
        mTitleTextView?.let { titleTextView ->
            titleWidth += titleTextView.paint.measureText(titleTextView.text?.toString() ?: "").toInt()
        }

//        if (null != mTitleTextView) {
//            Trace.d("containerWidth: " + containerWidth + ", startActionsWidth:" + startActionsWidth
//                    + ", endActionsWidth:" + endActionsWidth + ", titleWidth:" + titleWidth
//                    + ", containerWidth - (startActionsWidth + endActionsWidth):" + (containerWidth - ( 2 * (startActionsWidth > endActionsWidth ? startActionsWidth : endActionsWidth)))
//                    + "\n title = " + mTitleTextView.getText().toString());
//        }
        mTitleTextView?.let { titleTextView ->
            var paddingStart: Int = mTitleTextPadding.left
            val paddingTop: Int = mTitleTextPadding.top
            var paddingEnd: Int = mTitleTextPadding.right
            val paddingBottom: Int = mTitleTextPadding.bottom
            if (mTitleGravity and Gravity.START == Gravity.START || mTitleGravity and Gravity.END == Gravity.END) {
                paddingStart += startActionsWidth
                paddingEnd += endActionsWidth
            } else if (mTitleGravity and Gravity.CENTER_HORIZONTAL == Gravity.CENTER_HORIZONTAL) {
                if (containerWidth - 2 * (if (startActionsWidth > endActionsWidth) startActionsWidth else endActionsWidth) < titleWidth) {
                    paddingStart += startActionsWidth
                    paddingEnd += endActionsWidth
                }
            } else {
                paddingStart += startActionsWidth
                paddingEnd += endActionsWidth
            }

//            Trace.d("containerWidth: paddingStart=" + paddingStart + ", paddingEnd:" + paddingEnd);
            titleTextView.setViewPadding(paddingStart, paddingTop, paddingEnd, paddingBottom)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (changed) {
            updateTitleLocation()
        }
        super.onLayout(changed, left, top, right, bottom)
    }
}