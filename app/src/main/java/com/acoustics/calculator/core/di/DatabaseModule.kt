package com.acoustics.calculator.core.di

import android.content.Context
import androidx.room.Room
import com.acoustics.calculator.data.local.AppDatabase
import com.acoustics.calculator.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "acoustics_calculator.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideMaterialDao(db: AppDatabase): MaterialDao = db.materialDao()
    @Provides fun provideMaterialCategoryDao(db: AppDatabase): MaterialCategoryDao = db.materialCategoryDao()
    @Provides fun provideProjectDao(db: AppDatabase): ProjectDao = db.projectDao()
    @Provides fun provideProjectRoomDao(db: AppDatabase): ProjectRoomDao = db.projectRoomDao()
    @Provides fun provideProjectMaterialDao(db: AppDatabase): ProjectMaterialDao = db.projectMaterialDao()
    @Provides fun provideStandardDao(db: AppDatabase): StandardDao = db.standardDao()
    @Provides fun provideSilencerDao(db: AppDatabase): SilencerDao = db.silencerDao()
    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
}
