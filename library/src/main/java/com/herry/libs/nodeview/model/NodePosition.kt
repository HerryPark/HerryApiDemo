package com.herry.libs.nodeview.model


@Suppress("unused")
class NodePosition(internal val positions: IntArray) {
    companion object {
        const val NO_POSITION = -1

        /**
         * Creates the NodePosition with relative position
         */
        internal fun compose(position: Int, relative: NodePosition?): NodePosition {
            if (relative != null) {
                val relativePosition = IntArray(relative.positions.size + 1)
                relativePosition[0] = position
                for (index in 1..relative.positions.size) {
                    relativePosition[index] = relative.positions[index - 1]
                }
                return NodePosition(relativePosition)
            }
            return NodePosition(intArrayOf(position))
        }
    }

    fun getViewPosition(): Int {
        return positions.sum()
    }

    fun getPosition(): Int {
        if (positions.isNotEmpty()) {
            return positions[positions.size - 1]
        }
        return NO_POSITION
    }

    fun getParentPosition(): NodePosition? {
        if (positions.size > 1) {
            val parentPositions = IntArray(positions.size - 1)
            for (parentPosition in parentPositions.indices) {
                parentPositions[parentPosition] = positions[parentPosition]
            }
            return NodePosition(parentPositions)
        }
        return null
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (position in positions) {
            sb.append("[$position]")
        }
        return sb.toString()
    }
}