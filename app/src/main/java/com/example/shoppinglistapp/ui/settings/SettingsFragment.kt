package com.example.shoppinglistapp.ui.settings

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.system.Os.open
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.shoppinglistapp.AppDatabase
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.Dao.Item.Item
import com.example.shoppinglistapp.R
import com.example.shoppinglistapp.StartupClass
import com.example.shoppinglistapp.databinding.FragmentSettingsBinding
import com.example.shoppinglistapp.databinding.FragmentShoppinglistBinding
import com.example.shoppinglistapp.ui.shoppinglist.ShoppinglistViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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

}