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
            if(!usernameEditText.text.equals("") && !passwordEditText.equals(""))
            {
                val handler = Handler(Looper.getMainLooper())
                handler.post(Runnable {
                    //Starting Write and Read data with URL
                    //Creating array for parameters
                    val field = arrayOfNulls<String>(2)
                    field[0] = "username"
                    field[1] = "password"
                    //Creating array for data
                    val data = arrayOfNulls<String>(2)
                    data[0] = usernameEditText.text.toString()
                    data[1] = passwordEditText.text.toString()
                    val putData = PutData(
                        "http://192.168.56.1/Pawtrack/login.php", //savo ipv4 adresa jei lokaliai darot
                        "POST",
                        field,
                        data
                    )
                    if (putData.startPut()) {
                        if (putData.onComplete()) {
                            val result: String = putData.getResult()
                            if(result.equals("Login successful"))
                            {
                                Toast.makeText(applicationContext,result, Toast.LENGTH_SHORT).show()
                                val intent = Intent(applicationContext, HomePageActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else
                            {
                                Toast.makeText(applicationContext,result, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    //End Write and Read data with URL
                })
            }
            else
            {
                Toast.makeText(applicationContext,"All fields are required.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}