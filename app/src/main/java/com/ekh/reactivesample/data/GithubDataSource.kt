package com.ekh.reactivesample.data

import com.ekh.reactivesample.data.network.GithubService
import com.ekh.reactivesample.data.network.model.RepoSearchResponse
import javax.inject.Inject
import javax.inject.Singleton

interface GithubDataSource {
    suspend fun getSearchResultStream(
        query: String,
        page: Int,
        itemsPerPage: Int
    ): RepoSearchResponse
}

@Singleton
class GithubRepository @Inject constructor(
    private val service: GithubService
) : GithubDataSource {

    override suspend fun getSearchResultStream(query: String, page: Int, itemsPerPage: Int): RepoSearchResponse =
        service.searchRepos(query, page, itemsPerPage)
}
