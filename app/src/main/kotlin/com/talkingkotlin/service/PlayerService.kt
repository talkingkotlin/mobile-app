package com.talkingkotlin.service

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.talkingkotlin.manager.PlayerManager
import com.talkingkotlin.model.rss.Item
import com.talkingkotlin.util.*

/**
 * Player Service is the Android Service for the player itself
 * @author Alexander Gherschon
 */
class PlayerService : Service() {

    private val networkReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            manager.networkConnectionChanged(context.isConnected())
        }
    }

    private val manager by lazy {
        PlayerManager(object : PlayerManager.PlayerPresenterCallback {

            override fun stopService() {
                stopSelf()
            }

            override fun showNotification(notification: Notification?) {
                startForeground(NOTIFICATION_ID, notification)
            }
        })
    }

    override fun onBind(intent: Intent) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        manager.initialize(this)

        if (intent != null && intent.action != null) {

            when (intent.action) {
                ACTION_PLAY -> {
                    if (intent.hasExtra(ITEM_ACTION)) {
                        val currentItem = intent.getSerializableExtra(ITEM_ACTION) as Item
                        val carryOn = intent.getBooleanExtra(EXTRA_CARRY_ON, false)
                        manager.play(applicationContext, currentItem, carryOn)
                    } else {
                        throw IllegalArgumentException("Cannot play without an item")
                    }
                }
                ACTION_PAUSE -> manager.pause(applicationContext, abandonAudioFocus = true)
                ACTION_STOP -> manager.stop()
                ACTION_RESUME -> manager.resume(applicationContext)
                ACTION_STATUS -> manager.broadcastCurrentState()
                ACTION_UPDATE_PLAYBACK_SPEED -> manager.updatePlaybackSpeed(this)
            }
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onDestroy() {
        unregisterReceiver(networkReceiver)
        manager.onDestroy()
        super.onDestroy()
    }
}

