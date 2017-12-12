package com.talkingkotlin.network

import com.talkingkotlin.model.rss.Rss
import com.talkingkotlin.util.RetrofitUtils
import retrofit2.Response

/**
 * Retrofit Network Request to download the RSS Feed
 * @author Alexander Gherschon
 */

object GetRssFeedRequest {

    private val TAG = GetRssFeedRequest::class.java.simpleName

    fun getFeed(): Response<Rss>? {
        try {
            val retrofit = RetrofitUtils.retrofit
            val service = retrofit.create(RssFeedService::class.java)
            val feed = service.getFeed()
            return feed.execute()

        } catch (e: Exception) {

        }
        return null
    }
}