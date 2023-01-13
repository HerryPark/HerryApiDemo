package com.herry.libs.draw

import android.graphics.Path
import java.io.Writer

class DrawMove(val x: Float, val y: Float) : DrawAction {
    override fun perform(path: Path) {
        path.moveTo(x, y)
    }

    override fun perform(writer: Writer) {
        writer.write("M$x,$y")
    }
}