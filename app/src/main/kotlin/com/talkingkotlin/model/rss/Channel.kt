package com.talkingkotlin.model.rss

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * Channel is part of the entities to parse the RSS Feed
 * @author Alexander Gherschon
 */

@Root(name = "channel", strict = false)
data class Channel @JvmOverloads constructor(
        @field:ElementList(name = "item", inline = true) var items: List<Item>? = null
)