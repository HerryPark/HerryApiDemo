package com.herry.test.app.dialog.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.herry.libs.app.nav.NavBundleUtil
import com.herry.libs.helper.ToastHelper
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.widget.extension.launchWhenViewResumed
import com.herry.libs.widget.extension.navigateTo
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.libs.widget.view.dialog.BottomSheetListDialog
import com.herry.libs.widget.view.dialog.BottomSheetListSingleChoiceItemForm
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.widget.TitleBarForm

class DialogListFragment : BaseNavView<DialogListContract.View, DialogListContract.Presenter>(), DialogListContract.View {

    override fun onCreatePresenter(): DialogListContract.Presenter = DialogListPresenter()

    override fun onCreatePresenterView(): DialogListContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    private var bottomSheetListDialog: BottomSheetListDialog? = null

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(ListItemForm(
                onClickItem = { form, holder ->
                    NodeRecyclerForm.getBindModel(form, holder)?.let { model ->
                        presenter?.moveToScreen(model)
                    }
                }
            ))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.list_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        val context = view?.context ?: return

        TitleBarForm(
            activity = { requireActivity() }
        ).apply {
            bindFormHolder(context, view.findViewById(R.id.list_fragment_title))
            bindFormModel(context, TitleBarForm.Model(title = "Dialog List", backEnable = true))
        }

        view.findViewById<RecyclerView>(R.id.list_fragment_list)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }

            adapter = this@DialogListFragment.adapter
        }
    }

    override fun onScreen(type: DialogListContract.ItemType) {
        when (type) {
            DialogListContract.ItemType.APP_DIALOGS -> {
                navigateTo(destinationId = R.id.app_dialog_list_fragment)
            }
            DialogListContract.ItemType.BOTTOM_SHEET_LIST_DIALOG -> {
                showBottomSheetListDialog()
            }
            DialogListContract.ItemType.BOTTOM_SHEET_SCREEN_DIALOG -> {
                navigateTo(destinationId = R.id.bottom_sheet_dialog_fragment)
            }
            DialogListContract.ItemType.FULL_SCREEN_DIALOG -> {
                navigateTo(destinationId = R.id.full_screen_dialog_fragment)
            }
        }
    }

    private enum class BottomSheetListItem {
        ENABLED,
        SELECTED,
        DISABLED
    }

    private fun showBottomSheetListDialog() {
        if (bottomSheetListDialog?.isShowing() == true) return

        val dialog = bottomSheetListDialog ?: (BottomSheetListDialog(
            activity = activity,
            themeResId = R.style.AppTheme_BottomSheetDialog_HideStatusBar,
            bindForms = mutableListOf(
                BottomSheetListSingleChoiceItemForm(
                    onClick = { model ->
                        ToastHelper.showToast(activity, "choose ${model.data}")
                        bottomSheetListDialog?.dismiss() // hide
                    }
                )
            )
        ).apply {
            setItems(mutableListOf<BottomSheetListSingleChoiceItemForm.Model>().apply {
                BottomSheetListItem.values().forEach { model ->
                    this.add(BottomSheetListSingleChoiceItemForm.Model(
                        text = model.name,
                        subText = model.name,
                        selected = model == BottomSheetListItem.SELECTED,
                        enabled = model != BottomSheetListItem.DISABLED,
                        data = model
                    ))
                }
            })
        }).also { this@DialogListFragment.bottomSheetListDialog = it }

        dialog.show()
    }

    override fun onNavigateUpResult(fromNavigationId: Int, result: Bundle) {
        if (fromNavigationId == R.id.full_screen_dialog_fragment
            || fromNavigationId == R.id.bottom_sheet_dialog_fragment) {
            launchWhenViewResumed {
                if (NavBundleUtil.isNavigationResultOk(result)) {
                    ToastHelper.showToast(activity, "with OK")
                } else {
                    ToastHelper.showToast(activity, "with Cancel")
                }
            }
        }
    }

    private inner class ListItemForm(
        private val onClickItem: (form: ListItemForm, holder: ListItemForm.Holder) -> Unit
    ) : NodeForm<ListItemForm.Holder, DialogListContract.ItemType>(
        Holder::class, DialogListContract.ItemType::class
    ) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val title: TextView? = view.findViewById(R.id.list_item_name)

            init {
                view.setOnSingleClickListener {
                    onClickItem(this@ListItemForm, this@Holder)
                }
            }
        }

        override fun onLayout(): Int = R.layout.list_item

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onBindModel(context: Context, holder: Holder, model: DialogListContract.ItemType) {
            holder.title?.text = model.name
        }
    }
}