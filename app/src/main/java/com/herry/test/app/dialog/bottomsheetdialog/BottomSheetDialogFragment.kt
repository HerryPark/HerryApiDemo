package com.herry.test.app.dialog.bottomsheetdialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavFragment

/**
 *
 *     <dialog
 *         android:id="@+id/full_screen_dialog_fragment"
 *         android:name="com.herry.test.app.dialog.fullscreendialog.FullScreenDialogFragment"
 *         tools:layout="@layout/full_screen_dialog_fragment" />
 *
 */
class BottomSheetDialogFragment : BaseNavFragment() {

    private var container: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = this.activity ?: return super.onCreateDialog(savedInstanceState)
        // creates to BottomSheetDialog
        return BottomSheetDialog(activity, R.style.AppTheme_BottomSheetDialog).apply {
            dismissWithAnimation = true
            setCanceledOnTouchOutside(true)
            setOnShowListener {
                val bottomSheet = this.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

                if (bottomSheet != null) {
                    BottomSheetBehavior.from(bottomSheet).apply {
                        state = BottomSheetBehavior.STATE_EXPANDED
                        skipCollapsed = true
                        isHideable = true
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.bottom_sheet_dialog_fragment, container, false)
            init(this.container)
        } else {
            // fixed: "java.lang.IllegalStateException: The specified child already has a parent.
            // You must call removeView() on the child's parent first."
            ViewUtil.removeViewFormParent(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        val context = view?.context ?: return

        view.findViewById<View?>(R.id.bottom_sheet_dialog_fragment_close)?.setOnSingleClickListener {
            cancel()
        }

        view.findViewById<View?>(R.id.bottom_sheet_dialog_fragment_cancel)?.setOnSingleClickListener {
            cancel()
        }

        view.findViewById<View?>(R.id.bottom_sheet_dialog_fragment_ok)?.setOnSingleClickListener {
            ok()
        }
    }

    private fun cancel() {
        navigateUp(false)
    }

    private fun ok() {
        navigateUp(true)
    }
}