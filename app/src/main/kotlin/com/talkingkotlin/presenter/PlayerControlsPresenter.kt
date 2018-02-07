package com.talkingkotlin.presenter

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.Intent
import android.support.annotation.DrawableRes
import android.view.View
import com.talkingkotlin.R
import com.talkingkotlin.model.event.PlayingInfo
import com.talkingkotlin.model.player.PlayerState
import com.talkingkotlin.service.PlayerService
import com.talkingkotlin.util.ACTION_PAUSE
import com.talkingkotlin.util.ACTION_RESUME
import com.talkingkotlin.util.ACTION_STOP
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Player Controls Presenter is the presentation layer for the player controls
 * @author Alexander Gherschon
 */

class PlayerControlsPresenter(private val callback: PlayerControlsCallback): LifecycleObserver {

    companion object {
        private val TAG = PlayerControlsPresenter::class.java.simpleName
    }

    /**
     * Callbacks to be implemented by any class using this presenter
     */
    interface PlayerControlsCallback {
        fun updatePlayPauseButton(pictureUrl: String, title: String, @DrawableRes buttonIcon: Int, clickListener: View.OnClickListener)
        fun updateProgressBarPosition(playingInfo: PlayingInfo)
        fun sendPlayerAction(action: String)
        fun showIndeterminateProgressBar(showIndeterminate: Boolean)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun start() {
        EventBus.getDefault().register(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun stop() {
        EventBus.getDefault().unregister(this)
    }

    fun stopButtonPressed() {
        callback.sendPlayerAction(ACTION_STOP)
    }

    private fun sendPlayerAction(context: Context, action: String) {
        val intent = Intent(context, PlayerService::class.java)
        intent.action = action
        context.startService(intent)
    }

    @Subscribe
    fun onPlayerStateEvent(playerState: PlayerState) {

        if (playerState is PlayerState.Itemized) {
            val title = playerState.item.title
            val pictureUrl = playerState.item.image!!.href!!
            val iconRes = if (playerState is PlayerState.Paused) R.drawable.ic_play_arrow_black_36dp else R.drawable.ic_pause_black_36dp // TODO change to an AVD, if Nick sees this he will kill me
            val clickListener = View.OnClickListener {

                val nextStatus = if (playerState is PlayerState.Paused) ACTION_RESUME else ACTION_PAUSE
                callback.sendPlayerAction(nextStatus)
            }

            callback.updatePlayPauseButton(pictureUrl, title, iconRes, clickListener)
        }
    }

    @Subscribe
    fun onPlayingPosition(playingInfo: PlayingInfo) {
        callback.updateProgressBarPosition(playingInfo)
    }
}