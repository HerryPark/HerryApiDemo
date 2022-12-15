package com.herry.test.app.gif.decoder

import com.herry.libs.media.gif.decoder.GifDecoder
import com.herry.libs.media.gif.decoder.GifHeader
import com.herry.test.data.GifMediaFileInfoData
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers


/**
 * Created by herry.park on 2020/06/11.
 **/
class GifDecoderPresenter(private val data: GifMediaFileInfoData) : GifDecoderContract.Presenter() {

    override fun onResume(view: GifDecoderContract.View, state: ResumeState) {
        if (state.isLaunch()) {
            decode()
        }
    }

    private fun decode() {
        subscribeObservable(
            getDecodedGif(data)
            , {
                launch(Dispatchers.Main) {
                    view?.onDecoded(it)
                }
            },
            loadView = true
        )
    }

    private fun getDecodedGif(data: GifMediaFileInfoData) : Observable<GifDecoderContract.DecodedGifMediaInfo> {
        return Observable.create { emitter ->
            val decoder = GifDecoder()
            try {
                decoder.setData(data.path)

                val header: GifHeader = decoder.header ?: run {
                    emitter.onError(Throwable())
                    return@create
                }

                val frames = mutableListOf<GifDecoderContract.DecodedGifFrame>()
                do {
                    val bitmap = decoder.getNextFrame()
                    val currentFrameIndex = decoder.getCurrentFrameIndex()
                    val delay = decoder.getDelay(currentFrameIndex)
                    if (null != bitmap) {
                        frames.add(GifDecoderContract.DecodedGifFrame(
                            bitmap = bitmap,
                            delay = delay,
                            index = frames.size
                        ))
                    }
                    decoder.advance()
                } while (bitmap != null && frames.size < decoder.getFrameCount())

                val duration = decoder.getDuration()

                decoder.clear()

                emitter.onNext(
                    GifDecoderContract.DecodedGifMediaInfo(
                        data = data,
                        width = header.width,
                        height = header.height,
                        frameCounts = header.frameCount,
                        totalDuration = duration,
                        frames = GifDecoderContract.DecodedGifFrames(frames)
                    ))

                emitter.onComplete()
                return@create
            } catch (ex: Exception) {
                emitter.onError(Throwable())
            }
        }
    }
}