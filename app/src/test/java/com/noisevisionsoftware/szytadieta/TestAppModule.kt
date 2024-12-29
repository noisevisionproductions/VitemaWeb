package com.noisevisionsoftware.szytadieta

import com.noisevisionsoftware.szytadieta.domain.auth.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
class TestAppModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideNetworkConnectivityManager(): NetworkConnectivityManager = mockk(relaxed = true)
}