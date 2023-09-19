package com.herry.libs.media.exoplayer

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.herry.libs.log.Trace

@UnstableApi @Suppress("unused")
class ExoPlayerManager(
    private val context: () -> Context?,
    private val isSingleInstance: Boolean = false,
    private val progressUpdateIntervalMs: Long = MAX_UPDATE_INTERVAL_MS,
    private val loadControl: DefaultLoadControl? = DefaultLoadControl.Builder()
        .setBufferDurationsMs(8 * 1024, 64 * 1024, 50, 1024)
        .build(),
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

    enum class PlayerState {
        UNKNOWN,
        IDLE,
        BUFFERING,
        PLAYING,
        PAUSED,
        ENDED
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
            player = ExoPlayer.Builder(context).apply {
                loadControl?.let {
                    setLoadControl(it)
                }
            }.build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val state = convertToPlayerState(id, playbackState)
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

                    override fun onIsLoadingChanged(isLoading: Boolean) {
                        Trace.d(tag, "onIsLoadingChanged[$id]: isLoading: $isLoading")
                        super.onIsLoadingChanged(isLoading)
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Trace.d(tag, "onPlayerError[$id]: error: ${error.errorCode} ${error.errorCodeName}")
                        super.onPlayerError(error)
                    }

                    override fun onPlayerErrorChanged(error: PlaybackException?) {
                        Trace.d(tag, "onPlayerErrorChanged[$id]: error: ${error?.errorCode} ${error?.errorCodeName}")
                        super.onPlayerErrorChanged(error)
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
                    Trace.d(tag, "[$id] prepare for $url")
                    player.prepare()
                }
            } else {
                Trace.d(tag, "[$id] prepare for $url")
                player.prepare()
            }
        }

        Trace.d(tag, "prepare using media code counts: (${playerMap.size})")

        return player
    }

    @MainThread
    fun prepare(id: String, url: String): ExoPlayer? {
        return internalPrepare(id, url, false)
    }

    @MainThread
    fun getPlayer(id: String): ExoPlayer? = playerMap[id]

    @MainThread
    fun play(id: String, url: String, repeat: Boolean = false) {
        var player = playerMap[id]

        // release unused players for the single instance mode
        if (isSingleInstance) {
            playerMap.keys.filter { it != id }.forEach { unusedPlayerId -> stop(unusedPlayerId) }
        }

        if (player == null) {
            player = internalPrepare(id, url, true) ?: return
            player.repeatMode = if (repeat) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            player.playWhenReady = true
        } else {
            player.repeatMode = if (repeat) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo( 0L)
                player.play()
            } else {
                if (isSingleInstance) {
                    Trace.d(tag, "[$id] prepare")
                    player.prepare()
                    player.playWhenReady = true
                } else {
                    Trace.d(tag, "[$id] play")
                    player.play()
                }
            }
            Trace.d(tag, "play using media code counts: (${playerMap.size})")
        }

        setVolume(player, isMute)
    }

    @MainThread
    fun stop(id: String) {
        val player = playerMap[id]
        player?.let { exoPlayer ->
            exoPlayer.stop()
            exoPlayer.release()

            playerMap.remove(id)

            Trace.d(tag, "[$id] stopped, player total counts = ${playerMap.size}")
        }

        Trace.d(tag, "stop using media code counts: (${playerMap.size})")

        stopProgressUpdater(id)
    }

    @MainThread
    fun stopAll() {
        Trace.d(tag, "stop all (${playerMap.size})")
        playerMap.values.forEach { exoPlayer ->
            exoPlayer.stop()
            exoPlayer.release()
        }
        playerMap.clear()

        stopAllProgressUpdater()
    }

    @MainThread
    fun isPlaying(id: String): Boolean {
        val player = playerMap[id] ?: return false
        return player.isPlaying
    }

    fun isReadyToPlay(id: String): Boolean {
        val player = playerMap[id] ?: return false
        return player.playbackState == Player.STATE_READY
    }

    @MainThread
    fun isPaused(id: String): Boolean {
        return getPlayerStatus(id) == PlayerState.PAUSED
    }

    @MainThread
    fun isEnd(id: String): Boolean {
        val player = playerMap[id] ?: return false
        return player.playbackState == Player.STATE_ENDED || player.duration <= player.currentPosition + 1L
    }

    @MainThread
    fun isIdle(id: String): Boolean {
        val player = playerMap[id] ?: return false
        return player.playbackState == Player.STATE_IDLE
    }

    @MainThread
    fun pause(id: String) {
        playerMap[id]?.let { player ->
            Trace.d(tag, "[$id] pause")
            player.pause()
        }
    }

    @MainThread
    fun resume(id: String) {
        playerMap[id]?.let { player ->
            Trace.d(tag, "[$id] resume")
            setVolume(player, isMute)
            val currentPosition = player.currentPosition
            val totalDuration = player.duration - 1
            if (currentPosition >= totalDuration || convertToPlayerState(id, player.playbackState) == PlayerState.ENDED) {
                player.seekTo(0)
            }
            player.play()
        }
    }

    @MainThread
    fun seekTo(id: String, time: Long) {
        playerMap[id]?.let { player ->
            val seekingTime = (if (player.duration <= time) player.duration - 1L else time).run {
                if (this < 0L) 0L else this
            }
            Trace.d(tag, "[$id] seekTo ($time > $seekingTime)")
            player.seekTo(seekingTime)
        }
    }

    fun isMute(): Boolean = this.isMute

    private fun setVolume(player: ExoPlayer, mute: Boolean) {
        player.volume = if (mute) 0f else 1f
    }

    @MainThread
    fun mute() {
        playerMap.values.forEach { exoPlayer ->
            setVolume(exoPlayer, true)
        }
        this.isMute = true
    }

    @MainThread
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
        } else if (playbackState == Player.STATE_BUFFERING) {
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

    @MainThread
    private fun convertToPlayerState(id: String, playbackState: Int): PlayerState {
        return when (playbackState) {
            Player.STATE_IDLE -> PlayerState.IDLE
            Player.STATE_BUFFERING -> PlayerState.BUFFERING
            Player.STATE_READY -> {
                getPlayer(id)?.playWhenReady?.let { playing ->
                    if (playing) PlayerState.PLAYING else PlayerState.PAUSED
                } ?: PlayerState.UNKNOWN
            }
            Player.STATE_ENDED -> PlayerState.ENDED
            else -> PlayerState.UNKNOWN
        }
    }

    @MainThread
    fun getPlayerStatus(id: String): PlayerState {
        val player = getPlayer(id) ?: return PlayerState.UNKNOWN
        return convertToPlayerState(id, player.playbackState)
    }
}