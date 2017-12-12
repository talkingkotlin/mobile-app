package com.talkingkotlin.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.talkingkotlin.model.rss.Item
import com.talkingkotlin.util.SingletonHolder

/**
 * Database definition with a Singleton holder for our database instance
 * @author Alexander Gherschon
 */
@Database(entities = arrayOf(Item::class), version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TKDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object : SingletonHolder<TKDatabase, Context>({
        Room.databaseBuilder(it.applicationContext,
                TKDatabase::class.java, "talkingkotlin.db")
                .fallbackToDestructiveMigration() // TODO migrate properly when released
                .build()
    })
}