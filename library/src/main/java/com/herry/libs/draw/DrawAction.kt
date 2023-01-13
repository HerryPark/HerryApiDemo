package com.herry.libs.draw

import android.graphics.Path
import java.io.Serializable
import java.io.Writer

interface DrawAction : Serializable {
    fun perform(path: Path)
    fun perform(writer: Writer)
}