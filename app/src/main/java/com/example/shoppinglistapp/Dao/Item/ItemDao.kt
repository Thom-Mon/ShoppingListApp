package com.example.energy_meter_roomdb

import androidx.room.*
import com.example.shoppinglistapp.Dao.Item.Item

@Dao
interface ItemDao {

    @Query("SELECT * FROM item_table")
    fun getAll(): List<Item>

    @Query("SELECT * FROM item_table WHERE category LIKE :category")
    suspend fun findByCategory(category: String): List<Item>

    @Query("SELECT * FROM item_table WHERE category LIKE :status")
    suspend fun findByStatus(status: Int): List<Item>

    // This function is used to determine if a item needs to be created or updated
    @Query("SELECT * FROM item_table WHERE category LIKE :name Limit 1")
    suspend fun findByName(name: String): Item

    @Query("SELECT * FROM item_table WHERE id LIKE :id LIMIT 1")
    suspend fun findById(id: Int): Item

    @Query("SELECT EXISTS (SELECT * FROM item_table WHERE id LIKE :id LIMIT 1)")
    suspend fun isExistingId(id: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item)

    // used for testing, to insert from List on Startup (dummy data)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(objects: List<Item>)


    @Delete
    suspend fun delete(item: Item)

    @Query("DELETE FROM item_table")
    suspend fun  deleteAll()

    @Query("UPDATE item_table SET name=:name, category=:category, status=:status WHERE id = :id")
    suspend fun update(id: Int, name : String , category: String , status: Int)

    //get last inserted id
    @Query("SELECT seq FROM sqlite_sequence WHERE name = :tableName")
    abstract fun getSequenceNumber(tableName: String): Long?
}