package com.example.shoppinglistapp.Dao.Category

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shoppinglistapp.Dao.Item.Item

@Dao
interface CategoryDao {

    @Query("SELECT * FROM item_table")
    fun getAll(): List<Category>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)

    // used for testing, to insert from List on Startup (dummy data)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(category: List<Category>)
}