package com.lee.shoppe.data.repository

import android.content.Context
import com.lee.shoppe.data.network.networking.NetworkManager
import com.lee.shoppe.data.network.networking.NetworkManagerImp
import com.lee.shoppe.data.network.networking.RetrofitHelper
import com.lee.shoppe.data.network.networking.RetrofitHelperPayment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideNetworkManager(
        @ApplicationContext context: Context,
        retrofitHelper: RetrofitHelper,
        retrofitHelperPayment: RetrofitHelperPayment
    ): NetworkManager {
        return NetworkManagerImp(context, retrofitHelper, retrofitHelperPayment)
    }

    @Provides
    @Singleton
    fun provideRepository(networkManager: NetworkManager): Repository = RepositoryImp(networkManager)
}