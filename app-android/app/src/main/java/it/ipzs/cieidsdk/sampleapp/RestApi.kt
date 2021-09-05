package it.ipzs.cieidsdk.sampleapp

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface RestApi {
    //@Headers("Content-Type: application/json")
    /*@Headers("Content-Type: multipart/form-data")
    @POST("services_list_use")
    fun addUser(@Body userData: UserInfo): Call<UserInfo>

    @Multipart
    @POST("services_list_user")
    //Call<User> updateUser(@Part("photo") RequestBody token, @Part("description") RequestBody description);
    */
    @Multipart
    @POST("services_list_user")
    suspend fun uploadEmployeeData(@PartMap map: HashMap<String?, RequestBody?>): Response<ResponseBody>



}