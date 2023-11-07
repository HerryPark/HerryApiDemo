package com.herry.test.app.dialog.fullscreendialog

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.setOnProtectClickListener
import com.herry.libs.widget.view.dialog.AppDialog
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavFragment
import com.herry.test.widget.Popup
import com.herry.test.widget.TitleBarForm

/**
 *
 *     <dialog
 *         android:id="@+id/full_screen_dialog_fragment"
 *         android:name="com.herry.test.app.dialog.fullscreendialog.FullScreenDialogFragment"
 *         tools:layout="@layout/full_screen_dialog_fragment" />
 *
 */
class FullScreenDialogFragment : BaseNavFragment() {

    private var container: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Popup(activity).apply {
            val context = this.getContext()

            val container = container ?: View.inflate(context, R.layout.full_screen_dialog_fragment, null).also {
                this@FullScreenDialogFragment.container = it
                init(it)
            }
            setCustomView(container)
            setOnBackPressedListener(object: AppDialog.OnBackPressedListener {
                override fun onBackPressed(): Boolean {
                    this@FullScreenDialogFragment.cancel()
                    return true
                }
            })
            setBackgroundDrawable(ViewUtil.getColorDrawable(context, android.R.color.transparent))
            setDialogSize(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
            setCancelable(true)
        }.getDialog() ?: Dialog(requireActivity())
    }

    private fun init(view: View?) {
        val context = view?.context ?: return

        TitleBarForm(
            activity = { requireActivity() },
            onClickBack = {
                cancel()
            }
        ).apply {
            bindFormHolder(context, view.findViewById(R.id.full_screen_dialog_fragment_title))
            bindFormModel(context, TitleBarForm.Model(title = "Full Screen Dialog (Fragment)", backEnable = true))
        }

        view.findViewById<View?>(R.id.bottom_sheet_dialog_fragment_cancel)?.setOnProtectClickListener {
            cancel()
        }

        view.findViewById<View?>(R.id.bottom_sheet_dialog_fragment_ok)?.setOnProtectClickListener {
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