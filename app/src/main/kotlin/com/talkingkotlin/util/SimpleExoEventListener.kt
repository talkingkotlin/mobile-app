package com.talkingkotlin.util

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray

/**
 * Simple Listener for ExoPlayer.EventListener (so we don't have to have empty functions in our code)
 * @author Alexander Gherschon
 */
open class SimpleExoEventListener: ExoPlayer.EventListener {

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
    }

    override fun onLoadingChanged(isLoading: Boolean) {
    }

    override fun onPositionDiscontinuity() {
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
    }
}