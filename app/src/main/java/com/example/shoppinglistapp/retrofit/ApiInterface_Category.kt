package com.example.shoppinglistapp.retrofit

import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface_Category {

    @GET("index.php?c=pages&a=response")
    fun getData_Category(): Call<List<com.example.shoppinglistapp.Dao.Category.Category>>





}