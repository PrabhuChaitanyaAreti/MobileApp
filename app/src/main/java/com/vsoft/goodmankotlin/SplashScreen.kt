package com.vsoft.goodmankotlin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler().postDelayed(Runnable
        // Using handler with postDelayed called runnable run method
        {
            val i = Intent(this, MaskingActivity::class.java)
            startActivity(i)

            // close this activity
            finish()
        }, 1 * 1000
        ) // wait for 5 seconds

    }
}