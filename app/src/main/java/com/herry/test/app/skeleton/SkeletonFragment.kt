package com.herry.test.app.skeleton

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.test.R
import com.herry.test.app.base.nav.BaseMVPNavView

class SkeletonFragment : BaseMVPNavView<SkeletonContract.View, SkeletonContract.Presenter>(), SkeletonContract.View {
    override fun onCreatePresenter(): SkeletonContract.Presenter = SkeletonPresenter()

    override fun onCreatePresenterView(): SkeletonContract.View = this

    private var container: View? = null
    private val contentsForm: ContentsForm = ContentsForm()
    private var button: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.skeleton_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    @SuppressLint("SetTextI18n")
    private fun init(view: View?) {
        view ?: return

        contentsForm.bindFormHolder(requireContext(), view.findViewById(R.id.skeleton_fragment_contents))

        button = view.findViewById(R.id.skeleton_fragment_button)
        button?.setOnSingleClickListener {
            val current = contentsForm.model ?: return@setOnSingleClickListener

            if (current.show) {
                presenter?.hide()
            } else {
                presenter?.show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onUpdate(model: SkeletonContract.ContentsModel) {
        contentsForm.bindFormModel(requireContext(), model)
        button?.text = "${if (model.show) "hide" else "show"} skeleton"
    }

    private inner class ContentsForm : NodeForm<ContentsForm.Holder, SkeletonContract.ContentsModel>(Holder::class, SkeletonContract.ContentsModel::class) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val loadingContainer: View? = view.findViewById(R.id.skeleton_sample_loading_container)
            val contentsContainer: View? = view.findViewById(R.id.skeleton_sample_contents_container)
        }

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onLayout(): Int = R.layout.skeleton_sample

        override fun onBindModel(context: Context, holder: Holder, model: SkeletonContract.ContentsModel) {
            holder.loadingContainer?.isVisible = model.show
            holder.contentsContainer?.isVisible = !model.show
        }
    }
}