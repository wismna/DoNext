package com.wismna.geoffroy.donext.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Task.class, TaskList.class}, views = {TodayTasksView.class}, version = 6)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    public abstract TaskListDao taskListDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "donext.db")
                            .addMigrations(Migrations.MIGRATION_1_2, Migrations.MIGRATION_2_3,
                                    Migrations.MIGRATION_3_4, Migrations.MIGRATION_4_6)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
