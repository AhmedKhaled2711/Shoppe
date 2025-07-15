package com.lee.shoppe.data.repository

import com.lee.shoppe.data.network.networking.NetworkManager
import com.lee.shoppe.data.network.networking.NetworkManagerImp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideNetworkManager(): NetworkManager = NetworkManagerImp.getInstance()

    @Provides
    @Singleton
    fun provideRepository(networkManager: NetworkManager): Repository = RepositoryImp(networkManager)
} 