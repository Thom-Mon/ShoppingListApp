package com.example.shoppinglistapp

import android.app.AlertDialog
import android.content.DialogInterface
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

