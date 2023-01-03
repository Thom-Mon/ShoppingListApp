package com.example.shoppinglistapp.ui.shoppinglist

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.shoppinglistapp.AppDatabase
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.Dao.Item.Item
import com.example.shoppinglistapp.R
import com.example.shoppinglistapp.databinding.FragmentShoppinglistBinding
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ShoppinglistFragment : Fragment() {

    private var _binding: FragmentShoppinglistBinding? = null
    private  lateinit var appDb : AppDatabase
    private val gson = Gson()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val shoppinglistViewModel =
            ViewModelProvider(this).get(ShoppinglistViewModel::class.java)

        _binding = FragmentShoppinglistBinding.inflate(inflater, container, false)
        val root: View = binding.root


        appDb = AppDatabase.getDatabase(requireContext())

        lateinit var categories: List<Category>
        lateinit var items: List<Item>

        GlobalScope.launch {
            categories = appDb.categoryDao().getAll()

            if(categories.isNotEmpty())
            {
                withContext(Dispatchers.Main) {
                    if (categories != null)
                    {
                        categories.forEach {
                            items = appDb.itemDao().findByCategoryWithStatus(it.name!!,0)

                            addCategory(it.name)
                            for (item in items) {
                                addItem(item)
                            }
                            addPlusButton(it)
                        }
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



        return root
    }

    private fun addItem(item: Item, index: Int = 0) {

        val product_layout = getLayoutInflater().inflate(R.layout.product_linearlayout, null, false)
        val shoppinglist_layout = binding.layoutShoppingList

        product_layout.findViewById<TextView>(R.id.textView_product).text = item.name

        //get the checkbox to do something on checked
        product_layout.findViewById<CheckBox>(R.id.deletion_checkbox).setOnCheckedChangeListener {
                compoundButton, b -> Log.e("Checkbox", "Checkbox changed")
                removeItem(product_layout, item.id!!)
        }
        if(index != 0)
        {
            shoppinglist_layout.addView(product_layout, index);
            return
        }
        shoppinglist_layout.addView(product_layout);
    }

    private fun addCategory(name: String) {

        val product_layout = getLayoutInflater().inflate(R.layout.category_headline_linearlayout, null, false)
        val shoppinglist_layout = binding.layoutShoppingList

        product_layout.findViewById<TextView>(R.id.textView_category).text = name

        shoppinglist_layout.addView(product_layout);
    }

    private fun addPlusButton(category: Category)
    {
        val addProduct_layout = getLayoutInflater().inflate(R.layout.add_product_linearlayout, null, false)
        val shoppinglist_layout = binding.layoutShoppingList

        val productname = addProduct_layout.findViewById<EditText>(R.id.editText_Item_Name).text

        shoppinglist_layout.addView(addProduct_layout);

        addProduct_layout.findViewById<ImageButton>(R.id.add_Item).setOnClickListener {
            if(productname.isNotEmpty())
            {
                val newItem = Item(null, productname.toString(), category.name,0 )
                productname.clear()
                hideKeyboard()
                // add the item just before the plus-button position
                addItem(newItem, shoppinglist_layout.indexOfChild(addProduct_layout))
                addProduct(newItem)
            }
        }
        // on pressing enter within the edittext field for better usability
        addProduct_layout.findViewById<EditText>(R.id.editText_Item_Name).setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if(productname.isNotEmpty())
                {
                    val newItem = Item(null, productname.toString(), category.name,0 )
                    productname.clear()
                    hideKeyboard()
                    // add the item just before the plus-button position
                    addItem(newItem, shoppinglist_layout.indexOfChild(addProduct_layout))
                    addProduct(newItem)
                }

                return@OnKeyListener true
            }
            false
        })
    }

    private fun addProduct(item: Item)
    {
        GlobalScope.launch {
            appDb.itemDao().insert(item)
            //deleteData(elementsViewModel.id.toInt())
        }
    }

    private fun removeItem(view: View, id: Int )
    {
        binding.layoutShoppingList.removeView(view)

        GlobalScope.launch {
            appDb.itemDao().updateStatus(id,1)
            //deleteData(elementsViewModel.id.toInt())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

}