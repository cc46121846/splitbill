package com.example.finalexam

import android.app.Activity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import kotlin.properties.Delegates

class Group : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    private lateinit var progressBar: ProgressBar
    private var groupId by Delegates.notNull<Int>()

    private lateinit var editGroupActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onRestart() {
        super.onRestart()
        onRefresh(groupId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.group)

        progressBar = findViewById<ProgressBar>(R.id.progressBar2)

        groupId = intent.getIntExtra("group_id",0)
        val groupName = intent.getStringExtra("GroupName")
        val groupText = findViewById<TextView>(R.id.groupText)

        val backButton_group = findViewById<ImageButton>(R.id.backButton_group)
        backButton_group.setOnClickListener{
            finish();
        }

        editGroupActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val updatedGroupName = data.getStringExtra("updatedGroupName")
                    if (updatedGroupName != null) {
                        groupText.text = updatedGroupName
                        Toast.makeText(this, "群組名稱已更新", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "更新失敗", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val editButton_group = findViewById<ImageButton>(R.id.editButton_group)
        editButton_group.setOnClickListener{
            val intent = Intent(this,EditGroup::class.java)
            intent.putExtra("group_id", groupId)
            editGroupActivityResultLauncher.launch(intent)
        }

        val addButton_group = findViewById<Button>(R.id.addButton_group)
        addButton_group.setOnClickListener{
            val intent = Intent(this,AddAmount::class.java)
            intent.putExtra("group_id", groupId)
            intent.putExtra("group_name", groupName)
            startActivity(intent)
        }

        val endButton = findViewById<Button>(R.id.endButton)
        endButton.setOnClickListener{
            val intent = Intent(this,End::class.java)
            intent.putExtra("group_id", groupId)
            intent.putExtra("group_name", groupName)
            startActivity(intent)
        }


        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val dummyData = mutableListOf<ItemItem>()

        adapter = ItemAdapter(
            dummyData,
            onDelete = { item -> onDelete(item) },
            onEdit = {item -> onEdit(item,groupId,groupName)  }
        )
        recyclerView.adapter = adapter

        groupText.text = groupName

        onRefresh(groupId)
    }

    private fun onRefresh(groupId: Int) {
        progressBar.visibility = View.VISIBLE;
        adapter.updateList(emptyList())

        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url("https://bnsw.tech/final/expenses.php?group_id=$groupId") // 替換為你的 API URL
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
                    val expenses = data.getJSONArray("expenses")

                    if (success) {
                        runOnUiThread{
                            val itemItems = mutableListOf<ItemItem>()
                            for (i in 0 until expenses.length()) {
                                val expense = expenses.getJSONObject(i)
                                val paidBy = expense.getJSONObject("paid_by")
                                val nickname = paidBy.getString("nickname")
                                val itemItem = ItemItem(
                                    id = expense.getInt("id"),
                                    date = expense.getString("expense_date"),
                                    billName = expense.getString("bill_name"),
                                    whoPaid = nickname,
                                    youOwe = if (expense.optDouble(
                                            "paid",
                                            0.0
                                        ) > 0.0
                                    ) "0" else expense.optString("owed", "0"),
                                    youPaid = expense.getString("paid"),
                                    amount = expense.getString("amount"),
                                    remark = expense.getString("note")
                                )
                                itemItems.add(itemItem)
                            }
                            progressBar.visibility = View.INVISIBLE
                            adapter.updateList(itemItems)
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

    private fun onEdit(item: ItemItem, Group_id: Int, Group_name: String?) {
        val intent = Intent(this, EditAmount::class.java)
        intent.putExtra("Group_id",Group_id)
        intent.putExtra("Group_name",Group_name)
        intent.putExtra("item_id",item.id)
        startActivity(intent)
    }

    private fun onDelete(item: ItemItem) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar2)
        progressBar.visibility = View.VISIBLE;

        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url("https://bnsw.tech/final/expenses.php?expense_id=${item.id}") // 替換為你的 API URL
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
                            adapter.updateList(adapter.itemList.filter { it != item })
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
    }
}