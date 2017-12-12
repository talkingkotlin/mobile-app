package com.talkingkotlin.activity


import android.content.Intent
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.talkingkotlin.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testing the specific behavior of the Settings Screen
 */
@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {

    @get:Rule
    private var settingsActivityActivityTestRule = ActivityTestRule(SettingsActivity::class.java)

    @Before
    fun before() {
        settingsActivityActivityTestRule.launchActivity(Intent())
    }

    @Test
    fun playbackSpeedDefaultValue() {
        val speedTextView = onView(withId(R.id.speed))
        speedTextView.check(matches(withText("1.0x")))
    }
}
