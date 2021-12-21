package com.ekh.reactivesample.di

import com.ekh.reactivesample.Flipper
import com.ekh.reactivesample.data.network.GithubService
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGithubSearchApi(
        client: OkHttpClient,
        converterFactory: Converter.Factory
    ): GithubService = Retrofit.Builder()
        .client(client)
        .baseUrl("https://api.github.com/")
        .addConverterFactory(converterFactory)
        .build()
        .create(GithubService::class.java)

    @Provides
    @Singleton
    fun provideConverterFactory(gson: Gson): Converter.Factory =
        GsonConverterFactory.create(gson)

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(FlipperOkhttpInterceptor(Flipper.networkFlipperPlugin))
        .build()
}