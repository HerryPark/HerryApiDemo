package com.herry.test.widget

import android.content.Context
import android.view.View
import android.widget.TextView
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

    enum class PermissionType {
        PARTIAL,
        FULL,
        DENIED
    }

    data class Model(
        val currentType: MediaType,
        val permissionType: PermissionType
    )

    inner class Holder(context: Context, view: View): NodeHolder(context, view) {
        val images: View? = view.findViewById(R.id.media_selection_form_images)
        val videos: View? = view.findViewById(R.id.media_selection_form_videos)
        val visualAll: View? = view.findViewById(R.id.media_selection_form_visual_all)
        val audios: View? = view.findViewById(R.id.media_selection_form_audios)
        val mediaAll: View? = view.findViewById(R.id.media_selection_form_all)

        val permission: TextView? = view.findViewById(R.id.media_selection_form_permission)

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

    override fun onBindModel(context: Context, holder: Holder, model: Model) {
        holder.images?.isSelected = model.currentType == MediaType.IMAGES_ONLY
        holder.videos?.isSelected = model.currentType == MediaType.VIDEOS_ONLY
        holder.visualAll?.isSelected = model.currentType == MediaType.VISUAL_ALL
        holder.audios?.isSelected = model.currentType == MediaType.AUDIOS_ONLY
        holder.mediaAll?.isSelected = model.currentType == MediaType.MEDIA_ALL

        holder.permission?.text = model.permissionType.name
    }
}