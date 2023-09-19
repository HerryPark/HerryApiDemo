package com.herry.libs.widget.view.recyclerview.tabrecycler

import android.os.Parcelable
import androidx.lifecycle.lifecycleScope
import com.herry.libs.nodeview.model.NodeHelper

abstract class TabRecyclerPresenter : TabRecyclerContract.Presenter() {

    protected var view: TabRecyclerContract.View? = null
        private set
    private var launched = false
    private var relaunched = false

    private var saveInstanceState: Parcelable? = null

    protected val nodes = NodeHelper.createNodeGroup()

    private var currentResumeState: ResumeState? = null

    private fun setResumeState(view: TabRecyclerContract.View, resumeState: ResumeState) {
        currentResumeState = resumeState
        onResume(view, resumeState)
    }

    protected fun getResumeState(): ResumeState? = currentResumeState

    override fun onAttach(view: TabRecyclerContract.View) {
        this.view = view
        view.root.let {
            it.beginTransition()
            it.clearChild()
            NodeHelper.addNode(it, nodes)
            it.endTransition()

            if (!launched) {
                launched = true
                setResumeState(view, ResumeState.LAUNCH)
            } else if (relaunched) {
                relaunched = false
                setResumeState(view, ResumeState.RELAUNCH)
            } else {
                setResumeState(view, ResumeState.RESUME)
            }
        }
        view.onAttached(saveInstanceState)
        saveInstanceState = null
    }

    override fun onDetach() {
        this.view?.let { view ->
            saveInstanceState = view.onDetached()
            view.root.beginTransition()
            view.root.clearChild()
            view.root.endTransition()
        }
        this.view = null
    }

    fun init() {
        launched = if (view != null) {
            onResume()
            true
        } else {
            nodes.clearChild()
            false
        }
    }

    override fun setCurrentPresent() {
        view?.onNotifyScrollState()
    }

    protected fun isEmpty(): Boolean = this.nodes.getChildCount() == 0
}