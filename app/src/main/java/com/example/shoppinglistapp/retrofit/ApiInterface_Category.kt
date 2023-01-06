package com.example.shoppinglistapp.retrofit

import com.example.shoppinglistapp.Dao.Category.Category
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface ApiInterface_Category {

    @GET("index.php?c=pages&a=response")
    fun getData_Category(): Call<List<com.example.shoppinglistapp.Dao.Category.Category>>

    @POST("index.php?c=pages&a=response")
    fun sendDataCategory(
        @Body body: List<Category?>
    ): Call<List<com.example.shoppinglistapp.Dao.Category.Category>>


}