package com.herry.test.app.tflite.digitclassifier

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.herry.libs.app.activity_caller.module.ACPick
import com.herry.libs.app.nav.NavBundleUtil
import com.herry.libs.util.AppUtil
import com.herry.libs.widget.extension.navigateTo
import com.herry.libs.widget.extension.setImage
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.app.painter.PainterFragment
import com.herry.test.widget.Popup
import com.herry.test.widget.TitleBarForm
import java.util.*

class DigitClassifierFragment : BaseNavView<DigitClassifierContract.View, DigitClassifierContract.Presenter>(), DigitClassifierContract.View {
    override fun onCreatePresenter(): DigitClassifierContract.Presenter = DigitClassifierPresenter()

    override fun onCreatePresenterView(): DigitClassifierContract.View = this

    private var container: View? = null
    private var loadedImageView: ImageView? = null
    private var loadImageButton: View? = null
    private var classifyView: View? = null
    private var clearView: View? = null
    private var resultView: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return this.container ?: inflater.inflate(R.layout.image_classifier_fragment, container, false)?.apply { init(this) }.also { this.container = it}
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun init(view: View) {
        TitleBarForm(
            activity = { requireActivity() },
            onClickBack = { AppUtil.pressBackKey(requireActivity(), view) }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.image_classifier_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Digit Image Classifier", backEnable = true))
        }

        loadImageButton = view.findViewById<View?>(R.id.image_classifier_fragment_load_image)?.apply {
            this.setOnSingleClickListener {
                Popup(requireActivity()).apply {
                    setTitle("Choice from")
                    setItems(
                        arrayOf("Draw", "Pick photo")) { dialog, which ->
                        when (which) {
                            0 -> {
                                navigateTo(
                                    destinationId = R.id.painter_fragment,
                                    args = PainterFragment.createArguments(
                                        PainterFragment.Companion.CallData(
                                            strokeWidth = 50f,
                                            strokeColor = Color.WHITE,
                                            backgroundColor = Color.BLACK,
                                            ratioWidth = 1,
                                            ratioHeight = 1
                                        )
                                    ))
                            }
                            1 -> {
                                activityCaller?.call(ACPick.PickImageOnly { result ->
                                    if (result.success) {
                                        val picked: Uri = result.uris.firstOrNull() ?: return@PickImageOnly
                                        presenter?.loadedImage(picked)
                                    }
                                })
                            }
                        }
                        dialog.dismiss()
                    }
                    setPositiveButton(android.R.string.cancel)
                }.show()
            }
        }

        loadedImageView = view.findViewById(R.id.image_classifier_fragment_image)

        classifyView = view.findViewById<View?>(R.id.image_classifier_fragment_classify)?.apply {
            this.setOnSingleClickListener {
                loadedImageView?.let { imageView ->
                    (imageView.drawable as?BitmapDrawable)?.bitmap?.let { bitmap ->
                        presenter?.classify(bitmap)
                    }
                }
            }
        }
        clearView = view.findViewById<View?>(R.id.image_classifier_fragment_clear)?.apply {
            this.setOnSingleClickListener {
                presenter?.clear()
            }
        }
        resultView = view.findViewById(R.id.image_classifier_fragment_result)
    }

    override fun onLoadedImage(loadedImage: Any?) {
        val imageView = this.loadedImageView ?: return
        val context = this.context ?: return

        if (loadedImage is String || loadedImage is Uri || loadedImage is Bitmap) {
            Glide.with(context).load(loadedImage).into(imageView)
            classifyView?.isEnabled = true
            clearView?.isEnabled = true
            loadImageButton?.isVisible = false
        } else {
            imageView.setImage(null)
            loadImageButton?.isVisible = true
            classifyView?.isEnabled = false
            clearView?.isEnabled = false
            resultView?.text = ""
            return
        }
    }

    override fun onClassified(result: String, accuracy: Float) {
        resultView?.text = String.format(Locale.ENGLISH, "Number: %s, Accuracy: %.0f%%", result, accuracy * 100f)
    }

    override fun onError() {
        resultView?.text = String.format(Locale.ENGLISH, "Cannot classified")
    }

    override fun onNavigateUpResult(fromNavigationId: Int, result: Bundle) {
        if (fromNavigationId == R.id.painter_fragment) {
            if (NavBundleUtil.isNavigationResultOk(result)) {
                presenter?.loadedImage(PainterFragment.getResult(result)?.bitmapByteArray ?: return)
            }
        }
    }
}