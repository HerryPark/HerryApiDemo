package com.herry.libs.app.activity_caller.result

import android.net.Uri

class TakeMediaRequest internal constructor() {
    var mediaType: ACActivityResultContracts.TakeMediaType = ACActivityResultContracts.TakeImage
        internal set

    var input: Uri? = null
        internal set

    class Builder {
        private var mediaType: ACActivityResultContracts.TakeMediaType = ACActivityResultContracts.TakeImage
        private var input: Uri? = null

        fun setMediaType(mediaType: ACActivityResultContracts.TakeMediaType) : Builder {
            this.mediaType = mediaType
            return this
        }

        fun setInputUri(input: Uri) : Builder {
            this.input = input
            return this
        }

        fun build() : TakeMediaRequest = TakeMediaRequest().apply {
            this.mediaType = this@Builder.mediaType
            this.input = this@Builder.input
        }
    }
}
