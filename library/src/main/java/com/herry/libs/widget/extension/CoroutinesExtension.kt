@file:Suppress("unused")

package com.herry.libs.widget.extension

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun <T> CancellableContinuation<T>.resumeOnActive(value: T) {
    if (this.isActive) (this as Continuation<T>).resume(value)
}

fun <T> CancellableContinuation<T>.resumeWithExceptionOnActive(exception: Throwable) {
    if (this.isActive) (this as Continuation<T>).resumeWithException(exception)
}
