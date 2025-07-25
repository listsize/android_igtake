package com.instadownloader.instasave.igsave.ins.ui.main.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [(PostBean::class)], version = 2)
abstract class PostDataBase : RoomDatabase() {

    companion object {
        val DATABASE_NAME = "datadb"

    }
    abstract fun getDao(): PostDao
}