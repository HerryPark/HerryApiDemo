package com.herry.test.app.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.herry.libs.helper.ToastHelper
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.util.ViewUtil.getColorDrawable
import com.herry.libs.widget.view.dialog.AppDialog
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.widget.Popup
import com.herry.test.widget.TitleBarForm


/**
 * Created by herry.park on 2020/06/11.
 **/
class AppDialogListFragment : BaseNavView<AppDialogListContract.View, AppDialogListContract.Presenter>(), AppDialogListContract.View {

    override fun onCreatePresenter(): AppDialogListContract.Presenter = AppDialogListPresenter()

    override fun onCreatePresenterView(): AppDialogListContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.app_dialog_list_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        view ?: return

        TitleBarForm(
            activity = { requireActivity() }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.app_dialog_list_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Test List"))
        }

        view.findViewById<RecyclerView>(R.id.app_dialog_list_fragment_list)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            adapter = this@AppDialogListFragment.adapter
        }
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(TestItemForm())
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onScreen(type: AppDialogListContract.TestItemType) {
        val context = this.context ?: return

        when (type) {
            AppDialogListContract.TestItemType.TITLE_MESSAGE_BUTTON_1 -> {
                AlertDialog.Builder(requireActivity()).apply {
                    setTitle("title")
                    setMessage("message\n\nfejailfejlaef\n\nfjeilafje\n" +
                            "\n" +
                            "fjeilafje\n" +
                            "\n" +
                            "fjeilafje\n" +
                            "\n" +
                            "fjeilafje\n" +
                            "\n" +
                            "fjeilafje\n" +
                            "\n" +
                            "fjeilafje")
                    setPositiveButton("positive") { dialog, _ ->
                        dialog.dismiss()
                        ToastHelper.showToast(requireActivity(), "OK")
                    }
                    setNegativeButton("negative") { dialog, _ -> dialog.dismiss() }
                    setNeutralButton("neutral") { dialog, _ -> dialog.dismiss() }
                }.show()
            }
            AppDialogListContract.TestItemType.TITLE_MESSAGE_BUTTON_2 -> {
                Popup(requireActivity()).apply {
                    setTitle("title")
                    setMessage("message1234567890\n12345678901234567890123456789012345678901234567890123456789012345678901234567890\n" +
                            "1234567890\n" +
                            "1234567890\n" +
                            "1234567890\n" +
                            "1234567890\n" +
                            "1234567890\n" +
                            "1234567890\n" +
                            "1234567890\n" +
                            "1234567890\n" +
                            "1234567890\n" +
                            "1234567890\n" +
                            "1234567890\n" +
                            "1234567890\n" +
                            "1234567890\n" +
                            "1234567890\n" +
                            "1234567890\n" +
                            "1234567890\n" +
                            "1234567890")
                    setPositiveButton("button3")
                    setNegativeButton("button1")
                }.show()
            }
            AppDialogListContract.TestItemType.TITLE_MESSAGE_BUTTON_3 -> {
                Popup(requireActivity()).apply {
                    setTitle("title")
                    setMessage("message")
                    setNegativeButton("button1")
                    setNeutralButton("button2")
                    setPositiveButton("button3")
                }.show()
            }
            AppDialogListContract.TestItemType.TITLE_MESSAGE_CLICKS_BUTTONS -> {
                Popup(requireActivity()).apply {
                    setTitle("title")
                    setMessage("button is long clickable")
                    setPositiveButton("ok", listener = object : AppDialog.OnClicksListener {
                        override fun onLongClick(dialog: DialogInterface, which: Int) : Boolean {
                            ToastHelper.showToast(activity, "positive button long click")
                            return true
                        }

                        override fun onClick(dialog: DialogInterface, which: Int) {
                            dialog.dismiss()
                        }
                    })
                    setNegativeButton("cancel", listener = object : AppDialog.OnClicksListener {
                        override fun onLongClick(dialog: DialogInterface, which: Int) : Boolean {
                            ToastHelper.showToast(activity, "negative button long click")
                            return true
                        }

                        override fun onClick(dialog: DialogInterface, which: Int) {
                            dialog.dismiss()
                        }
                    })
                    setNeutralButton("center", listener = object : AppDialog.OnClicksListener {
                        override fun onLongClick(dialog: DialogInterface, which: Int) : Boolean {
                            ToastHelper.showToast(activity, "neutral button long click")
                            return true
                        }

                        override fun onClick(dialog: DialogInterface, which: Int) {
                            dialog.dismiss()
                        }
                    })
                }.show()
            }
            AppDialogListContract.TestItemType.TITLE_MESSAGE_SUB_MESSAGE_BUTTON_1 -> {
                Popup(requireActivity()).apply {
                    setTitle("title")
                    setMessage("message")
                    setSubMessage("sub-message")
                    setPositiveButton("ok")
                }.show()
            }
            AppDialogListContract.TestItemType.TITLE_LIST_BUTTON_2 -> {
                Popup(requireActivity()).apply {
                    setTitle("title")
                    setMultiChoiceItems(arrayOf("1", "2"), arrayOf(false, true)) { _, witch, checked ->
                        ToastHelper.showToast(requireActivity(), "$witch is checked = $checked")
                    }
                    setNegativeButton("button1")
                    setPositiveButton("button3")
                }.show()
            }
            AppDialogListContract.TestItemType.MESSAGE_BUTTON_3 -> {
                Popup(requireActivity()).apply {
                    setMessage("message")
                    setNegativeButton("button1") { dialog, _ ->
                        dialog.dismiss()
                        ToastHelper.showToast(activity, "button1")
                    }
                    setNeutralButton("button2") { dialog, _ ->
                        dialog.dismiss()
                        ToastHelper.showToast(activity, "button2")
                    }
                    setPositiveButton("button3") { dialog, _ ->
                        dialog.dismiss()
                        ToastHelper.showToast(activity, "button3")
                    }
                }.show()
            }
            AppDialogListContract.TestItemType.TITLE_VIEW -> {
                Popup(requireActivity()).apply {
                    setTitle("title")
                    setView(TextView(requireContext()).apply {
                        minimumHeight = 100
                        text = "set view"
                        setBackgroundColor(Color.argb(0xff, 0xff, 0, 0))
                    })
                }.show()
            }
            AppDialogListContract.TestItemType.VIEW -> {
                Popup(requireActivity()).apply {
                    setView(TextView(requireContext()).apply {
                        layoutParams = ViewGroup.LayoutParams(500, ViewGroup.LayoutParams.WRAP_CONTENT)
                        minimumHeight = 100
                        text = "set view"
                        setBackgroundColor(Color.argb(0xff, 0xff, 0, 0))
                    })
                }.show()
            }
            AppDialogListContract.TestItemType.VIEW_BUTTON_1 -> {
                Popup(requireActivity()).apply {
                    setTitle("title")
                    setView(TextView(requireContext()).apply {
                        minimumHeight = 100
                        text = "set viewafeijalfj\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n" +
                                "\n" +
                                "\n" +
                                "\n" +
                                "\n" +
                                "\n" +
                                "\n\n" +
                                "\n" +
                                "\n" +
                                "\n" +
                                "\n" +
                                "\n" +
                                "\n\n" +
                                "\n" +
                                "\n" +
                                "\n" +
                                "\n" +
                                "\n" +
                                "\n\n\n\n\n\n\n\n\n\n\neialjfleaj"
                        setBackgroundColor(Color.argb(0xff, 0xff, 0, 0))
                    })
                    setPositiveButton("button3") { dialog, _ ->
                        dialog.dismiss()
                    }
                }.show()
            }
            AppDialogListContract.TestItemType.CUSTOM_VIEW -> {
                Popup(requireActivity()).apply {
                    setCustomView(TextView(requireContext()).apply {
                        minimumHeight = 400
                        text = "custom view"
                        setBackgroundColor(Color.argb(0xff, 0xff, 0xff, 0))
                    })
                }.show()

            }
            AppDialogListContract.TestItemType.RESIZE_DIALOG -> {
                Popup(requireActivity()).apply {
                    setCustomView(TextView(requireContext()).apply {
                        gravity = Gravity.CENTER
                        text = "full size custom view"
//                        setBackgroundColor(Color.argb(0xff, 0xff, 0xff, 0))
                    })
                    setBackgroundDrawable(getColorDrawable(context, android.R.color.transparent))
                    setDialogSize(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, Gravity.TOP or Gravity.CENTER_HORIZONTAL)
                    setDialogDimAmount(0f)
                }.show()
            }
            AppDialogListContract.TestItemType.VERTICAL_BUTTONS -> {
                Popup(requireActivity()).apply {
                    setTitle("title")
                    setMessage("message")
                    setNegativeButton("negative") { dialog, which ->
                        dialog.dismiss()
                        ToastHelper.showToast(activity, "$which")
                    }
                    setNeutralButton("neutral") { dialog, which ->
                        dialog.dismiss()
                        ToastHelper.showToast(activity, "$which")
                    }
                    setPositiveButton("positive") { dialog, which ->
                        dialog.dismiss()
                        ToastHelper.showToast(activity, "$which")
                    }
                    setButtonOrientation(AppDialog.VERTICAL)
                }.show()
            }
        }
    }

    private inner class TestItemForm : NodeForm<TestItemForm.Holder, AppDialogListContract.TestItemType>(Holder::class, AppDialogListContract.TestItemType::class) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val title: TextView? = view.findViewById(R.id.main_test_item_title)
            init {
                view.setOnClickListener {
                    NodeRecyclerForm.getBindModel(this@TestItemForm, this@Holder)?.let {
                        presenter?.moveToScreen(it)
                    }
                }
            }
        }

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onLayout(): Int = R.layout.main_test_item

        override fun onBindModel(context: Context, holder: TestItemForm.Holder, model: AppDialogListContract.TestItemType) {
            holder.title?.text = when (model) {
                AppDialogListContract.TestItemType.TITLE_MESSAGE_BUTTON_1 -> "TITLE_MESSAGE_BUTTON_1"
                AppDialogListContract.TestItemType.TITLE_MESSAGE_BUTTON_2 -> "TITLE_MESSAGE_BUTTON_2"
                AppDialogListContract.TestItemType.TITLE_MESSAGE_BUTTON_3 -> "TITLE_MESSAGE_BUTTON_3"
                AppDialogListContract.TestItemType.TITLE_MESSAGE_CLICKS_BUTTONS -> "TITLE_MESSAGE_CLICKS_BUTTONS"
                AppDialogListContract.TestItemType.TITLE_MESSAGE_SUB_MESSAGE_BUTTON_1 -> "TITLE_MESSAGE_SUB_MESSAGE_BUTTON_1"
                AppDialogListContract.TestItemType.TITLE_LIST_BUTTON_2 -> "TITLE_LIST_BUTTON_2"
                AppDialogListContract.TestItemType.MESSAGE_BUTTON_3 -> "MESSAGE_BUTTON_3"
                AppDialogListContract.TestItemType.TITLE_VIEW -> "TITLE_VIEW"
                AppDialogListContract.TestItemType.VIEW -> "VIEW"
                AppDialogListContract.TestItemType.VIEW_BUTTON_1 -> "VIEW_BUTTON_1"
                AppDialogListContract.TestItemType.CUSTOM_VIEW -> "CUSTOM_VIEW"
                AppDialogListContract.TestItemType.RESIZE_DIALOG -> "RESIZE_DIALOG"
                AppDialogListContract.TestItemType.VERTICAL_BUTTONS -> "VERTICAL_BUTTONS"
            }
        }
    }
}