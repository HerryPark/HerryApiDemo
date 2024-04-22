package com.herry.libs.annotation

import androidx.annotation.IntDef

object OrientationScreen {
    const val LANDSCAPE = 0
    const val PORTRAIT = 1
}

/** hide */
@IntDef(OrientationScreen.LANDSCAPE, OrientationScreen.PORTRAIT)
@Retention(AnnotationRetention.SOURCE)
annotation class OrientationScreenMode

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.EXPRESSION, AnnotationTarget.FILE)
annotation class Orientation(@OrientationScreenMode val orientation: Int)