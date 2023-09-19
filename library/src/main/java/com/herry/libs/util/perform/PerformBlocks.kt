package com.herry.libs.util.perform

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.herry.libs.log.Trace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * action() 호출로, 조건을 만족할 때까지 (@see setReady(true)) block (function)을 쌓아 놓은 후,
 * 조건 전 까지 쌓아 놓은 block 들을 performActions()로 실행
 */
open class PerformBlocks(private val tag: String = "", lifecycleOwner: LifecycleOwner? = null) {
    private val lifecycleScope: CoroutineScope? = lifecycleOwner?.lifecycleScope
    private val blockedActs: ConcurrentLinkedQueue<suspend CoroutineScope.() -> Unit> = ConcurrentLinkedQueue()

    private var isPerformed: Boolean = false

    fun isPerformed(): Boolean = isPerformed

    // perform blocked actions on the main thread
    @MainThread
    fun perform() {
        performActionsJob {
            if (isPerformed) return@performActionsJob
            val iterator = blockedActs.iterator()
            var performCounts = 0
            while (iterator.hasNext()) {
                performCounts++
                iterator.next().also { iterator.remove() }.invoke(this/*CoroutineScope*/)
            }

            if (tag.isNotBlank()) {
                Trace.d(tag, "perform with $performCounts acts")
            }

            isPerformed = true
        }
    }

    protected open fun performActionsJob(block: suspend CoroutineScope.() -> Unit): Job? = lifecycleScope?.launch(Dispatchers.Main.immediate) { block() }

    @MainThread
    fun act(block: suspend CoroutineScope.() -> Unit): Job? {
        return actionJob {
            if (tag.isNotBlank()) {
                Trace.d(tag, "act in performed ($isPerformed)")
            }
            if (isPerformed) {
                block()
            } else {
                blockedActs.add(block)
            }
        }
    }

    protected open fun actionJob(block: suspend CoroutineScope.() -> Unit): Job? = lifecycleScope?.launch(Dispatchers.Main.immediate) { block() }

    fun flush() {
        blockedActs.clear()
    }

    fun reset() {
        flush()
        isPerformed = false
    }
}