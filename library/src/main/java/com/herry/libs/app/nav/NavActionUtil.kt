package com.herry.libs.app.nav

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.herry.libs.util.BundleUtil
import com.herry.libs.widget.extension.setFragmentResult
import com.herry.libs.widget.extension.setFragmentResultListener

object NavActionUtil {

    const val ACTION_DATA = "action_data"

    /**
     * Adds a data of the action key.
     * data is MUST class instance of java.io.Serializable or annotated to @kotlinx.serialization.Serializable
     */
    inline fun <reified T: Any> createActionDataBundle(action: String, data: T?): Bundle {
        return NavBundleUtil.createNavigationAction(action).apply {
            BundleUtil.putSerializableDataToBundle(this, ACTION_DATA, data)
        }
    }

    inline fun <reified T: Any> getActionDataFromBundle(bundle: Bundle): T? {
        val action = NavBundleUtil.getNavigationAction(bundle)
        if (action.isEmpty()) {
            return null
        }

        return BundleUtil.getSerializableDataFromBundle(bundle, ACTION_DATA)
    }

    fun setChildActionListener(fragment: Fragment, requestKey: String, onReceivedAction: (action: String, bundle: Bundle) -> Unit) {
        if (requestKey.isBlank()) throw IllegalArgumentException("request key is blank")

        fragment.setFragmentResultListener(/* requestKey = */ requestKey) { _, bundle ->
            val action = NavBundleUtil.getNavigationAction(bundle)
            if (action.isBlank()) return@setFragmentResultListener

            onReceivedAction(action, bundle)
        }
    }

    inline fun <reified T: Any> notifyChildAction(fragment: Fragment, requestKey: String, action: String, data: T? = null) {
        fragment.parentFragment ?: throw IllegalArgumentException("${fragment::class.java.name} has not parent fragment")
        if (requestKey.isBlank()) throw IllegalArgumentException("request key is blank")
        fragment.setFragmentResult(/* requestKey = */ requestKey, createActionDataBundle<T>(action, data))
    }
}