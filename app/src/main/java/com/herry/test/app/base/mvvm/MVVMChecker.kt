package com.herry.test.app.base.mvvm

class MVVMChecker(
    val type: Type,
    val enforce: Enforce
) {
    enum class Type {
        NONE,
        NETWORK
    }

    enum class Enforce {
        ON_START,
        ON_RESUME
    }
}