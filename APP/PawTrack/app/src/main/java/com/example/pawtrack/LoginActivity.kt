package com.example.pawtrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.codec.digest.DigestUtils
import java.io.IOException
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        val usernameEditText = findViewById<EditText>(R.id.editTextText)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val buttonSignIn = findViewById<Button>(R.id.signinbutton)


        val forgotPasswordTextView = findViewById<TextView>(R.id.textView2)
        forgotPasswordTextView.setOnClickListener{
            val intent = Intent(applicationContext, RemindPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
        val registerTextView = findViewById<TextView>(R.id.textView4)
        registerTextView.setOnClickListener{
            val intent = Intent(applicationContext, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonSignIn.setOnClickListener{
            if(usernameEditText.text.isNotEmpty() || passwordEditText.text.isNotEmpty())
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
                        runOnUiThread {
                            val responseBodyString = response.body?.string() ?: ""
                            if (responseBodyString == "Login successful") {
                                val token = "abcd123"
                                saveToken(this@LoginActivity, token, username)
                                val intent = Intent(applicationContext, HomePageActivity::class.java)
                                intent.putExtra("USERNAME", username)
                                startActivity(intent)
                                finish()
                            }
                            else {
                                Toast.makeText(applicationContext,"Username or password is incorrect", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }
            else
            {
                Toast.makeText(applicationContext,"All fields are required", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun saveToken (context: Context, token: String, username: String)
    {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "user_preferences",
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        with(sharedPreferences.edit()){
            putString("user_token", token)
            putString("USERNAME", username)
            apply()
        }
    }
}