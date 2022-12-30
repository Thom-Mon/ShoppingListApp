package com.example.shoppinglistapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shoppinglistapp.R

class CustomAdapter(private val mList: List<ElementsViewModel>): RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private var listener: OnItemsClickListener? = null

    interface OnItemsClickListener {
        fun onItemClick(itemsViewModel: ElementsViewModel, buttonId: Int)
    }

    fun setWhenClickListener(listener: OnItemsClickListener?) {
        this.listener = listener
    }

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false) // here put the layout inside of the design of the cardview

        return ViewHolder(view)
    }


    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ElementsViewModel = mList[position]

        // sets the text to the textview from our itemHolder class
        holder.textView.text = ElementsViewModel.name
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textView: TextView = itemView.findViewById(R.id.card_category_name)
    }
}
