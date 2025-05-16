package com.herry.test.app.keepchildvm.child

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.herry.libs.log.Trace
import com.herry.test.app.base.mvvm.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KeepChildVMChildViewModel(
    savedStateHandle: SavedStateHandle
): BaseViewModel() {

    private val _uiState = MutableStateFlow<KeepChildVMChildUIState>(KeepChildVMChildUIState.Idle)
    val uiState: StateFlow<KeepChildVMChildUIState> = _uiState.asStateFlow()

    init {
        val callData = KeepChildVMChildConstants.getCallData(savedStateHandle)
        Trace.d("Herry", "callData = $callData")

        val name = callData.name
        updateName(name)
    }

    private fun updateName(name: String) {
        viewModelScope.launch {
            _uiState.value = KeepChildVMChildUIState.UpdateName(name)
        }
    }
}
