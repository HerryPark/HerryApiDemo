package com.herry.test.app.painter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.herry.libs.draw.CircleView
import com.herry.libs.draw.ColorPickerView
import com.herry.libs.draw.DrawView
import com.herry.libs.util.AppUtil
import com.herry.libs.util.BundleUtil
import com.herry.libs.util.ColorUtil
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.setOnProtectClickListener
import com.herry.libs.widget.extension.setTintColor
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavFragment
import com.herry.test.widget.Popup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.util.*

class PainterFragment : BaseNavFragment() {

    companion object {
        private const val DRAWING_TOOL_SHOW_HEIGHT_DP = 56f
        private const val DRAWING_DEFAULT_WIDTH = 50

        private const val ARG_CALL_DATA = "arg_call_data"

        data class CallData(
            val strokeWidth: Float,
            @ColorInt
            val strokeColor: Int,
            @ColorInt
            val backgroundColor: Int,
            val ratioWidth: Int = 0,
            val ratioHeight: Int = 0
        ): Serializable {
            val hasRatio: Boolean = ratioWidth > 0 && ratioHeight > 0
        }

        fun createArguments(callData: CallData? = null): Bundle = Bundle().apply {
            if (callData != null) {
                putSerializable(ARG_CALL_DATA, callData)
            }
        }

        fun getCallData(args: Bundle?): CallData? = BundleUtil.getSerializableData(args, ARG_CALL_DATA, CallData::class)

        private const val RESULT_DATA = "result_data"

        data class ResultData(val bitmapByteArray: ByteArray): Serializable {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as ResultData

                if (!bitmapByteArray.contentEquals(other.bitmapByteArray)) return false

                return true
            }

            override fun hashCode(): Int {
                return bitmapByteArray.contentHashCode()
            }
        }

        fun getResult(bundle: Bundle): ResultData? {
            return BundleUtil[bundle, RESULT_DATA, ResultData::class]
        }

        private fun createResultBundle(bitmapByteArray: ByteArray): Bundle {
            return Bundle().apply {
                putSerializable(RESULT_DATA, ResultData(bitmapByteArray = bitmapByteArray))
            }
        }
    }

    private var container: View? = null

    private var done: View? = null

    private var drawView: DrawView? = null

    private var drawTools: View? = null

    private var drawOpacitySetter: View? = null
    private var drawOpacityValue: CircleView? = null
    private var drawWidthSetter: View? = null
    private var drawWidthValue: CircleView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return this.container ?: inflater.inflate(R.layout.painter_fragment, container, false).also {
            this.container = it
            init(it)
        }
    }

    private fun init(view: View) {

        this.drawView = view.findViewById<DrawView?>(R.id.painter_fragment_draw_view)?.apply {
            getCallData(arguments)?.let {
                this.setStrokeWidth(it.strokeWidth)
                this.setColor(it.strokeColor)
                this.setBackgroundColor(it.backgroundColor)
                if (it.hasRatio && view is ConstraintLayout) {
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(view)
                    val dimensionRatio = String.format(Locale.ENGLISH, "${if (ViewUtil.isPortraitOrientation(context)) "H" else "W"},%d:%d", it.ratioWidth, it.ratioHeight)
                    constraintSet.setDimensionRatio(this.id, dimensionRatio)
                    constraintSet.applyTo(view)
                }
            } ?: kotlin.run {
                this.setStrokeWidth(DRAWING_DEFAULT_WIDTH.toFloat())
            }
        }

        view.findViewById<View?>(R.id.painter_fragment_close)?.setOnProtectClickListener {
            AppUtil.pressBackKey(activity)
        }

        this.done = view.findViewById<View?>(R.id.painter_fragment_done)?.apply {
            this.setOnProtectClickListener {
                val drawView = this@PainterFragment.drawView ?: return@setOnProtectClickListener
                // create png
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    var bitmapByteArray: ByteArray? = null
                    val bitmap = drawView.getBitmap()
                    if (bitmap != null) {
                        val byteStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)
                        bitmapByteArray = byteStream.toByteArray()
                    }

                    withContext(Dispatchers.Main) {
                        if (bitmapByteArray?.isNotEmpty() == true) {
                            navigateUp(resultOK = true, result = createResultBundle(bitmapByteArray))
                        } else {
                            navigateUp(resultOK = false)
                        }
                    }
                }
            }
        }

        this.drawTools = view.findViewById(R.id.painter_fragment_draw_tools)

        // this.drawEraser
        view.findViewById<ImageView?>(R.id.painter_fragment_draw_eraser)?.apply {
            this.setOnClickListener {
                drawView?.toggleEraser()
                it.isSelected = drawView?.isEraseOn() ?: false
                showDrawTools(false)
            }

            this.setOnLongClickListener {
                drawView?.clear()
                showDrawTools(false)
                true
            }
        }

        // this.drawWidth
        view.findViewById<ImageView>(R.id.painter_fragment_draw_width)?.apply {
            this.setOnClickListener {
                toggleDrawTools()

                drawWidthSetter?.isVisible = true
                drawOpacitySetter?.isVisible = false
            }
        }
        this.drawWidthSetter = view.findViewById(R.id.painter_fragment_draw_width_setter)

        // this.drawOpacity
        view.findViewById<ImageView>(R.id.painter_fragment_draw_opacity)?.apply {
            this.setOnClickListener {
                toggleDrawTools()

                drawWidthSetter?.isVisible = false
                drawOpacitySetter?.isVisible = true
            }
        }
        this.drawOpacitySetter = view.findViewById(R.id.painter_fragment_draw_opacity_setter)

        // this.drawColor
        view.findViewById<ImageView>(R.id.painter_fragment_draw_color)?.apply {
            this.setOnClickListener {
                val drawView = drawView ?: return@setOnClickListener
                showDrawTools(false)

                showColorPicker(drawView.getColor()) { color, _ ->
                    drawView.setColor(color)
                    (it as? ImageView)?.setTintColor(color)
                }

                drawWidthSetter?.isVisible = false
                drawOpacitySetter?.isVisible = false
            }
        }

        // this.drawUndo
        view.findViewById<ImageView>(R.id.painter_fragment_draw_undo)?.apply {
            this.setOnClickListener {
                drawView?.undo()
                showDrawTools(false)
            }
        }

        // this.drawRedo
        view.findViewById<ImageView>(R.id.painter_fragment_draw_redo)?.apply {
            this.setOnClickListener {
                drawView?.redo()
                showDrawTools(false)
            }
        }

        // controller
        this.drawOpacityValue = view.findViewById<CircleView?>(R.id.painter_fragment_circle_view_opacity)?.apply {
            this.setRadius(100f)
        }

        // drawOpacitySeekBar
        view.findViewById<SeekBar?>(R.id.painter_fragment_seekbar_opacity)?.apply {
            this.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    drawView?.setAlpha(progress)
                    drawOpacityValue?.setAlpha(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        this.drawWidthValue = view.findViewById<CircleView?>(R.id.painter_fragment_circle_view_width)?.apply {}

        // drawWidthSeekBar
        view.findViewById<SeekBar?>(R.id.painter_fragment_seekbar_width)?.apply {
            this.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    drawView?.setStrokeWidth(progress.toFloat())
                    drawWidthValue?.setRadius(progress.toFloat())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            max = 100
            progress = this@PainterFragment.drawView?.getStrokeWidth()?.toInt() ?: 0
        }

        showDrawTools(false)
    }

    private fun isShownDrawTools(): Boolean {
        return this.drawTools?.translationY == 0f
    }

    private fun toggleDrawTools() {
        showDrawTools(!isShownDrawTools())
    }

    private fun showDrawTools(show: Boolean) {
        this.drawTools?.translationY = if (show) 0f else ViewUtil.convertDpToPixel(DRAWING_TOOL_SHOW_HEIGHT_DP)
    }

    @SuppressLint("SetTextI18n")
    private fun showColorPicker(@ColorInt color: Int, onPickedColor: (Int, String) -> Unit) {
        val activity = this.activity ?: return
        val view = ViewUtil.inflate(activity, R.layout.color_picker_dialog)
        val pickedColorView = view.findViewById<MaterialCardView>(R.id.color_picker_dialog_picked_color)
        val pickedColorHexView = view.findViewById<TextView>(R.id.color_picker_dialog_picked_color_hex)

        val colorPicker = view.findViewById<ColorPickerView>(R.id.color_picker_dialog_picker_view)?.apply {
            this.setColorListener { color, colorHex ->
                pickedColorView?.setCardBackgroundColor(color)
                pickedColorHexView?.text = colorHex
            }
        } ?: return

        colorPicker.setColor(color)
        pickedColorView.setCardBackgroundColor(color)

        Popup(activity).apply {
            setView(view)
            setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                val pickedColor = colorPicker.getColor()
                val colorHex = ColorUtil.formatColor(pickedColor)
                onPickedColor(pickedColor, colorHex)
            }
            setNegativeButton(android.R.string.cancel)
        }.show()
    }
}