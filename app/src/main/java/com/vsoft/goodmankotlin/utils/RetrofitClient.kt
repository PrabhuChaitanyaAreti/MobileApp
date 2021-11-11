package com.vsoft.goodmankotlin.utils

import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient {
    companion object {
        private lateinit var context: Context

        fun setContext(con: Context) {
            context=con
        }
    private var instance: RetrofitClient? = null
    private var myApi: RetrofitApiInterface? = null
   // private var myApi1: RetrofitApiInterface? = null
   private var BASE_URL:String = "http://111.93.3.148:12808"
    //private var BASE_URL1:String = "http://111.93.3.148:13808"
       //  http://3.218.249.156:12808
        //private var BASE_URL:String="http://3.218.249.156:12808"

            //private var BASE_URL:String="http://192.168.10.116:12808"
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
       /* val retrofit1: Retrofit = Retrofit.Builder().baseUrl(BASE_URL1)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        myApi1 = retrofit1.create(RetrofitApiInterface::class.java)*/
    }

        @Synchronized
        fun getInstance(context: Context): RetrofitClient? {
//            val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
//            BASE_URL= prefs.getString("EdgeServerIp", "http://192.168.10.116:12808")!!
            if (instance == null) {
                instance = RetrofitClient()
            }
            return instance
        }
    }

    fun getMyApi(): RetrofitApiInterface? {
        return myApi
    }
    /*fun getMyApi1(): RetrofitApiInterface? {
        return myApi1
    }*/
}