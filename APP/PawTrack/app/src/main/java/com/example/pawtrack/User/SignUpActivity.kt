package com.example.pawtrack.User

import android.content.Intent
import android.os.Bundle

import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pawtrack.R
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.codec.digest.DigestUtils
import java.io.IOException


class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_layout)

        val usernameEditText = findViewById<EditText>(R.id.editTextText)
        val emailEditText = findViewById<EditText>(R.id.editTextText2)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val buttonSignUp = findViewById<Button>(R.id.signupbutton)

        buttonSignUp.setOnClickListener {
            if (!usernameEditText.text.equals("") || !emailEditText.text.equals("") || !passwordEditText.equals(
                    ""
                )
            ) {
                val username = usernameEditText.text.toString()
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                val hashedPassword = DigestUtils.sha256Hex(password)
                val jsonMediaType = "application/json; charset=utf-8".toMediaType()
                val json = """
                {
                    "type": "u_r",
                    "u": "$username",
                    "p": "$hashedPassword",
                    "e": "$email",
                    "p_p": null,
                    "s": 0,
                    "p_e": "2028-03-30 00:01:00"
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
                                val intent = Intent(applicationContext, LoginActivity::class.java)
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
        val returnButton = findViewById<Button>(R.id.button2)
        returnButton.setOnClickListener {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}