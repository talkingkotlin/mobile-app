package com.talkingkotlin.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.talkingkotlin.database.TKDatabase
import com.talkingkotlin.model.network.Resource
import com.talkingkotlin.model.rss.Item
import com.talkingkotlin.model.rss.Rss
import com.talkingkotlin.repository.RssFeedRepository

/**
 * View Model to keep the data alive during configuration changes
 * This class does the following:
 * 1. Load the data from the database if already exist (if so present it)
 * 2. Load the data from the network (which updates the observed database from point 1)
 * @author Alexander Gherschon
 */

class ItemsViewModel : ViewModel() {

    companion object {
        private val TAG = ItemsViewModel::class.java.simpleName
    }

    private val mediator by lazy {
        MediatorLiveData<Resource<List<Item>>>()
    }

    private val feedRepository: RssFeedRepository by lazy {
        RssFeedRepository()
    }

    /**
     * Gets the items from the database and triggers a network update of the list of items
     */
    fun getItems(context: Context): LiveData<Resource<List<Item>>> {

        // Use the existing data from the Items table and observe it (through Room)
        mediator.addSource(TKDatabase.getInstance(context).itemDao().getAll(), { items ->
            // no data yet in the table, we will presume we need to load for the first time the table
            mediator.value = if (items == null || items.isEmpty()) Resource.Loading() else Resource.Success(items)
        })

        // update the list of items with a network call
        updateItems(context)
        return mediator
    }

    /**
     * Updates the items from a network call and manages the other states (no data, error loading) depending on the mediator state
     */
    private fun updateItems(context: Context) {

        val networkRequest = feedRepository.getFeed(context)
        // add the request data as a a source, and act on its data
        mediator.addSource(networkRequest, { resource: Resource<Rss>? ->
            // we get updates from observing the database, so here we will only pay attention to the status ERROR
            if (resource is Resource.NetworkError) {
                // if we already delivered data (from the database) we will notify an error but still show data
                val itemsResource: Resource<List<Item>>? = mediator.value

                if (itemsResource is Resource.Success) {
                    mediator.value = Resource.NetworkError(itemsResource.data)
                }
            }
            // remove this source as we won't need it observed anymore
            mediator.removeSource(networkRequest)
        })
    }
}