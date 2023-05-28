package com.example.shoppinglistapp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import kotlinx.coroutines.withContext


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

