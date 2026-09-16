package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TrafficObservationEntity::class,
        PredictionRecordEntity::class,
        AlertItemEntity::class,
        FavoriteJunctionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TrafficSenseDatabase : RoomDatabase() {
    abstract fun trafficDao(): TrafficDao

    companion object {
        @Volatile
        private var INSTANCE: TrafficSenseDatabase? = null

        fun getDatabase(context: Context): TrafficSenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrafficSenseDatabase::class.java,
                    "trafficsense_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
