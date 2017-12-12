package com.talkingkotlin.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.talkingkotlin.model.rss.Item

/**
 * Room Data Access Object for items from the RSS Feed
 * @author Alexander Gherschon
 */
@Dao
interface ItemDao {

    @Query("SELECT * FROM item order by pubDate DESC")
    fun findAll(): List<Item>

    @Query("SELECT * FROM item order by pubDate DESC")
    fun getAll(): LiveData<List<Item>>

    @Query("SELECT * FROM item where title = :title")
    fun findByTitle(title: String): Item

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(items: List<Item>)

    @Query("UPDATE item set position = :position where title = :title")
    fun updatePosition(title: String, position: Long)

    @Query("UPDATE item set listened = :listened where title = :title")
    fun updateListened(title: String, listened: Boolean)
}