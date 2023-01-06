package com.example.shoppinglistapp.retrofit

import com.example.shoppinglistapp.Dao.Item.Item
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface_Item {

    @GET("index.php?c=pages&a=getitems")
    fun getDataItem(): Call<List<com.example.shoppinglistapp.Dao.Item.Item>>

    @POST("index.php?c=pages&a=getitems")
    fun sendDataItem(
        @Body body: List<Item?>
    ): Call<List<com.example.shoppinglistapp.Dao.Item.Item>>
}