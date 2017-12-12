package com.talkingkotlin.util

import android.content.Context
import android.net.ConnectivityManager

/**
 * Extension Functions related to Network management
 * @author Alexander Gherschon
 */

fun Context.isConnected(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetworkInfo
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting
}