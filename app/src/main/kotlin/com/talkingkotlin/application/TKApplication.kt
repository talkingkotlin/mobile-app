package com.talkingkotlin.application

import android.app.Application
import android.os.StrictMode
import com.talkingkotlin.BuildConfig


/**
 * Application class for general purpose settings, like StrictMode
 * @author Alexander Gherschon
 */
class TKApplication: Application() {

    override fun onCreate() {

        super.onCreate()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build())
        }
    }
}