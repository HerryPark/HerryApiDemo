package com.herry.libs.mvp

import androidx.annotation.MainThread
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class MVPPresenterLifecycle {

    enum class State {
        CREATED, //
        ATTACHED, //
        LAUNCHED, // onLaunch()
        RESUMED; // onResume()

        fun isAtLeast(state: State): Boolean = this.ordinal >= state.ordinal
    }

    private val launchWhenPresenterBlocks: ConcurrentHashMap<State, ConcurrentLinkedQueue<suspend CoroutineScope.() -> Unit>> = ConcurrentHashMap()

    private var currentState: State = State.CREATED

    @MainThread
    fun setState(viewLifecycleScope: CoroutineScope? = null, state: State) {
        if (this.currentState != state) {
            this.currentState = state
        }

        viewLifecycleScope?.launch {
            performPendingStateBlocks(state)
        }
    }

    @MainThread
    fun getCurrentState(): State = this.currentState

    private fun addPresenterLaunchBlocks(state: State, block: suspend CoroutineScope.() -> Unit) {
        (launchWhenPresenterBlocks[state] ?: kotlin.run {
            ConcurrentLinkedQueue<suspend CoroutineScope.() -> Unit>().also { launchWhenPresenterBlocks[state] = it }
        }).apply {
            add(block)
        }
    }

    private suspend fun performPendingStateBlocks(maxState: State) = withContext(Dispatchers.Main.immediate) {
        val remainLaunchBlocksStates = launchWhenPresenterBlocks.keys.filter { key ->
            key.ordinal <= maxState.ordinal && launchWhenPresenterBlocks[key]?.isNotEmpty() == true
        }.sortedBy { it.ordinal }
        remainLaunchBlocksStates.forEach { state ->
            launchWhenPresenterBlocks[state]?.let { actions ->
                val iterator = actions.iterator()
                while (iterator.hasNext()) {
                    if (currentState.ordinal >= State.LAUNCHED.ordinal) {
                        iterator.next().also { iterator.remove() }.invoke(this/*CoroutineScope*/)
                    } else {
                        break
                    }
                }
            }
        }
    }

    fun clearPendingStateBlocks() {
        launchWhenPresenterBlocks.clear()
    }

    private suspend fun launchWhenPresenterStateAtLeast(state: State, block: suspend CoroutineScope.() -> Unit) {
        withContext(Dispatchers.Main.immediate) {
            if (getCurrentState().isAtLeast(state)) {
                block()
            } else {
                addPresenterLaunchBlocks(state, block)
            }
        }
    }

    fun launchWhenPresenterLaunched(viewLifecycleScope: CoroutineScope, block: suspend CoroutineScope.() -> Unit): Job = viewLifecycleScope.launch {
        launchWhenPresenterStateAtLeast(State.LAUNCHED, block)
    }

    fun launchWhenPresenterResumed(viewLifecycleScope: CoroutineScope, block: suspend CoroutineScope.() -> Unit): Job = viewLifecycleScope.launch {
        launchWhenPresenterStateAtLeast(State.RESUMED, block)
    }
}