package com.herry.test.app.gif.decoder

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.herry.libs.helper.ToastHelper
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.util.AppUtil
import com.herry.libs.util.BundleUtil
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.test.R
import com.herry.test.app.base.nav.BaseMVPNavView
import com.herry.test.data.GifMediaFileInfoData
import com.herry.test.widget.TitleBarForm
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by herry.park on 2020/06/11.
 **/
class GifDecoderFragment : BaseMVPNavView<GifDecoderContract.View, GifDecoderContract.Presenter>(), GifDecoderContract.View {

    companion object {
        const val ARG_GIF_INFO_DATA: String = "ARG_GIF_INFO_DATA"
    }
    override fun onCreatePresenter(): GifDecoderContract.Presenter? {
        val data = BundleUtil.getSerializableData(getDefaultArguments(), ARG_GIF_INFO_DATA, GifMediaFileInfoData::class) ?: return null
        return GifDecoderPresenter(data)
    }

    override fun onCreatePresenterView(): GifDecoderContract.View = this

    private var container: View? = null
    private var mediaInfoText: TextView? = null
    private var decodedFrames: GifFramesForm? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.gif_decoder_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        view ?: return

        TitleBarForm(
            activity = { requireActivity() },
            onClickBack = { AppUtil.pressBackKey(requireActivity(), view) }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.gif_decoder_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Gif Decoder", backEnable = true))
        }

        mediaInfoText = view.findViewById(R.id.gif_decoder_fragment_media_info)

        decodedFrames = GifFramesForm(
            context = view.context,
            onClick = { position, model ->
                ToastHelper.showToast(activity, "$position frame (${model.delay})")
            }
        )
        decodedFrames?.bindFormHolder(view.context, view.findViewById(R.id.gif_decoder_fragment_decoded_gif_frames))
    }

    override fun onResume() {
        super.onResume()

        // sets title
        activity?.actionBar?.apply {
            title = "Gif Decoder"
        }
    }

    override fun onDecoded(mediaInfo: GifDecoderContract.DecodedGifMediaInfo) {
        val divider = "-------------------------------------------"
        mediaInfoText?.text = StringBuilder()
            .append("[File Info]")
            .append("\n$divider")
            .append("\n\tName: ${mediaInfo.data.name}")
            .append("\n\tPath: ${mediaInfo.data.path}")
            .append("\n\tSize: ${mediaInfo.data.size} byte")
            .append("\n\tDate: ${SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(mediaInfo.data.date * 1000L)}")
            .append("\n\n[Media Info]")
            .append("\n$divider")
            .append("\n\tWidth: ${mediaInfo.width} pixel X Height: ${mediaInfo.height} pixel")
            .append("\n\tFrame Counts: ${mediaInfo.frameCounts} (is animation? = ${mediaInfo.frameCounts > 1})")
            .append("\n\tTotal Duration: ${mediaInfo.totalDuration}")
            .append("\n")
            .toString()

        decodedFrames?.bindFormModel(requireContext(), mediaInfo.frames)
    }

    private class GifFramesForm(
        private val context: Context,
        private val onClick: ((position: Int, model: GifDecoderContract.DecodedGifFrame) -> Unit)?
    ) : NodeForm<GifFramesForm.Holder, GifDecoderContract.DecodedGifFrames>(Holder::class, GifDecoderContract.DecodedGifFrames::class) {
        inner class Adapter: NodeRecyclerAdapter(::context) {
            override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
                list.add(GifFrameForm(
                    onClick = { position, model ->
                        onClick?.let { it(position, model) }
                    }
                ))
            }
        }

        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
//            val snapHelper: PagerSnapExHelper = PagerSnapExHelper().apply {
//                setOnSnappedListener(object: PagerSnapExHelper.OnSnappedListener {
//                    override fun onSnapped(position: Int, itemCount: Int) {
//                        setIndicator(this@Holder, position, itemCount)
//                    }
//
//                    override fun onUnsnapped(position: Int, itemCount: Int) {
//                    }
//                })
//            }
            val adapter: Adapter = Adapter()

//            val indicator: LinearLayout = view.gif_decoder_decoded_gif_frames_indicator

            init {
                val recyclerView = view.findViewById<RecyclerView>(R.id.gif_decoder_decoded_gif_frames)
                recyclerView?.let {
                    it.setHasFixedSize(true)
                    it.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                    it.itemAnimator = null
                    it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                            super.onScrollStateChanged(recyclerView, newState)
                            recyclerView.parent.requestDisallowInterceptTouchEvent(true)
                        }
                    })

                    it.adapter = adapter
//                    snapHelper.attachToRecyclerView(it)
                }
            }
        }

        override fun onLayout(): Int = R.layout.gif_decoder_decoded_gif_frames

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onBindModel(context: Context, holder: Holder, model: GifDecoderContract.DecodedGifFrames) {
            holder.adapter.root.let { node ->
                node.beginTransition()
                NodeHelper.addModels(node, *model.frames.toTypedArray())
                node.endTransition()
            }

//            holder.snapHelper.scrollToSnapPosition(0)
        }

//        @SuppressLint("SetTextI18n")
//        private fun setIndicator(holder: Holder, position: Int, count: Int) {
//            ViewUtil.removeAllViews(holder.indicator)
//            if (1 >= count) return
//
//            for (index in 0 until count) {
//                val view = ViewUtil.inflate(R.layout.gif_decoder_gif_frames_indicator, holder.indicator).apply {
//                    this.gif_decoder_gif_frames_indicator_item_view.background = ViewUtil.getColorDrawable(this.context, if (index == position) R.color.tbc_10 else R.color.tbc_10_a40)
//                }
//                ViewUtil.addView(holder.indicator, view)
//            }
//        }
    }

    private class GifFrameForm(
        private val onClick: ((position: Int, model: GifDecoderContract.DecodedGifFrame) -> Unit)?
    ) : NodeForm<GifFrameForm.Holder, GifDecoderContract.DecodedGifFrame>(Holder::class, GifDecoderContract.DecodedGifFrame::class) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val number: TextView? = view.findViewById(R.id.gif_decoder_gif_frame_number)
            val duration: TextView? = view.findViewById(R.id.gif_decoder_gif_frame_duration)
            val frame: ImageView? = view.findViewById(R.id.gif_decoder_gif_frame_image)

            init {
                view.setOnSingleClickListener {
                    NodeRecyclerForm.getBindNode(this@GifFrameForm, this@Holder)?.let { node ->
                        onClick?.let { it(node.getViewPosition(), node.model) }
                    }
                }
            }
        }

        override fun onLayout(): Int = R.layout.gif_decoder_gif_frame

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        @SuppressLint("SetTextI18n")
        override fun onBindModel(context: Context, holder: Holder, model: GifDecoderContract.DecodedGifFrame) {
            holder.number?.text = "${model.index + 1} frame"
            holder.duration?.text = "${model.delay} ms"
            holder.frame?.setImageDrawable(BitmapDrawable(null, model.bitmap))
        }
    }
}