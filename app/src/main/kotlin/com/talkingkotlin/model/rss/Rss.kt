package com.talkingkotlin.model.rss

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * Rss is the root element in the RSS Feed
 * @author Alexander Gherschon
 */

@Root(name = "rss", strict = false)
data class Rss @JvmOverloads constructor(
        @field:Element(name = "channel") var channel: Channel? = null
)
