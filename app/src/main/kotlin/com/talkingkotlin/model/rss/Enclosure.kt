package com.talkingkotlin.model.rss

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root
import java.io.Serializable

/**
 * Enclosure is an entity used to parse the RSS Feed
 * @author Alexander Gherschon
 */

@Root(name = "enclosure")
@Namespace(prefix = "itunes")
data class Enclosure @JvmOverloads constructor(
        @field:Attribute var type: String? = null,
        @field:Attribute var url: String? = null,
        @field:Attribute var length: Int? = null) : Serializable