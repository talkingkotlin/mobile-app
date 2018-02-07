package com.talkingkotlin.util

import android.content.Context
import android.net.ConnectivityManager
import androidx.content.systemService

/**
 * Extension Functions related to Network management
 * @author Alexander Gherschon
 */

fun Context.isConnected(): Boolean {
    val cm = systemService<ConnectivityManager>()
    val activeNetwork = cm.activeNetworkInfo
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting
}