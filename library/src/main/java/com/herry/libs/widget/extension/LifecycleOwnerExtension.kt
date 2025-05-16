package com.herry.libs.widget.extension

import android.app.Activity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

inline fun LifecycleOwner?.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    val lifecycleOwner = this ?: return null

    return lifecycleOwner.lifecycleScope.launch(context = context, start = start) {
        block()
    }
}

inline fun LifecycleOwner?.launchWhen(
    state: Lifecycle.State,
    repeat: Boolean = false,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    val lifecycleOwner = this ?: return null

    return lifecycleOwner.lifecycleScope.launch(context = context, start = start) {
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
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    return launchWhen(state = Lifecycle.State.CREATED, repeat = repeat, context = context, start = start, block = block)
}

inline fun LifecycleOwner?.launchWhenStarted(
    repeat: Boolean = false,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    return launchWhen(state = Lifecycle.State.STARTED, repeat = repeat, context = context, start = start, block = block)
}

inline fun LifecycleOwner?.launchWhenResumed(
    repeat: Boolean = false,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    return launchWhen(state = Lifecycle.State.RESUMED, repeat = repeat, context = context, start = start, block = block)
}

inline fun LifecycleOwner?.launchView(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    val lifecycleOwner = when (this) {
        is Activity -> this
        is DialogFragment ->
            if (this.dialog != null && this.dialog?.isShowing == true) this /*for the dialog fragment */
            else this.viewLifecycleOwner /*for the flat fragment */
        is Fragment -> this.viewLifecycleOwner /*for the flat fragment */
        else -> null
    }

    return lifecycleOwner?.lifecycleScope?.launch(context = context, start = start) {
        block()
    }
}

inline fun LifecycleOwner?.launchWhenView(
    state: Lifecycle.State,
    repeat: Boolean = false,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    val lifecycleOwner = when (this) {
        is Activity -> this
        is DialogFragment ->
            if (this.dialog != null && this.dialog?.isShowing == true) this /*for the dialog fragment */
            else this.viewLifecycleOwner /*for the flat fragment */
        is Fragment -> this.viewLifecycleOwner /*for the flat fragment */
        else -> null
    }

    return lifecycleOwner?.launchWhen(state = state, repeat = repeat, context = context, start = start, block = block)
}

inline fun LifecycleOwner?.launchWhenViewCreated(
    repeat: Boolean = false,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    return launchWhenView(state = Lifecycle.State.CREATED, repeat = repeat, context = context, start = start, block = block)
}

inline fun LifecycleOwner?.launchWhenViewStarted(
    repeat: Boolean = false,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    return launchWhenView(state = Lifecycle.State.STARTED, repeat = repeat, context = context, start = start, block = block)
}

inline fun LifecycleOwner?.launchWhenViewResumed(
    repeat: Boolean = false,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    return launchWhenView(state = Lifecycle.State.RESUMED, repeat = repeat, context = context, start = start, block = block)
}
