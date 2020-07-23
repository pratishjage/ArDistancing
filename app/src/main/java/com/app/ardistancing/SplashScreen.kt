package com.app.ardistancing

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


class SplashScreen : AppCompatActivity() {
    private val SPLASH_TIME_OUT: Long = 700

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed(Runnable { // This method will be executed once the timer is over
            // Start your app main activity
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)

            // close this activity
            finish()
        }, SPLASH_TIME_OUT)
    }
}