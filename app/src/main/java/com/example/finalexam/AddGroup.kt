package com.example.finalexam

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class AddGroup : AppCompatActivity() {
    private lateinit var addItemButton: ImageButton
    private lateinit var itemContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.add_group)



        val backButton_addgroup = findViewById<ImageButton>(R.id.backButton_addgroup)
        backButton_addgroup.setOnClickListener {
            finish()
        }


        addItemButton = findViewById(R.id.addItemButton)
        itemContainer = findViewById(R.id.itemContainer)


        addItemButton.setOnClickListener {
            showInputDialog()
        }

        val checkButton_addgroup = findViewById<ImageButton>(R.id.checkButton_addgroup)
        checkButton_addgroup.setOnClickListener {

            val groupNameEditText = findViewById<EditText>(R.id.groupNameEditText)
            val typeEditText = findViewById<EditText>(R.id.typeEditText)
            val ownerEditText = findViewById<EditText>(R.id.ownerEditText)

            itemContainer = findViewById(R.id.itemContainer)

            val groupName = groupNameEditText.text.toString()
            val type = typeEditText.text.toString()
            val owner = ownerEditText.text.toString()
            val client = OkHttpClient()

            val textList = mutableListOf<String>()

            for (i in 0 until itemContainer.childCount) {
                val childView = itemContainer.getChildAt(i)
                val textView = childView.findViewById<TextView>(R.id.textItem)
                textView?.let {
                    textList.add(it.text.toString())
                }
            }
            val textListJSONArray = JSONArray(textList)
            val postFormBody = okhttp3.FormBody.Builder()
                .add("group_name", groupName)
                .add("group_type", type)
                .add("owner_nickname", owner)
                .add("nicknames", textListJSONArray.toString())
                .build()

            val request: Request = Request.Builder()
                .url("https://bnsw.tech/final/groups.php")
                .header("Authorization", "Bearer " + PreferenceHelper.getToken(this)) // 替換為你的 API 金鑰
                .post(postFormBody)
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
    }
    private fun showInputDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_single_input, null)
        val inputField = dialogView.findViewById<EditText>(R.id.inputField)

        val dialog = AlertDialog.Builder(this)
            .setTitle("輸入群組成員")
            .setView(dialogView)
            .setPositiveButton("確認", null)  // 先設 null，待會自訂點擊邏輯
            .setNegativeButton("取消", null)
            .create()

        dialog.setOnShowListener {
            val confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            confirmButton.isEnabled = false

            // 動態判斷是否有輸入文字
            inputField.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    confirmButton.isEnabled = !s.isNullOrBlank()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
            // 設定確認按鈕行為
            confirmButton.setOnClickListener {
                val inputText = inputField.text.toString().trim()
                if (inputText.isNotEmpty()) {
                    addItemToScreen(inputText)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun addItemToScreen(text: String) {
        // 載入 item_input.xml 為一個 View
        val itemView = layoutInflater.inflate(R.layout.item_input, itemContainer, false)
        // 取得這個 View 裡的元件
        val textView = itemView.findViewById<TextView>(R.id.textItem)
        val deleteButton = itemView.findViewById<ImageButton>(R.id.deleteButton)
        textView.text = text
        // 點擊 X 移除這整個項目
        deleteButton.setOnClickListener {
            itemContainer.removeView(itemView)
        }
        // 加進容器
        itemContainer.addView(itemView)
    }
}

