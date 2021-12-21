package com.ekh.reactivesample.domain.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

abstract class UseCase<in P, R>(private val dispatcher: CoroutineDispatcher)  {
    suspend operator fun invoke(parameters: P): Flow<Resource<R>> = flow {
        emit(Resource.Loading)
        val result = try {
            execute(parameters).let {
                Resource.Success(it)
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
        emit(result)
    }.flowOn(dispatcher)

    protected abstract suspend fun execute(parameters: P): R
}