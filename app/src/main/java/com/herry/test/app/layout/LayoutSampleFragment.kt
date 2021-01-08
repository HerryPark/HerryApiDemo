package com.herry.test.app.layout

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.herry.libs.helper.ToastHelper
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.util.AppUtil
import com.herry.libs.widget.extension.setOnProtectClickListener
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView

/**
 * Created by herry.park on 2020/08/19.
 **/
class LayoutSampleFragment : BaseNavView<LayoutSampleContract.View, LayoutSampleContract.Presenter>(), LayoutSampleContract.View {

    override fun onCreatePresenter(): LayoutSampleContract.Presenter = LayoutSamplePresenter()

    override fun onCreatePresenterView(): LayoutSampleContract.View = this

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.layout_sample_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    @SuppressLint("SetTextI18n")
    private fun init(view: View?) {
        view ?: return

        view.findViewById<TextView>(R.id.layout_sample_fragment_title)?.text = "Select aspect ratio"
        view.findViewById<View>(R.id.layout_sample_fragment_close)?.setOnClickListener {
            AppUtil.pressBackKey(requireActivity(), container)
        }

        view.findViewById<View>(R.id.layout_sample_fragment_aspect_ratio_16x9)?.run {
            val form = AspectRatioItemFrom { model ->
                ToastHelper.showToast(requireActivity(), "Selected: $model")
            }
            form.bindFormHolder(this.context, this)
            form.bindFormModel(this.context, LayoutSampleContract.AspectRatioType.RATIO_16v9)
        }

        view.findViewById<View>(R.id.layout_sample_fragment_aspect_ratio_9x16)?.run {
            val form = AspectRatioItemFrom { model ->
                ToastHelper.showToast(requireActivity(), "Selected: $model")
            }
            form.bindFormHolder(this.context, this)
            form.bindFormModel(this.context, LayoutSampleContract.AspectRatioType.RATIO_9v16)
        }

        view.findViewById<View>(R.id.layout_sample_fragment_aspect_ratio_1x1)?.run {
            val form = AspectRatioItemFrom { model ->
                ToastHelper.showToast(requireActivity(), "Selected: $model")
            }
            form.bindFormHolder(this.context, this)
            form.bindFormModel(this.context, LayoutSampleContract.AspectRatioType.RATIO_1v1)
        }

        view.findViewById<View>(R.id.layout_sample_fragment_aspect_ratio_4x3)?.run {
            val form = AspectRatioItemFrom { model ->
                ToastHelper.showToast(requireActivity(), "Selected: $model")
            }
            form.bindFormHolder(this.context, this)
            form.bindFormModel(this.context, LayoutSampleContract.AspectRatioType.RATIO_4v3)
        }

        view.findViewById<View>(R.id.layout_sample_fragment_aspect_ratio_3x4)?.run {
            val form = AspectRatioItemFrom { model ->
                ToastHelper.showToast(requireActivity(), "Selected: $model")
            }
            form.bindFormHolder(this.context, this)
            form.bindFormModel(this.context, LayoutSampleContract.AspectRatioType.RATIO_3v4)
        }

        view.findViewById<View>(R.id.layout_sample_fragment_aspect_ratio_4x5)?.run {
            val form = AspectRatioItemFrom { model ->
                ToastHelper.showToast(requireActivity(), "Selected: $model")
            }
            form.bindFormHolder(this.context, this)
            form.bindFormModel(this.context, LayoutSampleContract.AspectRatioType.RATIO_4v5)
        }

        view.findViewById<View>(R.id.layout_sample_fragment_aspect_ratio_2_35x1)?.run {
            val form = AspectRatioItemFrom { model ->
                ToastHelper.showToast(requireActivity(), "Selected: $model")
            }
            form.bindFormHolder(this.context, this)
            form.bindFormModel(this.context, LayoutSampleContract.AspectRatioType.RATIO_2_35v1)
        }

        view.findViewById<TextView>(R.id.layout_sample_fragment_information)?.text = "프로젝트에 사용한 사진이나 동영상, 오디오를 기기에서 지우면 더 이상 편집에 사용할 수 없습니다."
    }

    private inner class AspectRatioItemFrom(
        private val onClick: ((model: LayoutSampleContract.AspectRatioType) -> Unit)?
    ) : NodeForm<AspectRatioItemFrom.Holder, LayoutSampleContract.AspectRatioType>(Holder::class, LayoutSampleContract.AspectRatioType::class) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val iconView: ImageView = view.findViewById(R.id.layout_sample_aspect_ratio_icon)
            val textView: TextView = view.findViewById(R.id.layout_sample_aspect_ratio_text)

            init {
                view.setOnProtectClickListener {
                    model?.run { onClick?.let { it(this) } }
                }
            }
        }

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onLayout(): Int = R.layout.layout_sample_aspect_ratio

        override fun onBindModel(context: Context, holder: Holder, model: LayoutSampleContract.AspectRatioType) {
            holder.iconView.setImageResource(getRatioIcon(model))
            holder.textView.text = getRatioText(model)
        }

        private fun getRatioIcon(model: LayoutSampleContract.AspectRatioType) : Int {
            return when(model) {
                LayoutSampleContract.AspectRatioType.RATIO_16v9 -> R.mipmap.ratio_square_16_9_enabled
                LayoutSampleContract.AspectRatioType.RATIO_9v16 -> R.mipmap.ratio_square_9_16_enabled
                LayoutSampleContract.AspectRatioType.RATIO_1v1 -> R.mipmap.ratio_square_1_1_enabled
                LayoutSampleContract.AspectRatioType.RATIO_4v3 -> R.mipmap.ratio_square_4_3_enabled
                LayoutSampleContract.AspectRatioType.RATIO_3v4 -> R.mipmap.ratio_square_3_4_enabled
                LayoutSampleContract.AspectRatioType.RATIO_4v5 -> R.mipmap.ratio_square_4_5_enabled
                LayoutSampleContract.AspectRatioType.RATIO_2_35v1 -> R.mipmap.ratio_square_235_1_enabled
            }
        }

        private fun getRatioText(model: LayoutSampleContract.AspectRatioType) : String {
            return when(model) {
                LayoutSampleContract.AspectRatioType.RATIO_16v9 -> "16:9"
                LayoutSampleContract.AspectRatioType.RATIO_9v16 -> "9:16"
                LayoutSampleContract.AspectRatioType.RATIO_1v1 -> "1:1"
                LayoutSampleContract.AspectRatioType.RATIO_4v3 -> "4:3"
                LayoutSampleContract.AspectRatioType.RATIO_3v4 -> "3:4"
                LayoutSampleContract.AspectRatioType.RATIO_4v5 -> "4:5"
                LayoutSampleContract.AspectRatioType.RATIO_2_35v1 -> "2.35:1"
            }
        }
    }
}