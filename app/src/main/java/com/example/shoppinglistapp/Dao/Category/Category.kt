package com.example.shoppinglistapp.Dao.Category

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "category_table")
data class Category(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "uuid") val uuid: String?,
    @ColumnInfo(name = "deleted") val deleted: Boolean?
) {
    constructor(id: Int?, name: String?) : this(id, name, UUID.randomUUID().toString(), false)
}
