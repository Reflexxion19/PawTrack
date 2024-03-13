package com.example.pawtrack

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vishnusivadas.advanced_httpurlconnection.PutData


class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_layout)

        val usernameEditText = findViewById<EditText>(R.id.editTextText)
        val emailEditText = findViewById<EditText>(R.id.editTextText2)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val buttonSignUp = findViewById<Button>(R.id.signupbutton)

        buttonSignUp.setOnClickListener{
            if(!usernameEditText.text.equals("") && !emailEditText.text.equals("") && !passwordEditText.equals(""))
            {
                val handler = Handler(Looper.getMainLooper())
                handler.post(Runnable {
                    //Starting Write and Read data with URL
                    //Creating array for parameters
                    val field = arrayOfNulls<String>(3)
                    field[0] = "username"
                    field[1] = "email"
                    field[2] = "password"
                    //Creating array for data
                    val data = arrayOfNulls<String>(3)
                    data[0] = usernameEditText.text.toString()
                    data[1] = emailEditText.text.toString()
                    data[2] = passwordEditText.text.toString()
                    val putData = PutData(
                        "http://192.168.56.1/Pawtrack/signup.php", //savo ipv4 adresa jei lokaliai darot
                        "POST",
                        field,
                        data
                    )
                    if (putData.startPut()) {
                        if (putData.onComplete()) {
                            val result: String = putData.getResult()
                            if(result.equals("Signed Up Successfully"))
                            {
                                Toast.makeText(applicationContext,result, Toast.LENGTH_SHORT).show()
                                val intent = Intent(applicationContext, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else
                            {
                                print(result)
                                Toast.makeText(applicationContext,result, Toast.LENGTH_LONG).show()
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
        val returnButton = findViewById<Button>(R.id.button2)
        returnButton.setOnClickListener {
            setContentView(R.layout.login_layout)
            finish()
        }
    }
}