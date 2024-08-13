package com.herry.test.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.test.R

open class MediaSelectionForm(
    private val onClickMediaType: (type: MediaType) -> Unit
) : NodeForm<MediaSelectionForm.Holder, MediaSelectionForm.Model>(Holder::class, Model::class) {
    enum class MediaType {
        IMAGES_ONLY,
        VIDEOS_ONLY,
        VISUAL_ALL,
        AUDIOS_ONLY,
        MEDIA_ALL
    }

    data class Model(
        val currentType: MediaType
    )

    inner class Holder(context: Context, view: View): NodeHolder(context, view) {
        val images: View? = view.findViewById(R.id.media_selection_form_images)
        val videos: View? = view.findViewById(R.id.media_selection_form_videos)
        val visualAll: View? = view.findViewById(R.id.media_selection_form_visual_all)
        val audios: View? = view.findViewById(R.id.media_selection_form_audios)
        val mediaAll: View? = view.findViewById(R.id.media_selection_form_all)

        init {
            images?.setOnSingleClickListener {
                onClickMediaType(MediaType.IMAGES_ONLY)
            }

            videos?.setOnSingleClickListener {
                onClickMediaType(MediaType.VIDEOS_ONLY)
            }

            visualAll?.setOnSingleClickListener {
                onClickMediaType(MediaType.VISUAL_ALL)
            }

            audios?.setOnSingleClickListener {
                onClickMediaType(MediaType.AUDIOS_ONLY)
            }

            mediaAll?.setOnSingleClickListener {
                onClickMediaType(MediaType.MEDIA_ALL)
            }
        }
    }

    override fun onLayout(): Int = R.layout.media_selection_form

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

    @SuppressLint("SetTextI18n")
    override fun onBindModel(context: Context, holder: Holder, model: Model) {
        holder.images?.isSelected = model.currentType == MediaType.IMAGES_ONLY
        holder.videos?.isSelected = model.currentType == MediaType.VIDEOS_ONLY
        holder.visualAll?.isSelected = model.currentType == MediaType.VISUAL_ALL
        holder.audios?.isSelected = model.currentType == MediaType.AUDIOS_ONLY
        holder.mediaAll?.isSelected = model.currentType == MediaType.MEDIA_ALL
    }
}