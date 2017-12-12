package com.talkingkotlin.model.rss

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root
import java.io.Serializable

/**
 * Image is an entity used to parse the image of an item of the RSS Feed
 * @author Alexander Gherschon
 */

@Root(name = "image")
@Namespace(prefix = "itunes")
data class Image @JvmOverloads constructor(
        @field:Attribute var href: String? = null) : Serializable