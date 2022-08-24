package com.herry.libs.mvp

import androidx.annotation.MainThread
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class MVPPresenterLifecycle {

    enum class State {
        CREATED, //
        ATTACHED, //
        LAUNCHED, // onLaunch()
        RESUMED; // onResume()

        fun isAtLeast(state: State): Boolean = this.ordinal >= state.ordinal
    }

    private val launchWhenPresenterBlocks: ConcurrentHashMap<State, MutableList<suspend CoroutineScope.() -> Unit>> = ConcurrentHashMap()

    private var currentState: State = State.CREATED

    @MainThread
    fun setState(lifecycleScope: CoroutineScope? = null, state: State) {
        if (this.currentState != state) {
            this.currentState = state
        }

        lifecycleScope?.launch {
            performPendingStateBlocks(state)
        }
    }

    fun getCurrentState(): State = this.currentState

    private fun addPresenterLaunchBlocks(state: State, block: suspend CoroutineScope.() -> Unit) {
        (launchWhenPresenterBlocks[state] ?: kotlin.run {
            val value: MutableList<suspend CoroutineScope.() -> Unit> = mutableListOf()
            launchWhenPresenterBlocks[state] = value
            value
        }).apply {
            add(block)
        }
    }

    private suspend fun performPendingStateBlocks(maxState: State) {
        withContext(Dispatchers.Main.immediate) {
            val coroutineScope = this
            coroutineScope.launch((Dispatchers.Main.immediate)) {
                val remainLaunchBlocksStates = launchWhenPresenterBlocks.keys.filter { key ->
                    key.ordinal <= maxState.ordinal && launchWhenPresenterBlocks[key]?.isNotEmpty() == true
                }.sortedBy { it.ordinal }
                remainLaunchBlocksStates.forEach { state ->
                    launchWhenPresenterBlocks[state]?.let { actions ->
                        val iterator = actions.iterator()
                        while (iterator.hasNext()) {
                            val block = iterator.next()
                            block(coroutineScope)
                            iterator.remove()
                        }
                    }
                }
            }
        }
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

    fun launchWhenPresenterLaunched(lifecycleScope: CoroutineScope, block: suspend CoroutineScope.() -> Unit): Job = lifecycleScope.launch {
        launchWhenPresenterStateAtLeast(State.LAUNCHED, block)
    }

    fun launchWhenPresenterResumed(lifecycleScope: CoroutineScope, block: suspend CoroutineScope.() -> Unit): Job = lifecycleScope.launch {
        launchWhenPresenterStateAtLeast(State.RESUMED, block)
    }
}