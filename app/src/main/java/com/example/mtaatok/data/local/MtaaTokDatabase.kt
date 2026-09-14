package com.example.mtaatok.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VideoPostEntity::class, CommentEntity::class, NotificationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MtaaTokDatabase : RoomDatabase() {
    abstract fun dao(): MtaaTokDao

    companion object {
        @Volatile
        private var INSTANCE: MtaaTokDatabase? = null

        fun getDatabase(context: Context): MtaaTokDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MtaaTokDatabase::class.java,
                    "mtaatok_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
