package com.herry.libs.app.activity_caller

interface AC {
    companion object {
//        const val REQ_LOCATION_RESOLUTION_REQUIRED = 0xFFF2
//        const val REQ_LOGIN = 0xFFF3
//        const val REQ_SHARED_TRANSITION = 0xFFF4
        const val REQ_NAVIGATION = 0xFFF5
    }

    fun <T> call(caller: T?)
}