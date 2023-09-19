@file:Suppress("unused")

package com.herry.libs.widget.extension

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun <T> CancellableContinuation<T>.resumeOnActive(value: T) {
    if (this.isActive) (this as Continuation<T>).resume(value)
}

fun <T> CancellableContinuation<T>.resumeWithExceptionOnActive(exception: Throwable) {
    if (this.isActive) (this as Continuation<T>).resumeWithException(exception)
}

inline fun LifecycleOwner?.launchWhen(
    state: Lifecycle.State,
    repeat: Boolean = false,
    crossinline block: suspend CoroutineScope.() -> Unit): Job? {
    val lifecycleOwner = this ?: return null
    return lifecycleOwner.lifecycleScope.launch {
        val coroutineScope = this
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
