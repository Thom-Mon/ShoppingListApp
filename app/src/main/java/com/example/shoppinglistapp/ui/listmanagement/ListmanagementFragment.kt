package com.example.shoppinglistapp.ui.listmanagement

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoppinglistapp.AppDatabase
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.Dao.Item.Item
import com.example.shoppinglistapp.adapter.CustomAdapter
import com.example.shoppinglistapp.adapter.ElementsViewModel
import com.example.shoppinglistapp.databinding.FragmentListmanagementBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ListmanagementFragment : Fragment() {
    private var _binding: FragmentListmanagementBinding? = null
    private  lateinit var appDb : AppDatabase
    private var data = ArrayList<ElementsViewModel>()
    private val adapter = CustomAdapter(data)
    private val gson = Gson()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val listmanagementViewModel =
            ViewModelProvider(this).get(ListmanagementViewModel::class.java)

        _binding = FragmentListmanagementBinding.inflate(inflater, container, false)
        val root: View = binding.root

        appDb = AppDatabase.getDatabase(requireContext())
        val recyclerView = binding.recyclerviewFiles
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = adapter


        binding.btnSaveToFile.setOnClickListener {
            saveToExternalStorage()
        }

        binding.btnLoadFromFile.setOnClickListener {
            loadFromExternalStorage()
        }

        refreshRecyclerView()

        return root
    }

    private fun refreshRecyclerView()
    {
        val size = data.size
        data.clear()
        adapter.notifyItemRangeRemoved(0, size)

        // watch out the filesystem read to fast, so it cannot show the newly
        // created file on the recycler view yet!!! TODO:
        // Further TODO:
        // 1. On Click on Item of recycler view delete it on filesystem to
        // 2. On Click get it to load
        // 3. On Click get name to textinput to make it possible to save
        // get list of files stored on phone
        var files: Array<String> = requireContext().fileList()
        var index = 0
        for(file in files)
        {
            data.add(
                ElementsViewModel(index, SpannableStringBuilder(file).toString())
            )
            adapter.notifyItemInserted(data.size-1)
            binding.recyclerviewFiles.scheduleLayoutAnimation()
            index++
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
        var fileName = "Default"
        //-> saving to JSON for easy get it back
        if(binding.entryFilename.text!!.isNotEmpty())
        {
            fileName = binding.entryFilename.text.toString()
        }

        File(requireContext().filesDir, fileName).delete()

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
        Toast.makeText(requireContext(), "Datei gespeichert: $fileName", Toast.LENGTH_SHORT).show()

        // add new file to recyclerview
        refreshRecyclerView()
    }

    private fun loadFromExternalStorage()
    {
        var loadedData = java.util.ArrayList<Item>()



        var filename = "Default"
        if(binding.entryFilename.text!!.isNotEmpty())
        {
            filename = binding.entryFilename.text.toString()
        }

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


            Toast.makeText(requireContext(), "Datei geladen: ${filename}", Toast.LENGTH_SHORT).show()
        }
        catch (e: Exception){
            Toast.makeText(requireContext(), "Fehler beim Laden: ${e.message}", Toast.LENGTH_SHORT).show()
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