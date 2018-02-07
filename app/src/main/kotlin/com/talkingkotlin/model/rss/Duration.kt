package com.talkingkotlin.model.rss

import com.talkingkotlin.util.DURATION_TIME_FORMAT
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Duration is an entity to parse the duration if an item from the RSS Feed
 * @author Alexander Gherschon
 */

@Root(name = "duration")
@Namespace(prefix = "itunes")
class Duration @JvmOverloads constructor(
        @field:Text var value: String? = null) : Serializable {

    companion object {
        private var durationFormatter = SimpleDateFormat(DURATION_TIME_FORMAT, Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    val timestamp: Long
        get() = durationFormatter.parse(value).time
}
