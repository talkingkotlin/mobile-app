package com.talkingkotlin.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.talkingkotlin.R
import com.talkingkotlin.presenter.SettingsPresenter
import com.talkingkotlin.service.PlayerService
import com.talkingkotlin.util.ACTION_UPDATE_PLAYBACK_SPEED
import com.talkingkotlin.util.SimpleOnSeekBarChangeListener
import kotlinx.android.synthetic.main.fragment_settings.*

/**
 * Settings Fragment is the component presenting and interacting with settings
 * @author Alexander Gherschon
 */
class SettingsFragment : Fragment() {

    companion object {
        private val TAG = SettingsFragment::class.java.simpleName
    }

    private val presenter: SettingsPresenter by lazy {
        SettingsPresenter(context!!, object : SettingsPresenter.SettingsPresenterCallback {

            override fun notifyService() {
                val intent = Intent(context, PlayerService::class.java)
                intent.action = ACTION_UPDATE_PLAYBACK_SPEED
                context?.startService(intent)
            }

            override fun setValues(showingValue: String, currentValue: Int, maxValue: Int) {
                setText(showingValue)
                seekBar.progress = currentValue
                seekBar.max = maxValue
            }

            override fun setText(text: String) {
                speed.text = getString(R.string.speed_format, text)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        lifecycle.addObserver(presenter)
        seekBar.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                presenter.progressChanged(context!!, progress)
            }
        })
    }
}
