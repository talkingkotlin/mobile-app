package com.talkingkotlin.util

import android.widget.SeekBar

/**
 * Simple Listener for SeekBar.OnSeekBarChangeListener (so we don't have to have empty functions in our code)
 * @author Alexander Gherschon
 */
open class SimpleOnSeekBarChangeListener: SeekBar.OnSeekBarChangeListener {

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }
}