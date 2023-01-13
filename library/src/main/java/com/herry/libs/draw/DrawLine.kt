package com.herry.libs.draw

import android.graphics.Path
import java.io.Writer

class DrawLine(val x: Float, val y: Float) : DrawAction {
    override fun perform(path: Path) {
        path.lineTo(x, y)
    }

    override fun perform(writer: Writer) {
        writer.write("L$x,$y")
    }
}