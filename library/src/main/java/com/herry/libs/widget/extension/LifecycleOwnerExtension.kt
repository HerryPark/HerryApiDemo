package com.herry.libs.widget.extension

import android.app.Activity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

inline fun LifecycleOwner?.launchWhen(
    state: Lifecycle.State,
    repeat: Boolean = false,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    val lifecycleOwner = this ?: return null

    return lifecycleOwner.lifecycleScope.launch {
        val coroutineScope = this@launch
        lifecycleOwner.lifecycle.repeatOnLifecycle(state) {
            block()
            if (!repeat) {
                coroutineScope.coroutineContext.cancel()
            }
        }
    }
}

inline fun LifecycleOwner?.launchWhenCreated(
    repeat: Boolean = false,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    return launchWhen(state = Lifecycle.State.CREATED, repeat = repeat, block = block)
}

inline fun LifecycleOwner?.launchWhenStarted(
    repeat: Boolean = false,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    return launchWhen(state = Lifecycle.State.STARTED, repeat = repeat, block = block)
}

inline fun LifecycleOwner?.launchWhenResumed(
    repeat: Boolean = false,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    return launchWhen(state = Lifecycle.State.RESUMED, repeat = repeat, block = block)
}

inline fun LifecycleOwner?.launchWhenView(
    state: Lifecycle.State,
    repeat: Boolean = false,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    val lifecycleOwner = when (this) {
        is Activity -> this
        is DialogFragment ->
            if (this.dialog != null && this.dialog?.isShowing == true) this /*for the dialog fragment */
            else this.viewLifecycleOwnerLiveData.value /*for the flat fragment */
        is Fragment -> this.viewLifecycleOwnerLiveData.value /*for the flat fragment */
        else -> null
    }

    return lifecycleOwner?.launchWhen(state, repeat, block)
}

inline fun LifecycleOwner?.launchWhenViewCreated(
    repeat: Boolean = false,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    return launchWhenView(state = Lifecycle.State.CREATED, repeat = repeat, block = block)
}

inline fun LifecycleOwner?.launchWhenViewStarted(
    repeat: Boolean = false,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    return launchWhenView(state = Lifecycle.State.STARTED, repeat = repeat, block = block)
}

inline fun LifecycleOwner?.launchWhenViewResumed(
    repeat: Boolean = false,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    return launchWhenView(state = Lifecycle.State.RESUMED, repeat = repeat, block = block)
}
