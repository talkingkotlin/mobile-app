package com.talkingkotlin.network

import com.talkingkotlin.model.rss.Rss
import retrofit2.Call
import retrofit2.http.GET

/**
 * Retrofit service to interact with the RSS Feed API
 * @author Alexander Gherschon
 */

interface RssFeedService {
    @GET("users/soundcloud:users:280353173/sounds.rss")
    fun getFeed(): Call<Rss>
}