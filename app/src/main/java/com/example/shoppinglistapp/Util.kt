package com.example.shoppinglistapp

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.shoppinglistapp.Dao.Item.Item
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
import java.text.SimpleDateFormat
import java.util.*

fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

   fun Fragment.showConfirmationDialog(title: String, message: String, onConfirmed: () -> Unit) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)

        alertDialogBuilder.setPositiveButton("Ja") { dialogInterface: DialogInterface, _: Int ->
            // Perform the action you want to execute when the user clicks "Yes"
            onConfirmed.invoke()
            dialogInterface.dismiss()
        }

        alertDialogBuilder.setNegativeButton("Nein") { dialogInterface: DialogInterface, _: Int ->
            // Perform any action or dismiss the dialog when the user clicks "No"
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun Fragment.showEditDialog(
        context: Context,
        layoutId: Int,
        initialText: String,
        onSave: (newText: String) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(layoutId, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val editText: EditText = dialogView.findViewById(R.id.editText)
        val saveButton: Button = dialogView.findViewById(R.id.saveButton)

        // Set the initial text in the EditText
        editText.setText(initialText)

        // TextWatcher Element to use on editText - only save if the textinput is not empty
        class MyTextWatcher : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString()
                saveButton.isEnabled = !text.isNullOrEmpty()}
        }

        // disable the save button if there is not text in the edittext provided
        editText.addTextChangedListener(MyTextWatcher())

        // Save button click listener
        saveButton.setOnClickListener {
            val newText = editText.text.toString()
            // Call the provided onSave callback to handle the saved data
            onSave(newText)

            // Dismiss the dialog
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }




    fun openMessengerSendCurrentList(appDb: AppDatabase, gson: Gson, context: Context) {
        lateinit var entries: List<Item>

        GlobalScope.launch(Dispatchers.IO){
            entries = appDb.itemDao().getAll()
            if(entries.isNotEmpty())
            {
                if(entries != null)
                {
                    withContext(Dispatchers.Main)
                    {
                        val fileName = "Einkaufsliste_" + getCurrentDate()
                        val file = File(context.filesDir, fileName)

                        FileOutputStream(file).use { outputStream ->
                            outputStream.write(gson.toJson(entries).toByteArray())
                        }

                        val uri = FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            file
                        )

                        val intent = Intent(Intent.ACTION_SEND)
                        intent.putExtra(Intent.EXTRA_STREAM, uri)
                        intent.type = "application/x-hai"

                        // Grant read permission to the receiving app
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        try {
                            startActivityWithChooser(context, Intent.createChooser(intent, "Send File"),"Send File")
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "No messaging app found.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }

    fun startActivityWithChooser(context: Context, intent: Intent, title: String) {
        val chooserIntent = Intent.createChooser(intent, title)
        if (chooserIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooserIntent)
        } else {
            // Handle the case where no activity can handle the intent
            // For example, show an error message or take appropriate action.
        }
    }

    fun readTextFromUri(context: Context,uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri)
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




