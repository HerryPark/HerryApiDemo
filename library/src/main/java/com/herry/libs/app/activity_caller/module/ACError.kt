package com.herry.libs.app.activity_caller.module

import android.app.Activity
import com.herry.libs.app.activity_caller.ACModule
import java.util.concurrent.ExecutionException

class ACError(private val caller: Caller, private val listener: ACErrorListener): ACModule {

    interface ACErrorListener: ACModule.OnIntentListener

    open class Caller(
        internal val throwable: Throwable,
        internal val listener: ((throwable: Throwable) -> Unit)? = null
    )

    override fun call(activity: Activity) {
        when(caller.throwable) {
            is ExecutionException -> {
                caller.throwable.cause?.let {
                    call(it)
                }
            }
            else -> {
                call(caller.throwable)
            }
        }
    }

    private fun call(throwable: Throwable) {
        when(throwable) {
//            is RestNetworkError ->  {
//                when(throwable.what) {
//                    RestError.ERROR_NETWORK_DISABLED,
//                    RestError.ERROR_TIMEOUT,
//                    RestError.ERROR_NO_CONNECTION,
//                    RestError.ERROR_RESPONSE,
//                    RestError.ERROR_UNKNOWN,
//                    RestError.ERROR_UNKNOWN_RESULT -> {
//                        ToastHelper.showToast(listener.getActivity(), R.string.text_disabled_network)
//                    }
////                    RestError.ERROR_AUTH_FAIL -> {
////                        listener.getPopupHelper().show403Popup()
////                    }
//                    RestError.ERROR_AUTH_PERMISSION_DENIED -> {
//                        listener.getPopupHelper().showPopup(
//                            R.string.popup_notice,
//                            R.string.text_permission_denied
//                        )
//                    }
////                    RestError.ERROR_SERVER_CHECKING_TIME -> {
////                        listener.getPopupHelper().showServerCheckingTime()
////                    }
//                    else -> {
//                        caller.listener?.let {
//                            it(throwable)
//                        }
//                    }
//                }
//            }
////import com.android.volley.AuthFailureError
////import com.android.volley.NoConnectionError
////import com.android.volley.ServerError
////import com.android.volley.TimeoutError
//            is NoConnectionError,
//            is TimeoutError -> {
////                Trace.i("RestApi", "error : $throwable")
//                ToastHelper.showToast(listener.getActivity(), R.string.text_disabled_network)
//                caller.listener?.let {
//                    it(throwable)
//                }
//            }
//            is ServerError -> {
//                when (throwable.networkResponse?.statusCode) {
//                    503 -> {
//                        listener.getPopupHelper().showServerCheckingTime()
//                    }
//                    else -> {
//                        Trace.i("RestApi", "ServerError statusCode : ${throwable.networkResponse.statusCode} error : $throwable ")
//                        ToastHelper.showToast(listener.getActivity(), R.string.text_disabled_network)
//                        caller.listener?.let {
//                            it(throwable)
//                        }
//                    }
//                }
//            }
//            is AuthFailureError -> {
//                listener.getPopupHelper().show403Popup()
//            }
//            is ServiceUnableError ->
            else -> {
                caller.listener?.let {
                    it(throwable)
                }
            }
        }
    }
}