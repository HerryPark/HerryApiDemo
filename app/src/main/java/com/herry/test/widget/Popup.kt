package com.herry.test.widget

import android.content.Context
import androidx.annotation.StyleRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.herry.libs.widget.view.dialog.AppDialog
import com.herry.test.R

@Suppress("unused")
class Popup(
    context: Context?,
    @StyleRes style: Int = R.style.PopupTheme_AppDialog,
    @StyleRes theme: Int = 0
) : AppDialog(context, style, theme) {
    private var owner: LifecycleOwner? = null

    private var keepOnPause: Boolean = true

    private val popupObserver by lazy { PopupObserver(this) }

    override fun onDismissed() {
        this.owner?.lifecycle?.removeObserver(popupObserver)
    }

    fun setKeepOnPause(owner: LifecycleOwner, keep: Boolean) {
        this.owner = owner
        owner.lifecycle.addObserver(popupObserver)

        this.keepOnPause = keep
    }

    private class PopupObserver(val dialog: Popup) : DefaultLifecycleObserver {
        override fun onPause(owner: LifecycleOwner) {
            if (dialog.isShowing() && !dialog.keepOnPause) {
                dialog.dismiss()
            }
            super.onPause(owner)
        }
    }
}