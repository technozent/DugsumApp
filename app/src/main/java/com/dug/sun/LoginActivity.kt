package com.dug.sun

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dug.sun.api.RetrofitClient
import com.dug.sun.api.getErrorMessage
import com.dug.sun.model.LoginRequest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        RetrofitClient.init(SessionManager(this))

        val etMobile = findViewById<EditText>(R.id.etMobile)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val mobile = etMobile.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInputs(mobile, password)) {
                performLogin(mobile, password)
            }
        }
    }

    private fun validateInputs(mobile: String, password: String): Boolean {
        if (mobile.isEmpty()) {
            Toast.makeText(this, "Please enter mobile number", Toast.LENGTH_SHORT).show()
            return false
        }
        if (mobile.length != 10) {
            Toast.makeText(this, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun performLogin(mobile: String, password: String) {
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
        val request = LoginRequest(mobile, password, deviceId)

        findViewById<Button>(R.id.btnLogin).isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(request)
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val sessionManager = SessionManager(this@LoginActivity)
                    sessionManager.setLoggedIn(true)
                    sessionManager.saveUserDetails(
                        loginResponse?.username,
                        loginResponse?.planStartDate,
                        loginResponse?.planEndDate,
                        loginResponse?.accessToken
                    )
                    Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    val message = response.getErrorMessage()
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                }
            } catch (_: java.net.ConnectException) {
                Toast.makeText(this@LoginActivity, "Cannot connect to server. Please check your connection.", Toast.LENGTH_SHORT).show()
            } catch (_: java.net.SocketTimeoutException) {
                Toast.makeText(this@LoginActivity, "Connection timed out. Please try again.", Toast.LENGTH_SHORT).show()
            } catch (_: java.net.UnknownHostException) {
                Toast.makeText(this@LoginActivity, "Unable to resolve server address. Check your internet.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "An unexpected error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                findViewById<Button>(R.id.btnLogin).isEnabled = true
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}