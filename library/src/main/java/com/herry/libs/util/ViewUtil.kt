package com.herry.libs.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.ResultReceiver
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.DisplayMetrics
import android.util.Size
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.*
import androidx.core.content.ContextCompat

@Suppress("MemberVisibilityCanBePrivate", "unused")
object ViewUtil {
    enum class StatusBarMode {
        LIGHT,
        DARK
    }

    fun makeFullScreen(activity: Activity?, isFull: Boolean = true) {
        activity?.window?.let { window ->
//            if (ApiHelper.hasOSv11()) {
//                window.setDecorFitsSystemWindows(!isFull)
//            } else {
//            }
            window.decorView.systemUiVisibility = if (isFull) View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN else View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    fun setStatusBarColor(activity: Activity?, @ColorInt color: Int) {
        activity?.window?.let { window ->
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
        }
    }

    fun setStatusBarTransparent(activity: Activity?, mode: StatusBarMode = StatusBarMode.DARK) {
        activity?.window?.let { window ->
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.decorView.systemUiVisibility = when (mode) {
                StatusBarMode.LIGHT -> window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                StatusBarMode.DARK -> window.decorView.systemUiVisibility and (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).inv()
            }
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    fun getStatusBarHeight(context: Context?): Int {
        var result = 0
        if (null != context) {
            val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = context.resources.getDimensionPixelSize(resourceId)
            }
        }
        return result
    }

    fun setNavigationBarColor(activity: Activity?, @ColorInt color: Int) {
        activity?.window?.navigationBarColor = color
    }

    fun isSystemFullScreen(context: Context?): Boolean {
        var isFullScreen = false
        val typedValue = TypedValue()
        val attrs: TypedArray? = context?.obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.windowFullscreen))
        if (attrs != null) {
            isFullScreen = attrs.getBoolean(0, false)
            attrs.recycle()
        }
        return isFullScreen
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

    fun hideSoftKeyboard(context: Context?, rootView: View?) {
        if (isSoftKeyboardShown(rootView) && context != null) {
            val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputMethodManager?.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
        }
    }

    fun hideSoftKeyboard(view: View?, activity: Activity?): Boolean {
        return hideSoftKeyboard(view, activity, 0, null)
    }

    fun hideSoftKeyboard(view: View?, activity: Activity?, flag: Int): Boolean {
        return hideSoftKeyboard(view, activity, flag, null)
    }

    fun hideSoftKeyboard(view: View?, activity: Activity?, resultReceiver: ResultReceiver?): Boolean {
        return hideSoftKeyboard(view, activity, 0, resultReceiver)
    }

    fun hideSoftKeyboard(view: View?, activity: Activity?, flag: Int, resultReceiver: ResultReceiver?): Boolean {
        if (null == activity || null == view) {
            return false
        }
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        return null != imm && imm.hideSoftInputFromWindow(view.applicationWindowToken, flag, resultReceiver)
    }

    fun showSoftKeyboard(view: View?, activity: Activity?, flag: Int): Boolean {
        return showSoftKeyboard(view, activity, flag, null)
    }

    fun showSoftKeyboard(view: View?, activity: Activity?, resultReceiver: ResultReceiver?): Boolean {
        return showSoftKeyboard(view, activity, 0, resultReceiver)
    }

    fun showSoftKeyboard(view: View?, activity: Activity?, flag: Int, resultReceiver: ResultReceiver?): Boolean {
        if (null == activity || null == view) {
            return false
        }
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        return null != imm && imm.showSoftInput(view, flag, resultReceiver)
    }

    fun isSoftKeyboardShown(rootView: View?): Boolean {
        if (null == rootView) {
            return false
        }

        /* 128dp = 32dp * 4, minimum button height 32dp and generic 4 rows soft keyboard */
        @Suppress("LocalVariableName")
        val SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128
        val r = Rect()
        rootView.getWindowVisibleDisplayFrame(r)
        val dm = rootView.resources.displayMetrics

        /* heightDiff = rootView height - status bar height (r.top) - visible frame height (r.bottom - r.top) */
        val heightDiff = rootView.bottom - r.bottom

        /* Threshold size: dp to pixels, multiply with display density */return heightDiff > SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD * dm.density
    }

    @JvmStatic
    @SuppressLint("ClickableViewAccessibility")
    fun setProtectTouchLowLayer(view: View?, protect: Boolean) {
        view?.setOnTouchListener { _: View?, _: MotionEvent? -> protect }
    }

    fun removeViewFormParent(view: View?) {
        view ?: return

        if (view.parent is ViewGroup) {
            val parent = view.parent as ViewGroup
            parent.removeView(view)
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
        //Trace.d("Herry", "convertDpToPixel dp:" + dp + " to px:" + px);
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
        //Trace.d("Herry", "convertPixelsToDp px:" + px + " to dp:" + dp);
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
        if (null != view) {
            view.isEnabled = enabled
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    val child = view.getChildAt(i)
                    if (null != child) {
                        if (child is ViewGroup) {
                            setViewGroupEnabled(child, enabled)
                        } else {
                            child.isEnabled = enabled
                        }
                    }
                }
            }
        }
    }

    fun setViewGroupSelected(view: View?, selected: Boolean) {
        if (null != view) {
            view.isSelected = selected
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    val child = view.getChildAt(i)
                    if (null != child) {
                        if (child is ViewGroup) {
                            setViewGroupSelected(child, selected)
                        } else {
                            child.isSelected = selected
                        }
                    }
                }
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
}