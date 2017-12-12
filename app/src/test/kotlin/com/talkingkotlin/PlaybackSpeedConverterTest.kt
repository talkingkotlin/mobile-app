package com.talkingkotlin

import com.talkingkotlin.util.progressToSpeed
import com.talkingkotlin.util.speedToProgress
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


/**
 * @author Alexander Gherschon
 */

@RunWith(JUnit4::class)
class PlaybackSpeedConverterTest {
    private val data = mapOf(
            0 to 0.75f,
            1 to 1f,
            2 to 1.25f,
            3 to 1.5f,
            4 to 1.75f,
            5 to 2f,
            6 to 2.25f
    )

    @Test
    fun playbackSpeedToProgress() {
        data.forEach { key, value ->
            assertEquals(value, progressToSpeed(key))
        }
    }

    @Test
    fun progressToPlaybackSpeed() {
        data.entries.associate { (k, v) -> v to k }.forEach { k, v ->
            assertEquals(v, speedToProgress(k))
        }
    }
}