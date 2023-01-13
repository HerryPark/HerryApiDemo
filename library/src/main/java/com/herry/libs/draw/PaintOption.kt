package com.herry.libs.draw

import android.graphics.Color

data class PaintOption(
    var color: Int = Color.BLACK,
    var strokeWidth: Float = 0f,
    var alpha: Int = 255,
    var isEraserOn: Boolean = false
)