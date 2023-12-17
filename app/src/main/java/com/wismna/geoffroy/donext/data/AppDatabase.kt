package com.wismna.geoffroy.donext.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlin.concurrent.Volatile

@Database(entities = [Task::class, TaskList::class], views = [TodayTasksView::class], version = 6)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao?
    abstract fun taskListDao(): TaskListDao?

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        @JvmStatic
        fun getDatabase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = databaseBuilder(context.applicationContext,
                                AppDatabase::class.java, "donext.db")
                                .addMigrations(Migrations.MIGRATION_1_2, Migrations.MIGRATION_2_3,
                                        Migrations.MIGRATION_3_4, Migrations.MIGRATION_4_6)
                                .build()
                    }
                }
            }
            return INSTANCE
        }
    }
}
