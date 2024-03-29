package com.example.shoppinglistapp.ui.listmanagement

import android.app.Activity
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoppinglistapp.*
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
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

class ListmanagementFragment : Fragment() {
    private var _binding: FragmentListmanagementBinding? = null
    private  lateinit var appDb : AppDatabase
    private var data = ArrayList<ElementsViewModel>()
    private val adapter = CustomAdapter(data)
    private val gson = Gson()
    private val filePickerRequestCode = 42

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

        binding.btnSendViaMessenger.setOnClickListener {
            openMessengerWithFile()
        }

        binding.btnOpenFilePicker.setOnClickListener {
            openFilePickerDialog()
        }

        // recyclerview button listener Implementation
        adapter.setWhenClickListener(object : CustomAdapter.OnItemsClickListener {
            override fun onItemClick(position: Int, elementsViewModel: ElementsViewModel, buttonId: Int, filename: String) {
                if(buttonId == 1){
                    // Click on Element Button
                    if(filename.isNotEmpty())
                    {
                        binding.entryFilename.setText(filename)
                    }
                }
                else if(buttonId == 0){
                    // Delete Button
                    showConfirmationDialog("Liste löschen", getString(R.string.dialog_list_deletion_warning_text)){
                        deleteInternalFile(filename)
                        refreshRecyclerView()
                    }
                }
                else if(buttonId == 2){
                    // Edit Button (Placeholder is unused)
                    if(filename.isNotEmpty())
                    {
                        binding.entryFilename.setText(filename)
                    }

                }
            }
        })

        refreshRecyclerView()

        return root
    }


    fun openFilePickerDialog() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // Set the initial type to show all files
        }
        startActivityForResult(intent, filePickerRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == filePickerRequestCode && resultCode == Activity.RESULT_OK) {
            // Handle the selected file URI here (data?.data)
            loadFromFilePicker(data?.data)
        }
    }

    /*
    ** Opens the Messenger-Choose-Dialog to send the current shopping list, if there is text in the entry-field the app tries to send the file there
    */
    private fun openMessengerWithFile() {
        if (binding.entryFilename.text!!.isNotEmpty()) {
            val filename = binding.entryFilename.text.toString()
            val file = File(requireContext().filesDir, filename)

            if (!file.exists()) {
                // File does not exist, handle the error //TODO: MELDUNG AN USER FEHLT!
                return
            }
            val uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName + ".provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.type = "application/x-hai"

            // Grant read permission to the receiving app
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            try {
                startActivity(Intent.createChooser(intent, "Send File"))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "No messaging app found.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        else
        {
            // if the input field is empty use the current shoppinglist and send this with currentdate
            openMessengerSendCurrentList(appDb, gson, requireContext())
        }
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

        var files: Array<String> = requireContext().fileList()
        var index = 0
        for(file in files)
        {
            data.add(
                ElementsViewModel(index, SpannableStringBuilder(file).toString())
            )
            index++
        }
        adapter.notifyDataSetChanged()
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
        var fileName = "Einkaufsliste_" + getCurrentDate() + ".hai"
        val mimeType = "application/x-hai"
        //-> saving to JSON for easy get it back
        if(binding.entryFilename.text!!.isNotEmpty())
        {
            fileName = binding.entryFilename.text.toString()
            // if filename does not end with ".hai" set it. needed to send it later and recognize it on other phones
            if(!fileName.endsWith(".hai"))
            {
                fileName += ".hai"
            }
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
                        /*File(requireContext().filesDir, fileName).printWriter().use { out ->
                            out.println(gson.toJson(entries))}*/ // -> old working version keep until tested! 03.06.2023
                        FileOutputStream(File(requireContext().filesDir, fileName)).use { outputStream ->
                            outputStream.write(gson.toJson(entries).toByteArray())
                        }
                        if (isExporting) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                saveFileToDownloads(fileName, gson.toJson(entries))
                            } else {
                                saveFileLegacy(fileName, gson.toJson(entries))
                            }
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

    private fun loadFromFilePicker(data: Uri?)
    {
        var loadedData = java.util.ArrayList<Item>()

        if(data == null)
        {
            Toast.makeText(requireContext(), "Die Datei konnte nicht geladen werden", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val fileContents = readTextFromUri(data) // i guess data is the uri here?!
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
            Toast.makeText(requireContext(), "Datei geladen", Toast.LENGTH_SHORT).show()
        }
        catch (e: Exception){
            Toast.makeText(requireContext(), "Fehler beim Laden: Datei nicht gefunden!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readTextFromUri(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
            stringBuilder.append("\n")
        }
        reader.close()
        return stringBuilder.toString()
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


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveFileToDownloads(fileName: String, fileContent: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/x-hai")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver: ContentResolver = requireContext().contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let { uri ->
            resolver.openOutputStream(uri)?.let { outputStream ->
                outputStream.write(fileContent.toByteArray())
                outputStream.flush()
                outputStream.close()
            }
        }
    }

    private fun saveFileLegacy(fileName: String, fileContent: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)
        FileOutputStream(file).use { outputStream ->
            outputStream.write(fileContent.toByteArray())
            outputStream.flush()
            outputStream.close()
        }
    }
}