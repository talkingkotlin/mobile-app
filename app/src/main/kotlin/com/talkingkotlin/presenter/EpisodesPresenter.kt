package com.talkingkotlin.presenter

import android.arch.lifecycle.*
import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import com.talkingkotlin.database.TKDatabase
import com.talkingkotlin.fragment.EpisodesFragment
import com.talkingkotlin.model.network.Resource
import com.talkingkotlin.model.player.PlayerState
import com.talkingkotlin.model.rss.Item
import com.talkingkotlin.service.PlayerService
import com.talkingkotlin.util.ACTION_PLAY
import com.talkingkotlin.util.EXTRA_CARRY_ON
import com.talkingkotlin.util.ITEM_ACTION
import com.talkingkotlin.util.isConnected
import com.talkingkotlin.viewmodel.ItemsViewModel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Episodes Presenter is the presentation layer for the list of episodes
 * @author Alexander Gherschon
 */

class EpisodesPresenter(private val fragment: Fragment, private val callback: EpisodesPresenterCallback) : LifecycleObserver {

    private var playerState: PlayerState.Itemized? = null

    companion object {
        private val TAG = EpisodesFragment::class.java.simpleName
    }

    /**
     * Callbacks to be implemented by any class using this presenter
     */
    interface EpisodesPresenterCallback {

        fun showLoader()
        fun showList()
        fun showEmptyView()

        fun setAdapter(adapter: RecyclerView.Adapter<*>)
        fun getAdapter(): RecyclerView.Adapter<*>
        fun setEpisodes(items: List<Item>)
        fun confirmChangeToItem(item: Item)
        fun showContinueDialog(item: Item)
    }

    private lateinit var itemsViewModel: ItemsViewModel

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {

        EventBus.getDefault().register(this)

        itemsViewModel = ViewModelProviders.of(fragment).get(ItemsViewModel::class.java).apply {
            getItems().observe(fragment, Observer<Resource<List<Item>>> { resource ->

                callback.apply {
                    if (fragment.context!!.isConnected()) {
                        when (resource) {
                            is Resource.Loading -> showLoader()
                            is Resource.Success -> {
                                showList()
                                setEpisodes(resource.data)
                            }
                            is Resource.NetworkError -> {
                                if (resource.data == null) {
                                    showEmptyView()
                                } else {
                                    showList()
                                    setEpisodes(resource.data)
                                }
                            }
                        }
                    } else {
                        when (resource) {
                            is Resource.Success -> setEpisodes(resource.data)
                            is Resource.NetworkError -> {
                                if (resource.data != null) {
                                    setEpisodes(resource.data)
                                }
                            }
                        }
                    }
                }
            })
        }

    }

    fun onItemTapped(context: Context, item: Item) {

        if (playerState == null) {
            continuePlay(context, item)
        } else if (playerState != null && playerState!!.item.title != item.title) {
            callback.confirmChangeToItem(item)
        }
    }

    fun continuePlay(context: Context, item: Item) {

        if (item.position == null || item.listened) {
            // if the item was listened to, update the database as playing again resets it
            if(item.listened) {
                async(CommonPool) {
                    TKDatabase.getInstance(context)
                            .itemDao()
                            .updateListened(item.title, listened = false)
                }
            }

            playEpisode(context, item)
        } else {
            callback.showContinueDialog(item)
        }
    }

    fun playEpisode(context: Context, item: Item, carryOn: Boolean = false) {
        val intent = Intent(context, PlayerService::class.java)
        intent.action = ACTION_PLAY
        intent.putExtra(ITEM_ACTION, item)
        intent.putExtra(EXTRA_CARRY_ON, carryOn)
        context.startService(intent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewPlayerState(playerState: PlayerState?) {
        this.playerState = if (playerState is PlayerState.Itemized) playerState else null
    }

    fun networkConnectionChanged(connected: Boolean) {

        if (connected) {
            callback.showList()
        } else {
            // show network error on the UI
            callback.showEmptyView()
        }
    }
}