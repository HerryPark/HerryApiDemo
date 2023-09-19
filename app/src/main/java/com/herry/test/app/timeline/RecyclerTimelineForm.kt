package com.herry.test.app.timeline

import android.content.Context
import android.view.View
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder

class RecyclerTimelineForm: NodeForm<RecyclerTimelineForm.Holder, TimelineModel>(Holder::class, TimelineModel::class) {
    inner class Holder(context: Context, view: View): NodeHolder(context, view) {
    }

    override fun onLayout(): Int {
        TODO("Not yet implemented")
    }

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

    override fun onBindModel(context: Context, holder: Holder, model: TimelineModel) {
        TODO("Not yet implemented")
    }
}

enum class TrackType {
    PRIMARY,
    SECONDARY
}

data class TrackModel(
    val type: TrackType,
    val items: MutableList<TrackItemModel> = mutableListOf()
)

enum class TrackItemType {
    SPACE,
    VIDEO,
    IMAGE,
    AUDIO,
    TRANSITION,
    TEXT,
    ASSET,
    HANDWRITE
}

data class TrackItemModel(
    val type: TrackItemType,
    val start: Long,
    val end: Long,
    val label: String = type.name
) {
    val duration: Long = end - start
}

data class TimelineModel(
    val tracks: MutableList<TrackModel>
)