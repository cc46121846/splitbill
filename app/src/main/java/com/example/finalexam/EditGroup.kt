package com.example.finalexam

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class EditGroup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.edit_group)

        val group_id = intent.getIntExtra("group_id", 0)

        val groupNameEditText_edit = findViewById<EditText>(R.id.groupNameEditText_edit)
        val typeEditText_edit = findViewById<EditText>(R.id.typeEditText_edit)
        val ownerEditText_edit = findViewById<EditText>(R.id.ownerEditText_edit)

        val client = OkHttpClient();

        val request: Request = Request.Builder()
            .url("https://bnsw.tech/final/groups.php?group_id=${group_id}") // 替換為你的 API URL
            .header("Authorization", "Bearer " + PreferenceHelper.getToken(this)) // 替換為你的 API 金鑰
            .build()

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
                    val data = root.getJSONObject("data")
                    val group = data.getJSONObject("group")
                    val ownerNickname = group.getString("owner_nickname")
                    val groupName = group.getString("name")
                    val groupType = group.getString("type")

                    if (success) {
                        runOnUiThread{
                            groupNameEditText_edit.setText(groupName)
                            typeEditText_edit.setText(groupType)
                            ownerEditText_edit.setText(ownerNickname)
                        }
                    } else {
                        Log.e("HTTP", "請求失敗: " + root.getString("message"))
                    }
                } else {
                    Log.e("HTTP", "請求失敗: " + response.code)
                }
            }
        })
        val backButton_editgroup = findViewById<ImageButton>(R.id.backButton_editgroup)
        backButton_editgroup.setOnClickListener{
            finish()
        }

        val checkButton_editgroup = findViewById<ImageButton>(R.id.checkButton_editgroup)
        checkButton_editgroup.setOnClickListener {
            val newGroupName = groupNameEditText_edit.text.toString()
            val newGroupType = typeEditText_edit.text.toString()
            val newOwnerNickname = ownerEditText_edit.text.toString()

            if (newGroupName.isEmpty() || newGroupType.isEmpty() || newOwnerNickname.isEmpty()) {
                Toast.makeText(this@EditGroup, "請填寫所有欄位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // php//input 資料
            val jsonBody = JSONObject()
            jsonBody.put("group_id", group_id)
            jsonBody.put("group_name", newGroupName)
            jsonBody.put("group_type", newGroupType)
            jsonBody.put("owner_nickname", newOwnerNickname)
            Log.d("HTTP", "更新資料: $jsonBody")

            // 建立 PATCH 請求
            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val updateRequest: Request = Request.Builder()
                .url("https://bnsw.tech/final/groups.php")
                .header("Authorization", "Bearer " + PreferenceHelper.getToken(this@EditGroup))
                .patch(requestBody) // 使用 PATCH 方法更新資料
                .build()

            client.newCall(updateRequest).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("HTTP", "更新失敗: " + e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@EditGroup, "群組已更新", Toast.LENGTH_SHORT).show()
                            val resultIntent = Intent()
                            resultIntent.putExtra("updatedGroupName", newGroupName)
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }
                    } else {
                        Log.e("HTTP", "更新失敗: " + response.code)
                    }
                }
            })
        }
        
    }
}