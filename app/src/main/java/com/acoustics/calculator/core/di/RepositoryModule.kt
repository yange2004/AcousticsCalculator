package com.acoustics.calculator.core.di

import com.acoustics.calculator.data.repository.MaterialRepositoryImpl
import com.acoustics.calculator.data.repository.ProjectRepositoryImpl
import com.acoustics.calculator.data.repository.StandardRepositoryImpl
import com.acoustics.calculator.domain.repository.MaterialRepository
import com.acoustics.calculator.domain.repository.ProjectRepository
import com.acoustics.calculator.domain.repository.StandardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMaterialRepository(impl: MaterialRepositoryImpl): MaterialRepository

    @Binds
    @Singleton
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository

    @Binds
    @Singleton
    abstract fun bindStandardRepository(impl: StandardRepositoryImpl): StandardRepository
}
