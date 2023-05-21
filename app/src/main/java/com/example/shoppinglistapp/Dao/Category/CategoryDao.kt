package com.example.shoppinglistapp.Dao.Category

import androidx.room.*
import com.example.shoppinglistapp.Dao.Item.Item

@Dao
interface CategoryDao {

    @Query("SELECT * FROM category_table")
    fun getAll(): List<Category>

    @Query("SELECT * FROM category_table WHERE deleted != 1")
    fun getAllNotDeleted(): List<Category>

    @Query("SELECT * FROM category_table WHERE id LIKE :id LIMIT 1")
    suspend fun findById(id: Int): Category

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("UPDATE category_table SET deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Int)

    @Query("DELETE FROM category_table")
    suspend fun  deleteAll()

    // used for testing, to insert from List on Startup (dummy data)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(category: List<Category>)

    //get last inserted id
    @Query("SELECT seq FROM sqlite_sequence WHERE name = :tableName")
    abstract fun getSequenceNumber(tableName: String): Long?
}