package com.herry.libs.nodeview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes

abstract class NodeView<H : NodeHolder> {

    protected var holder: H? = null
        private set

    fun getView() = holder?.view

    open fun createHolder(context: Context, parent: ViewGroup?, attach: Boolean = true): H? {
        val view = onCreateView(context, parent)
        if (attach) {
            parent?.addView(view)
        }
        return bindHolder(context, view)
    }

    fun bindHolder(context: Context, parent: ViewGroup?, @IdRes id: Int): H? {
        return bindHolder(context, parent?.findViewById(id))
    }

    fun bindHolder(context: Context, view: View?): H? {
        holder = view?.let {
            onCreateHolder(context, it)
        }

        holder?.let {
            onBindHolder(context, it)
        }
        return holder
    }

    protected open fun onCreateView(context: Context, parent: ViewGroup?): View =
        LayoutInflater.from(context).inflate(onLayout(), parent, false)

    /**
     * Sets a layout resource id for inflating view.
     */
    @LayoutRes
    protected abstract fun onLayout(): Int

    protected abstract fun onCreateHolder(context: Context, view: View): H

    protected open fun onBindHolder(context: Context, holder: H) {}

}