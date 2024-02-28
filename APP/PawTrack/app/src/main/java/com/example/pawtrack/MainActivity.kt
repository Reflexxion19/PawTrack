package com.example.pawtrack

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        val forgotPasswordTextView = findViewById<TextView>(R.id.textView2)
        val registerTextView = findViewById<TextView>(R.id.textView4)

        forgotPasswordTextView.setOnClickListener {
            showForgotPasswordLayout()
        }

        registerTextView.setOnClickListener {
            showRegisterLayout()
        }
    }

    private fun showForgotPasswordLayout() {
        setContentView(R.layout.forgot_password_layout)
        val returnButton = findViewById<Button>(R.id.button2)
        returnButton.setOnClickListener {
            setContentView(R.layout.login_layout)
            reattachListeners()
        }
    }

    private fun showRegisterLayout() {
        setContentView(R.layout.register_layout)
        val returnButton = findViewById<Button>(R.id.button2)
        returnButton.setOnClickListener {
            setContentView(R.layout.login_layout)
            reattachListeners()
        }
    }

    private fun reattachListeners() {
        val forgotPasswordTextView = findViewById<TextView>(R.id.textView2)
        val registerTextView = findViewById<TextView>(R.id.textView4)

        forgotPasswordTextView.setOnClickListener {
            showForgotPasswordLayout()
        }

        registerTextView.setOnClickListener {
            showRegisterLayout()
        }
    }
}