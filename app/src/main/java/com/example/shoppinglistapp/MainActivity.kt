package com.example.shoppinglistapp

import android.os.Bundle
import android.util.Log
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.Dao.Item.Item
import com.example.shoppinglistapp.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private  lateinit var appDb : AppDatabase
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        appDb = AppDatabase.getDatabase(this)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
               R.id.nav_shoppinglist, R.id.nav_settings, R.id.nav_standard
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}