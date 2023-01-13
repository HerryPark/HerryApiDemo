package com.herry.libs.draw

import android.graphics.Path
import java.io.ObjectInputStream
import java.io.Serializable
import java.util.*

class DrawPath : Path(), Serializable {
    val actions = LinkedList<DrawAction>()

    private fun read(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()

        val copiedActions = actions.map { it }
        copiedActions.forEach {
            it.perform(this)
        }
    }

    override fun reset() {
        actions.clear()
        super.reset()
    }

    override fun moveTo(x: Float, y: Float) {
        actions.add(DrawMove(x, y))
        super.moveTo(x, y)
    }

    override fun lineTo(x: Float, y: Float) {
        actions.add(DrawLine(x, y))
        super.lineTo(x, y)
    }

    override fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) {
        actions.add(DrawQuad(x1, x2, y1, y2))
        super.quadTo(x1, y1, x2, y2)
    }
}