package com.example.finalexam

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url("https://bnsw.tech/final/verify.php") // 替換為你的 API URL
            .header("Authorization", "Bearer " + PreferenceHelper.getToken(this)) // 替換為你的 API 金鑰
            .build()

        Log.d("HTTP", "發送請求到 https://bnsw.tech/final/")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HTTP", "錯誤: " + e.message)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body!!.string()
                    val root = JSONObject(jsonResponse)
                    val success = root.getBoolean("success")

                    if (success) {
                        runOnUiThread{
                            startActivity(Intent(this@MainActivity,Home::class.java))
                        }
                    } else {
                        PreferenceHelper.saveToken(this@MainActivity, "")
                        Log.e("HTTP", "請求失敗: " + root.getString("message"))
                    }
                } else {
                    Log.e("HTTP", "請求失敗: " + response.code)
                }
            }
        })

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        val SigninButton = findViewById<Button>(R.id.SigninButton)
        SigninButton.setOnClickListener{

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            val formBody: FormBody = FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build()

            val client = OkHttpClient()
            val signinRequest: Request = Request.Builder()
                .url(" https://bnsw.tech/final/login.php")
                .post(formBody)
                .build()
            client.newCall(signinRequest).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("HTTP", "錯誤: " + e.message)
                }
                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val jsonResponse = response.body!!.string()
                        val root = JSONObject(jsonResponse)
                        val success = root.getBoolean("success")
                        val data = root.getJSONObject("data")
                        val token = data.getString("token")

                        if (success) {
                            Log.d("HTTP", "請求成功: $token")
                            // 儲存 token 到 SharedPreferences
                            runOnUiThread{
                                PreferenceHelper.saveToken(this@MainActivity, token)
                                startActivity(Intent(this@MainActivity,Home::class.java))
                            }
                        } else {
                            Log.e("HTTP", "請求失敗: " + root.getString("message"))
                        }
                    } else {
                        Log.e("HTTP", "請求失敗: " + response.code)
                    }
                }
            })
        }

        val signupButton = findViewById<Button>(R.id.signupButton)
        signupButton.setOnClickListener{

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            val formBody: FormBody = FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build()

            val client = OkHttpClient()
            val signupRequest: Request = Request.Builder()
                .url(" https://bnsw.tech/final/register.php")
                .post(formBody)
                .build()
            client.newCall(signupRequest).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("HTTP", "錯誤: " + e.message)
                }
                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val jsonResponse = response.body!!.string()
                        val root = JSONObject(jsonResponse)
                        val success = root.getBoolean("success")
                        val data = root.getJSONObject("data")
                        val token = data.getString("token")

                        if (success) {
                            Log.d("HTTP", "請求成功: $token")
                            // 儲存 token 到 SharedPreferences
                            runOnUiThread{
                                PreferenceHelper.saveToken(this@MainActivity, token)
                                startActivity(Intent(this@MainActivity,Home::class.java))
                            }
                        } else {
                            Log.e("HTTP", "請求失敗: " + root.getString("message"))
                        }
                    } else {
                        Log.e("HTTP", "請求失敗: " + response.code)
                    }
                }
            })
        }
    }
}