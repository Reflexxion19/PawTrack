package com.example.pawtrack

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.pawtrack.ui.theme.PawTrackTheme
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView




class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize osmdroid configuration
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE))
    }

    fun openMap(view: View) {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    fun onSetReminderButtonClick(view: View) {
        // Start the ReminderSettingActivity
        val intent = Intent(this, ReminderSettingActivity::class.java)
        startActivity(intent)
    }
}

/* Not yet implemented
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
} */

//Opens the map


//Empty function frames:

//Login function
//Returns success factor
fun LoginUser():Boolean{
    return true;
}

//Logout function
//Returns success factor
fun LogoutUser():Boolean{
    return true;
}

//Register function
//Returns success factor
fun RegisterUser():Boolean{
    return true;
}

//Password recovery function
//Returns success factor
fun ForgotPassword():Boolean{
    return true;
}

//Extends premium account status
//Returns success factor
fun ExtendPremiumAccount():Boolean{
    return true;
}

//User account deletion function
//Returns success factor
fun DeleteUser():Boolean{
    return true;
}

//Profile edit function
//Returns success factor
fun EditProfile():Boolean{
    return true;
}

//Profile view function
//Returns success factor
fun ViewProfile(){
    //to implement
}

//Pet Register function
//Returns success factor
fun RegisterPet():Boolean{
    return true;
}

//Pet delete function
//Returns success factor
fun DeletePet():Boolean{
    return true;
}

//Pet edit function
//Returns success factor
fun EditPet():Boolean{
    return true;
}

//Pet view function
fun ViewPet(){
    //to implement
}

//Pet diet view function
fun ViewDietPet(){
    //to implement
}

//Create Pet diet  function
//Returns success factor
fun CreateDietPet():Boolean{
    return true;
}

//Edit Pet diet  function
//Returns success factor
fun EditDietPet():Boolean{
    return true;
}

//Delete Pet diet  function
//Returns success factor
fun DeleteDietPet():Boolean{
    return true;
}

//Start activity function
//Returns success factor
fun StartActivity():Boolean{
    return true;
}

//Submit activity function
//Returns success factor
fun SubmitActivity():Boolean{
    return true;
}

//View Activity report function
fun ViewActivityReport(){
    //to implement
}

//Get activity data function
//Returns success factor
fun GetActivityData():Boolean{
    return true;
}

//Processes data from the controller on the pet
fun ProcessControllerData(){
    //to implement
}

//Lists all users
fun ListAllUsers(){
    //to implement
}

//Views specific user account
fun ViewUserAccount(){
    //to implement
}

//Deletes specific user account
//Returns success factor
fun DeleteUserAccount():Boolean{
    return true;
}