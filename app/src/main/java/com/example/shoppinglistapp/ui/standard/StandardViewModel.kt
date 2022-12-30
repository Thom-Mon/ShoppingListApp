package com.example.shoppinglistapp.ui.standard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StandardViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is Standard Fragment"
    }
    val text: LiveData<String> = _text
}