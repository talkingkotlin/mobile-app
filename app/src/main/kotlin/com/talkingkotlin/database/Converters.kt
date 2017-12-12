package com.talkingkotlin.database

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.talkingkotlin.model.rss.Duration
import com.talkingkotlin.model.rss.Enclosure
import com.talkingkotlin.model.rss.Image
import java.util.*


/**
 * Room converters
 * @author Alexander Gherschon
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = if (value == null) null else Date(value)

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromEnclosure(value: Enclosure?): String? =
            if (value == null) null else Gson().toJson(value)

    @TypeConverter
    fun stringToEnclosure(value: String?): Enclosure? =
            if(value == null) null else Gson().fromJson(value, Enclosure::class.java)

    @TypeConverter
    fun fromImage(value: Image?): String? = if (value == null) null else Gson().toJson(value)

    @TypeConverter
    fun stringToImage(value: String?): Image? =
            if(value == null) null else Gson().fromJson(value, Image::class.java)

    @TypeConverter
    fun fromDuration(value: Duration?): String? = if (value == null) null else Gson().toJson(value)

    @TypeConverter
    fun stringToDuration(value: String?): Duration? =
            if(value == null) null else Gson().fromJson(value, Duration::class.java)
}