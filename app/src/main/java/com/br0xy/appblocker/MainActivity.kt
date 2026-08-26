package com.br0xy.appblocker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val prefsName = "AppBlockerPrefs"
    private val keyMode = "device_mode"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val savedMode = prefs.getString(keyMode, null)

        // Daha önce mod seçilmişse direkt o ekrana git
        if (savedMode == "controller") {
            startActivity(Intent(this, ControllerActivity::class.java))
            finish()
            return
        } else if (savedMode == "target") {
            startActivity(Intent(this, TargetActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val btnController = findViewById<Button>(R.id.btnController)
        val btnTarget = findViewById<Button>(R.id.btnTarget)

        btnController.setOnClickListener {
            prefs.edit().putString(keyMode, "controller").apply()
            startActivity(Intent(this, ControllerActivity::class.java))
            finish()
        }

        btnTarget.setOnClickListener {
            prefs.edit().putString(keyMode, "target").apply()
            startActivity(Intent(this, TargetActivity::class.java))
            finish()
        }
    }
}
