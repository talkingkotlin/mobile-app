package com.talkingkotlin.adapter

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.talkingkotlin.R
import com.talkingkotlin.model.rss.Item
import com.talkingkotlin.util.PRESENTED_DATE_FORMAT
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.episode_card.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Episodes Adapter transforms the list of Items of the RSS to views for its recycler view
 * @author Alexander Gherschon
 */

class EpisodesAdapter(private var items: List<Item>?, private val listener: (Item) -> Unit) : RecyclerView.Adapter<EpisodesAdapter.ViewHolder>() {

    companion object {
        val TAG: String = EpisodesAdapter::class.java.simpleName
        val simpleDateFormatter = SimpleDateFormat(PRESENTED_DATE_FORMAT, Locale.ENGLISH)
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.episode_card, parent, false), listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(items!![position])
    }

    override fun getItemCount() = items?.size ?: 0

    override fun getItemId(position: Int) = position.toLong()

    /**
     * View Holder class for Episodes views
     */
    inner class ViewHolder(override val containerView: View?, listener: (Item) -> Unit) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        // keeps the loaded url in a property to avoid reloading with the same url
        private var url: String? = null
        // instantiate the onClickListener only once (so no SAM conversion happens in the bind() function
        private val onClickListener = View.OnClickListener {
            items?.get(adapterPosition)?.let {
                item -> listener.invoke(item)
            }
        }

        fun bind(item: Item) {

            date.text = simpleDateFormatter.format(item.pubDate)
            name.text = item.title

            // checks first that the url requested be to loaded was not loaded previously
            if (image.tag == null || image.tag != item.image!!.href) {

                if (containerView != null) {
                    Picasso.with(containerView.context)
                            .load(item.image!!.href)
                            .placeholder(ContextCompat.getDrawable(containerView.context, R.drawable.placeholder))
                            .into(image)
                    image.tag = item.image!!.href
                }
            }

            description.text = item.description
            duration.text = item.duration?.value ?: ""

            val position = item.position?.toInt() ?: 0
            val duration = item.duration?.timestamp?.toInt() ?: 0

            this.progress.max = duration // GODAMNIT ANDROID IF MAX IS SET AFTER PROGRESS IT DOES NOT DRAW THE FIRST TIME
            this.progress.progress = position

            card.setOnClickListener(onClickListener)
        }
    }

    fun updateEpisodes(items: List<Item>) {

        this.items = items
        notifyDataSetChanged()
    }
}
