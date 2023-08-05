package com.example.energy_meter_roomdb

import androidx.room.*
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.Dao.Item.Item

@Dao
interface ItemDao {

    @Query("SELECT * FROM item_table")
    suspend fun getAll(): List<Item>

    @Query("SELECT * FROM item_table WHERE deleted != 1")
    fun getAllNotDeleted(): List<Item>

    @Query("SELECT * FROM item_table Limit 1")
    fun getOne(): Item

    @Query("SELECT * FROM item_table WHERE category LIKE :category")
    suspend fun findByCategory(category: String): List<Item>

    @Query("SELECT * FROM item_table WHERE category LIKE :category AND status LIKE :status")
    suspend fun findByCategoryWithStatus(category: String, status: Int): List<Item>

    @Query("SELECT * FROM item_table WHERE status LIKE :status")
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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(objects: List<Item>)

    @Delete
    suspend fun delete(item: Item)

    @Query("UPDATE item_table SET deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Int)

    @Query("DELETE FROM item_table")
    suspend fun  deleteAll()

    @Delete
    suspend fun deleteItems(objects: List<Item>)

    @Query("UPDATE item_table SET name=:name, category=:category, status=:status WHERE id = :id")
    suspend fun update(id: Int, name : String , category: String , status: Int)

    @Query("UPDATE item_table SET name=:newName WHERE id = :id")
    suspend fun updateItemName(newName: String, id: Int)

    @Query("UPDATE item_table SET status=:status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: Int)

    //get last inserted Object
    @Query("SELECT * FROM item_table WHERE status LIKE 0 ORDER BY id DESC LIMIT 1")
    fun getLastInsertedItem(): List<Item>

    //get last inserted id
    @Query("SELECT seq FROM sqlite_sequence WHERE name = :tableName")
    abstract fun getSequenceNumber(tableName: String): Long?
}