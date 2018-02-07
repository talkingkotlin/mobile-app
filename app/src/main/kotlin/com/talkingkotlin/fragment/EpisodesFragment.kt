package com.talkingkotlin.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.talkingkotlin.R
import com.talkingkotlin.adapter.EpisodesAdapter
import com.talkingkotlin.model.rss.Item
import com.talkingkotlin.presenter.EpisodesPresenter
import com.talkingkotlin.util.isConnected
import kotlinx.android.synthetic.main.fragment_episodes.*

/**
 * Episodes Fragment is the component presenting and interacting with the list of items from the RSS Feed
 * @author Alexander Gherschon
 */

class EpisodesFragment : Fragment() {

    companion object {
        var CHILD_DATA = 0
        var CHILD_LOADER = 1
        var CHILD_EMPTY_VIEW = 2

        const val EXTRA_EPISODE_TITLE = "EXTRA_EPISODE_TITLE"

        fun newInstance(episodeTitle: String?): Fragment {

            val fragment = EpisodesFragment()
            val bundle = Bundle()
            bundle.putString(EXTRA_EPISODE_TITLE, episodeTitle)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val networkReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            presenter.networkConnectionChanged(context.isConnected())
        }
    }

    private val presenter: EpisodesPresenter by lazy {
        EpisodesPresenter(this, object : EpisodesPresenter.EpisodesPresenterCallback {

            override fun showLoader() {
                if (viewFlipper.displayedChild != CHILD_LOADER) {
                    viewFlipper.displayedChild = CHILD_LOADER
                }
            }

            override fun showList() {
                if (viewFlipper.displayedChild != CHILD_DATA) {
                    viewFlipper.displayedChild = CHILD_DATA
                }
            }

            override fun showEmptyView() {
                if (viewFlipper.displayedChild != CHILD_EMPTY_VIEW) {
                    viewFlipper.displayedChild = CHILD_EMPTY_VIEW
                }
            }

            override fun setAdapter(adapter: RecyclerView.Adapter<*>) {
                recyclerView.adapter = adapter
            }

            override fun getAdapter(): RecyclerView.Adapter<*> = recyclerView.adapter

            override fun setEpisodes(items: List<Item>) {

                val adapter: EpisodesAdapter? = recyclerView.adapter as EpisodesAdapter?
                if (adapter == null) {
                    recyclerView.adapter = EpisodesAdapter(items) { item -> presenter.onItemTapped(context!!, item) }

                    // after setting the adapter, we need to find the item we are looking for for deep link
                    val episodeTitle = arguments?.getString(EXTRA_EPISODE_TITLE)
                    if (episodeTitle?.isNotEmpty() == true) {
                        val item = items.firstOrNull { item -> item.title.cleanUp() == episodeTitle.cleanUp() }
                        if (item != null) {
                            presenter.onItemTapped(context!!, item)
                        }
                    }
                } else {
                    adapter.updateEpisodes(items)
                }
            }

            override fun confirmChangeToItem(item: Item) {
                AlertDialog.Builder(context!!)
                        .setTitle(getString(R.string.switch_title))
                        .setMessage(getString(R.string.switch_message))
                        .setPositiveButton(getString(R.string.switch_action), { _, _ -> presenter.continuePlay(context!!, item) })
                        .setNegativeButton(getString(R.string.no), null)
                        .setCancelable(true)
                        .show()
            }

            override fun showContinueDialog(item: Item) {
                AlertDialog.Builder(context!!)
                        .setTitle(getString(R.string.carry_on))
                        .setMessage(getString(R.string.carry_on_message))
                        .setPositiveButton(getString(R.string.continue_action), { _, _ -> presenter.playEpisode(context!!, item, carryOn = true) })
                        .setNegativeButton(getString(R.string.restart_action), { _, _ -> presenter.playEpisode(context!!, item) })
                        .setCancelable(true)
                        .show()
            }

            private fun String.cleanUp() = this.replace(" ", "").toLowerCase()
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(presenter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_episodes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val simpleItemAnimator = recyclerView.itemAnimator as SimpleItemAnimator
        simpleItemAnimator.supportsChangeAnimations = false

        settings.setOnClickListener { startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)) }
    }

    override fun onResume() {
        super.onResume()
        context?.registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onPause() {
        super.onPause()
        context?.unregisterReceiver(networkReceiver)
    }
}
