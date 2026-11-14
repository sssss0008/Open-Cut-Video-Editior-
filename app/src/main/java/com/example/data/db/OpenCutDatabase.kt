package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        ImportedMediaEntity::class,
        ProjectLayerEntity::class,
        ProjectEditHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class OpenCutDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun importedMediaDao(): ImportedMediaDao
    abstract fun projectLayerDao(): ProjectLayerDao
    abstract fun projectEditHistoryDao(): ProjectEditHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: OpenCutDatabase? = null

        fun getDatabase(context: Context): OpenCutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OpenCutDatabase::class.java,
                    "opencut_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
