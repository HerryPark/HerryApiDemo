package com.herry.test.app.sample.forms

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.herry.libs.helper.ApiHelper
import com.herry.libs.log.Trace
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.nodeview.recycler.NodeRecyclerHolder
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.anim.ViewAnimCreator
import com.herry.libs.widget.anim.ViewAnimListener
import com.herry.libs.widget.anim.ViewAnimPlayer
import com.herry.test.R
import com.herry.test.app.sample.repository.database.feed.Feed
import java.util.*

@Suppress("unused")
class FeedForm(
    private val onAttachedVideoView: (model: Model?) -> ExoPlayer?,
    private val onDetachedVideoView: (model: Model?) -> Unit,
    private val onTogglePlayer: ((form: FeedForm, holder: Holder) -> Unit)? = null,
    private val onTogglePlayerVolume: ((form: FeedForm, holder: Holder) -> Boolean)? = null,
    private val onClickTag: (text: String) -> Unit
): NodeForm<FeedForm.Holder, FeedForm.Model> (Holder::class, Model::class), NodeRecyclerForm {
    data class Model(
        val index: Int,
        val feed: Feed
    )

    inner class Holder(context: Context, view: View): NodeHolder(context, view) {
        val videoView: StyledPlayerView? = view.findViewById(R.id.feed_form_video_view)
//        val playStatus: View? = view.findViewById(R.id.feed_form_play_status)
        val cover: ImageView? = view.findViewById(R.id.feed_form_cover)
        private val volumeStatusContainer: View? = view.findViewById(R.id.feed_form_volume_status_container)
        private val volumeStatus: ImageView? = view.findViewById(R.id.feed_form_volume_status)
        private val descriptionContainer: View? = view.findViewById(R.id.feed_form_description_container)
        val title: TextView? = view.findViewById(R.id.feed_form_title)
        val tags: TextView? = view.findViewById(R.id.feed_form_tags)

        val videoViewPlayerListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
//                val model = NodeRecyclerForm.getBindModel(this@FeedForm, this@Holder)
//                Trace.d("Herry", "feed ${model?.index} isPlaying changed = $isPlaying")
//                super.onIsPlayingChanged(isPlaying)
                cover?.let { cover ->
                    if (cover.isVisible && cover.alpha == 1f) {
                        ViewAnimPlayer().apply {
                            add(ViewAnimCreator(cover)
                                    .alpha(1f, 0f)
                                    .duration(1000L))
                            onStopListener = object : ViewAnimListener.OnStop {
                                override fun onStop() {
                                    cover.isVisible = false
                                }
                            }
                        }.also {
                            it.start()
                        }
                    } else {
                        cover.isVisible = false
                    }
                }
//                playStatus?.isVisible = !isPlaying
            }
        }

        init {
            ViewUtil.setProtectTouchLowLayer(descriptionContainer, true)

            view.setOnClickListener {
                if (onTogglePlayerVolume?.invoke(this@FeedForm, this) == true) {
                    showVolumeStatus()
                }
//                onTogglePlayer(this@FeedForm, this)
            }
            videoView?.useController = false
        }

        private fun isMute(): Boolean = videoView?.player?.volume == 0f

        private val volumeStatusAimPlayer = ViewAnimPlayer().apply {
            volumeStatusContainer?.let { container ->
                add(ViewAnimCreator(container)
                    .alpha(1f, 0f)
                    .duration(1000L))
                onStopListener = object : ViewAnimListener.OnStop {
                    override fun onStop() {
                        container.isVisible = false
                    }
                }
            }
        }

        private fun showVolumeStatus() {
            volumeStatus?.setImageResource(if (!isMute()) R.drawable.ic_volume else R.drawable.ic_volume_mute)
            volumeStatusContainer?.let { container ->
                volumeStatusAimPlayer.cancel()

                container.isVisible = true
                container.alpha = 1f

                volumeStatusAimPlayer.start(1000L)
            }
        }
    }

    override fun onLayout(): Int = R.layout.feed_form

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

    override fun onBindModel(context: Context, holder: Holder, model: Model) {
//        Trace.d("Herry", "onBindModel for ${model.index}")
        val constraintLayout = holder.view as? ConstraintLayout
        if (constraintLayout != null) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)
            holder.cover?.let { cover ->
                val width = model.feed.width
                val height = model.feed.height
                val dimensionRatio = String.format(Locale.ENGLISH, "${if (ViewUtil.isPortraitOrientation(context)) "H" else "W"},%d:%d", width, height)
                constraintSet.setDimensionRatio(cover.id, dimensionRatio)
                constraintSet.applyTo(constraintLayout)
            }
        }

        holder.cover?.let { cover ->
            cover.isVisible = !isAvailablePlaying(holder.videoView)
            cover.alpha = 1f
            Glide.with(context).load(model.feed.imagePath).into(cover)
        }

        holder.title?.text = model.feed.title
        holder.tags?.let { tags ->
            ViewUtil.setLinkText(tags, model.feed.tags, ViewUtil.LinkTextData(
                links = model.feed.getTags(),
                linkTextColor = ViewUtil.getColor(context, R.color.tbc_20),
                isUnderlineText = false,
                onClicked = { _, text ->
                    onClickTag.invoke(text)
                }
            ))
//            ViewUtil.setReadMoreText(tags, model.feed.tags, ViewUtil.ReadMoreTextData(2, context.getString(R.string.text_more), ViewUtil.getColor(context, R.color.tbc_70)))
            tags.visibility = if (model.feed.tags.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun isAvailablePlaying(videoView: StyledPlayerView?): Boolean {
        val exoPlayerPlaybackState = videoView?.player?.playbackState
        return (exoPlayerPlaybackState == ExoPlayer.STATE_READY && videoView.player?.isPlaying == true
                || exoPlayerPlaybackState == ExoPlayer.STATE_READY)
    }

    override fun onViewRecycled(context: Context, nodeRecyclerHolder: NodeRecyclerHolder) {
        val model = NodeRecyclerForm.getBindModel(this@FeedForm, nodeRecyclerHolder)
        Trace.d("Herry", "onViewRecycled for ${model?.index}")
    }

    override fun onViewAttachedToWindow(context: Context, nodeRecyclerHolder: NodeRecyclerHolder) {
        val holder = nodeRecyclerHolder.holder as? Holder ?: return
        val model = NodeRecyclerForm.getBindModel(this@FeedForm, nodeRecyclerHolder)
        val videoView = holder.videoView

//        Trace.d("Herry", "onViewAttachedToWindow for ${model?.index}")

        val onAvailableSurfaceView : () -> Unit = {
            videoView?.player = onAttachedVideoView(model)
            videoView?.player?.addListener(holder.videoViewPlayerListener)

            holder.cover?.isVisible = !isAvailablePlaying(videoView)
        }
        videoView?.player?.removeListener(holder.videoViewPlayerListener)
        (videoView?.videoSurfaceView as? TextureView)?.let { surfaceView ->
            if (surfaceView.isAvailable) {
                onAvailableSurfaceView()
            } else {
                surfaceView.surfaceTextureListener = object : SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                        Trace.d("Herry", "onSurfaceTextureAvailable width = $width, height = $height")
                        onAvailableSurfaceView()
                    }

                    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                        Trace.d("Herry", "onSurfaceTextureSizeChanged width = $width, height = $height")
                    }

                    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                        Trace.d("Herry", "onSurfaceTextureDestroyed")
                        if (ApiHelper.hasOreo() && !surfaceTexture.isReleased) {
                            Trace.d("Herry", "onSurfaceTextureDestroyed release")
                            surfaceTexture.release()
                        }
                        return false
                    }

                    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
                        Trace.d("Herry", "onSurfaceTextureUpdated")
                    }
                }
            }
        }
    }

    override fun onViewDetachedFromWindow(context: Context, nodeRecyclerHolder: NodeRecyclerHolder) {
        val holder = nodeRecyclerHolder.holder as? Holder ?: return
        val model = NodeRecyclerForm.getBindModel(this@FeedForm, nodeRecyclerHolder)
        val videoView = holder.videoView

        Trace.d("Herry", "onViewDetachedFromWindow for ${model?.index}")
        videoView?.player?.removeListener(holder.videoViewPlayerListener)
        videoView?.player = null
        (videoView?.videoSurfaceView as? TextureView)?.let { surfaceView ->
            if (ApiHelper.hasOreo() && surfaceView.surfaceTexture?.isReleased != true) {
                Trace.d("Herry", "onViewDetachedFromWindow release")
                surfaceView.surfaceTexture?.release()
            }
        }

        onDetachedVideoView(model)
    }

    fun getVideoSurfaceView(): View? = holder?.videoView?.videoSurfaceView
}
