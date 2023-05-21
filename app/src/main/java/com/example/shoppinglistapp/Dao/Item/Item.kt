package com.example.shoppinglistapp.Dao.Item

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "item_table")
data class Item(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "category") val category: String?,
    @ColumnInfo(name = "status") val status: Int?,
    @ColumnInfo(name = "uuid") val uuid: String?,
    @ColumnInfo(name = "deleted") val deleted: Boolean?
    ) {
    constructor(id: Int?, name: String?, category: String?, status: Int?) : this(id, name, category,status, UUID.randomUUID().toString(), false)
}

