package com.herry.libs.util.perform

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.herry.libs.log.Trace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("MemberVisibilityCanBePrivate")
open class PerformBlocks(private val tag: String = "", lifecycleOwner: LifecycleOwner? = null) {
    private val lifecycleScope: CoroutineScope? = lifecycleOwner?.lifecycleScope
    private val blockedActs: ConcurrentLinkedQueue<suspend CoroutineScope.() -> Unit> = ConcurrentLinkedQueue()

    private var isPerformed: AtomicBoolean = AtomicBoolean(false)

    fun isPerformed(): Boolean = isPerformed.get()

    // perform blocked actions on the main thread
    @MainThread
    fun perform(start: CoroutineStart = CoroutineStart.DEFAULT) {
        performActionsJob(start = start) {
            val isAlreadyPerformed = isPerformed.get()
            if (tag.isNotBlank()) {
                Trace.d(tag, "is already performed? $isAlreadyPerformed")
            }
            if (isAlreadyPerformed) return@performActionsJob
            val pendingPerformIterator = blockedActs.iterator()
            var pendingPerformCounts = 0
            while (pendingPerformIterator.hasNext()) {
                pendingPerformCounts++
                pendingPerformIterator.next().also {
                    it.invoke(this/*CoroutineScope*/)
                    pendingPerformIterator.remove()
                }
            }

            isPerformed.set(true)

            val whilePerformIterator = blockedActs.iterator()
            var whilePerformCounts = 0
            while (whilePerformIterator.hasNext()) {
                whilePerformCounts++
                whilePerformIterator.next().also {
                    it.invoke(this/*CoroutineScope*/)
                    whilePerformIterator.remove()
                }
            }

            if (tag.isNotBlank()) {
                Trace.d(tag, "performed ${pendingPerformCounts + whilePerformCounts} acts (pending: $pendingPerformCounts, while: $whilePerformCounts)")
            }
        }
    }

    protected open fun performActionsJob(start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> Unit): Job? = lifecycleScope?.launch(Dispatchers.Main.immediate, start = start) { block() }

    @MainThread
    fun act(start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> Unit): Job? {
        return actionJob(start = start) {
            if (tag.isNotBlank()) {
                Trace.d(tag, "act in performed (${isPerformed.get()})")
            }
            if (isPerformed.get()) {
                block()
            } else {
                blockedActs.add(block)
            }
        }
    }

    protected open fun actionJob(start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> Unit): Job? = lifecycleScope?.launch(context = Dispatchers.Main.immediate, start = start) { block() }

    fun flush() {
        blockedActs.clear()
    }

    fun reset() {
        flush()
        isPerformed.set(false)
    }
}