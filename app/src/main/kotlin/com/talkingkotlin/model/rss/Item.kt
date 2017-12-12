package com.talkingkotlin.model.rss

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import java.io.Serializable
import java.util.*

/**
 * Item is the representation of the item list from the RSS Feed
 * @author Alexander Gherschon
 */

@Entity
@Root(name = "item", strict = false)
data class Item @Ignore @JvmOverloads constructor(
        @field:PrimaryKey @field:Element var title: String,
        @field:ColumnInfo @field:Element var description: String? = null,
        @field:ColumnInfo @field:Element var pubDate: Date? = null,
        @field:ColumnInfo @field:Element var duration: Duration? = null,
        @field:ColumnInfo @field:Element var enclosure: Enclosure? = null,
        @field:ColumnInfo @field:Element var image: Image? = null,
        @field:ColumnInfo var position: Long? = null,
        @field:ColumnInfo var listened: Boolean = false) : Serializable {
    constructor() : this("")
}