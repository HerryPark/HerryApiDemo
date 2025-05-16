package com.herry.test.app.keepchildvm.child

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.herry.libs.log.Trace
import com.herry.libs.widget.extension.createChildFragmentViewModel
import com.herry.libs.widget.extension.launchWhenCreated
import com.herry.libs.widget.extension.launchWhenResumed
import com.herry.test.R
import com.herry.test.app.base.mvvm.BaseMVVMFragment
import kotlinx.coroutines.flow.collectLatest

class KeepChildVMChildFragment: BaseMVVMFragment<KeepChildVMChildViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showsDialog = false

        setupUIState()
    }

    override fun onViewModel(): KeepChildVMChildViewModel {
        val callData = KeepChildVMChildConstants.getCallData(getDefaultArguments())
        return createChildFragmentViewModel(key = callData.name, modelClass = KeepChildVMChildViewModel::class)
    }

    private var container: View? = null
    private var name: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return this.container ?: inflater.inflate(R.layout.keep_child_vm_child_fragment, container, false)
            .apply { init(this) }
            .also { this.container = it }
    }

    private fun init(view: View) {
        name = view.findViewById(R.id.keep_child_vm_child_fragment_name)
    }

    private fun setupUIState() {
        Trace.d("Herry", "setupUIState $this")
        launchWhenCreated {
            viewModel.uiState.collectLatest { uiState ->
                launchWhenResumed {
                    Trace.d("Herry", "uiState $uiState $this")
                    when (uiState) {
                        KeepChildVMChildUIState.Idle -> {}
                        is KeepChildVMChildUIState.UpdateName -> name?.text = uiState.name
                    }
                }
            }
        }
    }
    companion object {
        fun newInstance(name: String): KeepChildVMChildFragment = KeepChildVMChildFragment().apply {
            arguments = KeepChildVMChildConstants.createCallArguments(name)
        }
    }
}