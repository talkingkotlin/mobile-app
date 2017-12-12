package com.talkingkotlin.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.talkingkotlin.database.TKDatabase
import com.talkingkotlin.model.network.Resource
import com.talkingkotlin.model.rss.Rss
import com.talkingkotlin.network.GetRssFeedRequest
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import retrofit2.Response

/**
 * Rss Feed Repository gets the RSS Feed and updates the items table in the database accordingly
 * @author Alexander Gherschon
 */
class RssFeedRepository {

    fun getFeed(context: Context): LiveData<Resource<Rss>> {

        val data = MutableLiveData<Resource<Rss>>()
        async(UI) {
            val response: Response<Rss>? = bg { GetRssFeedRequest.getFeed() }.await()

            if (response?.isSuccessful == true) {
                val rss = response.body()!!
                rss.channel?.items?.let { items ->

                    async(CommonPool) {
                        TKDatabase.getInstance(context).itemDao().insert(items)
                    }
                    data.value = Resource.Success(rss)
                }
            } else {
                data.value = Resource.NetworkError()
            }
        }
        return data
    }
}
