package com.example.finalexam

import EndAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import kotlin.properties.Delegates

class End : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EndAdapter

    private var groupId by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.end)

        groupId = intent.getIntExtra("group_id", 0)

        val backButtonEnd = findViewById<ImageButton>(R.id.backButton_end)
        backButtonEnd.setOnClickListener{
            finish();
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val dummyData = mutableListOf<EndItem>()
        adapter = EndAdapter(
            dummyData,
            onSettled = { holder, item -> onSettled(holder, item )}
        )
        recyclerView.adapter = adapter

        onRefresh(groupId)
    }

    private fun onSettled(holder: EndAdapter.GroupViewHolder, item: EndItem) {
        val client = OkHttpClient()

        val jsonBody = JSONObject()
        jsonBody.put("group_id", groupId)
        jsonBody.put("member_id", item.fromId)

        val body = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request: Request = Request.Builder()
            .url("https://bnsw.tech/final/settleme.php") // 替換為你的 API URL
            .header("Authorization", "Bearer " + PreferenceHelper.getToken(this)) // 替換為你的 API 金鑰
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("End", "Error settling: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        item.donePaid = true
                        holder.settleButton.visibility = View.INVISIBLE
                        holder.settleText.visibility = View.VISIBLE
                    }
                } else {
                    Log.e("End", "Failed to settle: ${response.message}")
                }
            }
        })
    }

    private fun onRefresh(groupId: Int) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar3)
        progressBar.visibility = ProgressBar.VISIBLE
        val client = OkHttpClient();

        val request: Request = Request.Builder()
            .url("https://bnsw.tech/final/settlement.php?group_id=$groupId") // 替換為你的 API URL
            .header("Authorization", "Bearer " + PreferenceHelper.getToken(this)) // 替換為你的 API 金鑰
            .build()

        val callback: Callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        val jsonResponse = response.body!!.string()
                        val root = JSONObject(jsonResponse)
                        val success = root.getBoolean("success")
                        val data = root.getJSONObject("data")
                        val settlements = data.getJSONArray("settlements")
                        val settledRecords = data.getJSONArray("settled_records")

                        if (success) {
                            runOnUiThread {
                                val endItems = mutableListOf<EndItem>()
                                for (i in 0 until settlements.length()) {
                                    val from = settlements.getJSONObject(i).getJSONObject("from")
                                    val to = settlements.getJSONObject(i).getJSONObject("to")
                                    val amount = settlements.getJSONObject(i).getString("amount")

                                    val fromName = from.getString("nickname")
                                    val toName = to.getString("nickname")

                                    endItems.add(EndItem(fromName, toName, amount, fromId = from.getInt("id")))
                                }
                                for (i in 0 until settledRecords.length()) {
                                    val from = settledRecords.getJSONObject(i).getJSONObject("share_to")
                                    val to = settledRecords.getJSONObject(i).getJSONObject("paid_by")
                                    val amount = settledRecords.getJSONObject(i).getString("amount")

                                    val fromName = from.getString("nickname")
                                    val toName = to.getString("nickname")

                                    endItems.add(EndItem(fromName, toName, amount, donePaid = true, fromId = from.getInt("id")))
                                }
                                adapter.updateList(endItems)
                                recyclerView.adapter = adapter
                                progressBar.visibility = ProgressBar.INVISIBLE
                            }
                        } else {
                            Log.e("SplitBillEnd", "Failed to fetch settlements")
                        }
                    } catch (e: Exception) {
                        Log.e("SplitBillEnd", "Error parsing JSON response", e)
                    }
                }
            }
        }

        client.newCall(request).enqueue(callback)
    }
}