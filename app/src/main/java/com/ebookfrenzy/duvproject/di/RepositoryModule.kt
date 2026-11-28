package com.ebookfrenzy.duvproject.di

import com.ebookfrenzy.duvproject.data.repository.AuthRepository
import com.ebookfrenzy.duvproject.data.repository.LocalOnBoardingRepository
import com.ebookfrenzy.duvproject.data.repository.NetworkAuthRepository
import com.ebookfrenzy.duvproject.data.repository.EntertainerSetupRepository
import com.ebookfrenzy.duvproject.data.repository.NetworkEntertainerSetupRepository
import com.ebookfrenzy.duvproject.data.repository.OnBoardingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Tells Hilt which implementation to use for each repository interface. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindOnBoardingRepository(impl: LocalOnBoardingRepository): OnBoardingRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: NetworkAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindEntertainerSetupRepository(impl: NetworkEntertainerSetupRepository): EntertainerSetupRepository
}
