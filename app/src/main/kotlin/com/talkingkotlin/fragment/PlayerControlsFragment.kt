package com.talkingkotlin.fragment

import android.content.Intent
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.talkingkotlin.R
import com.talkingkotlin.model.event.PlayingInfo
import com.talkingkotlin.presenter.PlayerControlsPresenter
import com.talkingkotlin.service.PlayerService
import kotlinx.android.synthetic.main.fragment_player_control.*

/**
 * Players Controls Fragment is the component presenting and interacting with the players controls
 * @author Alexander Gherschon
 */
class PlayerControlsFragment : Fragment() {

    companion object {
        private val TAG = PlayerControlsFragment::class.java.simpleName
    }

    private val presenter: PlayerControlsPresenter by lazy {
        PlayerControlsPresenter(object : PlayerControlsPresenter.PlayerControlsCallback {

            override fun updatePlayPauseButton(pictureUrl: String, title: String, @DrawableRes buttonIcon: Int, clickListener: View.OnClickListener) {
                player_title.text = title
                Picasso.with(context)
                        .load(pictureUrl)
                        .placeholder(ContextCompat.getDrawable(context!!, R.drawable.placeholder))
                        .into(player_image)

                player_button_play_pause.setImageDrawable(context!!.getDrawable(buttonIcon))
                player_button_play_pause.setOnClickListener(clickListener)

                progressBar.progress = 0
                progressBar.isIndeterminate = true
            }

            override fun showIndeterminateProgressBar(showIndeterminate: Boolean) {
                progressBar.isIndeterminate = showIndeterminate
            }

            override fun updateProgressBarPosition(playingInfo: PlayingInfo) {
                if (progressBar.isIndeterminate) {
                    progressBar.isIndeterminate = false
                }

                progressBar.progress = playingInfo.position.toInt()
                progressBar.max = playingInfo.duration.toInt()
            }

            override fun sendPlayerAction(action: String) {
                val intent = Intent(context, PlayerService::class.java)
                intent.action = action
                context?.startService(intent)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        lifecycle.addObserver(presenter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_player_control, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        player_button_stop.setOnClickListener {
            presenter.stopButtonPressed()
        }
    }
}
