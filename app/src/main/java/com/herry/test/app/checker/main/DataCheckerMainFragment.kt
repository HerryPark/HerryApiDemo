package com.herry.test.app.checker.main

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
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.util.AppUtil
import com.herry.libs.widget.extension.navigateTo
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.widget.TitleBarForm

/**
 * Created by herry.park on 2020/7/7
 **/
class DataCheckerMainFragment : BaseNavView<DataCheckerMainContract.View, DataCheckerMainContract.Presenter>(), DataCheckerMainContract.View {

    override fun onCreatePresenter(): DataCheckerMainContract.Presenter =
        DataCheckerMainPresenter()

    override fun onCreatePresenterView(): DataCheckerMainContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.data_checker_main_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        view ?: return

        TitleBarForm(
            activity = { requireActivity() },
            onClickBack = { AppUtil.pressBackKey(requireActivity(), view) }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.data_checker_main_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Data Checker Main", backEnable = true))
        }

        view.findViewById<RecyclerView>(R.id.data_checker_main_fragment_container)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            adapter = this@DataCheckerMainFragment.adapter
        }
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(DataCheckerPasswordItemForm())
        }
    }

    private inner class DataCheckerPasswordItemForm : NodeForm<DataCheckerPasswordItemForm.Holder, DataCheckerMainContract.PasswordModel>(
        Holder::class, DataCheckerMainContract.PasswordModel::class) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val mainPasswordStatus: TextView? = view.findViewById(R.id.data_checker_main_password_status)
            init {
                view.setOnSingleClickListener {
                    NodeRecyclerForm.getBindModel(this@DataCheckerPasswordItemForm, this@Holder)?.let {
                        navigateTo(destinationId = R.id.password_setting_fragment)
                    }
                }
            }
        }

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onLayout(): Int = R.layout.data_checker_main_password

        override fun onBindModel(context: Context, holder: Holder, model: DataCheckerMainContract.PasswordModel) {
            holder.mainPasswordStatus?.text = if (model.password.isBlank()) "Not exit password" else "Exist password"
        }
    }

    override fun onNavigateUpResult(from: Int, result: Bundle) {
        when(from) {
            R.id.password_setting_fragment ->
                if (NavBundleUtil.isNavigationResultOk(result)) {
                    presenter?.refresh()
                }
        }
    }
}