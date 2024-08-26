package com.herry.test.app.base.nestednav

import com.herry.libs.mvp.MVPPresenter
import com.herry.libs.mvp.MVPView
import com.herry.test.app.base.nav.BaseMVPNavView

@Suppress("unused")
abstract class BaseMVPNestedNavView<V: MVPView<P>, P: MVPPresenter<V>>: BaseMVPNavView<V, P>(), NestedNavMovement