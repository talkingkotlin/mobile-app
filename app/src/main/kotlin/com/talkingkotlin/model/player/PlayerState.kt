package com.talkingkotlin.model.player

import com.talkingkotlin.model.rss.Item
import java.io.Serializable

/**
 * Player State to be communicated through the app
 * @author Alexander Gherschon
 */

sealed class PlayerState: Serializable {

    interface Itemized {
        val item: Item
    }
    
    class Playing(override val item: Item) : PlayerState(), Itemized
    class Paused(override val item: Item) : PlayerState(), Itemized

    class Started: PlayerState()
    class Stopped : PlayerState()

    class Buffering: PlayerState()
    class Idle: PlayerState()

}
