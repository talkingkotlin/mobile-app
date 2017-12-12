package com.talkingkotlin.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.talkingkotlin.R
import com.talkingkotlin.fragment.EpisodesFragment
import com.talkingkotlin.presenter.MainPresenter


/**
 * MainActivity is the Entry Point of the application.
 * It show the lists of episodes of the podcast, through a presenter and a fragment.
 * @author Alexander Gherschon
 */

class MainActivity : AppCompatActivity() {

    companion object {
        private val FRAGMENT_TAG = "FRAGMENT_TAG"
    }

    // lazy loading of the presenter
    private val presenter: MainPresenter by lazy {

        // Instantiation of the MainPresenter, with its callback implemented as an anonymous inner class
        MainPresenter(this, object : MainPresenter.MainPresenterCallback {

            override fun showEpisodes() {
                // Episode deep linking, url example: "http://talkingkotlin.com/Sitting-down-with-Tor-Norbye/"
                var episodeTitle: String? = null
                if (intent?.action == Intent.ACTION_VIEW && intent?.data != null) {
                    // extract the episode name from the url (and removes the dashes) to send it to the fragment showing episodes
                    episodeTitle = intent?.data?.lastPathSegment?.replace("-", " ")
                }

                // shows the fragment (that shows episodes to the activity) if not showed already
                var fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                if (fragment == null) {
                    fragment = EpisodesFragment.newInstance(episodeTitle)
                    supportFragmentManager.beginTransaction()
                            .add(R.id.fragment_container, fragment, FRAGMENT_TAG)
                            .commit()
                }
            }

            override fun showPlayerControls() {
                // shows the fragment (that shows the player UI Controls) if they were previously hidden
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_playback_controls)
                if (fragment != null && !isFinishing) {
                    supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                    R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom,
                                    R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom)
                            .show(fragment)
                            .commit()
                }
            }

            override fun hidePlayerControls() {
                // hides the fragment (that shows the player UI Controls) if they were previously shown
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_playback_controls)
                if (fragment != null && !isFinishing) {
                    supportFragmentManager.beginTransaction()
                            .hide(fragment)
                            .commit()
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // adds the presenter as an observer of the lifecycle so it can act accordingly
        lifecycle.addObserver(presenter)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_settings) {
            SettingsActivity.startActivity(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

