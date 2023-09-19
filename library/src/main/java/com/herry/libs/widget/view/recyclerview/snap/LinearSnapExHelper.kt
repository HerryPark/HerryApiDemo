package com.herry.libs.widget.view.recyclerview.snap

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.herry.libs.log.Trace

/**
 * Created by herry.park
 */
@Suppress("unused")
class LinearSnapExHelper(private val snapStyle: SnapStyle = SnapStyle.CENTER) : LinearSnapHelper() {

    companion object {
        private const val INVALID_DISTANCE = 1f
    }

    enum class SnapStyle {
        START, CENTER, END
    }

    interface OnSnapPositionChangeListener {
        fun onSnapped(position: Int)
        fun onSnapPositionChange(position: Int)
    }

    // Orientation helpers are lazily created per LayoutManager.
    private var verticalHelper: OrientationHelper? = null
    private var horizontalHelper: OrientationHelper? = null
    private var snapPosition: Int = RecyclerView.NO_POSITION

    private var attachedRecyclerView: RecyclerView? = null

    private var onSnapPositionChangeListener: OnSnapPositionChangeListener? = null

    @Throws(IllegalStateException::class)
    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        super.attachToRecyclerView(recyclerView)
        this.attachedRecyclerView = recyclerView
        if (null != this.attachedRecyclerView) {
            this.attachedRecyclerView?.addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            maybeNotifySnapPositionChange(recyclerView, true)
                        }
                    }

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        maybeNotifySnapPositionChange(recyclerView, false)
                    }
                }
            )
        }
    }

    private fun maybeNotifySnapPositionChange(recyclerView: RecyclerView, idle: Boolean) {
        val snapPosition = getSnapPosition(recyclerView)
//        Trace.d("maybeNotifySnapPositionChange snapPosition : " + snapPosition + ", idle = " + idle);
        if (idle) {
            onSnapPositionChangeListener?.onSnapped(snapPosition)
        } else {
            onSnapPositionChangeListener?.onSnapPositionChange(snapPosition)
        }
        this.snapPosition = snapPosition
    }

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray {
        val out = IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            when(snapStyle) {
//                SnapStyle.START_AND_END -> {
//                    val start = distanceToStart(targetView, getHorizontalHelper(layoutManager))
//                    val end = distanceToEnd(targetView, getHorizontalHelper(layoutManager))
//                    Trace.d("LinearSnapExHelper calculateDistanceToFinalSnap start=$start, end=$end")
//                    out[0] = start
//                }
                SnapStyle.START -> {
                    out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager))
                }
                SnapStyle.END -> {
                    out[0] = distanceToEnd(targetView, getHorizontalHelper(layoutManager))
                }
                else -> {
                    out[0] = distanceToCenter(layoutManager, targetView, getHorizontalHelper(layoutManager))
                }
            }
        } /* else {
            out[0] = 0;
        }*/
        if (layoutManager.canScrollVertically()) {
            when(snapStyle) {
//                SnapStyle.START_AND_END -> {
//                    out[1] = distanceToCenter(layoutManager, targetView, getVerticalHelper(layoutManager))
////                    out[1] = distanceToCenter(layoutManager, targetView, getVerticalHelper(layoutManager))
//                }
                SnapStyle.START -> {
                    out[1] = distanceToCenter(layoutManager, targetView, getVerticalHelper(layoutManager))
                }
                SnapStyle.END -> {
                    out[1] = distanceToCenter(layoutManager, targetView, getVerticalHelper(layoutManager))
                }
                else -> { // SnapStyle.CENTER
                    out[1] = distanceToCenter(layoutManager, targetView, getVerticalHelper(layoutManager))
                }
            }
        } /* else {
            out[1] = 0;
        }*/
        return out
    }

    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager, velocityX: Int, velocityY: Int): Int {
        val targetPos: Int = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
        Trace.d("findTargetSnapPosition targetPos = $targetPos")
        return targetPos
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        if (layoutManager.canScrollVertically()) {
            return when(snapStyle) {
//                SnapStyle.START_AND_END -> {
//                    findStartView(layoutManager, getVerticalHelper(layoutManager))
//                }
                SnapStyle.START -> {
                    findStartView(layoutManager, getVerticalHelper(layoutManager))
                }
                SnapStyle.END -> {
                    findEndView(layoutManager, getVerticalHelper(layoutManager))
                }
                else -> { // SnapStyle.CENTER == snapStyle
                    findCenterView(layoutManager, getVerticalHelper(layoutManager))
                }
            }
        } else if (layoutManager.canScrollHorizontally()) {
            return when(snapStyle) {
//                SnapStyle.START_AND_END -> {
//                    findStartView(layoutManager, getHorizontalHelper(layoutManager))
//                }
                SnapStyle.START -> {
                    findStartView(layoutManager, getHorizontalHelper(layoutManager))
                }
                SnapStyle.END -> {
                    findEndView(layoutManager, getHorizontalHelper(layoutManager))
                }
                else -> { // SnapStyle.CENTER == snapStyle
                    findCenterView(layoutManager, getHorizontalHelper(layoutManager))
                }
            }
        }
        return null
    }

    private fun findStartView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper?): View? {
        if (layoutManager is LinearLayoutManager && helper != null) {
            val firstChild: Int = layoutManager.findFirstVisibleItemPosition()
            val isLastItem = layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1
            if (firstChild == RecyclerView.NO_POSITION || isLastItem) {
                return null
            }
            val child: View? = layoutManager.findViewByPosition(firstChild)
            return if (helper.getDecoratedEnd(child) >= helper.getDecoratedMeasurement(child) / 2
                && helper.getDecoratedEnd(child) > 0
            ) {
                child
            } else {
                if (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1) {
                    null
                } else {
                    layoutManager.findViewByPosition(firstChild + 1)
                }
            }
        }
        return null
    }

    private fun findEndView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {
        if (layoutManager is LinearLayoutManager) {
            val lastChild: Int = layoutManager.findLastVisibleItemPosition()
            val isFirstItem = layoutManager.findFirstCompletelyVisibleItemPosition() == 0
            if (lastChild == RecyclerView.NO_POSITION || isFirstItem) {
                return null
            }
            val child: View = layoutManager.findViewByPosition(lastChild) ?: return null
            return if (helper.getDecoratedStart(child) >= helper.getDecoratedMeasurement(child) / 2
                && helper.getDecoratedStart(child) > 0
            ) {
                child
            } else {
                if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    null
                } else {
                    layoutManager.findViewByPosition(lastChild + 1)
                }
            }
        }
        return null
    }

    /**
     * Return the child view that is currently closest to the center of this parent.
     *
     * @param layoutManager The [RecyclerView.LayoutManager] associated with the attached
     * [RecyclerView].
     * @param helper The relevant [OrientationHelper] for the attached [RecyclerView].
     *
     * @return the child view that is currently closest to the center of this parent.
     */
    private fun findCenterView(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper
    ): View? {
        val childCount: Int = layoutManager.childCount
        if (childCount == 0) {
            return null
        }
        var closestChild: View? = null
        val center: Int = if (layoutManager.clipToPadding) {
            helper.startAfterPadding + helper.totalSpace / 2
        } else {
            helper.end / 2
        }
        var absClosest = Int.MAX_VALUE
        for (i in 0 until childCount) {
            val child: View = layoutManager.getChildAt(i) ?: continue
            val childCenter: Int = helper.getDecoratedStart(child) + helper.getDecoratedMeasurement(child) / 2
            val absDistance = kotlin.math.abs(childCenter - center)
            /** if child center is closer than previous closest, set it as closest   */
            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }
        return closestChild
    }

    /**
     * Estimates a position to which SnapHelper will try to scroll to in response to a fling.
     *
     * @param layoutManager The [RecyclerView.LayoutManager] associated with the attached
     * [RecyclerView].
     * @param helper        The [OrientationHelper] that is created from the LayoutManager.
     * @param velocityX     The velocity on the x axis.
     * @param velocityY     The velocity on the y axis.
     *
     * @return The diff between the target scroll position and the current position.
     */
    private fun estimateNextPositionDiffForFling(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper,
        velocityX: Int,
        velocityY: Int
    ): Int {
        val distances: IntArray = calculateScrollDistance(velocityX, velocityY)
        val distancePerChild = computeDistancePerChild(layoutManager, helper)
        if (distancePerChild <= 0) {
            return 0
        }
        val distance = if (kotlin.math.abs(distances[0]) > kotlin.math.abs(distances[1])) distances[0] else distances[1]
        return kotlin.math.round(distance / distancePerChild).toInt()
    }

    /**
     * Computes an average pixel value to pass a single child.
     *
     *
     * Returns a negative value if it cannot be calculated.
     *
     * @param layoutManager The [RecyclerView.LayoutManager] associated with the attached
     * [RecyclerView].
     * @param helper        The relevant [OrientationHelper] for the attached
     * [RecyclerView.LayoutManager].
     *
     * @return A float value that is the average number of pixels needed to scroll by one view in
     * the relevant direction.
     */
    private fun computeDistancePerChild(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper
    ): Float {
        var minPosView: View? = null
        var maxPosView: View? = null
        var minPos = Int.MAX_VALUE
        var maxPos = Int.MIN_VALUE
        val childCount: Int = layoutManager.childCount
        if (childCount == 0) {
            return INVALID_DISTANCE
        }
        for (i in 0 until childCount) {
            val child: View = layoutManager.getChildAt(i) ?: continue
            val pos: Int = layoutManager.getPosition(child)
            if (pos == RecyclerView.NO_POSITION) {
                continue
            }
            if (pos < minPos) {
                minPos = pos
                minPosView = child
            }
            if (pos > maxPos) {
                maxPos = pos
                maxPosView = child
            }
        }
        if (minPosView == null || maxPosView == null) {
            return INVALID_DISTANCE
        }
        val start: Int = kotlin.math.min(
            helper.getDecoratedStart(minPosView),
            helper.getDecoratedStart(maxPosView)
        )
        val end: Int = kotlin.math.max(
            helper.getDecoratedEnd(minPosView),
            helper.getDecoratedEnd(maxPosView)
        )
        val distance = end - start
        return if (distance == 0) {
            INVALID_DISTANCE
        } else 1f * distance / (maxPos - minPos + 1)
    }

    private fun distanceToCenter(layoutManager: RecyclerView.LayoutManager, targetView: View, helper: OrientationHelper): Int {
        val childCenter: Int = helper.getDecoratedStart(targetView) + helper.getDecoratedMeasurement(targetView) / 2
        val containerCenter: Int = if (layoutManager.clipToPadding) {
            helper.startAfterPadding + helper.totalSpace / 2
        } else {
            helper.end / 2
        }
        return childCenter - containerCenter
    }

    private fun distanceToStart(targetView: View, helper: OrientationHelper): Int {
        return helper.getDecoratedStart(targetView) - helper.startAfterPadding
    }

    private fun distanceToEnd(targetView: View, helper: OrientationHelper): Int {
        return helper.getDecoratedEnd(targetView) - helper.endAfterPadding
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        val verticalHelper = this.verticalHelper ?: OrientationHelper.createVerticalHelper(layoutManager)
        this.verticalHelper = verticalHelper
        return verticalHelper
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        val horizontalHelper = this.horizontalHelper ?: OrientationHelper.createHorizontalHelper(layoutManager)
        this.horizontalHelper = horizontalHelper
        return horizontalHelper
    }

    fun setOnSnapPositionChangeListener(listener: OnSnapPositionChangeListener?) {
        onSnapPositionChangeListener = listener
    }

    fun getSnapPosition(): Int {
        return if (snapPosition == RecyclerView.NO_POSITION && null != attachedRecyclerView) {
            getSnapPosition(attachedRecyclerView)
        } else snapPosition
    }

    private fun getSnapPosition(recyclerView: RecyclerView?): Int {
        if (null == recyclerView) {
            return RecyclerView.NO_POSITION
        }
        val layoutManager: RecyclerView.LayoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
        val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        return layoutManager.getPosition(snapView)
    }

//    fun scrollToSnapPosition(position: Int, force: Boolean) {
//        val recyclerView = attachedRecyclerView ?: return
//        val adapter = recyclerView.adapter ?: return
//
//        val layoutManager: RecyclerView.LayoutManager? = recyclerView.layoutManager
//        if (null != layoutManager) {
//            val snapViewPosition = findTargetSnapPosition(layoutManager, recyclerView.computeHorizontalScrollOffset(), recyclerView.computeVerticalScrollOffset())
//            if (snapViewPosition != position || force) {
//                val itemCount: Int = adapter.itemCount
//                layoutManager.scrollToPosition(position)
//
//                var unsnappedPosition: Int = RecyclerView.NO_POSITION
//                if (this.snapPosition >= 0) {
//                    unsnappedPosition = this.snapPosition
//                }
//                this.snapPosition = position
////                notifySnapped(unsnappedPosition, this.snappedPosition, itemCount)
//            }
//        }
//    }
}