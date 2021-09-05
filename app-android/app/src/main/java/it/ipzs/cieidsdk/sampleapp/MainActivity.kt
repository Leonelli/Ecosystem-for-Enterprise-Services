package it.ipzs.cieidsdk.sampleapp

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatActivity
import it.ipzs.cieidsdk.sampleapp.internal.InternalActivity
import it.ipzs.cieidsdk.sampleapp.redirection.RedirectionActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MainActivity : AppCompatActivity(){
    companion object {
        lateinit var serverIP:String
    }
    init {
        serverIP= "10.20.207.218:5000"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        button_internal.setOnClickListener {

            startActivity(Intent (this, InternalActivity::class.java))
        }

        button_redirection.setOnClickListener {

            startActivity(Intent (this, RedirectionActivity::class.java))
        }

        microservices_access.setOnClickListener {
            System.out.println("access microservices activity")


            val toReturn = JSONObject()
            toReturn.put("user_token", "aaa")
            toReturn.put("tokenSession", "aaa")
            toReturn.put("jwt", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJBQjEyM0NEIiwiTWFyY2EiOiJGaWF0IiwiTW9kZWxsbyI6IjUwMCIsImlzcyI6Ik1STE1SVDg4UzIzTDg0NVJcLzAwMDAwMDAwMDAwMSIsImlhdCI6MTYyMzY3NjkxNzIzMCwiZXhwIjoxNjIzNjc3MDAzNjMwfQ.d8f9YWvAJ1YeoYhxoMP5idf7A437yazjJFnN6Zs7N81b4OHjLmxO81W3Ibtn7QE5oc47bPAL12exnSI5W7uM7KxuDfE2sg2bvTHY336OTVfeXIXe8EVXq8cUfdFelEFCuDyvcXxxPe3E2dKIJ1ZLaW80tsN-4rtPbJSjrRzePDdfhs-lXbWdUE6v15YhDfoZ5a-Xi4zuucqrI2VAI5x6b0feb6OQIWKPF3GbW1Y_7RJGmP2NC_j9i4gAhLDNuU1rxaQYXp74bKSibdVWT4pKpzuHYnL02KstIo2cIDZtY_k4faHQ3H32eidYi7cTI0rLGCrrRVTTbRkp6uG_VKPjEQ")
            toReturn.put("service_name", "timbrature")
            toReturn.put("username", "aaa")
            toReturn.put("location", "aaa")

            val body = toReturn.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            //val body = toReturn.toString().toRequestBody("multipart/form-data; charset=utf-8".toMediaTypeOrNull())

            val request = Request.Builder()
                .method("POST", body)
                //.url("http://10.20.207.218:5000/services_list")
                .url("http://"+serverIP+"/services_list_user")
                .build()

            val policy = StrictMode.ThreadPolicy.Builder()
                .permitAll().build()
            StrictMode.setThreadPolicy(policy)

            val okHttpClient = OkHttpClient.Builder().build()
            println(request.toString())
            println(request.body)
            println(request.headers)
            val jsonData: String
            okHttpClient.newCall(request).execute().use { response ->
                println("isSuccess:"+response.isSuccessful)
                println("code:"+response.code)
                println("body:"+response.body)
                jsonData = response.body!!.string()
                println("jsonData:"+jsonData)
            }

            val intent = Intent(this, Microservices::class.java)
            intent.putExtra("json", jsonData.toString());
            startActivity(intent)
        }
    }



}