package com.herry.test.widget

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.permission.PermissionHelper
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.test.R

class MediaAccessPermissionNoticeForm(
    val onClickSelection: () -> Unit,
    val onClickSetting: () -> Unit,
    val onClickDismiss: () -> Unit
): NodeForm<MediaAccessPermissionNoticeForm.Holder, MediaAccessPermissionNoticeModel>(Holder::class, MediaAccessPermissionNoticeModel::class) {
    inner class Holder(context: Context, view: View): NodeHolder(context, view) {
        val message: TextView? = view.findViewById(R.id.media_access_permission_notice_message)
        val selection: View? = view.findViewById(R.id.media_access_permission_notice_selection)
        val setting: View? = view.findViewById(R.id.media_access_permission_notice_setting)
        val dismiss: View? = view.findViewById(R.id.media_access_permission_notice_dismiss)
        private val noticeContainer: ConstraintLayout? = view.findViewById(R.id.media_access_permission_notice_container)
        private val noticeButtons: View? = view.findViewById(R.id.media_access_permission_notice_buttons)

        init {
            setViewLayouts(context, this.noticeContainer)

            selection?.setOnSingleClickListener {
                onClickSelection()
            }

            setting?.setOnSingleClickListener {
                onClickSetting()
            }

            dismiss?.setOnSingleClickListener {
                onClickDismiss()
            }
        }

        private fun setViewLayouts(context: Context, noticeConstraintLayout: ConstraintLayout?) {
            noticeConstraintLayout ?: return
            val messageView = message
            val buttonsView = noticeButtons
            if (messageView != null && buttonsView != null) {
                val constraintSet = ConstraintSet()
                constraintSet.clone(noticeContainer)
                if (ViewUtil.getScreenWidth(context) > ViewUtil.convertDpToPx(560f).toInt()) {
                    // horizontal
                    constraintSet.connect(messageView.id, ConstraintSet.END, buttonsView.id, ConstraintSet.START)
                    constraintSet.connect(messageView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                    constraintSet.connect(buttonsView.id, ConstraintSet.START, messageView.id, ConstraintSet.END)
                    constraintSet.connect(buttonsView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                    constraintSet.setMargin(buttonsView.id, ConstraintSet.TOP, 0)
                } else {
                    // vertical
                    constraintSet.connect(messageView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                    constraintSet.connect(messageView.id, ConstraintSet.BOTTOM, buttonsView.id, ConstraintSet.TOP)

                    constraintSet.clear(buttonsView.id, ConstraintSet.START)
                    constraintSet.connect(buttonsView.id, ConstraintSet.TOP, messageView.id, ConstraintSet.BOTTOM)
                    constraintSet.setMargin(buttonsView.id, ConstraintSet.TOP, ViewUtil.convertDpToPx(16f).toInt())
                }
                constraintSet.applyTo(noticeConstraintLayout)
            }
        }
    }

    override fun onLayout(): Int = R.layout.media_access_permission_notice

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

    override fun onBindModel(context: Context, holder: Holder, model: MediaAccessPermissionNoticeModel) {
        val access = model.access
        holder.message?.text = when (model.type) {
            MediaAccessPermissionNoticeType.VISUAL_MEDIA -> when (access) {
                PermissionHelper.Access.FULL -> ""
                PermissionHelper.Access.PARTIAL -> "Select photos and videos you allow to access"
                PermissionHelper.Access.DENIED -> "Access to music and audio is required"
            }
            MediaAccessPermissionNoticeType.AUDIO -> when (access) {
                PermissionHelper.Access.FULL,
                PermissionHelper.Access.PARTIAL -> ""
                PermissionHelper.Access.DENIED -> "Access to music and audio is required"
            }
        }

        when (access) {
            PermissionHelper.Access.FULL -> {
                holder.setting?.isVisible = false
                holder.selection?.isVisible = false
                holder.dismiss?.isVisible = true
            }
            PermissionHelper.Access.PARTIAL -> {
                holder.setting?.isVisible = true
                holder.selection?.isVisible = true
                holder.dismiss?.isVisible = true
            }
            PermissionHelper.Access.DENIED -> {
                holder.setting?.isVisible = true
                holder.selection?.isVisible = false
                holder.dismiss?.isVisible = true
            }
        }
    }
}

enum class MediaAccessPermissionNoticeType {
    VISUAL_MEDIA,
    AUDIO
}

data class MediaAccessPermissionNoticeModel(
    val type: MediaAccessPermissionNoticeType,
    val access: PermissionHelper.Access
)