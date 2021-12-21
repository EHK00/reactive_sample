package com.ekh.reactivesample.di

import com.ekh.reactivesample.data.GithubDataSource
import com.ekh.reactivesample.data.GithubRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    @Singleton
    abstract fun provideSearchRepository(repository: GithubRepository): GithubDataSource
}