package com.herry.libs.util.listener

import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue

class ListenerRegistry<LISTENER> {
    private val listeners: ConcurrentLinkedQueue<WeakReference<LISTENER>> = ConcurrentLinkedQueue()

    interface NotifyCB<LISTENER> {
        fun notify(listener: LISTENER)
    }

    fun register(listener: LISTENER) {
        unregister(null)
        for (weakReference in listeners) {
            if (weakReference.get() === listener) {
                return
            }
        }
        listeners.add(WeakReference(listener))
    }

    fun unregister(listenerToRemove: LISTENER?) {
        val toRemove: MutableList<WeakReference<LISTENER>> = ArrayList()
        for (ref in listeners) {
            val listener = ref.get()
            if (listener == null || listener === listenerToRemove) {
                toRemove.add(ref)
            }
        }
        if (listeners.isNotEmpty() && toRemove.isNotEmpty()) {
            listeners.removeAll(toRemove.toSet())
        }
    }

    fun unregisterAll() {
        val toRemove: List<WeakReference<LISTENER>> = ArrayList(listeners)
        if (listeners.isNotEmpty() && toRemove.isNotEmpty()) {
            listeners.removeAll(toRemove.toSet())
        }
    }

    fun notifyListeners(notifyCB: NotifyCB<LISTENER>) {
        for (ref in listeners) {
            val listener = ref.get()
            if (listener != null) {
                notifyCB.notify(listener)
            }
        }
    }

    fun size(activeOnly: Boolean = false): Int {
        return if (activeOnly) listeners.filter { ref -> ref.get() != null }.size else listeners.size
    }

    fun hasActives(): Boolean = size(true) > 0
}