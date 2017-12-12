package com.talkingkotlin.manager

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.session.MediaSessionManager
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.support.v4.app.NotificationCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.talkingkotlin.R
import com.talkingkotlin.activity.MainActivity
import com.talkingkotlin.database.TKDatabase
import com.talkingkotlin.model.event.PlayingInfo
import com.talkingkotlin.model.player.PlayerState
import com.talkingkotlin.model.rss.Item
import com.talkingkotlin.service.PlayerService
import com.talkingkotlin.util.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Presentation/Manager Layer of the Player itself
 * Think of a Manager as a Presenter for non-UI components
 * @author Alexander Gherschon
 */
class PlayerManager(private val callback: PlayerPresenterCallback) {

    interface PlayerPresenterCallback {
        fun stopService()
        fun showNotification(notification: Notification?)
    }

    private var bandwidthMeter: DefaultBandwidthMeter? = null
    private var trackSelectionFactory: AdaptiveTrackSelection.Factory? = null
    private var trackSelector: DefaultTrackSelector? = null

    private var player: ExoPlayer? = null
    private var mediaSessionManager: MediaSessionManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaController: MediaControllerCompat? = null

    private var currentItem: Item? = null
    private var playerState: PlayerState = PlayerState.Started()

    private var initialized: AtomicBoolean = AtomicBoolean(false)
    private var countdownTimer: CountDownTimer? = null

    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private lateinit var onAudioFocusChange: OnAudioFocusChangeListener
    private var hasAudioFocus: Boolean = false

    fun initialize(context: Context) {

        if (!initialized.get()) {

            bandwidthMeter = DefaultBandwidthMeter()
            trackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
            trackSelector = DefaultTrackSelector(trackSelectionFactory)

            player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)

            updatePlaybackSpeed(context)

            player!!.addListener(object : SimpleExoEventListener() {

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

                    when (playbackState) {
                        ExoPlayer.STATE_READY -> onPlayerReady(context)
                        ExoPlayer.STATE_ENDED -> onPlayerStop(context)
                        else -> if (countdownTimer != null) countdownTimer!!.cancel()
                    }
                }
            })

            // media session stuff
            mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            mediaSession = MediaSessionCompat(context, "MusicService")
            mediaController = MediaControllerCompat(context, mediaSession!!.sessionToken)
            mediaSession?.setCallback(object : MediaSessionCompat.Callback() {

                override fun onPlay() {
                    player?.playWhenReady = true
                }

                override fun onPause() {
                    player?.playWhenReady = false
                }

                override fun onStop() {
                    player?.stop()
                }
            })

            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            onAudioFocusChange = object : OnAudioFocusChangeListener {
                override fun onAudioFocusChange(focusChange: Int) {

                    logd( "In onAudioFocusChange (${this.toString().substringAfterLast("@")}), focus changed to = $focusChange while playerState is ${playerState.javaClass.simpleName}")

                    if (playerState !is PlayerState.Stopped) {

                        when (focusChange) {
                            AudioManager.AUDIOFOCUS_GAIN -> resume(context)
                            AudioManager.AUDIOFOCUS_LOSS -> pause(context, abandonAudioFocus = true)
                            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause(context)
                        }
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()

                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(audioAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(onAudioFocusChange)
                        .build()
            }

            initialized.set(true)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun requestAudioFocus(): Boolean {

        logd("requestAudioFocus() called")

        val focusRequest: Int? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager?.requestAudioFocus(audioFocusRequest)
        } else {
            @Suppress("DEPRECATION") // why do we need to deprecated if added @TargetApi ?
            audioManager?.requestAudioFocus(onAudioFocusChange, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }

        return focusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun abandonAudioFocus(): Boolean {

        logd("abandonAudioFocus() called")

        val focusRequest: Int? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager?.abandonAudioFocusRequest(audioFocusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(onAudioFocusChange)
        }

        return focusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun onPlayerReady(context: Context) {
        val duration = player?.duration ?: 0

        if (countdownTimer == null) {
            countdownTimer = object : CountDownTimer(duration, 1000) {
                override fun onTick(tick: Long) {

                    val position = player?.currentPosition ?: 0
                    EventBus.getDefault().post(PlayingInfo(currentItem!!, position, duration))

                    async(CommonPool) {
                        currentItem?.let {
                            TKDatabase.getInstance(context)
                                    .itemDao()
                                    .updatePosition(it.title, position)
                        }
                    }
                }

                override fun onFinish() {}

            }.start()
        }
    }

    private fun onPlayerStop(context: Context) {

        async(CommonPool) {
            currentItem?.let {
                TKDatabase.getInstance(context)
                        .itemDao()
                        .updateListened(it.title, listened = true)
            }
        }

        // as we finished to listen to an episode, closes the service
        stop()
    }

    fun play(context: Context, item: Item, carryOn: Boolean) {

        if (!hasAudioFocus) {
            hasAudioFocus = requestAudioFocus()
        }

        if (hasAudioFocus) {
            currentItem = item
            player?.stop()
            val dataSourceFactory = DefaultDataSourceFactory(context, "Talking-Kotlin-Android")
            val extractorsFactory = DefaultExtractorsFactory()

            val mediaSource = ExtractorMediaSource(Uri.parse(currentItem!!.enclosure!!.url),
                    dataSourceFactory,
                    extractorsFactory,
                    null, null)

            if (carryOn) {
                player?.seekTo(currentItem!!.position!!)

/*
            // Test the end playing by positioning the player at 99% of the episode
            val durationStr = currentItem!!.duration!!.value!!
            val units: List<String> = durationStr.split(":")
            val milliseconds = (units[0].toInt() * 3600 + units[1].toInt() * 60 + units[2].toInt()) * 1000 * 99 / 100L
            player?.seekTo(milliseconds)
*/
            }
            player?.playWhenReady = true
            player?.prepare(mediaSource)

            playerState = PlayerState.Playing(currentItem!!)
            broadcastCurrentState()
            showNotification(context, ACTION_PLAY)
        }
    }

    fun pause(context: Context, abandonAudioFocus: Boolean = false) {

        if(abandonAudioFocus && hasAudioFocus) {
            hasAudioFocus = !abandonAudioFocus()
        }

        if(!abandonAudioFocus || (abandonAudioFocus && !hasAudioFocus)) {

            mediaController?.transportControls?.pause()
            showNotification(context, ACTION_PAUSE)
            playerState = PlayerState.Paused(currentItem!!)
            broadcastCurrentState()
        }
    }

    fun stop() {
        
        if(hasAudioFocus) {
            hasAudioFocus = !abandonAudioFocus()
        }
        
        mediaController?.transportControls?.stop()
        playerState = PlayerState.Stopped()

        if (countdownTimer != null) {
            countdownTimer!!.cancel()
        }

        broadcastCurrentState()
        callback.stopService()
    }

    fun resume(context: Context) {

        if (!hasAudioFocus) {
            hasAudioFocus = requestAudioFocus()
        }

        if (hasAudioFocus) {
            mediaController?.transportControls?.play()
            playerState = PlayerState.Playing(currentItem!!)
            broadcastCurrentState()
            showNotification(context, ACTION_PLAY)
        }
    }

    fun broadcastCurrentState() {
        EventBus.getDefault().post(playerState)
    }

    fun onDestroy() {
        player?.release()
        mediaSession?.release()

        if (countdownTimer != null) {
            countdownTimer!!.cancel()
        }
    }

    private fun showNotification(context: Context, action: String) {
        val style = android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession!!.sessionToken)
        val pIntent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 99, pIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = "listening_channel_id"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Listening"
            val importance = NotificationManager.IMPORTANCE_LOW
            val notificationChannel = NotificationChannel(channelId, channelName, importance)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(currentItem!!.title)
                //.setContentText("Talking Kotlin")
                .setStyle(style)
                .setContentIntent(pi)

        notificationBuilder.addAction(action(context, R.drawable.ic_stop_black_24dp, context.getString(R.string.stopped), ACTION_STOP))

        val iconRes = if (action == ACTION_PLAY) R.drawable.ic_pause_black_24dp else R.drawable.ic_play_arrow_black_24dp
        val nextAction = if (action == ACTION_PLAY) ACTION_PAUSE else ACTION_RESUME
        val title = context.getString(if (action == ACTION_PLAY) R.string.pause else R.string.resume)

        notificationBuilder.addAction(action(context, iconRes, title, nextAction))
        callback.showNotification(notificationBuilder.build())
    }

    private fun action(context: Context, iconRes: Int, title: String, intentAction: String): NotificationCompat.Action {
        val intent = Intent(context, PlayerService::class.java)
        intent.action = intentAction
        val pi = PendingIntent.getService(context, 1, intent, 0)
        return NotificationCompat.Action(iconRes, title, pi)
    }

    fun updatePlaybackSpeed(context: Context) {
        val playbackSpeed: Float by Preference(context, PLAYBACK_SPEED, 1f)
        val params = PlaybackParameters(playbackSpeed, 1f)
        player!!.playbackParameters = params
    }

    fun networkConnectionChanged(connected: Boolean) {
        if (!connected) {
            callback.stopService()
        }
    }
}