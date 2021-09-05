package it.ipzs.cieidsdk.sampleapp

import android.os.Bundle
import android.os.StrictMode
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.microservices.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject


class Microservices : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.microservices)

        val my_json = JSONObject(intent.getStringExtra("json"))
        //val obj:String = intent.getStringExtra("json").toString()


        val x: Iterator<*> = my_json.keys()
        val jsonArray = JSONArray()

        microservice1.visibility = View.GONE;
        microservice2.visibility = View.GONE;
        microservice3.visibility = View.GONE;
        microservice4.visibility = View.GONE;
        microservice5.visibility = View.GONE;
        microservice6.visibility = View.GONE;
        microservice7.visibility = View.GONE;
        microservice8.visibility = View.GONE;


        while (x.hasNext()) {
            val key = x.next() as String
            jsonArray.put(my_json[key])
            print(key + " - " + my_json[key])

            if (key.equals("apertura uffici")){
                println("- show view")
                microservice1.visibility = View.VISIBLE;
            }
            if (key.equals("firma elettronica")){
                println("- show view")
                microservice2.visibility = View.VISIBLE;
            }
            if (key.equals("mensa")){
                println("- show view")
                microservice3.visibility = View.VISIBLE;
            }
            if (key.equals("notifiche")){
                println("- show view")
                microservice4.visibility = View.VISIBLE;
            }
            if (key.equals("parcheggio")){
                println("- show view")
                microservice5.visibility = View.VISIBLE;
            }
            if (key.equals("stampa")){
                println("- show view")
                microservice6.visibility = View.VISIBLE;
            }
            if (key.equals("stampa_sensibile")){
                println("- show view")
                microservice7.visibility = View.VISIBLE;
            }
            if (key.equals("timbrature")){
                println("- show view")
                microservice8.visibility = View.VISIBLE;
            }

        }



        microservice1.setOnClickListener {
            println("click apertura uffici")
            chiamata_microservizio("tokenSession","jwt","apertura uffici","username","location")
            //startActivity(Intent (this, InternalActivity::class.java))
        }
        microservice2.setOnClickListener {
            println("click firma elettronica")
            chiamata_microservizio("tokenSession","jwt","firma elettronica","username","location")
            //startActivity(Intent (this, InternalActivity::class.java))
        }
        microservice2.setOnClickListener {
            println("click mensa")
            chiamata_microservizio("tokenSession","jwt","mensa","username","location")
            //startActivity(Intent (this, InternalActivity::class.java))
        }
        microservice6.setOnClickListener {
            println("click notifiche")
            chiamata_microservizio("tokenSession","jwt","notifiche","username","location")
            //startActivity(Intent (this, InternalActivity::class.java))
        }
        microservice6.setOnClickListener {
            println("click parcheggio")
            chiamata_microservizio("tokenSession","jwt","parcheggio","username","location")
            //startActivity(Intent (this, InternalActivity::class.java))
        }
        microservice7.setOnClickListener {
            println("click stampa")
            chiamata_microservizio("tokenSession","jwt","stampa","username","location")
            //startActivity(Intent (this, InternalActivity::class.java))
        }
        microservice7.setOnClickListener {
            println("click stampa_sensibile")
            chiamata_microservizio("tokenSession","jwt","stampa_sensibile","username","location")
            //startActivity(Intent (this, InternalActivity::class.java))
        }
        microservice8.setOnClickListener {
            println("click timbrature")
            chiamata_microservizio("tokenSession","jwt","timbrature","username","location")
            //startActivity(Intent (this, InternalActivity::class.java))
        }
    }



    fun chiamata_microservizio(tokenSession:String,jwt:String,service_name:String,username:String,location:String)
    {
        val toReturn = JSONObject()
        toReturn.put("tokenSession", tokenSession)
        toReturn.put("jwt", jwt)
        toReturn.put("service_name", service_name)
        toReturn.put("username", username)
        toReturn.put("location", location)

        val body = toReturn.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        //val body = toReturn.toString().toRequestBody("multipart/form-data; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .method("POST", body)
            //.url("http://10.20.207.218:5000/services_list")
            .url("http://"+MainActivity.serverIP+"/forward_request")
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
    }

}

