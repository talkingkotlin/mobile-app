package com.talkingkotlin.activity


import android.content.ComponentName
import android.support.test.InstrumentationRegistry.getTargetContext
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.talkingkotlin.R
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    var activityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun before() {
        Intents.init()
    }

    @After
    fun after() {
        Intents.release()
    }

    @Test
    fun showSettings() {
        // check the presence of the Settings button
        val settingsButton = onView(withId(R.id.action_settings))
        settingsButton.check(matches(isDisplayed()))
        // enter the settings screen
        settingsButton.perform(click())
        // check that the settings screen opened
        intended(hasComponent(ComponentName(getTargetContext(), SettingsActivity::class.java)))
    }
}
