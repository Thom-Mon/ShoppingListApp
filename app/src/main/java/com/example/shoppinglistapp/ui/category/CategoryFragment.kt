package com.example.shoppinglistapp.ui.category

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoppinglistapp.AppDatabase
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.Dao.Item.Item
import com.example.shoppinglistapp.R
import com.example.shoppinglistapp.adapter.CustomAdapter
import com.example.shoppinglistapp.adapter.ElementsViewModel
import com.example.shoppinglistapp.databinding.FragmentCategoryBinding
import com.example.shoppinglistapp.databinding.FragmentShoppinglistBinding
import com.example.shoppinglistapp.ui.shoppinglist.ShoppinglistViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private  lateinit var appDb : AppDatabase
    private val gson = Gson()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var data = ArrayList<ElementsViewModel>()
    private val adapter = CustomAdapter(data)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val categoryViewModel =
            ViewModelProvider(this).get(CategoryViewModel::class.java)

        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*val textView: TextView = binding.textCategory
        categoryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }*/
        // code here

        appDb = AppDatabase.getDatabase(requireContext())

        val recyclerView = binding.recyclerviewCategory
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = adapter

        adapter.setWhenClickListener(object : CustomAdapter.OnItemsClickListener {
            override fun onItemClick(elementsViewModel: ElementsViewModel, buttonId: Int) {
                // the button id refers to either delete or edit from the recyclerview
                if(buttonId == 1){
                    Log.e("Button","Button 1 pressed")
                }
                else if(buttonId == 0){
                    Log.e("Button","Button 0 pressed (DELETION)")
                    GlobalScope.launch {
                        deleteData(elementsViewModel.id.toInt())
                    }
                }
            }
        })

        lateinit var entries: List<Category>

        // Create here the setWhenClickListener für den Adapter
        GlobalScope.launch {
            entries = appDb.categoryDao().getAll()

            if(entries.isNotEmpty())
            {
                withContext(Dispatchers.Main) {
                    if (entries != null) {
                        entries.forEach {
                            data.add(
                                ElementsViewModel(it.id!!,SpannableStringBuilder(it.name).toString())
                                    )
                        }
                        adapter.notifyItemInserted(data.size-1)
                        binding.recyclerviewCategory.scheduleLayoutAnimation()
                    }
                }
            }
            else
            {
                withContext(Dispatchers.Main){
                    //toastMessage("Keine Daten gefunden")
                }
            }
        }
        //END
        binding.textFieldCategoryEntry.setEndIconOnClickListener{
            //Toast.makeText(requireContext(), "Endicon Pressed",Toast.LENGTH_LONG).show()
            if(binding.entryCategory.text!!.isNotEmpty())
            {
                addCategory()
                binding.entryCategory.text?.clear()
                hideKeyboard()
            }
        }

        binding.entryCategory.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                addCategory()
                binding.entryCategory.text?.clear()
                hideKeyboard()

                return@OnKeyListener true
            }
            false
        })

        return root
    }

    fun addCategory(){

        val name = binding.entryCategory.text
        val category = Category(null, name.toString())

        GlobalScope.launch(Dispatchers.IO){
            appDb.categoryDao().insert(category)
            val lastInsertedId = appDb.categoryDao().getSequenceNumber("category_table")?.toInt()

            withContext(Dispatchers.Main){
                data.add(ElementsViewModel(lastInsertedId!!, category.name!!))
                adapter.notifyItemInserted(data.size-1)
            }
        }
    }

    // Helpers rebase to activity later!!!!!!
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

    private suspend fun deleteData(_id: Int) {
        val category: Category
        var dataId = 0
        category = appDb.categoryDao().findById(_id)
        appDb.categoryDao().delete(category)

        for(category in data)
        {
            if(category.id.toInt() == _id)
            {
                break;
            }
            dataId++
        }
        withContext(Dispatchers.Main){
            data.removeAt(dataId)
            adapter.notifyItemRemoved(dataId)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}