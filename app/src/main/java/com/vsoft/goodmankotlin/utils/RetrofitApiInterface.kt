package com.vsoft.goodmankotlin.utils

import com.vsoft.goodmankotlin.model.*
import com.vsoft.goodmankotlin.video_response.VideoAnnotationResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitApiInterface {


    @GET("/operatorlist")
    fun getOperatorsList(): Call<OperatorList?>?
    @GET("/getDieId")
    fun doGetListDieDetails(): Call<DieIdDetailsModel?>?
    @Multipart
    @POST("/placeholder/api/upload")
    fun uploadDyeImage(@Part filePart: MultipartBody.Part?): Call<PunchResponse?>?
    @Multipart
    @POST("/placeholder/api/video")
    fun uploadDyeVideo(@Part file: MultipartBody.Part?): Call<VideoAnnotationResponse?>?

    @Headers("Connection:close")
    @Multipart
    @POST("/uploadFile")
    fun saveVideo(@Part meta_data:MultipartBody.Part?, @Part file: MultipartBody.Part?): Call<VideoUploadSaveResponse?>?

    @POST("/login")
    fun authenticate(@Body userAuthRequest: UserAuthRequest): Call<UserAuthResponse?>?
}