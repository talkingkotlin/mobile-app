package com.talkingkotlin.presenter

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import com.talkingkotlin.util.PLAYBACK_SPEED
import com.talkingkotlin.util.Preference
import com.talkingkotlin.util.progressToSpeed
import com.talkingkotlin.util.speedToProgress

/**
 * Player Controls Presenter is the presentation layer for the player controls
 * @author Alexander Gherschon
 */
class SettingsPresenter(private val context: Context, private val callback: SettingsPresenterCallback) : LifecycleObserver {

    /**
     * Callbacks to be implemented by any class using this presenter
     */
    interface SettingsPresenterCallback {
        fun notifyService()
        fun setValues(showingValue: String, currentValue: Int, maxValue: Int)
        fun setText(text: String)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        val playbackSpeed: Float by Preference(context, PLAYBACK_SPEED, 1f)
        val currentValue = speedToProgress(playbackSpeed)
        val maxValue = 5
        callback.setValues(playbackSpeed.toString(), currentValue, maxValue)
    }

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")
    fun progressChanged(context: Context, progress: Int) {

        var playbackSpeed: Float by Preference(context, PLAYBACK_SPEED, 1f)

        val calcSpeed = progressToSpeed(progress)
        // saving the new playback speed into shared preferences
        playbackSpeed = calcSpeed
        // notifying the service the value of playback speed changed
        callback.notifyService()
        // update the text on the right of the bar
        callback.setText(calcSpeed.toString())
    }
}