package com.ekh.reactivesample.domain

import com.ekh.reactivesample.data.GithubDataSource
import com.ekh.reactivesample.data.network.model.Repo
import com.ekh.reactivesample.di.IoDispatcher
import com.ekh.reactivesample.domain.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class SearchGithubRepoUseCase @Inject constructor(
    private val githubDataSource: GithubDataSource,
    @IoDispatcher dispatcher: CoroutineDispatcher,
) : UseCase<SearchGithubRepoUseCase.Param, SearchGithubRepoUseCase.Result>(dispatcher) {

    override suspend fun execute(parameters: Param): Result {
        val list = githubDataSource.getSearchResultStream(
            parameters.query,
            parameters.page,
            parameters.itemsPerPage
        )

        val result = list.items

        return Result(result)
    }

    data class Param(
        val query: String,
        val page: Int,
        val itemsPerPage: Int
    )

    data class Result(
        val repoList: List<Repo>
    )
}