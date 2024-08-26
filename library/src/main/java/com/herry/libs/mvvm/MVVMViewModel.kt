package com.herry.libs.mvvm

import android.os.Bundle
import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.herry.libs.log.Trace
import java.util.concurrent.atomic.AtomicBoolean

open class MVVMViewModel: ViewModel(), LifecycleEventObserver {
    val arguments: Bundle = Bundle()
    private val isViewCreated: AtomicBoolean = AtomicBoolean(false)

    final override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                val recreated = isViewCreated.get()
                if (!recreated) {
                    isViewCreated.set(true)
                }
                Trace.d("onViewCreate(recreated? $recreated)")
                onViewCreate(source, recreated)
            }
            Lifecycle.Event.ON_START -> {
                Trace.d("onViewStart()")
                onViewStart(source)
            }
            Lifecycle.Event.ON_RESUME -> {
                Trace.d("onViewResume()")
                onViewResume(source)
            }
            Lifecycle.Event.ON_PAUSE -> {
                Trace.d("onViewPause()")
                onViewPause(source)
            }
            Lifecycle.Event.ON_STOP -> {
                Trace.d("onViewStop()")
                onViewStop(source)
            }
            Lifecycle.Event.ON_DESTROY -> {
                Trace.d("onViewDestroy()")
                onViewDestroy(source)
            }
            Lifecycle.Event.ON_ANY -> { }
        }
    }

    @MainThread
    protected open fun onViewCreate(lifecycleOwner: LifecycleOwner, recreated: Boolean) {}

    @MainThread
    protected open fun onViewStart(lifecycleOwner: LifecycleOwner) {}

    @MainThread
    protected open fun onViewResume(lifecycleOwner: LifecycleOwner) {}

    @MainThread
    protected open fun onViewPause(lifecycleOwner: LifecycleOwner) {}

    @MainThread
    protected open fun onViewStop(lifecycleOwner: LifecycleOwner) {}

    @MainThread
    protected open fun onViewDestroy(lifecycleOwner: LifecycleOwner) {}
}