package com.herry.libs.widget.configure

import androidx.annotation.ColorInt

data class SystemUIAppearances(
    val isFullScreen: Boolean = false,
    val showBehavior: SystemUIShowBehavior? = null,
    val statusBar: SystemUIAppearance = SystemUIAppearance(),
    val navigationBar: SystemUIAppearance = SystemUIAppearance(visibility = SystemUIVisibility.SHOW)
)

data class SystemUIAppearance(
    val appearanceColorStyle: SystemUIAppearanceColorStyle? = null,
    @ColorInt val backgroundColor: Int? = null,
    val visibility: SystemUIVisibility? = if (backgroundColor != null || appearanceColorStyle != null) SystemUIVisibility.SHOW else null
)

enum class SystemUIAppearanceColorStyle {
    LIGHT,
    DARK
}

enum class SystemUIVisibility {
    SHOW,
    HIDE
}

enum class SystemUIShowBehavior {
    /**
     * https://developer.android.com/reference/androidx/core/view/WindowInsetsControllerCompat#BEHAVIOR_DEFAULT()
     */
    DEFAULT,

    /**
     * https://developer.android.com/reference/androidx/core/view/WindowInsetsControllerCompat#BEHAVIOR_SHOW_BARS_BY_SWIPE()
     */
    TRANSIENT_BARS_BY_SWIPE
}
