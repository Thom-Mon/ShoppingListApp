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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
            deleteAll()
        }

        binding.btnCallApi.setOnClickListener {
            callApi()
        }

        binding.btnCallApiPOST.setOnClickListener {
            callApi_Post()
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

    fun callApi_Post()
    {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL) //TODO: BaseURL An Pi-Zero anpassen!!!
            .build()
            .create(ApiInterface_Category::class.java)

        val req_category = Category(1, "Post-Kategorie")
        val retrofitData = retrofitBuilder.sendDataCategory(req_category)

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

    private fun insertResponseToDB(responseBody: List<Category>)
    {

        GlobalScope.launch(Dispatchers.IO) {
            // write contents from JSON-String to DB
            appDb.categoryDao().insertAll(responseBody)
            Log.e("LastEntry",appDb.itemDao().getSequenceNumber("item_table").toString())
        }

    }

}