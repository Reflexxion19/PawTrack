package com.example.pawtrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.apache.commons.codec.digest.DigestUtils
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
class ChangePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_password_layout)

        val currentPasswordEditText = findViewById<EditText>(R.id.editText2)
        val newPasswordEditText = findViewById<EditText>(R.id.editText3)
        val buttonChangePassword = findViewById<Button>(R.id.signinbutton)

        buttonChangePassword.setOnClickListener {
            val currentPassword = currentPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()

            if (currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                val currentHashedPassword = DigestUtils.sha256Hex(currentPassword)
                val newHashedPassword = DigestUtils.sha256Hex(newPassword)
                val username = intent.getStringExtra("USERNAME")
                val jsonMediaType = "application/json; charset=utf-8".toMediaType()
                val json = """
                {
                    "type": "c_p",
                    "u_n": "$username",
                    "cr_p": "$currentHashedPassword",
                    "n_p": "$newHashedPassword"
                }
                """.trimIndent()
                val body = json.toRequestBody(jsonMediaType)

                val request = Request.Builder()
                    .url("https://pvp.seriouss.am")
                    .post(body)
                    .build()

                val client = OkHttpClient()
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        e.printStackTrace()
                        showToast("Failed to change password. Please try again.")
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        runOnUiThread {
                            if (response.isSuccessful) {
                                val responseString = response.body?.string()
                                Toast.makeText(
                                    applicationContext,
                                    responseString,
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(applicationContext, ChangePasswordActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                print(response.body?.string())
                                Toast.makeText(
                                    applicationContext,
                                    response.body.toString(),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                })
            } else {
                Toast.makeText(applicationContext, "All fields are required.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val returnButton = findViewById<Button>(R.id.button)
        returnButton.setOnClickListener {
            val intent = Intent(applicationContext, UserSettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }
}
