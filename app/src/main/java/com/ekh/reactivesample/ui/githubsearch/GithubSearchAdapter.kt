package com.ekh.reactivesample.ui.githubsearch

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.ekh.reactivesample.data.network.model.Repo

class GithubSearchAdapter(
    private val action: (Repo) -> Unit
) : ListAdapter<Repo, GithubSearchViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GithubSearchViewHolder =
        GithubSearchViewHolder.createInstance(parent, action)

    override fun onBindViewHolder(holder: GithubSearchViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
    }

    override fun onViewRecycled(holder: GithubSearchViewHolder) {
        (holder as? GithubSearchViewHolder)?.onRecycled()
    }
}

private val diffCallback: DiffUtil.ItemCallback<Repo> =
    object : DiffUtil.ItemCallback<Repo>() {
        override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean =
            oldItem == newItem
    }