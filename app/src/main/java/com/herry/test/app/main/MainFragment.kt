package com.herry.test.app.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.herry.libs.app.activity_caller.module.ACNavigation
import com.herry.libs.app.activity_caller.module.ACPermission
import com.herry.libs.log.Trace
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.widget.extension.navigateTo
import com.herry.libs.widget.extension.setOnProtectClickListener
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.app.nbnf.NBNFActivity
import com.herry.test.app.sample.SampleActivity
import com.herry.test.app.nestedfragments.NestedNavFragmentsActivity
import com.herry.test.widget.TitleBarForm


/**
 * Created by herry.park on 2020/06/11.
 **/
class MainFragment : BaseNavView<MainContract.View, MainContract.Presenter>(), MainContract.View {

    override fun onCreatePresenter(): MainContract.Presenter = MainPresenter()

    override fun onCreatePresenterView(): MainContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.main_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        view ?: return

        TitleBarForm(activity = requireActivity()).apply {
            bindFormHolder(view.context, view.findViewById(R.id.main_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Test List"))
        }

        view.findViewById<RecyclerView>(R.id.main_fragment_list)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            adapter = this@MainFragment.adapter
        }
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(TestItemForm())
        }
    }

    override fun onScreen(type: MainContract.TestItemType) {
        when (type) {
            MainContract.TestItemType.SCHEME_TEST -> {
                navigateTo(destinationId = R.id.intent_list_fragment)
            }
            MainContract.TestItemType.GIF_DECODER -> {
                activityCaller?.call(
                    ACPermission.Caller(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        onGranted = {
                            Handler(Looper.getMainLooper()).post {
                                navigateTo(destinationId = R.id.gif_list_fragment)
                            }
                        }
                    ))
            }
            MainContract.TestItemType.CHECKER_LIST -> {
                navigateTo(destinationId = R.id.data_checker_main_fragment)
            }
            MainContract.TestItemType.LAYOUT_SAMPLE -> {
                navigateTo(destinationId = R.id.layout_sample_fragment)
            }
            MainContract.TestItemType.PICK -> {
                navigateTo(destinationId = R.id.pick_list_fragment)
            }
            MainContract.TestItemType.NESTED_FRAGMENTS -> {
                //.navigateTo(destinationId = R.id.nested_nav_fragments_navigation)
                activityCaller?.call(
                    ACNavigation.IntentCaller(
                        Intent(requireActivity(), NestedNavFragmentsActivity::class.java), onResult = { result ->
                            if (result.resultCode == Activity.RESULT_OK) {
                                Trace.d("Herry", "result = OK")
                            }
                        }
                    ))
            }
            MainContract.TestItemType.NESTED_BOTTOM_NAV_FRAGMENTS -> {
                activityCaller?.call(ACNavigation.IntentCaller(Intent(requireActivity(), NBNFActivity::class.java)))
            }
            MainContract.TestItemType.APP_DIALOG -> {
                navigateTo(destinationId = R.id.app_dialog_list_fragment)
            }
            MainContract.TestItemType.LIST -> {
                navigateTo(destinationId = R.id.list_fragment)
            }
            MainContract.TestItemType.SKELETON -> {
                navigateTo(destinationId = R.id.skeleton_fragment)
            }
            MainContract.TestItemType.RESIZING_UI -> {
                navigateTo(destinationId = R.id.resizing_ui_fragment)
            }
            MainContract.TestItemType.SAMPLE_APP -> {
                activityCaller?.call(ACNavigation.IntentCaller(Intent(requireActivity(), SampleActivity::class.java)))
            }
        }
    }

    private inner class TestItemForm : NodeForm<TestItemForm.Holder, MainContract.TestItemType>(Holder::class, MainContract.TestItemType::class) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val title: TextView? = view.findViewById(R.id.main_test_item_title)
            init {
                view.setOnProtectClickListener {
                    NodeRecyclerForm.getBindModel(this@TestItemForm, this@Holder)?.let {
                        presenter?.moveToScreen(it)
                    }
                }
            }
        }

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onLayout(): Int = R.layout.main_test_item

        override fun onBindModel(context: Context, holder: TestItemForm.Holder, model: MainContract.TestItemType) {
            holder.title?.text = when (model) {
                MainContract.TestItemType.SCHEME_TEST -> "Intent"
                MainContract.TestItemType.GIF_DECODER -> "GIF Decoder"
                MainContract.TestItemType.CHECKER_LIST -> "Data Checker"
                MainContract.TestItemType.LAYOUT_SAMPLE -> "Layout Sample"
                MainContract.TestItemType.PICK -> "Pick"
                MainContract.TestItemType.NESTED_FRAGMENTS -> "Nested Fragments"
                MainContract.TestItemType.NESTED_BOTTOM_NAV_FRAGMENTS -> "Nested Bottom Navigator Fragments"
                MainContract.TestItemType.APP_DIALOG -> "App Dialog"
                MainContract.TestItemType.LIST -> "List"
                MainContract.TestItemType.SKELETON -> "Skeleton"
                MainContract.TestItemType.RESIZING_UI -> "Resizing UI"
                MainContract.TestItemType.SAMPLE_APP -> "Sample Application"
            }
        }
    }
}