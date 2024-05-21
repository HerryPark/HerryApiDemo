@file:Suppress("unused")

package com.herry.libs.widget.extension

import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.text.util.Linkify
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import com.herry.libs.util.ViewUtil
import java.util.regex.Pattern

/**
 * Created by herry.park on 2020/06/17.
 **/
class OnProtectClick(val listener: ((v: View) -> Unit)): View.OnClickListener {

    companion object {
        const val protectTimeMs = 500L
        var lastClickTimeMs = 0L
    }

    private fun isClickable(): Boolean {
        val elapsedRealtime = SystemClock.elapsedRealtime()
        if(elapsedRealtime - lastClickTimeMs < protectTimeMs) {
            return false
        }
        lastClickTimeMs = elapsedRealtime
        return true
    }

    override fun onClick(v: View?) {
        v?.let {
            if(isClickable() ) {
                listener(it)
            }
        }

    }
}

fun View.setOnSingleClickListener(l: ((v: View) -> Unit)?) {
    if(l != null) {
        setOnClickListener(OnProtectClick(l))
    } else {
        setOnClickListener(null)
    }
}

fun ImageView.setImage(
    drawable: Drawable?,
    @ColorRes statefulColorSateResId: Int = 0,
    @ColorRes notStatefulColorSateResId: Int = 0
) {
    val context = this.context ?: return

    // set drawable to image view
    setImageDrawable(drawable)

    drawable ?: return

    // sets color sate list
    imageTintList = ViewUtil.getColorStateList(
        context, if (drawable.isStateful) statefulColorSateResId else notStatefulColorSateResId
    )
}

fun ImageView.setImage(
    @DrawableRes resId: Int,
    @ColorRes statefulColorSateResId: Int = 0,
    @ColorRes notStatefulColorSateResId: Int = 0
) {
    val context = this.context ?: return

    // gets drawable
    val drawable: Drawable? = ViewUtil.getDrawable(context, resId)

    setImage(drawable, statefulColorSateResId, notStatefulColorSateResId)
}

fun ImageView.setImage(drawable: Drawable?, statefulColorSate: ColorStateList?, notStatefulColorSate: ColorStateList?) {
    // set drawable to image view
    setImageDrawable(drawable)

    drawable ?: return

    // sets color sate list
    imageTintList = if (drawable.isStateful) statefulColorSate else notStatefulColorSate
}

fun View.setViewSize(width: Int, height: Int) {
    val params = this.layoutParams ?: ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    // set width
    if (width >= 0) {
        params.width = width
    } else if (ViewGroup.LayoutParams.MATCH_PARENT == width || ViewGroup.LayoutParams.WRAP_CONTENT == width) {
        params.width = width
    }

    // set height
    if (height >= 0) {
        params.height = height
    } else if (ViewGroup.LayoutParams.MATCH_PARENT == height || ViewGroup.LayoutParams.WRAP_CONTENT == height) {
        params.height = height
    }
    this.layoutParams = params
}

fun View.setViewWidth(width: Int) {
    val params = this.layoutParams ?: ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    // set width
    if (0 <= width) {
        params.width = width
    } else if (ViewGroup.LayoutParams.MATCH_PARENT == width || ViewGroup.LayoutParams.WRAP_CONTENT == width) {
        params.width = width
    }
    this.layoutParams = params
}

fun View.setViewHeight(height: Int) {
    val params = this.layoutParams ?: ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    if (0 <= height) {
        params.height = height
    } else if (ViewGroup.LayoutParams.MATCH_PARENT == height || ViewGroup.LayoutParams.WRAP_CONTENT == height) {
        params.height = height
    }

    this.layoutParams = params
}

fun View.setViewWeight(weight: Float) {
    val params = this.layoutParams
    if (params is LinearLayout.LayoutParams) {
        params.weight = weight
    } else {
        return
    }
    this.layoutParams = params
}

fun View.setViewMargin(@Px left: Int, @Px top: Int, @Px right: Int, @Px bottom: Int) {
    if (this.layoutParams is ViewGroup.MarginLayoutParams) {
        val params = this.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(left, top, right, bottom)
        this.layoutParams = params
    }
}

fun View.setViewMargin(margins: Rect) {
    setViewMargin(margins.left, margins.top, margins.right, margins.bottom)
}

fun View.setViewMargin(@Px margin: Int) {
    setViewMargin(margin, margin, margin, margin)
}

fun View.setViewMarginTop(@Px margin: Int) {
    setViewMargin(Rect(getViewMargins().apply { this.top = margin }))
}

fun View.setViewMarginBottom(@Px margin: Int) {
    setViewMargin(Rect(getViewMargins().apply { this.bottom = margin }))
}

fun View.setViewMarginStart(@Px margin: Int) {
    setViewMargin(Rect(getViewMargins().apply { this.left = margin }))
}

fun View.setViewMarginEnd(@Px margin: Int) {
    setViewMargin(Rect(getViewMargins().apply { this.right = margin }))
}

fun View.getViewMargins(): Rect {
    val margins = Rect()
    if (this.layoutParams is ViewGroup.MarginLayoutParams) {
        val params = this.layoutParams as ViewGroup.MarginLayoutParams
        margins.left = params.marginStart
        margins.top = params.topMargin
        margins.right = params.marginEnd
        margins.bottom = params.bottomMargin
    }
    return margins
}

fun View.setViewPadding(@Px start: Int = this.paddingStart, @Px top: Int = this.paddingTop, @Px end: Int = this.paddingEnd, @Px bottom: Int = this.paddingBottom) {
    this.setPadding(start, top, end, bottom)
}

fun View.setViewPadding(paddings: Rect) {
    setViewPadding(paddings.left, paddings.top, paddings.right, paddings.bottom)
}

fun View.setViewPadding(@Px padding: Int) {
    setViewPadding(padding, padding, padding, padding)
}

fun View.setViewPaddingTop(@Px padding: Int) {
    setViewPadding(Rect(getViewPaddings().apply { this.top = padding }))
}

fun View.setViewPaddingBottom(@Px padding: Int) {
    setViewPadding(Rect(getViewPaddings().apply { this.bottom = padding }))
}

fun View.setViewPaddingStart(@Px padding: Int) {
    setViewPadding(Rect(getViewPaddings().apply { this.left = padding }))
}

fun View.setViewPaddingEnd(@Px padding: Int) {
    setViewPadding(getViewPaddings().apply { this.right = padding })
}

fun View.getViewPaddings(): Rect = Rect(
    /*left = */this.paddingStart,
    /*top = */this.paddingTop,
    /*right = */this.paddingEnd,
    /*bottom = */this.paddingBottom
)

fun View.setLayoutGravity(gravity: Int) {
    if (layoutParams is LinearLayout.LayoutParams) {
        val params = layoutParams as LinearLayout.LayoutParams
        params.gravity = gravity
        layoutParams = params
    } else if (layoutParams is FrameLayout.LayoutParams) {
        val params = layoutParams as FrameLayout.LayoutParams
        params.gravity = gravity
        layoutParams = params
    }
}

fun TextView.addLink(target: String, url: String?) {
    if (target.isNotEmpty() && text?.toString()?.contains(target) == true) {
        Linkify.addLinks(this, Pattern.compile(target), url, null, Linkify.TransformFilter { _, _ -> "" })
    }
}

fun View.getViewMeasuredWidth(): Int {
    val displayMetrics = context?.resources?.displayMetrics ?: return 0
    measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
    return measuredWidth
}

fun View.getViewMeasuredHeight(): Int {
    val displayMetrics = context?.resources?.displayMetrics ?: return 0
    measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
    return measuredHeight
}

fun View.measure() {
    measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
}

fun ImageView.setTint(@ColorRes statefulColorSateResId: Int) {
    setImage(drawable, statefulColorSateResId, statefulColorSateResId)
}

fun ImageView.setTint(@ColorRes statefulColorSateResId: Int, @ColorRes notStatefulColorSateResId: Int) {
    setImage(drawable, statefulColorSateResId, notStatefulColorSateResId)
}

fun ImageView.setTint(statefulColorSate: ColorStateList?) {
    setTint(statefulColorSate, statefulColorSate)
}

fun ImageView.setTint(statefulColorSate: ColorStateList?, notStatefulColorSate: ColorStateList?) {
    setImage(drawable, statefulColorSate, notStatefulColorSate)
}

fun ImageView.setTintColor(@ColorInt color: Int) {
    this.imageTintList = ColorStateList.valueOf(color)
}

fun View.convertDpToPx(dp: Float): Int {
    return if (null == context) {
        0
    } else (dp * context.resources.displayMetrics.density + 0.5f).toInt()
}

