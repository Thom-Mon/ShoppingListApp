package com.example.shoppinglistapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.Dao.Item.Item
import com.example.shoppinglistapp.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private  lateinit var appDb : AppDatabase
    private val gson = Gson()
    private val filePickerRequestCode_Main = 42


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        appDb = AppDatabase.getDatabase(this)

        // getting the intention-filter that opens the App
        val intent = intent

        when {
            intent?.action == Intent.ACTION_VIEW -> {
                if ("application/octet-stream" == intent.type) {
                    loadFromIntent(intent)
                }
            }
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
               R.id.nav_shoppinglist, R.id.nav_settings, R.id.nav_standard, R.id.nav_category, R.id.nav_listmanagement
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
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

    private fun loadFromIntent(intent: Intent)
    {
        var loadedData = java.util.ArrayList<Item>()

        val uri = intent.data

        //final data from file that opened the app
        val inputStream = this.contentResolver.openInputStream(uri!!)
        val byteArray = inputStream!!.readBytes()
        val intentFileContent: String = String(byteArray)

        var currentIndex = 0
        var firstEntryDatetime = ""

        try {
            val arrayListTutorialType = object : TypeToken<List<Item>>() {}.type
            loadedData = gson.fromJson(intentFileContent, arrayListTutorialType)
            Log.e("Load",loadedData.toString())

            // create Category-Entry from Entries in Items
            extractCategoriesFromItems(loadedData)

            GlobalScope.launch(Dispatchers.IO){
                //appDb.entryDao().insert(entry)
                appDb.itemDao().insertAll(loadedData)
            }

            val filename: String = getFileNameFromUri(uri.lastPathSegment.toString())

            saveToInternalAppStorage(loadedData, filename)
            Log.i("intention-filenameOnly", filename)
            Toast.makeText(applicationContext, "Einkaufsliste importiert", Toast.LENGTH_SHORT).show()
        }
        catch (e: Exception){
            Toast.makeText(applicationContext, "Fehler beim Importieren der Liste: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("intention",  "Intention-Error: ${e.message}")
        }
    }

    // gets the fileName from the uri without the extension .hai, the extension is created if the list is exported again later !
    private fun getFileNameFromUri(uri: String): String
    {
        var fileName = uri.substring(uri.lastIndexOf("/") + 1)

        return fileName.substring(0, fileName.lastIndexOf(".hai"))
    }

    private fun extractCategoriesFromItems(loadedData: java.util.ArrayList<Item>)
    {
        var categories = mutableListOf<String>()
        var categoriesAlreadyInDb = mutableListOf<String>()

        var categoriesExtract = java.util.ArrayList<Category>()

        lateinit var entries: List<Category>
        val job = GlobalScope.launch {
            entries = appDb.categoryDao().getAll()

            if (entries.isNotEmpty()) {
                for(entry in entries)
                {
                    categoriesAlreadyInDb.add(entry.name.toString())
                }
            }
            withContext(Dispatchers.IO)
            {
                for (item in loadedData)
                {
                    if(!categories.contains(item.category.toString()) && !categoriesAlreadyInDb.contains(item.category.toString()))
                    {
                        categories.add(item.category.toString())
                    }
                }

                for (category in categories)
                {
                    categoriesExtract.add(Category(null, category))
                }
            }
        }
        insertResponseToDB(categoriesExtract)
    }

    private fun insertResponseToDB(responseBody: List<Category>)
    {
        GlobalScope.launch(Dispatchers.IO) {
            // write contents from JSON-String to DB
            appDb.categoryDao().insertAll(responseBody)
        }
    }

    // used to store the imported shopping list in the app internal storage
    private fun saveToInternalAppStorage(loadedData: ArrayList<Item>, filename: String = "Default")
    {
        if(File(applicationContext.filesDir, filename).exists())
        {
            File(applicationContext.filesDir, filename).delete()
        }

        File(applicationContext.filesDir, filename).printWriter().use { out ->
            out.println(gson.toJson(loadedData))}
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_messenger -> {
                openMessengerSendCurrentList(appDb,gson, context = this)
                true
            }
            R.id.action_fileopen -> {
                // Handle the action_fileopen click here
                openFilePickerDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun openFilePickerDialog() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // Set the initial type to show all files
        }
        startActivityForResult(intent, filePickerRequestCode_Main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == filePickerRequestCode_Main && resultCode == Activity.RESULT_OK) {
            // Handle the selected file URI here (data?.data)
            loadFromFilePicker(data?.data)
        }
    }

    private fun loadFromFilePicker(data: Uri?)
    {
        var loadedData = java.util.ArrayList<Item>()

        if(data == null)
        {
            Toast.makeText(this, "Die Datei konnte nicht geladen werden", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val fileContents = readTextFromUri(this,data) // i guess data is the uri here?!
            //Log.e("Mark_ Load",fileContents.toString())
            val arrayListTutorialType = object : TypeToken<List<Item>>() {}.type
            loadedData = gson.fromJson(fileContents, arrayListTutorialType)
            //Log.e("mark_ Load Data",loadedData.toString())

            // create Category-Entry from Entries in Items
            extractCategoriesFromItems(loadedData)
            //

            GlobalScope.launch(Dispatchers.IO){
                //appDb.entryDao().insert(entry)
                appDb.itemDao().insertAll(loadedData)
            }
            Toast.makeText(this, "Datei geladen", Toast.LENGTH_SHORT).show()
        }
        catch (e: Exception){
            Toast.makeText(this, "Fehler beim Laden: Datei nicht gefunden!", Toast.LENGTH_SHORT).show()
        }
    }
}