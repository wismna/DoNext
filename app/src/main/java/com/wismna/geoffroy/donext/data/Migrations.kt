package com.wismna.geoffroy.donext.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tasklist ADD COLUMN displayorder INTEGER")
        }
    }
    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tasklist ADD COLUMN visible INTEGER DEFAULT 1")
            db.execSQL("ALTER TABLE tasks ADD COLUMN duedate DATE")
        }
    }
    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tasks ADD COLUMN todaydate DATE")
        }
    }
    val MIGRATION_4_6: Migration = object : Migration(4, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tasks ADD COLUMN todayorder INTEGER")
        }
    }
}
