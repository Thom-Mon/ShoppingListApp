package com.example.shoppinglistapp.ui.category

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoppinglistapp.AppDatabase
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.Dao.Item.Item
import com.example.shoppinglistapp.adapter.CustomAdapter
import com.example.shoppinglistapp.adapter.ElementsViewModel
import com.example.shoppinglistapp.databinding.FragmentCategoryBinding
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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

        // recyclerview button listener Implementation
        adapter.setWhenClickListener(object : CustomAdapter.OnItemsClickListener {
            override fun onItemClick(elementsViewModel: ElementsViewModel, buttonId: Int, filename: String) {
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
                if(binding.entryCategory.text!!.isNotEmpty())
                {
                    // check if category exists already in Arraylist
                    val index = data.indexOfFirst{
                        it.name == binding.entryCategory.text.toString()
                    }
                    if(index != -1)
                    {
                        Log.i("Element", "This category already exists")
                        // Toast on the middle of the screen
                        val toast = Toast.makeText(context,"Diese Kategorie existiert bereits!",Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
                        toast.show()
                    }
                    else
                    {
                        addCategory()
                        binding.entryCategory.text?.clear()
                        hideKeyboard()
                    }

                }

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
        lateinit var items: List<Item>
        var dataId = 0
        category = appDb.categoryDao().findById(_id)
        items = appDb.itemDao().findByCategory(category.name!!)
        appDb.categoryDao().delete(category)
        // delete all products connected to the category
        appDb.itemDao().deleteItems(items)

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