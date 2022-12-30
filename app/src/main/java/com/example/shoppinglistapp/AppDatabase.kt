package com.example.shoppinglistapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.energy_meter_roomdb.ItemDao
import com.example.shoppinglistapp.Dao.Item.Item

@Database(entities = [Item :: class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao() : ItemDao

    companion object {

        @Volatile
        private var INSTANCE : AppDatabase? = null

        fun getDatabase(context: Context) : AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }

}