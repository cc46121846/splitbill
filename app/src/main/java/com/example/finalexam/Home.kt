package com.example.finalexam

import GroupAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.properties.Delegates

class Home : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroupAdapter
    private lateinit var progressBar: ProgressBar

    override fun onRestart() {
        super.onRestart()
        onRefresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home)

        val addButton_home = findViewById<ImageButton>(R.id.addButton_home)
        addButton_home.setOnClickListener{
            startActivity(Intent(this,AddGroup::class.java))
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val dummyData = mutableListOf<GroupItem>()
        adapter = GroupAdapter(
            dummyData,
            onDelete = { group -> onDelete(group) } // 加這行
        )
        recyclerView.adapter = adapter

        onRefresh()
    }

    private fun onRefresh() {
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        adapter.updateList(emptyList())

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(" https://bnsw.tech/final/mygroups.php")
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
                    val data = root.getJSONObject("data")
                    val groups = data.getJSONArray("groups")

                    if (success) {
                        runOnUiThread{
                            val groupList = mutableListOf<GroupItem>()
                            for (i in 0 until groups.length()) {
                                val group = groups.getJSONObject(i)
                                val name = group.getString("name")
                                val youOwe = group.getString("you_owe")
                                val type = group.getString("type")
                                val groupId = group.getInt("id")

                                groupList.add(
                                    GroupItem(groupId, name, youOwe,type)
                                )
                            }
                            adapter.updateList(groupList)
                        }
                        progressBar.visibility = View.INVISIBLE
                    } else {
                        Log.e("HTTP", "請求失敗: " + root.getString("message"))
                    }
                } else {
                    Log.e("HTTP", "請求失敗: " + response.code)
                }
            }
        })
    }

    private fun onDelete(group: Any) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        if (group is GroupItem) {
            progressBar.visibility = View.VISIBLE
            Log.d("GroupAdapter", "刪除群組: ${group.groupText_item}")
            val client = OkHttpClient()

            val groupId = group.group_id;

            val request: Request = Request.Builder()
                .url("https://bnsw.tech/final/groups.php?id=$groupId") // 替換為你的 API URL
                .header("Authorization", "Bearer " + PreferenceHelper.getToken(this)) // 替換為你的 API 金鑰
                .delete()
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
                                adapter.updateList(adapter.itemList.filter { it != group })
                            }
                        } else {
                            Log.e("HTTP", "請求失敗: " + root.getString("message"))
                        }
                        progressBar.visibility = View.INVISIBLE
                    } else {
                        Log.e("HTTP", "請求失敗: " + response.code)
                    }
                }
            })
        } else {
            Log.e("GroupAdapter", "無效的群組類型")
        }
    }
}