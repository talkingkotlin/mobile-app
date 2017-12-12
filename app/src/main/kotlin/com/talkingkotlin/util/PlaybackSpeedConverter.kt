package com.talkingkotlin.util

/**
 * Extensions functions for converting progress values to speed and vice versa
 * @author Alexander Gherschon
 */

fun progressToSpeed(progress: Int): Float = ((progress - 1) * RATIO_PLAYBACK_SPEED_TO_PROGRESS) + 1

fun speedToProgress(speed: Float): Int = (((speed - 1) / RATIO_PLAYBACK_SPEED_TO_PROGRESS) + 1).toInt()
