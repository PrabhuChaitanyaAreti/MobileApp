package com.vsoft.goodmankotlin.utils

import com.vsoft.goodmankotlin.model.PunchResponse
import com.vsoft.goodmankotlin.model.UserAuthRequest
import com.vsoft.goodmankotlin.model.UserAuthResponse
import okhttp3.MultipartBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface RetrofitApiInterface {


    /**
     * The return type is important here
     * The class structure that you've defined in Call<T>
     * should exactly match with your json response
     * If you are not using another api, and using the same as mine
     * then no need to worry, but if you have your own API, make sure
     * you change the return type appropriately
    </T> */
    @GET("/placeholder/api/predictions")
    fun getPunchData(): Call<PunchResponse?>?

    @Multipart
    @POST("/placeholder/api/upload")
    fun uploadDyeImage(@Part filePart: MultipartBody.Part?): Call<PunchResponse?>?
    @Multipart
    @POST("/postVideo")
    fun uploadDyeVideo(@Part file: MultipartBody.Part?): Call<PunchResponse?>?

    @POST("/login")
    fun authenticate(@Body userAuthRequest: UserAuthRequest): Call<UserAuthResponse?>?
}