package com.example.shoppinglistapp.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.shoppinglistapp.AppDatabase
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.Dao.Item.Item
import com.example.shoppinglistapp.R
import com.example.shoppinglistapp.databinding.FragmentSettingsBinding
import com.example.shoppinglistapp.retrofit.ApiInterface_Category
import com.example.shoppinglistapp.retrofit.ApiInterface_Item
import com.example.shoppinglistapp.showConfirmationDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.Util
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
            showConfirmationDialog("Löschen bestätigen", getString(R.string.dialog_shoppinglist_deletion_warning_text)) {
                deleteAll()
            }
        }

        binding.btnRemoveDoubleFromDb.setOnClickListener {
            showConfirmationDialog("Doppelte Entfernen", getString(R.string.dialog_shoppinglist_deletion_doubles_warning_text)) {
                deleteDoubles()
            }
        }

        binding.btnCallApi.setOnClickListener {
            callApiGetCategory()
            callApiGetItems()
        }

        binding.btnCallApiPOST.setOnClickListener {
            callApi_Post_Category()
            callApi_Post_Items()
        }

        binding.btnCallApiPOSTDownload.setOnClickListener {
            callApi_Post_Category()
            callApi_Post_Items(true)
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

    fun deleteDoubles(){
        GlobalScope.launch {
            appDb.itemDao().deleteDoubles()
        }
    }

    fun callApiGetCategory()
    {
        lateinit var entries: List<Category>
        GlobalScope.launch {
            entries = appDb.categoryDao().getAll()
            // sets generally the retrofit builder
            val retrofitBuilder = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL) //TODO: BaseURL An Pi-Zero anpassen!!!
                .build()
                .create(ApiInterface_Category::class.java)

            // set the specific url to get the data -> here from Category
            val retrofitData = retrofitBuilder.getData_Category()

            // do the actual call and handle the response
            retrofitData.enqueue(object : Callback<List<Category>?> {
                override fun onResponse(
                    call: Call<List<Category>?>,
                    response: Response<List<Category>?>
                ) {
                    val responseBody = response.body()!!
                    val categoriesToBeInserted = mutableListOf<Category>()
                    for (category in responseBody)
                    {
                        // check if an category uuid already exists on server if so do not insert it into
                        if(entries.any { categoryCompare -> categoryCompare.uuid == category.uuid })
                        {
                            continue
                        }
                        else
                        {
                            categoriesToBeInserted.add(category)
                        }
                    }

                    if( categoriesToBeInserted.size > 0)
                    {
                        insertResponseToDBCategory(categoriesToBeInserted)
                    }
                }

                override fun onFailure(call: Call<List<Category>?>, t: Throwable) {
                    Log.e("Response", "Something went wrong is the URL of Server correct?")
                    Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // POST only the products with status 0
    fun callApi_Post_Category()
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

                    // sends entries with POST to "c=pages&a=response"
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
        lateinit var entries: List<Item>
        GlobalScope.launch {
            entries = appDb.itemDao().getAll()
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
                    val itemsToBeInserted = mutableListOf<Item>()
                    for (item in responseBody)
                    {
                        // check if item already exists if so dont insert to list and db
                        if(entries.any { itemCompare -> itemCompare.uuid == item.uuid })
                        {
                            continue
                        }
                        else
                        {
                            itemsToBeInserted.add(item)
                        }
                    }

                    if(itemsToBeInserted.size > 0)
                    {
                        insertResponseToDbItem(itemsToBeInserted)
                    }
                }

                override fun onFailure(call: Call<List<Item>?>, t: Throwable) {
                    Log.e("Response", "Something went wrong is the URL of Server correct?")
                    Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show()
                }
            })
        }
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


                    // if download then use other call to server to get Data too
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

    private fun insertResponseToDBCategory(responseBody: List<Category>)
    {

        GlobalScope.launch(Dispatchers.IO) {
            // write contents from JSON-String to DB
            appDb.categoryDao().insertAll(responseBody)
        }
    }
}