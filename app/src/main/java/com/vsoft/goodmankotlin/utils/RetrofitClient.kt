package com.vsoft.goodmankotlin.utils

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient {
    companion object {
    private var instance: RetrofitClient? = null
    private var myApi: RetrofitApiInterface? = null
      //  private var BASE_URL:String="http://3.218.249.156:12808"
      private var BASE_URL:String="http://111.93.3.148:16808"
        //private var timeOut:Long= Long.MAX_VALUE
        private var timeOut:Long= 24

    init {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(timeOut, TimeUnit.HOURS)
            .readTimeout(timeOut, TimeUnit.HOURS)
            .writeTimeout(timeOut, TimeUnit.HOURS)
            .build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        myApi = retrofit.create(RetrofitApiInterface::class.java)

    }

        @Synchronized
        fun getInstance(): RetrofitClient? {
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