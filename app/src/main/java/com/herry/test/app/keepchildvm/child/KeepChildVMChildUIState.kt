package com.herry.test.app.keepchildvm.child

sealed class KeepChildVMChildUIState {
    data object Idle: KeepChildVMChildUIState()
    data class UpdateName(val name: String): KeepChildVMChildUIState()
}