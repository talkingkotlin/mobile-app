package com.talkingkotlin.presenter

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.Intent
import com.talkingkotlin.model.player.PlayerState
import com.talkingkotlin.service.PlayerService
import com.talkingkotlin.util.ACTION_STATUS
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Main Presenter is the presentation layer for the entry point of the app, the MainActivity
 * @author Alexander Gherschon
 */
class MainPresenter(private val context: Context, private val callback: MainPresenterCallback) : LifecycleObserver {

    /**
     * Callbacks to be implemented by any class using this presenter
     */
    interface MainPresenterCallback {

        fun showEpisodes()
        fun showPlayerControls()
        fun hidePlayerControls()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        callback.showEpisodes()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        callback.hidePlayerControls()
        requestPlayerState(context)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        EventBus.getDefault().register(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pause() {
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewPlayerState(playerState: PlayerState?) {
        when (playerState) {
            is PlayerState.Itemized -> callback.showPlayerControls()
            else -> callback.hidePlayerControls()
        }
    }

    private fun requestPlayerState(context: Context) {
        val intent = Intent(context, PlayerService::class.java)
        intent.action = ACTION_STATUS
        context.startService(intent)
    }
}