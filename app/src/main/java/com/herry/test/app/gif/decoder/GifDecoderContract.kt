package com.herry.test.app.gif.decoder

import android.graphics.Bitmap
import com.herry.libs.mvp.MVPView
import com.herry.test.app.base.mvp.BaseMVPPresenter
import com.herry.test.data.GifMediaFileInfoData
import java.io.Serializable

/**
 * Created by herry.park on 2020/06/11.
 **/
interface GifDecoderContract {

    interface View : MVPView<Presenter> {
        fun onDecoded(mediaInfo: DecodedGifMediaInfo)
    }

    abstract class Presenter : BaseMVPPresenter<View>()

    data class DecodedGifMediaInfo(
        val data: GifMediaFileInfoData,
        val width: Int = 0,
        val height: Int = 0,
        val frameCounts: Int = 0,
        val totalDuration: Long = 0L,
        val frames: DecodedGifFrames
    ) : Serializable {
        override fun toString(): String {
            return "DecodedGifMediaInfo(data=$data, width=$width, height=$height, frameCounts=$frameCounts, totalDuration=$totalDuration, frames=$frames)"
        }
    }

    data class DecodedGifFrame(
        val bitmap: Bitmap,
        val delay: Long,
        val index: Int
    )

    data class DecodedGifFrames(
        val frames: MutableList<DecodedGifFrame> = mutableListOf()
    ) : Serializable
}