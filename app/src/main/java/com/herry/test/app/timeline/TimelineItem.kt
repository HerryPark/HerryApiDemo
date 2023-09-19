package com.herry.test.app.timeline

import androidx.annotation.IntRange

open class TimelineItem(
    val type: TimelineItemType,
) {
    @IntRange(from = 0, to = Long.MAX_VALUE)
    var startTime: Long = 0L

    @IntRange(from = 0, to = Long.MAX_VALUE)
    var endTime: Long = 0L
        set(value) {
            if (value < startTime) {
                field = startTime
                startTime = value
            } else {
                field = value
            }
        }

    var duration: Long
        set(value) {
            if (value > 0) {
                endTime = startTime + value
            }
        }
        get() =  endTime + startTime
}

class TimelineClipItem : TimelineItem (type = TimelineItemType.CLIP)

class TimelineLayerItem : TimelineItem (type = TimelineItemType.LAYER)

enum class TimelineItemType {
    CLIP,
    LAYER
}
