package com.herry.libs.mvp

class MVPPresenterLifecycle {

    enum class State {
        CREATED, //
        ATTACHED, //
        LAUNCHED, // onLaunch()
        RESUMED; // onResume()

        fun isAtLeast(state: State): Boolean = this.ordinal >= state.ordinal
    }

    private val launchWhenPresenterBlocks: MutableMap<State, MutableList<() -> Unit>> = mutableMapOf()

    private var currentState: State = State.CREATED

    fun setState(state: State) {
        this.currentState = state
    }

    fun getCurrentState(): State = this.currentState

    private fun addPresenterLaunchBlocks(state: State, block: () -> Unit) {
        (launchWhenPresenterBlocks[state] ?: kotlin.run {
            val value: MutableList<() -> Unit> = mutableListOf()
            launchWhenPresenterBlocks[state] = value
            value
        }).apply {
            add(block)
        }
    }

    fun performPendingStateBlocks(maxState: State) {
        val remainLaunchBlocksStates = launchWhenPresenterBlocks.keys.filter { it.ordinal <= maxState.ordinal }.sortedBy { it.ordinal }
        remainLaunchBlocksStates.forEach { state ->
            launchWhenPresenterBlocks[state]?.let { actions ->
                val iterator = actions.iterator()
                while (iterator.hasNext()) {
                    iterator.next().invoke()
                    iterator.remove()
                }
            }
        }
    }

    private fun launchWhenPresenter(state: State, block: () -> Unit) {
        if (getCurrentState().isAtLeast(state)) {
            block()
        } else {
            addPresenterLaunchBlocks(state, block)
        }
    }

    fun launchWhenPresenterLaunched(block: () -> Unit) {
        launchWhenPresenter(State.LAUNCHED, block)
    }

    fun launchWhenPresenterResumed(block: () -> Unit) {
        launchWhenPresenter(State.RESUMED, block)
    }
}