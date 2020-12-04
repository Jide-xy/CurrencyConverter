package com.currency.converter.core.repository.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.currency.converter.core.repository.db.model.RateEntity

@Database(
    entities = [RateEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun rateDao(): RateDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(
            context: Context
        ): AppDatabase =
            INSTANCE
                ?: synchronized(this) {
                    INSTANCE
                        ?: buildDatabase(
                            context
                        )
                            .also {
                                INSTANCE = it
                            }
                }

        private fun buildDatabase(
            context: Context
        ) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "AkeDB"
            )
                .fallbackToDestructiveMigration()
                .build()

    }


}