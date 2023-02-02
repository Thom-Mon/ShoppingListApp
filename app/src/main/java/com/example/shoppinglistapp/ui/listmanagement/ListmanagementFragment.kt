package com.example.shoppinglistapp.ui.listmanagement

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.shoppinglistapp.AppDatabase
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.Dao.Item.Item
import com.example.shoppinglistapp.R
import com.example.shoppinglistapp.databinding.FragmentListmanagementBinding
import com.example.shoppinglistapp.databinding.FragmentSettingsBinding
import com.example.shoppinglistapp.ui.settings.SettingsViewModel
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

        binding.btnSaveToFile.setOnClickListener {
            saveToExternalStorage()
        }

        binding.btnLoadFromFile.setOnClickListener {
            loadFromExternalStorage()
        }


        return root
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
        Toast.makeText(requireContext(), "Datei gespeichert: $fileName", Toast.LENGTH_SHORT).show()
    }

    private fun loadFromExternalStorage()
    {
        //FIXME:
        // 1. Save categories to on saveToExternalStorage in different file
        // 2. Or what is more flexible -> use category in items to build category from
        //    items itself 01.02.2023
        var loadedData = java.util.ArrayList<Item>()

        // get list of files stored on phone
        var files: Array<String> = requireContext().fileList()
        for(file in files)
        {
            Log.e("Files", file)
        }

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