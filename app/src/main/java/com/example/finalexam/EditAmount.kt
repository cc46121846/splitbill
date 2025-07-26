package com.example.finalexam

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class EditAmount : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.edit_amount)

        val groupId = intent.getIntExtra("Group_id", 0)
        val groupName = intent.getStringExtra("Group_name") ?: "群組名稱"
        val itemId = intent.getIntExtra("item_id", 0)
        val token = PreferenceHelper.getToken(this)
        val client = OkHttpClient();
        val memberRequest: Request = Request.Builder()
            .url("https://bnsw.tech/final/group_members.php?group_id=$groupId")
            .header("Authorization", "Bearer $token")
            .build()

        val itemRequest: Request = Request.Builder()
            .url("https://bnsw.tech/final/expenses.php?group_id=$groupId&expense_id=$itemId")
            .header("Authorization", "Bearer $token")
            .build()

        val item = mutableListOf<GroupMember>()

        val backButton_editamount = findViewById<ImageButton>(R.id.backButton_editamount)
        backButton_editamount.setOnClickListener{
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.datePicker_editamount)) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                systemBars.bottom)
            insets
        }
        val dateButton_editamount = findViewById<Button>(R.id.dateButton_editamount)
        val dateText_editamount = findViewById<TextView>(R.id.dateText_editamount)
        // 取得今天的日期作為預設
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) // 注意：從 0 開始（0=1月）
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        dateButton_editamount.setOnClickListener {
            Log.e("EditAmount", "點擊了日期選擇按鈕")

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // 使用者按下確定後回傳的年月日
                    val dateString = "$selectedYear 年 ${selectedMonth + 1}月 $selectedDay 日"
                    dateText_editamount.text = dateString
                },
                year, month, day
            )
            // 可選擇是否設最小/最大日期（這裡不設）
            // datePickerDialog.datePicker.minDate = calendar.timeInMillis
            datePickerDialog.show()
            dateButton_editamount.text = "更改日期"
        }

        val payButton_addamount = findViewById<Button>(R.id.payButton_editamount)
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
        val needpayButton_editamount = findViewById<Button>(R.id.needpayButton_editamount)
        needpayButton_editamount.text = "更改名單"
        needpayButton_editamount.isEnabled = false
        needpayButton_editamount.setOnClickListener {
            // 初始選取狀態（全部預設沒選）
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
                }
                .setNegativeButton("取消", null)
                .show()
        }

        val itemNameEditText_edit = findViewById<TextView>(R.id.itemNameEditText_edit)
        val amountEditText_edit = findViewById<TextView>(R.id.amountEditText_edit)
        val remarkText_edit = findViewById<TextView>(R.id.remarkText_edit)

        val itemRequestCallback: Callback = object : Callback {
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
                        runOnUiThread {
                            val data = root.getJSONObject("data")
                            val expense = data.getJSONObject("expense")

                            val billName = expense.getString("bill_name")
                            val paidBy = expense.getInt("paid_by")
                            val amount = expense.getDouble("amount")
                            val shares = expense.getJSONArray("shares")
                            val date = expense.getString("expense_date")
                            val note = expense.getString("note")

                            // 轉換 2021-01-01 格式為 2021 年 1 月 1 日
                            val formattedDate = date.replace("-", " 年 ").replace("-", " 月 ") + " 日"
                            dateText_editamount.text = formattedDate

                            itemNameEditText_edit.text = billName
                            amountEditText_edit.text = amount.toString()

                            val paidItem = item.find { it.id == paidBy }
                            if (paidItem != null) {
                                payButton_addamount.text = paidItem.nickname
                            } else {
                                payButton_addamount.text = "選擇支付者"
                            }

                            for (i in 0 until shares.length()) {
                                val memberId = shares.getInt(i)
                                val member = item.find { it.id == memberId }
                                if (member != null) {
                                    selectedItems.add(member)
                                }
                            }

                            if (selectedItems.isNotEmpty()) {
                                needpayButton_editamount.text = "更改成員"
                            } else {
                                needpayButton_editamount.text = "選擇成員"
                            }

                            remarkText_edit.text = note
                        }
                    } else {
                        Log.e("HTTP", "請求失敗: " + root.getString("message"))
                    }
                } else {
                    Log.e("HTTP", "請求失敗: " + response.code)
                }
            }
        }

        client.newCall(memberRequest).enqueue(object : Callback {
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
                        runOnUiThread {
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
                            Toast.makeText(this@EditAmount, "取得成員資料成功", Toast.LENGTH_SHORT).show()
                            needpayButton_editamount.isEnabled = true
                            payButton_addamount.isEnabled = true
                            client.newCall(itemRequest).enqueue(itemRequestCallback)
                        }
                    } else {
                        Log.e("HTTP", "請求失敗: " + root.getString("message"))
                    }
                } else {
                    Log.e("HTTP", "請求失敗: " + response.code)
                }
            }
        })

        val checkButton_editamount = findViewById<ImageButton>(R.id.checkButton_editamount)
        checkButton_editamount.setOnClickListener{
            val dateString = dateText_editamount.text.toString()
            val dateParts = dateString.split("年", "月", "日").map { it.trim() }
            val formattedDate = "${dateParts[0]}-${dateParts[1].padStart(2, '0')}-${dateParts[2].padStart(2, '0')}"
            val paidBy = item.find { it.nickname == payButton_addamount.text.toString() }?.id ?: -1
            val amount = amountEditText_edit.text.toString().toDoubleOrNull() ?: 0.0
            val remark = remarkText_edit.text.toString()
            val billName = itemNameEditText_edit.text.toString()

            val needPayMembers: JSONArray = JSONArray()
            for (member in selectedItems) {
                needPayMembers.put(member.id)
            }

            val jsonBody = JSONObject()
            jsonBody.put("expense_id", itemId)
            jsonBody.put("expense_date", formattedDate)
            jsonBody.put("paid_by", paidBy)
            jsonBody.put("amount", amount)
            jsonBody.put("bill_name", billName)
            jsonBody.put("note", remark)
            jsonBody.put("expense_shares", needPayMembers)
            val postBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())


            val patchRequest: Request = Request.Builder()
                .url("https://bnsw.tech/final/expenses.php")
                .header("Authorization", "Bearer $token")
                .put(postBody)
                .build()

            client.newCall(patchRequest).enqueue(object : Callback {
                override fun onFailure(call: Call, e: java.io.IOException) {
                    Log.e("HTTP", "錯誤: " + e.message)
                }

                @Throws(java.io.IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@EditAmount, "修改成功", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        Log.e("HTTP", "請求失敗: " + response.code)
                    }
                }
            })
        }
    }
}