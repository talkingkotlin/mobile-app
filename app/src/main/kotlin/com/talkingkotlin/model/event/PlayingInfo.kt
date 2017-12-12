package com.talkingkotlin.model.event

import com.talkingkotlin.model.rss.Item

/**
 * Bus event to communicate the Playing Information through the app
 * @author Alexander Gherschon
 */
data class PlayingInfo(val item: Item, val position: Long, val duration: Long)