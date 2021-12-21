package com.ekh.reactivesample.ui.githubsearch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ekh.reactivesample.data.network.model.Repo
import com.ekh.reactivesample.databinding.ItemRepoBinding

class GithubSearchViewHolder private constructor(
    private val binding: ItemRepoBinding,
    private val listener: (Repo) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    lateinit var item: Repo

    init {
        binding.root.setOnClickListener {
            listener(item)
        }
    }

    fun bind(repo: Repo) {
        this.item = repo
        with(binding) {
            repoName.text = repo.fullName

            var descriptionVisibility = View.GONE
            if (repo.description != null) {
                repoDescription.text = repo.description
                descriptionVisibility = View.VISIBLE
            }
            repoDescription.visibility = descriptionVisibility

            repoStars.text = repo.stars.toString()
            repoForks.text = repo.forks.toString()
        }
    }

    fun onRecycled() {}

    companion object {
        fun createInstance(parent: ViewGroup, action: (Repo) -> Unit): GithubSearchViewHolder {
            return GithubSearchViewHolder(
                ItemRepoBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                action
            )
        }
    }
}