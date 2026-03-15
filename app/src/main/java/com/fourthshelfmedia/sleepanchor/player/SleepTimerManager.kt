package com.fourthshelfmedia.sleepanchor.player

import android.os.CountDownTimer
import androidx.media3.common.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages the sleep timer with optional volume fade-out.
 * The fade begins [fadeDurationMs] before the timer expires,
 * linearly reducing volume from current level to 0.
 */
class SleepTimerManager(
    private val player: Player,
) {
    private var countDownTimer: CountDownTimer? = null
    private var fadeTimer: CountDownTimer? = null
    private var originalVolume: Float = 1f

    private val _remainingMs = MutableStateFlow(0L)
    val remainingMs: StateFlow<Long> = _remainingMs

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive

    /**
     * Start a sleep timer.
     * @param durationMs Total time until playback stops
     * @param fadeDurationMs How long to fade volume before stopping (default 5 min)
     */
    fun start(durationMs: Long, fadeDurationMs: Long = 5 * 60 * 1000L) {
        cancel() // Cancel any existing timer
        originalVolume = player.volume
        _isActive.value = true

        val fadeStart = (durationMs - fadeDurationMs).coerceAtLeast(0)

        countDownTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingMs.value = millisUntilFinished

                // Start fading when we're within fadeDurationMs of the end
                if (millisUntilFinished <= fadeDurationMs && fadeTimer == null) {
                    startFade(millisUntilFinished)
                }
            }

            override fun onFinish() {
                _remainingMs.value = 0
                _isActive.value = false
                player.pause()
                player.volume = originalVolume // Restore for next play
            }
        }.start()
    }

    private fun startFade(durationMs: Long) {
        val steps = (durationMs / 1000).toInt().coerceAtLeast(1)
        val volumeStep = originalVolume / steps

        fadeTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = 1f - (millisUntilFinished.toFloat() / durationMs)
                player.volume = (originalVolume * (1f - progress)).coerceAtLeast(0f)
            }

            override fun onFinish() {
                player.volume = 0f
            }
        }.start()
    }

    fun cancel() {
        countDownTimer?.cancel()
        countDownTimer = null
        fadeTimer?.cancel()
        fadeTimer = null
        player.volume = originalVolume
        _remainingMs.value = 0
        _isActive.value = false
    }

    /** Format remaining time as "1:23:45" or "23:45". */
    fun formatRemaining(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
}
