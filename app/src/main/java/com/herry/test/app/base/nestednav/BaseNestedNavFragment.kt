package com.herry.test.app.base.nestednav

import androidx.annotation.LayoutRes
import com.herry.test.app.base.nav.BaseNavFragment

@Suppress("SameParameterValue", "unused")
open class BaseNestedNavFragment: BaseNavFragment, NestedNavMovement {
    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)
}