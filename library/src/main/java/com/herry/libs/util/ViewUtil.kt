package com.herry.libs.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.ResultReceiver
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.DisplayMetrics
import android.util.Size
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment


@Suppress("MemberVisibilityCanBePrivate", "unused")
object ViewUtil {

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    fun getStatusBarHeight(context: Context?): Int {
        return context?.resources?.getIdentifier("status_bar_height", "dimen", "android")?.let { resourceId ->
            context.resources.getDimensionPixelSize(resourceId)
        } ?: 0
    }

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    fun getNavigationBarHeight(context: Context?): Int {
        return context?.resources?.getIdentifier("navigation_bar_height", "dimen", "android")?.let { resourceId ->
            context.resources.getDimensionPixelSize(resourceId)
        } ?: 0
    }

    fun inflate(@LayoutRes layout: Int, root: ViewGroup): View {
        return LayoutInflater.from(root.context).inflate(layout, root, false)
    }

    fun inflate(context: Context, @LayoutRes layout: Int): View {
        return LayoutInflater.from(context).inflate(layout, null, false)
    }

    fun removeAllViews(view: View?) {
        if (view !is ViewGroup) {
            return
        }
        view.removeAllViews()
    }

    fun removeView(parent: View?, position: Int): Boolean {
        if (parent !is ViewGroup) {
            return false
        }
        val view: View = getChildAt(parent, position) ?: return false
        parent.removeView(view)
        return true
    }

    fun getChildPosition(parent: View?, view: View): Int {
        if (parent !is ViewGroup) {
            return -1
        }
        for (index in 0 until parent.childCount) {
            val child = parent.getChildAt(index)
            if (child === view) {
                return index
            }
        }
        return -1
    }

    fun addView(parent: View?, vararg child: View?) {
        if (parent !is ViewGroup) {
            return
        }
        for (view in child) {
            if (null == view) {
                continue
            }
            parent.addView(view)
        }
    }

    fun getChildAt(parent: View?, index: Int): View? {
        if (parent !is ViewGroup || 0 > index) {
            return null
        }
        return parent.getChildAt(index)
    }

    fun getChildCount(parent: View?): Int {
        if (parent !is ViewGroup) {
            return 0
        }
        return parent.childCount
    }

    fun getColor(context: Context?, @ColorRes id: Int): Int {
        if (null == context || 0 == id) {
            return 0
        }

        return try {
            ContextCompat.getColor(context, id)
        } catch (ex: Exception) {
            0
        }
    }

    fun getColorStateList(context: Context?, @ColorRes id: Int): ColorStateList? {
        if (null == context || 0 == id) {
            return null
        }

        return try {
            ContextCompat.getColorStateList(context, id)
        } catch (ex: Exception) {
            null
        }
    }

    fun getDrawable(context: Context?, @DrawableRes id: Int): Drawable? {
        if (null == context || 0 == id) {
            return null
        }

        return try {
            ContextCompat.getDrawable(context, id)
        } catch (ex: Exception) {
            null
        }
    }

    fun getColorDrawable(context: Context?, @ColorRes id: Int): Drawable? {
        if (null == context || 0 == id) {
            return null
        }
        val color = getColor(context, id)
        return ColorDrawable(color)
    }

    fun hideSoftKeyboard(activity: Activity?, flag: Int = 0, resultReceiver: ResultReceiver? = null) {
        hideSoftKeyboard(focusedView = activity?.currentFocus, flag = flag, resultReceiver = resultReceiver)
    }

    fun hideSoftKeyboard(fragment: Fragment?, flag: Int = 0, resultReceiver: ResultReceiver? = null) {
        val focusedView = fragment?.view?.findFocus()
            ?: (fragment as? DialogFragment)?.dialog?.window?.decorView
            ?: fragment?.activity?.currentFocus

        hideSoftKeyboard(focusedView = focusedView, flag = flag, resultReceiver = resultReceiver)
    }

    fun hideSoftKeyboard(focusedView: View?, flag: Int = 0, resultReceiver: ResultReceiver? = null) {
        val context = focusedView?.context ?: return
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(focusedView.windowToken, flag, resultReceiver)
    }

    fun showSoftKeyboard(view: View?, flag: Int = 0, resultReceiver: ResultReceiver? = null) {
        val context = view?.context ?: return

        view.postDelayed({
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(view, flag, resultReceiver)
        }, context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
    }

    fun isSoftKeyboardShown(rootView: View?): Boolean {
        if (null == rootView) {
            return false
        }

        /* 128dp = 32dp * 4, minimum button height 32dp and generic 4 rows soft keyboard */
        @Suppress("LocalVariableName")
        val SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128
        val screenRect = Rect()
        val screenHeight = rootView.height

        rootView.getWindowVisibleDisplayFrame(screenRect)
        // heightDiff = rootView height - status bar height (r.top) - visible frame height (r.bottom - r.top)
        val heightDiff: Int = screenHeight - (screenRect.bottom - screenRect.top)

        /* Threshold size: dp to pixels, multiply with display density */
        val density = rootView.resources.displayMetrics.density
        return heightDiff > SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD * density
    }

    @JvmStatic
    @SuppressLint("ClickableViewAccessibility")
    fun setProtectTouchLowLayer(view: View?, protect: Boolean) {
        view?.setOnTouchListener { _: View?, _: MotionEvent? -> protect }
    }

    fun isSoftKeyboardShown(activity: Activity?): Boolean {
        val contentView = activity?.findViewById<View?>(android.R.id.content) ?: return false
        return isSoftKeyboardShown(contentView.rootView)
    }

    fun setSoftKeyboardVisibilityListener(activity: Activity?, listener: OnSoftKeyboardVisibilityListener) {
        val contentView = activity?.findViewById<View?>(android.R.id.content) ?: return
        contentView.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            private var wasShown: Boolean? = null

            override fun onGlobalLayout() {
                val isShown = isSoftKeyboardShown(contentView.rootView)
                if (isShown == wasShown) {
                    // Keyboard state hasn't changed
                    return
                }

                wasShown = isShown
                listener.onChangedSoftKeyboardVisibility(isShown)
            }
        })
    }

    fun isConnectedHardwareKeyboard(rootView: View?): Boolean {
        val context = rootView?.context ?: return false
        val configuration = context.resources.configuration
        return configuration.navigation == Configuration.NAVIGATION_DPAD
                || configuration.navigation == Configuration.NAVIGATION_TRACKBALL
                || configuration.navigation == Configuration.NAVIGATION_WHEEL
    }

    fun removeViewFormParent(view: View?) {
        view ?: return

        if (view.parent is ViewGroup) {
            val parent = view.parent as ViewGroup
            parent.removeView(view)
        }
    }

    fun setViewEnabledWithChildView(view: View?, enabled: Boolean) {
        view?.isEnabled = enabled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setViewEnabledWithChildView(view.getChildAt(i), enabled) // Recursive call
            }
        }
    }

    fun getDimension(context: Context?, id: Int): Float {
        val resources = context?.resources ?: return 0f
        return resources.getDimension(id)
    }

    fun getDimensionPixelSize(context: Context?, @DimenRes id: Int): Int {
        val resources = context?.resources ?: return 0
        return resources.getDimensionPixelSize(id)
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    fun convertDpToPixel(dp: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        //float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        //Trace.d("convertDpToPixel dp:" + dp + " to px:" + px);
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    fun convertPixelsToDp(px: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        //float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        //Trace.d("convertPixelsToDp px:" + px + " to dp:" + dp);
        return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun getScreenSize(context: Context?): Size {
        val resources = context?.resources ?: return Size(0, 0)

        val displayMetrics = resources.displayMetrics ?: return Size(0, 0)
        return Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    fun isTabletDevice(context: Context?): Boolean = (context?.resources?.configuration?.smallestScreenWidthDp ?: 0) >= 600

    fun isPortraitOrientation(context: Context?): Boolean = context?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT

    data class ReadMoreTextData(
        val moreLine: Int,
        val moreText: String,
        @ColorInt val  moreTextColor: Int
    )

    fun setReadMoreText(view: TextView?, src: String, readMoreData: ReadMoreTextData) {
        view ?: return

        val moreText = readMoreData.moreText
        val moreLine = readMoreData.moreLine

        val expendText = "..."
        val expandedText = expendText + if (!TextUtils.isEmpty(moreText)) moreText else ""
        if (view.tag != null && view.tag == src) { //Tag로 전값 의 text를 비교하여똑같으면 실행하지 않음.
            return
        }
        view.tag = src //Tag에 text 저장
        view.text = src
        view.post {
            val textViewLines = view.lineCount
            //  compare text lines and more line
            if (textViewLines > moreLine) {
                var displayText = ""
                if (moreLine <= 0) {
                    // display only more text
                    displayText = expandedText
                } else {
                    // split original text to more line and adds more text to end
                    val lineEndIndex = view.layout.getLineEnd(moreLine - 1) - 1
                    for (index in lineEndIndex downTo 0) {
                        try {
                            val subSequence = src.subSequence(0, index)
                            val temp = StringBuilder(subSequence).append(expandedText)
                            view.text = temp.toString()
                            if (moreLine >= view.lineCount) {
                                displayText = temp.toString()
                                break
                            }
                        } catch (ignore: Exception) {
                        }
                    }
                    view.text = src
                }
                val clickText = if (!TextUtils.isEmpty(moreText)) moreText else expendText
                val displaySpannableString = SpannableString(displayText)
                displaySpannableString.setSpan(object : ClickableSpan() {
                    override fun onClick(v: View) {
                        // if click more text, set text to original text
                        view.text = src
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        // sets more text color
                        ds.color = readMoreData.moreTextColor
                    }
                }, displaySpannableString.length - clickText.length - 1, displaySpannableString.length, 0)
                view.text = displaySpannableString
                view.movementMethod = LinkMovementMethod.getInstance()
            }
            // else
            // sets text without more text
        }
    }

//    private fun createReadMoreSpannableString(view: TextView?, src: SpannableString, readMoreData: ReadMoreTextData, onResult: (text: SpannableString) -> Unit) {
//        view ?: return
//
//        val moreText = readMoreData.moreText
//        val moreLine = readMoreData.moreLine
//
//        val expendText = "... "
//        val expandedText = expendText + if (!TextUtils.isEmpty(moreText)) moreText else ""
//        if (view.tag != null && view.tag == src) { //Tag로 전값 의 text를 비교하여똑같으면 실행하지 않음.
//            return
//        }
//        view.tag = src.toString() //Tag에 text 저장
//        view.text = src
//        view.post {
//            val textViewLines = view.lineCount
//            //  compare text lines and more line
//            if (textViewLines > moreLine) {
//                var displayText = ""
//                if (moreLine <= 0) {
//                    // display only more text
//                    displayText = expandedText
//                } else {
//                    // split original text to more line and adds more text to end
//                    val lineEndIndex = view.layout.getLineEnd(moreLine - 1) - 1
//                    for (index in lineEndIndex downTo 0) {
//                        try {
//                            val subSequence = src.subSequence(0, index)
//                            val temp = StringBuilder(subSequence).append(expandedText)
//                            view.text = temp.toString()
//                            if (moreLine >= view.lineCount) {
//                                displayText = temp.toString()
//                                break
//                            }
//                        } catch (ignore: Exception) {
//                        }
//                    }
//                    view.text = src
//                }
//                val clickText = if (!TextUtils.isEmpty(moreText)) moreText else expendText
//                val displaySpannableString = SpannableString(displayText)
//                displaySpannableString.setSpan(object : ClickableSpan() {
//                    override fun onClick(v: View) {
//                        // if click more text, set text to original text
//                        view.text = src
//                    }
//
//                    override fun updateDrawState(ds: TextPaint) {
//                        // sets more text color
//                        ds.color = readMoreData.moreTextColor
//                    }
//                }, displaySpannableString.length - clickText.length - 1, displaySpannableString.length, 0)
//                onResult.invoke(displaySpannableString)
//            }
//            // else
//            // sets text without more text
//        }
//    }

    data class LinkTextData(
        val links: MutableList<String>,
        @ColorInt val  linkTextColor: Int? = null,
        val isUnderlineText: Boolean = true,
        val onClicked: ((view: View, text: String) -> Unit)? = null,
        val readMore: ReadMoreTextData? = null
    )

    fun setLinkText(view: TextView?, src: String? = null, linkData: LinkTextData) {
        view ?: return
        val text = src ?: (view.text ?: "")

        if (text.isEmpty()) {
            return
        }

        val links = linkData.links
        if (links.isEmpty()) {
            view.text = text
            return
        }
        val spannableString = SpannableString(text)
        var startIndexOfLink = -1
        for (link in links) {
            if (link.isEmpty() || !text.contains(link)) {
                continue
            }

            val clickableSpan = object : ClickableSpan() {
                override fun updateDrawState(textPaint: TextPaint) {
                    // use this to change the link color
                    textPaint.color = linkData.linkTextColor ?: textPaint.linkColor
                    // toggle below value to enable/disable
                    // the underline shown below the clickable text
                    textPaint.isUnderlineText = linkData.isUnderlineText
                }

                override fun onClick(view: View) {
                    val clickView = view as? TextView ?: return
                    val selectionText = clickView.text as? Spannable ?: return
                    Selection.setSelection(selectionText, 0)
                    view.invalidate()
                    linkData.onClicked?.invoke(view, link)
                }
            }
            startIndexOfLink = text.toString().indexOf(link, startIndexOfLink + 1)
            spannableString.setSpan(
                clickableSpan, startIndexOfLink, startIndexOfLink + link.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        if (linkData.onClicked != null) {
            view.movementMethod = LinkMovementMethod.getInstance()
        }

        // sets read more data
        view.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    fun setViewGroupEnabled(view: View?, enabled: Boolean) {
        view?.isEnabled = enabled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setViewGroupEnabled(view.getChildAt(i), enabled) // Recursive call
            }
        }
    }

    fun setViewGroupSelected(view: View?, selected: Boolean) {
        view?.isSelected = selected
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setViewGroupSelected(view.getChildAt(i), selected) // Recursive call
            }
        }
    }

    fun requestFocus(view: View?, focus: Boolean) {
        view ?: return

        if (focus) {
            if (!view.isFocusable || !view.isFocusableInTouchMode) {
                view.isFocusableInTouchMode = true
            }
            view.requestFocus()
        } else {
            view.clearFocus()
        }
    }

    fun hasFocus(view: View?): Boolean {
        return null != view && view.hasFocus()
    }

    fun createPaint(color: Int): Paint {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        paint.color = color
        return paint
    }

    fun createPaint(): Paint {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        return paint
    }

    fun mutatePaint(paint: Paint?, color: Int, strokeWidth: Float, style: Paint.Style? = null) {
        mutatePaint(paint, true, color, strokeWidth, style)
    }

    fun mutatePaint(paint: Paint?, antiAlias: Boolean, color: Int, strokeWidth: Float, style: Paint.Style? = null) {
        if (null == paint) {
            return
        }
        paint.reset()
        paint.isAntiAlias = antiAlias
        paint.color = color
        paint.strokeWidth = strokeWidth
        if (null != style) {
            paint.style = style
        }
    }

    private fun between(startColor: Int, endColor: Int, factor: Int, steps: Int): Int {
        val ratio = factor.toFloat() / steps
        return (endColor * ratio + startColor * (1 - ratio)).toInt()
    }

    fun gradient(startColor: Int, endColor: Int, factor: Int, steps: Int): Int {
        val alpha: Int = between(Color.alpha(startColor), Color.alpha(endColor), factor, steps)
        val red: Int = between(Color.red(startColor), Color.red(endColor), factor, steps)
        val green: Int = between(Color.green(startColor), Color.green(endColor), factor, steps)
        val blue: Int = between(Color.blue(startColor), Color.blue(endColor), factor, steps)
        return Color.argb(alpha, red, green, blue)
    }

    fun composeRoundedRectPath(rect: RectF, topLeftRadius: Float, topRightRadius: Float, bottomLeftRadius: Float, bottomRightRadius: Float): Path {
        val path = Path()
        val topLeft = if (topLeftRadius < 0) 0f else topLeftRadius
        val topRight = if (topRightRadius < 0) 0f else topRightRadius
        val bottomLeft = if (bottomLeftRadius < 0) 0f else bottomLeftRadius
        val bottomRight = if (bottomRightRadius < 0) 0f else bottomRightRadius
        path.moveTo(rect.left + topLeft, rect.top)
        path.lineTo(rect.right - topRight, rect.top)
        path.quadTo(rect.right, rect.top, rect.right, rect.top + topRight)
        path.lineTo(rect.right, rect.bottom - bottomRight)
        path.quadTo(rect.right, rect.bottom, rect.right - bottomRight, rect.bottom)
        path.lineTo(rect.left + bottomLeft, rect.bottom)
        path.quadTo(rect.left, rect.bottom, rect.left, rect.bottom - bottomLeft)
        path.lineTo(rect.left, rect.top + topLeft)
        path.quadTo(rect.left, rect.top, rect.left + topLeft, rect.top)
        path.close()
        return path
    }

    fun isRTL(context: Context?): Boolean {
        if (context?.resources == null) {
            return false
        }
        return context.resources.configuration?.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }

    /**
     * Creates shape drawable
     * @param context context
     * @param color drawable fill color
     * @param radius edge radius (dimen size)
     * @param applyEdge apply edge (ex Gravity.TOP | Gravity.START | Gravity.BOTTOM | Gravity.END)
     * @return drawable
     */
    fun createShapeDrawable(context: Context, color: Int, radius: Float, applyEdge: Int): Drawable {
        val drawable = GradientDrawable()
        drawable.setColor(color)
        drawable.shape = GradientDrawable.RECTANGLE
        val startTopEdge = applyEdge and (Gravity.TOP or Gravity.START) == Gravity.TOP or Gravity.START
        val startBottomEdge = applyEdge and (Gravity.BOTTOM or Gravity.START) == Gravity.BOTTOM or Gravity.START
        val endTopEdge = applyEdge and (Gravity.TOP or Gravity.END) == Gravity.TOP or Gravity.END
        val endBottomEdge = applyEdge and (Gravity.BOTTOM or Gravity.END) == Gravity.BOTTOM or Gravity.END
        val values = floatArrayOf(
            if (startTopEdge) radius else 0f, if (startTopEdge) radius else 0f,
            if (endTopEdge) radius else 0f, if (endTopEdge) radius else 0f,
            if (endBottomEdge) radius else 0f, if (endBottomEdge) radius else 0f,
            if (startBottomEdge) radius else 0f, if (startBottomEdge) radius else 0f
        )
        if (isRTL(context)) {
            val values2 = floatArrayOf(
                values[2], values[3],
                values[0], values[1],
                values[6], values[7],
                values[4], values[5]
            )
            drawable.cornerRadii = values2
        } else {
            drawable.cornerRadii = values
        }
        return drawable
    }

    fun getChildHeight(parentView: View?): Int {
        if (parentView !is ViewGroup) return 0

        val measureSpecW = View.MeasureSpec.makeMeasureSpec(parentView.width, View.MeasureSpec.EXACTLY)
        val measureSpecH = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        var totalHeight = 0

        parentView.children.forEach { childView ->
            val layoutParams = childView.layoutParams
            val height = layoutParams.height
            val childHeightSpec = if (height > 0) {
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            } else {
                measureSpecH
            }

            childView.measure(measureSpecW, childHeightSpec)
            totalHeight += childView.measuredHeight
        }

        return totalHeight
    }
}