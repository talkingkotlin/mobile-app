package com.talkingkotlin.util

import com.talkingkotlin.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.simpleframework.xml.core.Persister
import org.simpleframework.xml.transform.RegistryMatcher
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Alexander Gherschon
 */

object RetrofitUtils {

    val retrofit: Retrofit by lazy {

        val httpClient = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            httpClient.addInterceptor(logging)
        }

        val format = SimpleDateFormat(SERVER_DATE_FORMAT, Locale.ENGLISH)
        val matcher = RegistryMatcher()
        matcher.bind(Date::class.java, DateFormatTransformer(format))
        val ser = Persister(matcher)
        val factory = SimpleXmlConverterFactory.create(ser)

        return@lazy Retrofit.Builder()
                .baseUrl("http://feeds.soundcloud.com/")
                .addConverterFactory(factory)
                .client(httpClient.build())
                .build()
    }
}
