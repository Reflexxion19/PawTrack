package com.example.pawtrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
class ChangeEmailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_email_layout)
        val username = intent.getStringExtra("USERNAME")
        val newEmailEditText = findViewById<EditText>(R.id.editText2)
        val buttonChangePassword = findViewById<Button>(R.id.signinbutton)

        buttonChangePassword.setOnClickListener {
            val newEmail = newEmailEditText.text.toString()

            if (newEmail.isNotEmpty()) {
                val jsonMediaType = "application/json; charset=utf-8".toMediaType()
                val json = """
                {
                    "type": "c_e",
                    "u_n": "$username",
                    "e": "$newEmail"
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
                                val intent = Intent(applicationContext, UserSettingsActivity::class.java)
                                intent.putExtra("USERNAME", username)
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
            intent.putExtra("USERNAME", username)
            startActivity(intent)
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }
}