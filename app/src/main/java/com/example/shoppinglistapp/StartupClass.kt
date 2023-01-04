package com.example.shoppinglistapp

import android.app.Application
import android.util.Log
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.Dao.Item.Item
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class StartupClass : Application() {
    private  lateinit var appDb : AppDatabase
    private val gson = Gson()

    init {
        // this method fires only once per application start.
        // getApplicationContext returns null here
    }

    override fun onCreate() {
        super.onCreate()
        appDb = AppDatabase.getDatabase(this)
        // this method fires once as well as constructor
        // but also application has context here
        setupDbFromAssets()
        setupDbCategoriesFromAssets()
    }

    fun setupDbFromAssets()
    {
        // on startup fill RoomDB with data from assets/json
        var loadedData = ArrayList<Item>()
        val file = assets?.open("startItems.txt")?.bufferedReader()
        val fileContents = file?.readText()
        val arrayListTutorialType = object : TypeToken<List<Item>>() {}.type
        loadedData = gson.fromJson(fileContents, arrayListTutorialType)

        GlobalScope.launch(Dispatchers.IO){
            // write contents from JSON-String to DB
            appDb.itemDao().insertAll(loadedData)
            Log.e("LastEntry",appDb.itemDao().getSequenceNumber("item_table").toString())
        }
    }

    fun setupDbCategoriesFromAssets()
    {
        // on startup fill RoomDB with data from assets/json
        var loadedData = ArrayList<Category>()
        val file = assets?.open("startCategories.txt")?.bufferedReader()
        val fileContents = file?.readText()
        val arrayListTutorialType = object : TypeToken<List<Category>>() {}.type
        loadedData = gson.fromJson(fileContents, arrayListTutorialType)

        GlobalScope.launch(Dispatchers.IO){
            // write contents from JSON-String to DB
            appDb.categoryDao().insertAll(loadedData)
            Log.e("LastEntry",appDb.itemDao().getSequenceNumber("item_table").toString())
        }
    }
}