package com.acoustics.calculator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.acoustics.calculator.data.local.converter.Converters
import com.acoustics.calculator.data.local.dao.*
import com.acoustics.calculator.data.local.entity.*

@Database(
    entities = [
        MaterialEntity::class,
        MaterialCategoryEntity::class,
        ProjectEntity::class,
        ProjectRoomEntity::class,
        ProjectMaterialCrossRef::class,
        StandardEntity::class,
        SilencerProjectEntity::class,
        UserEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun materialDao(): MaterialDao
    abstract fun materialCategoryDao(): MaterialCategoryDao
    abstract fun projectDao(): ProjectDao
    abstract fun projectRoomDao(): ProjectRoomDao
    abstract fun projectMaterialDao(): ProjectMaterialDao
    abstract fun standardDao(): StandardDao
    abstract fun silencerDao(): SilencerDao
    abstract fun userDao(): UserDao
}
