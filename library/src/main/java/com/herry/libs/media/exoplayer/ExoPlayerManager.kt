package com.herry.libs.media.exoplayer

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.herry.libs.log.Trace

@Suppress("unused")
class ExoPlayerManager(
    private val context: () -> Context?,
    private val isSingleInstance: Boolean = false,
    private val progressUpdateIntervalMs: Long = MAX_UPDATE_INTERVAL_MS,
    private val onListener: OnListener? = null
) {

    companion object {
        private const val MAX_UPDATE_INTERVAL_MS = 1_000L
    }

    private val tag = "ExoPlayerManager"
    private val playerMap = HashMap<String, ExoPlayer>()
    private val playerProgressUpdater = HashMap<String, Handler>()
    private var isMute = false

    interface OnListener {
        fun onPlayerStateChanged(id: String, state: PlayerState) {}
        fun onPlayWhenReadyChanged(id: String, playWhenReady: Boolean, reason: PlayWhenReadyChangeReason) {}
        fun onIsPlayingChanged(id: String, isPlaying: Boolean) {}
        fun onProgressUpdated(id: String, current: Long, buffered: Long, total: Long) {}
    }

    enum class PlayerState(val value: Int) {
        UNKNOWN (-1),
        IDLE (Player.STATE_IDLE),
        BUFFERING (Player.STATE_BUFFERING),
        READY (Player.STATE_READY),
        ENDED (Player.STATE_ENDED);

        companion object {
            fun generate(value: Int): PlayerState = values().firstOrNull { it.value == value } ?: UNKNOWN
        }
    }

    enum class PlayWhenReadyChangeReason(val value: Int) {
        UNKNOWN (-1),
        /** Playback has been started or paused by a call to [.setPlayWhenReady].  */
        USER_REQUEST (Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST), // 1
        /** Playback has been paused because of a loss of audio focus.  */
        AUDIO_FOCUS_LOSS (Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS), // 2
        /** Playback has been paused to avoid becoming noisy.  */
        AUDIO_BECOMING_NOISY (Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY), // 3
        /** Playback has been started or paused because of a remote change.  */
        REMOTE (Player.PLAY_WHEN_READY_CHANGE_REASON_REMOTE), //4
        /** Playback has been paused at the end of a media item.  */
        END_OF_MEDIA_ITEM (Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM); //5

        companion object {
            fun generate(value: Int): PlayWhenReadyChangeReason = values().firstOrNull { it.value == value } ?: UNKNOWN
        }
    }

    private fun internalPrepare(id: String, url: String, fromPlay: Boolean): ExoPlayer? {
        val context = context.invoke() ?: return null

        var player = playerMap[id]
        if (player == null) {
            player = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val state = PlayerState.generate(playbackState)
                        Trace.d(tag, "onPlaybackStateChanged[$id]: playbackState: $state")
                        onListener?.onPlayerStateChanged(id, state)
                        updateProgress(id)
                    }

                    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                        val playWhenReadyChangeReason = PlayWhenReadyChangeReason.generate(reason)
                        Trace.d(tag, "onPlayWhenReadyChanged[$id]: playWhenReady: $playWhenReady reason: $playWhenReadyChangeReason")
                        onListener?.onPlayWhenReadyChanged(id, playWhenReady, playWhenReadyChangeReason)
                        updateProgress(id)
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Trace.d(tag, "onIsPlayingChanged[$id]: isPlaying: $isPlaying")
                        onListener?.onIsPlayingChanged(id, isPlaying)
                        updateProgress(id)
                    }
                })
            }
            playerMap[id] = player
        }

        if (player.playbackState == Player.STATE_IDLE) {
            player.setMediaSource(
                ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
                    .createMediaSource(MediaItem.fromUri(url))
            )

            if (isSingleInstance) {
                if (fromPlay) {
                    Trace.d(tag, "[$tag] prepare for $id")
                    player.prepare()
                }
            } else {
                Trace.d(tag, "[$tag] prepare for $id")
                player.prepare()
            }
        }

        Trace.d(tag, "[$tag] using media code counts: (${playerMap.size})")

        return player
    }

    fun prepare(id: String, url: String): ExoPlayer? {
        return internalPrepare(id, url, false)
    }

    fun getPlayer(id: String): ExoPlayer? = playerMap[id]

    fun play(id: String, url: String, repeat: Boolean = false) {
        var player = playerMap[id]
        if (player == null) {
            player = internalPrepare(id, url, true) ?: return
            player.repeatMode = if (repeat) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            player.playWhenReady = true
        } else {
            player.repeatMode = if (repeat) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            if (isSingleInstance) {
                Trace.d(tag, "[$tag] prepare for $id")
                player.prepare()
                player.playWhenReady = true

                Trace.d(tag, "[$tag] using media code counts: (${playerMap.size})")
            } else {
                Trace.d(tag, "[$tag] play for $id")
                player.play()
            }
        }

        setVolume(player, isMute)
    }

    fun stop(id: String) {
        val player = playerMap[id]
        player?.let { exoPlayer ->
            exoPlayer.stop()
            exoPlayer.release()

            playerMap.remove(id)

            Trace.d(tag, "[$tag] stopped for $id, player total counts = ${playerMap.size}")
        }

        Trace.d(tag, "[$tag] using media code counts: (${playerMap.size})")

        stopProgressUpdater(id)
    }

    fun stopAll() {
        Trace.d(tag, "[$tag] stop all (${playerMap.size})")
        playerMap.values.forEach { exoPlayer ->
            exoPlayer.stop()
            exoPlayer.release()
        }
        playerMap.clear()

        stopAllProgressUpdater()
    }

    fun isPlaying(id: String): Boolean {
        val player = playerMap[id] ?: return false
        return player.playbackState == Player.STATE_READY && player.isPlaying
    }

    fun isReadyToPlay(id: String): Boolean {
        val player = playerMap[id] ?: return false
        return player.playbackState == Player.STATE_READY
    }

    fun isEnd(id: String): Boolean {
        val player = playerMap[id] ?: return false
        return player.playbackState == Player.STATE_ENDED
    }

    fun pause(id: String) {
        playerMap[id]?.pause()
    }

    fun resume(id: String) {
        playerMap[id]?.let { player ->
            setVolume(player, isMute)
            if (PlayerState.generate(player.playbackState) == PlayerState.ENDED) {
                player.seekTo(0)
            }
            player.play()
        }
    }

    fun seekTo(id: String, time: Long) {
        playerMap[id]?.seekTo(time)
    }

    fun isMute(): Boolean = this.isMute

    private fun setVolume(player: ExoPlayer, mute: Boolean) {
        player.volume = if (mute) 0f else 1f
    }

    fun mute() {
        playerMap.values.forEach { exoPlayer ->
            setVolume(exoPlayer, true)
        }
        this.isMute = true
    }

    fun unMute() {
        playerMap.values.forEach { exoPlayer ->
            setVolume(exoPlayer, false)
        }
        this.isMute = false
    }

    private fun updateProgress(id: String) {
        val player = playerMap[id] ?: return

        val currentPosition = player.currentPosition
        val bufferedPosition = player.bufferedPosition
        val totalPosition = player.contentDuration

        if (totalPosition >= 0) {
            onListener?.onProgressUpdated(id, currentPosition, bufferedPosition, totalPosition)
        }

        removeProgressUpdatingCallback(id)
        val playbackState = player.playbackState

        if (player.isPlaying) {
            var mediaTimeDelayMs = progressUpdateIntervalMs

            // Limit delay to the start of the next full second to ensure position display is smooth.
            val mediaTimeUntilNextFullSecondMs: Long = 1000 - currentPosition % 1000
            mediaTimeDelayMs = kotlin.math.min(mediaTimeDelayMs, mediaTimeUntilNextFullSecondMs)

            // Calculate the delay until the next update in real time, taking playback speed into account.
            val playbackSpeed: Float = player.playbackParameters.speed
            var delayMs = if (playbackSpeed > 0) (mediaTimeDelayMs / playbackSpeed).toLong() else MAX_UPDATE_INTERVAL_MS

            // Constrain the delay to avoid too frequent / infrequent updates.
            delayMs = kotlin.math.max(progressUpdateIntervalMs, kotlin.math.min(delayMs, MAX_UPDATE_INTERVAL_MS))
            postProgressUpdatingDelayed(id, delayMs)
        } else if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE) {
            postProgressUpdatingDelayed(id, MAX_UPDATE_INTERVAL_MS)
        }
    }

    private fun removeProgressUpdatingCallback(id: String) {
        val handler = playerProgressUpdater[id] ?: return
        handler.removeCallbacksAndMessages(null)
    }

    private fun postProgressUpdatingDelayed(id: String, delayMs: Long = 0) {
        val handler = playerProgressUpdater[id] ?: run {
            val newHandler = Handler(Looper.getMainLooper())
            playerProgressUpdater[id] = newHandler
            newHandler
        }
        handler.postDelayed({ updateProgress(id) }, delayMs)
    }

    private fun stopProgressUpdater(id: String) {
        playerProgressUpdater[id]?.removeCallbacksAndMessages(null)
        playerProgressUpdater.remove(id)
    }

    private fun stopAllProgressUpdater() {
        playerProgressUpdater.values.forEach { handler ->
            handler.removeCallbacksAndMessages(null)
        }
        playerProgressUpdater.clear()
    }
}