package com.fourthshelfmedia.sleepanchor.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import android.util.Log
import com.fourthshelfmedia.sleepanchor.MainActivity

/**
 * Background media playback service.
 * Handles lock screen controls, notification, Bluetooth, and audio focus automatically.
 */
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true
            )
            .setHandleAudioBecomingNoisy(true) // Pause when headphones unplugged
            .build()

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                Log.d("SleepAnchor", "Now playing: ${mediaItem?.mediaId} uri=${mediaItem?.localConfiguration?.uri}")
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                val stateName = when (playbackState) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN($playbackState)"
                }
                Log.d("SleepAnchor", "Playback state: $stateName")
                if (playbackState == Player.STATE_IDLE && (player as ExoPlayer).playerError != null) {
                    Log.e("SleepAnchor", "Player error: ${(player as ExoPlayer).playerError?.message}", (player as ExoPlayer).playerError)
                }
            }
        })

        val sessionActivityIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    companion object {
        /**
         * Build a MediaItem from track data for the player queue.
         */
        fun buildMediaItem(
            trackId: String,
            title: String,
            artist: String,
            audioUrl: String,
            coverArtUrl: String,
        ): MediaItem {
            return MediaItem.Builder()
                .setMediaId(trackId)
                .setUri(audioUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(artist)
                        .setArtworkUri(android.net.Uri.parse(coverArtUrl))
                        .build()
                )
                .build()
        }
    }
}
