package com.example.finalexam


import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.finalexam.type.GroupMember
import com.example.finalexam.PreferenceHelper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class AddAmount : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_amount)

        val groupId = intent.getIntExtra("group_id",0)
        val groupName = intent.getStringExtra("group_name") ?: "群組名稱"
        val token = PreferenceHelper.getToken(this) ?:""
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://bnsw.tech/final/group_members.php?group_id=$groupId")
            .header("Authorization", "Bearer $token")
            .build()

        val item = mutableListOf<GroupMember>()

        val backButton_addamount = findViewById<ImageButton>(R.id.backButton_addamount)
        backButton_addamount.setOnClickListener{
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dateText_addamount)) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                systemBars.bottom)
            insets
        }
        val dateButton_addamount = findViewById<Button>(R.id.dateButton_addamount)
        val dateText_addamount = findViewById<TextView>(R.id.dateText_addamount)
        // 取得今天的日期作為預設
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) // 注意：從 0 開始（0=1月）
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        dateButton_addamount.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // 使用者按下確定後回傳的年月日
                    val dateString = "$selectedYear 年 ${selectedMonth + 1}月 $selectedDay 日"
                    dateText_addamount.text = "$dateString"
                },
                year, month, day
            )
            // 可選擇是否設最小/最大日期（這裡不設）
            // datePickerDialog.datePicker.minDate = calendar.timeInMillis
            datePickerDialog.show()
            dateButton_addamount.text = "更改日期"
        }

        val payButton_addamount = findViewById<Button>(R.id.payButton_addamount)
        payButton_addamount.isEnabled = false
        payButton_addamount.setOnClickListener {
            // 建立 AlertDialog 物件
            AlertDialog.Builder(this)
                .setTitle("成員名單")
                .setItems(item.map { it.nickname }.toTypedArray()) { _, i ->
                    payButton_addamount.text = item[i].nickname
                }
                .show()
        }



        val selectedItems = mutableListOf<GroupMember>() // 儲存被選中的 index
        val needpayButton_addamount = findViewById<Button>(R.id.needpayButton_addamount)
        needpayButton_addamount.isEnabled = false

        needpayButton_addamount.setOnClickListener {
            // 初始選取狀態 selectedItems 選擇所有項目
            val checkedItems = BooleanArray(item.size) { index ->
                selectedItems.any { it.id == item[index].id }
            }

            AlertDialog.Builder(this)
                .setTitle("成員名單")
                .setMultiChoiceItems(item.map { it.nickname }.toTypedArray(), checkedItems) { _, which, isChecked ->
                    if (isChecked) {
                        selectedItems.add(item[which])
                    } else {
                        selectedItems.remove(item[which])
                    }
                }
                .setPositiveButton("確認") { _, _ ->
                    val selectedNames = selectedItems.map { it.nickname }
                    Toast.makeText(this, "一共有：${selectedNames.joinToString(", ")}", Toast.LENGTH_LONG).show()
                    needpayButton_addamount.text = "更改名單"
                }
                .setNegativeButton("取消", null)
                .show()
        }

        val checkButtonAddAmount = findViewById<ImageButton>(R.id.checkButton_addamount)
        checkButtonAddAmount.setOnClickListener {
            val dateString = dateText_addamount.text.toString()
            val dateParts = dateString.split("年", "月", "日").map { it.trim() }
            val formattedDate = "${dateParts[0]}-${dateParts[1].padStart(2, '0')}-${dateParts[2].padStart(2, '0')}"
            val paidBy = item.find { it.nickname == payButton_addamount.text.toString() }?.id ?: -1
            val amount = findViewById<TextView>(R.id.amountEditText).text.toString().toDoubleOrNull() ?: 0.0
            val remark = findViewById<TextView>(R.id.remarkText).text.toString()
            val billName = findViewById<TextView>(R.id.itemNameEditText).text.toString()

            val needPayMembers: JSONArray = JSONArray()
            for (member in selectedItems) {
                needPayMembers.put(member.id)
            }

            // json格式
            val jsonBody = JSONObject().apply {
                put("group_id", groupId)
                put("expense_date", formattedDate)
                put("paid_by", paidBy)
                put("amount", amount)
                put("note", remark)
                put("bill_name", billName)
                put("expense_shares", needPayMembers)
            }.toString()

            val postBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val postRequest: Request = Request.Builder()
                .url("https://bnsw.tech/final/expenses.php")
                .post(postBody)
                .header("Authorization", "Bearer $token")
                .build()

            Log.d("HTTP", "請求: " + postRequest.url)

            client.newCall(postRequest).enqueue(object : Callback {
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
                            runOnUiThread {
                                Toast.makeText(this@AddAmount, "新增成功", Toast.LENGTH_SHORT).show()
                                finish()
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

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                Log.e("HTTP", "錯誤: " + e.message)
            }

            @Throws(java.io.IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body!!.string()
                    val root = JSONObject(jsonResponse)
                    val success = root.getBoolean("success")

                    if (success) {
                        runOnUiThread{
                            val data = root.getJSONObject("data")
                            val members = data.getJSONArray("members")
                            for (i in 0 until members.length()) {
                                val member = members.getJSONObject(i)
                                item.add(GroupMember(
                                    id = member.getInt("id"),
                                    nickname = member.getString("nickname"),
                                    group_id = member.getInt("group_id"),
                                    user_id = if (member.isNull("user_id")) null else member.getInt("user_id")
                                ))
                            }
                            Toast.makeText(this@AddAmount, "取得成員資料成功", Toast.LENGTH_SHORT).show()
                            needpayButton_addamount.isEnabled = true
                            payButton_addamount.isEnabled = true
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