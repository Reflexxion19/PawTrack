package com.example.pawtrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vishnusivadas.advanced_httpurlconnection.PutData
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.codec.digest.DigestUtils
import java.io.IOException

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        val usernameEditText = findViewById<EditText>(R.id.editTextText)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val buttonSignIn = findViewById<Button>(R.id.signinbutton)


        val forgotPasswordTextView = findViewById<TextView>(R.id.textView2)
        forgotPasswordTextView.setOnClickListener(){
            val intent = Intent(applicationContext, RemindPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
        val registerTextView = findViewById<TextView>(R.id.textView4)
        registerTextView.setOnClickListener(){
            val intent = Intent(applicationContext, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonSignIn.setOnClickListener{
            if(!usernameEditText.text.equals("") || !passwordEditText.equals(""))
            {
                val username = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()
                val hashedPassword = DigestUtils.sha256Hex(password)
                val jsonMediaType = "application/json; charset=utf-8".toMediaType()
                val json = """
                {
                    "type": "l_i",
                    "u": "$username",
                    "p": "$hashedPassword"
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
                        if (response.body.toString() == "Login successful") {
                            val responseString = response.body?.string()
                            val intent = Intent(applicationContext, HomePageActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else {
                            val intent = Intent(applicationContext, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                })
            }
            else
            {
                Toast.makeText(applicationContext,"All fields are required.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}