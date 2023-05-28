package com.example.shoppinglistapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import kotlinx.coroutines.withContext

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

