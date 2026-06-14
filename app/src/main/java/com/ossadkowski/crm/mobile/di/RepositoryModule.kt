package com.ossadkowski.crm.mobile.di

import com.ossadkowski.crm.mobile.data.nawozy.repository.NawozyRepositoryImpl
import com.ossadkowski.crm.mobile.data.serverstatus.ServerStatusRepositoryImpl
import com.ossadkowski.crm.mobile.data.serwis.parts.repository.PartsRepositoryImpl
import com.ossadkowski.crm.mobile.data.serwis.repository.SerwisRepositoryImpl
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import com.ossadkowski.crm.mobile.domain.serverstatus.repository.ServerStatusRepository
import com.ossadkowski.crm.mobile.domain.serwis.parts.repository.PartsRepository
import com.ossadkowski.crm.mobile.domain.serwis.repository.SerwisRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindServerStatusRepository(
        impl: ServerStatusRepositoryImpl
    ): ServerStatusRepository

    @Binds
    @Singleton
    abstract fun bindSerwisRepository(
        impl: SerwisRepositoryImpl
    ): SerwisRepository

    @Binds
    @Singleton
    abstract fun bindPartsRepository(
        impl: PartsRepositoryImpl
    ): PartsRepository

    @Binds
    @Singleton
    abstract fun bindNawozyRepository(
        impl: NawozyRepositoryImpl
    ): NawozyRepository

    // Add a @Binds line here every time a new feature adds a repository interface.
}
