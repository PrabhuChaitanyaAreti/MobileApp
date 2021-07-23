package com.vsoft.goodmankotlin.utils

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient() {
    companion object {
    private var instance: RetrofitClient? = null
    private var myApi: RetrofitApiInterface? = null
    //var BASE_URL:String = "http://111.93.3.148:12803"
    var BASE_URL:String = "http://111.93.3.148:12808"

    init {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        myApi = retrofit.create(RetrofitApiInterface::class.java)
    }

        @Synchronized
        public fun getInstance(): RetrofitClient? {
            if (instance == null) {
                instance = RetrofitClient()
            }
            return instance
        }
    }

    fun getMyApi(): RetrofitApiInterface? {
        return myApi
    }
}