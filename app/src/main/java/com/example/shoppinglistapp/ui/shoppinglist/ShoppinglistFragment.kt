package com.example.shoppinglistapp.ui.shoppinglist

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.transition.Slide
import androidx.transition.TransitionManager
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
    private var lastInsertedItem = Item(7777,"Fruchttiger","Grundnahrungsmittel",0)
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
                                addItemToView(item)
                            }
                            addItemsByCategory(it)
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

    private fun addItemToView(item: Item, index: Int = 0) {


        val product_layout = getLayoutInflater().inflate(R.layout.product_linearlayout, null, false)
        val shoppinglist_layout = binding.layoutShoppingList

        product_layout.findViewById<TextView>(R.id.textView_product).text = item.name

        //get the checkbox to do something on checked
        product_layout.findViewById<CheckBox>(R.id.deletion_checkbox).setOnCheckedChangeListener {
            compoundButton, b -> Log.e("Checkbox", "Checkbox changed")
            removeItem(product_layout, item.id!!)

            Log.e("Button", "Id: " + item.id!!)
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

    private fun getLastInsertedId(){
        lateinit var items: List<Item>

        GlobalScope.launch {
            items = appDb.itemDao().getLastInsertedItem()

            withContext(Dispatchers.Main) {
                if (items.isNotEmpty()) {
                    if(lastInsertedItem == items[0])
                    {
                        lastInsertedItem = items[0]
                        lastInsertedItem.id!!.plus(1)
                    }
                    else
                    {
                        lastInsertedItem = items[0]
                    }

                }
            }
        }
    }

    private fun addItemsByCategory(category: Category)
    {
        val addProduct_layout = getLayoutInflater().inflate(R.layout.add_product_linearlayout, null, false)
        val shoppinglist_layout = binding.layoutShoppingList

        val productname = addProduct_layout.findViewById<EditText>(R.id.editText_Item_Name).text

        shoppinglist_layout.addView(addProduct_layout);

        addProduct_layout.findViewById<ImageButton>(R.id.add_Item).setOnClickListener {
            if(productname.isNotEmpty())
            {
                getLastInsertedId()
                val newItem = Item(null, productname.toString(), category.name,0 ) // mark_ new content uuid
                // add the item just before the plus-button position
                insertItemToDb(newItem)
                hideKeyboard()

                val newShowItem = Item(lastInsertedItem.id,productname.toString(), category.name,0)
                addItemToView(newShowItem, shoppinglist_layout.indexOfChild(addProduct_layout))
                productname.clear()

            }
        }
        // on pressing enter within the edittext field for better usability
        addProduct_layout.findViewById<EditText>(R.id.editText_Item_Name).setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if(productname.isNotEmpty())
                {
                    getLastInsertedId()
                    val newItem = Item(null, productname.toString(), category.name,0 )
                    // add the item just before the plus-button position
                    insertItemToDb(newItem)
                    hideKeyboard()

                    val newShowItem = Item(lastInsertedItem.id,productname.toString(), category.name,0)
                    addItemToView(newShowItem, shoppinglist_layout.indexOfChild(addProduct_layout))
                    productname.clear()
                }

                return@OnKeyListener true
            }
            false
        })
    }

    private fun insertItemToDb(item: Item)
    {
        GlobalScope.launch {
            appDb.itemDao().insert(item)
        }
    }

    private fun removeItem(view: View, id: Int )
    {
        val animationFadeOut = AnimationUtils.loadAnimation(context, R.anim.item_animation_zoom)
        view.startAnimation(animationFadeOut)
        Handler().postDelayed({
        binding.layoutShoppingList.removeView(view)
        }, 300)

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

    private fun animationSlideInText(_view: View, fromLeft: Boolean = false, duration: Long = 250)
    {
        _view.visibility = View.INVISIBLE

        val mSlide = Slide()
        mSlide.duration = duration

        if(fromLeft)
        {
            mSlide.slideEdge = Gravity.LEFT
        }
        else
        {
            mSlide.slideEdge = Gravity.RIGHT
        }

        TransitionManager.beginDelayedTransition((view as ViewGroup?)!!, mSlide)

        _view.visibility = View.VISIBLE
    }

    // unused TODO: remove if not needed
    private fun animationInText(_view: View, fromLeft: Boolean = false, duration: Long = 250)
    {
        _view.visibility = View.INVISIBLE

        val mSlide = Slide()
        mSlide.duration = duration

        if(fromLeft)
        {
            mSlide.slideEdge = Gravity.TOP
        }
        else
        {
            mSlide.slideEdge = Gravity.TOP
        }

        TransitionManager.beginDelayedTransition((view as ViewGroup?)!!, mSlide)

        _view.visibility = View.VISIBLE
    }

}