package com.herry.test.app.keepchildvm.child

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import com.herry.libs.util.BundleUtil
import java.io.Serializable

object KeepChildVMChildConstants {
    internal const val ARG_CALL_DATA = "call_data"

    internal class CallData(
        val name: String = ""
    ): Serializable {
        override fun toString(): String {
            return "CallData(name='$name')"
        }
    }

    fun createCallArguments(name: String): Bundle = bundleOf(
        ARG_CALL_DATA to CallData(
            name = name
        )
    )

    @Throws(IllegalArgumentException::class)
    internal fun getCallData(bundle: Bundle): CallData {
        return BundleUtil.get(bundle, ARG_CALL_DATA, CallData::class) ?: throw IllegalArgumentException()
    }

    @Throws(IllegalArgumentException::class)
    internal fun getCallData(savedStateHandle: SavedStateHandle): CallData {
        return savedStateHandle.get<CallData>(ARG_CALL_DATA) ?: throw IllegalArgumentException()
    }
}