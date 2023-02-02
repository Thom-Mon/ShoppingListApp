package com.example.shoppinglistapp.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.shoppinglistapp.AppDatabase
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.Dao.Item.Item
import com.example.shoppinglistapp.databinding.FragmentSettingsBinding
import com.example.shoppinglistapp.retrofit.ApiInterface_Category
import com.example.shoppinglistapp.retrofit.ApiInterface_Item
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

const val BASE_URL = "http://192.168.185.38/LocalStorager/"
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private  lateinit var appDb : AppDatabase
    private val gson = Gson()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        appDb = AppDatabase.getDatabase(requireContext())


        binding.btnLoadTestdata.setOnClickListener {
            setupDbFromAssets()
            setupDbCategoriesFromAssets()
        }

        binding.btnDeleteDb.setOnClickListener {
            deleteAll()
        }

        binding.btnCallApi.setOnClickListener {
            callApi()
            callApiGetItems()
        }

        binding.btnCallApiPOST.setOnClickListener {
            callApi_Post()
            callApi_Post_Items()
        }

        binding.btnCallApiPOSTDownload.setOnClickListener {
            callApi_Post()
            callApi_Post_Items(true)
        }

        binding.btnSaveToFile.setOnClickListener {
            saveToExternalStorage()
        }

        binding.btnLoadFromFile.setOnClickListener {
            loadFromExternalStorage()
        }






        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun setupDbFromAssets()
    {
        // on startup fill RoomDB with data from assets/json
        var loadedData = ArrayList<Item>()
        val file = context?.assets?.open("startItems.txt")?.bufferedReader()
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
        val file = context?.assets?.open("startCategories.txt")?.bufferedReader()
        val fileContents = file?.readText()
        val arrayListTutorialType = object : TypeToken<List<Category>>() {}.type
        loadedData = gson.fromJson(fileContents, arrayListTutorialType)

        GlobalScope.launch(Dispatchers.IO){
            // write contents from JSON-String to DB
            appDb.categoryDao().insertAll(loadedData)
            Log.e("LastEntry",appDb.itemDao().getSequenceNumber("item_table").toString())
        }
    }

    fun deleteAll()
    {
        GlobalScope.launch {
            appDb.itemDao().deleteAll()
            appDb.categoryDao().deleteAll()
        }
    }

    fun callApi()
    {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL) //TODO: BaseURL An Pi-Zero anpassen!!!
            .build()
            .create(ApiInterface_Category::class.java)

        val retrofitData = retrofitBuilder.getData_Category()

        retrofitData.enqueue(object : Callback<List<Category>?> {
            override fun onResponse(
                call: Call<List<Category>?>,
                response: Response<List<Category>?>
            ) {
                val responseBody = response.body()!!
                for (category in responseBody)
                {
                    Log.e("Response", category.name!!)
                    Toast.makeText(context, "Daten erhalten: " + category.name,Toast.LENGTH_SHORT).show()
                }

                insertResponseToDB(responseBody)
            }

            override fun onFailure(call: Call<List<Category>?>, t: Throwable) {
                Log.e("Response", "Something went wrong is the URL of Server correct?")
                Toast.makeText(context, t.toString(),Toast.LENGTH_SHORT).show()
            }
        })
    }

    // POST only the products with status 0
    fun callApi_Post()
    {
        lateinit var entries: List<Category>
        GlobalScope.launch {
            entries = appDb.categoryDao().getAll()

            if(entries.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    val retrofitBuilder = Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(BASE_URL) //TODO: BaseURL An Pi-Zero anpassen!!!
                        .build()
                        .create(ApiInterface_Category::class.java)

                    // sets the id to null, to make on get request possible to set all ids freely (val in category.kt was changed to var)
                    //entries.onEach { it.id = null }
                    //val req_category = Category(1, "Post-Kategorie")
                    val retrofitData = retrofitBuilder.sendDataCategory(entries)

                    retrofitData.enqueue(object : Callback<List<Category>?> {
                        override fun onResponse(
                            call: Call<List<Category>?>,
                            response: Response<List<Category>?>
                        ) {
                            if(response.body() != null){
                                val responseBody = response.body()!!
                                for (category in responseBody)
                                {
                                    Log.e("Response", category.name!!)
                                }
                                Toast.makeText(context, "Daten erhalten erster Eintrag: " + responseBody[0].name,Toast.LENGTH_SHORT).show()

                            }

                            //insertResponseToDB(responseBody)
                        }

                        override fun onFailure(call: Call<List<Category>?>, t: Throwable) {
                            Log.e("Response", "Something went wrong is the URL of Server correct?")
                            Toast.makeText(context, t.toString(),Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }
    }
    fun callApiGetItems()
    {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL) //TODO: BaseURL An Pi-Zero anpassen!!!
            .build()
            .create(ApiInterface_Item::class.java)

        val retrofitData = retrofitBuilder.getDataItem()

        retrofitData.enqueue(object : Callback<List<Item>?> {
            override fun onResponse(
                call: Call<List<Item>?>,
                response: Response<List<Item>?>
            ) {
                val responseBody = response.body()!!
                for (category in responseBody)
                {
                    Log.e("Response", category.name!!)
                    Toast.makeText(context, "Daten erhalten: " + category.name,Toast.LENGTH_SHORT).show()
                }

                insertResponseToDbItem(responseBody)
            }

            override fun onFailure(call: Call<List<Item>?>, t: Throwable) {
                Log.e("Response", "Something went wrong is the URL of Server correct?")
                Toast.makeText(context, t.toString(),Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun insertResponseToDbItem(responseBody: List<Item>)
    {
        GlobalScope.launch(Dispatchers.IO) {
            // write contents from JSON-String to DB
            appDb.itemDao().insertAll(responseBody)
        }
    }

    fun callApi_Post_Items(download: Boolean = false)
    {
        lateinit var entries: List<Item>
        GlobalScope.launch {
            entries = appDb.itemDao().getAll()

            if(entries.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    val retrofitBuilder = Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(BASE_URL) //TODO: BaseURL An Pi-Zero anpassen!!!
                        .build()
                        .create(ApiInterface_Item::class.java)


                    // if download then use other call to server to get Data
                    var retrofitData = retrofitBuilder.sendDataItem(entries)
                    if(download)
                    {
                        retrofitData = retrofitBuilder.sendDataItemDownload(entries)
                    }

                    retrofitData.enqueue(object : Callback<List<Item>?>{
                        override fun onResponse(
                            call: Call<List<Item>?>,
                            response: Response<List<Item>?>
                        ) {
                            if(response.body() != null)
                            {
                                val responseBody = response.body()!!
                                for (item in responseBody)
                                {
                                    Log.e("Response", item.name!!)
                                    Toast.makeText(context, "Daten erhalten: " + item.name,Toast.LENGTH_SHORT).show()
                                }
                            }


                            //insertResponseToDB(responseBody)
                        }

                        override fun onFailure(call: Call<List<Item>?>, t: Throwable) {
                            Log.e("Response", "Something went wrong is the URL of Server correct?")
                            Toast.makeText(context, t.toString(),Toast.LENGTH_SHORT).show()
                        }
                    })
                }

            }
        }
    }

    private fun insertResponseToDB(responseBody: List<Category>)
    {

        GlobalScope.launch(Dispatchers.IO) {
            // write contents from JSON-String to DB
            appDb.categoryDao().insertAll(responseBody)
        }
    }
    private fun saveToExternalStorage()
    {
        //-> saving to JSON for easy get it back
        val fileName = "test.txt"
        File(requireContext().filesDir, fileName).delete()
        // SAVING WELL
        //    val fileOutputStream: FileOutputStream = openFileOutput("mytextfile.txt", Context.MODE_PRIVATE)
        //    val outputWriter = OutputStreamWriter(fileOutputStream)
        //    outputWriter.write("test with mytextfile")
        //    outputWriter.close()
        // ALSO WORKING WELL
        lateinit var entries: List<Item>

        GlobalScope.launch(Dispatchers.IO){
            entries = appDb.itemDao().getAll()
            if(entries.isNotEmpty())
            {
                if(entries != null)
                {
                    if(File(requireContext().filesDir, fileName).exists())
                    {
                        File(requireContext().filesDir, fileName).delete()
                    }

                    withContext(Dispatchers.Main)
                    {
                        Log.e("ALL_ENTRIES",entries.toString())
                        File(requireContext().filesDir, fileName).printWriter().use { out ->
                            out.println(gson.toJson(entries))}
                    }


                }
            }


        }

        Toast.makeText(requireContext(), "Datei gespeichert: $fileName",Toast.LENGTH_SHORT).show()
    }

    private fun loadFromExternalStorage()
    {
        //FIXME:
        // 1. Save categories to on saveToExternalStorage in different file
        // 2. Or what is more flexible -> use category in items to build category from
        //    items itself 01.02.2023
        var loadedData = java.util.ArrayList<Item>()

        var filename = "test.txt";
        var currentIndex = 0
        var firstEntryDatetime = ""

        try {
            val file = File(requireContext().filesDir,filename)
            val fileContents = file.readText()
            //Log.e("Load",fileContents.toString())
            val arrayListTutorialType = object : TypeToken<List<Item>>() {}.type
            loadedData = gson.fromJson(fileContents, arrayListTutorialType)
            Log.e("Load",loadedData.toString())

            // create Category-Entry from Entries in Items
            extractCategoriesFromItems(loadedData)
            //

            GlobalScope.launch(Dispatchers.IO){
                //appDb.entryDao().insert(entry)
                appDb.itemDao().insertAll(loadedData)
            }


            //toastMessage("Datei geladen: $filename")
            Toast.makeText(requireContext(), "Datei geladen: ${filename}",Toast.LENGTH_SHORT).show()
        }
        catch (e: Exception){
            //toastMessage("Dateilesefehler: " + e.message)
            Toast.makeText(requireContext(), "Datei gespeichert: ${e.message}",Toast.LENGTH_SHORT).show()
        }
        //readAllData()
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
}