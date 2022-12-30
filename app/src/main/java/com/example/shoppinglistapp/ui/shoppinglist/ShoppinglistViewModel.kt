package com.example.shoppinglistapp.ui.shoppinglist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ShoppinglistViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is ShoppingList Fragment"
    }
    val text: LiveData<String> = _text
}