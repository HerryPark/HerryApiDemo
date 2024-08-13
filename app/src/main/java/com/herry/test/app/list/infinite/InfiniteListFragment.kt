package com.herry.test.app.list.infinite

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.herry.libs.helper.ToastHelper
import com.herry.libs.widget.view.recyclerview.snap.LinearSnapExHelper
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.widget.TitleBarForm

class InfiniteListFragment: BaseNavView<InfiniteListContract.View, InfiniteListContract.Presenter>(), InfiniteListContract.View {
    override fun onCreatePresenter(): InfiniteListContract.Presenter = InfiniteListPresenter()

    override fun onCreatePresenterView(): InfiniteListContract.View = this

    private var container: View? = null

    private val listAdapter: InfiniteListAdapter = InfiniteListAdapter()
    private val listSnapHelper = LinearSnapExHelper(snapStyle = LinearSnapExHelper.SnapStyle.START).apply {
        setOnSnapPositionChangeListener(object : LinearSnapExHelper.OnSnapPositionChangeListener {
            override fun onSnapped(position: Int) {
                ToastHelper.showToast(activity, "Snapped to $position")
            }

            override fun onSnapPositionChange(position: Int) {
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return this.container ?: inflater.inflate(R.layout.list_fragment, container, false)
            ?.apply { init(this) }.also { this.container = it}
    }

    private fun init(view: View) {
        val context = view.context ?: return

        TitleBarForm(
            activity = { requireActivity() }
        ).apply {
            bindFormHolder(context, view.findViewById(R.id.list_fragment_title))
            bindFormModel(context, TitleBarForm.Model(title = "Infinite List", backEnable = true))
        }

        view.findViewById<RecyclerView>(R.id.list_fragment_list)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            adapter = this@InfiniteListFragment.listAdapter

            listSnapHelper.attachToRecyclerView(this)
        }
    }

    override fun onUpdatedList(items: List<InfiniteListContract.InfiniteListItem>) {
        listAdapter.setItems(items)
    }

    inner class InfiniteListAdapter: RecyclerView.Adapter<InfiniteListAdapter.ViewHolder>() {
        private val items: MutableList<InfiniteListContract.InfiniteListItem> = mutableListOf()

        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val textView: TextView? = view.findViewById(R.id.infinite_linear_list_item_label)
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setItems(items: List<InfiniteListContract.InfiniteListItem>) {
            this.items.clear()
            this.items.addAll(items)
            this.notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.infinite_linear_list_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = this.items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = try { this.items[position] } catch (_: Exception) { null } ?: return

            holder.textView?.text = item.index.toString()
        }
    }
}