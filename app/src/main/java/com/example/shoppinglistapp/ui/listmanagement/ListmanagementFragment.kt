package com.example.shoppinglistapp.ui.listmanagement

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoppinglistapp.AppDatabase
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.Dao.Item.Item
import com.example.shoppinglistapp.adapter.CustomAdapter
import com.example.shoppinglistapp.adapter.ElementsViewModel
import com.example.shoppinglistapp.databinding.FragmentListmanagementBinding
import com.example.shoppinglistapp.showConfirmationDialog
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


        binding.btnSaveToApp.setOnClickListener {
            saveToExternalStorage()
        }

        binding.btnSaveToDownload.setOnClickListener {
            saveToExternalStorage(true)
        }

        binding.btnLoadFromFile.setOnClickListener {
            loadFromExternalStorage()
        }

        // recyclerview button listener Implementation
        adapter.setWhenClickListener(object : CustomAdapter.OnItemsClickListener {
            override fun onItemClick(elementsViewModel: ElementsViewModel, buttonId: Int, filename: String) {
                if(buttonId == 1){
                    if(filename.isNotEmpty())
                    {
                        binding.entryFilename.setText(filename)
                    }
                }
                else if(buttonId == 0){
                    showConfirmationDialog("Liste löschen", "Wollen Sie die Liste wirklich löschen?"){
                        deleteInternalFile(filename)
                        refreshRecyclerView()
                    }

                }
            }
        })

        refreshRecyclerView()

        return root
    }

    private fun deleteInternalFile(filename: String)
    {
        //TODO: Implement
        Log.e("Tag-FILENAME TO DELETE:" , "->$filename<-")
        try {
            if(File(requireContext().filesDir, filename).exists())
            {
                File(requireContext().filesDir, filename).delete()
            }
        } catch (e: Exception)
        {
            Toast.makeText(requireContext(), "Fehler beim Löschen der Liste", Toast.LENGTH_SHORT).show()
        }
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

    private fun saveToExternalStorage(isExporting: Boolean = false)
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
                        File(requireContext().filesDir, fileName).printWriter().use { out ->
                            out.println(gson.toJson(entries))}
                        if(isExporting)
                        {
                            saveFileToDownloads(fileName,gson.toJson(entries))
                        }
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
            binding.textInputLayoutFilename.boxBackgroundColor = Color.WHITE

        }
        else
        {
            binding.textInputLayoutFilename.boxBackgroundColor = Color.rgb(244,178,178)

            Toast.makeText(requireContext(), "Keinen Dateinamen zum Laden eingegeben!", Toast.LENGTH_SHORT).show()
            return
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
            Toast.makeText(requireContext(), "Fehler beim Laden: Datei nicht gefunden!", Toast.LENGTH_SHORT).show()
        }
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


    private fun saveFileToDownloads(filename : String, fileContent: String) {
        // this is from another example it is working good better than any other example using the MediaStore, MediaStore might be overkill
        try{
            val f = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename+".hai")
            f.delete()
            f.appendText(fileContent)
            Toast.makeText(requireContext(), "Speicherort: " + Environment.DIRECTORY_DOWNLOADS + "/" + filename+".hai", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Fehler beim Speichern der Liste. Rechte vorhanden?", Toast.LENGTH_LONG).show()
        }

    }
}