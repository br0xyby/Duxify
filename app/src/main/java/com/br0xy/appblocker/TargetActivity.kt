package com.br0xy.appblocker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class TargetActivity : AppCompatActivity() {

    private val prefsName = "AppBlockerPrefs"
    private val keyPairingCode = "pairing_code"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_target)

        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)

        val tvPairingStatus = findViewById<TextView>(R.id.tvPairingStatus)
        val etPairingCode = findViewById<EditText>(R.id.etPairingCode)
        val btnSaveCode = findViewById<Button>(R.id.btnSaveCode)
        val btnGrant = findViewById<Button>(R.id.btnGrantPermission)
        val btnSendList = findViewById<Button>(R.id.btnSendAppList)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val savedCode = prefs.getString(keyPairingCode, null)
        if (savedCode != null) {
            tvPairingStatus.text = "Eşleştirilen kod: $savedCode"
            etPairingCode.setText(savedCode)
        }

        btnSaveCode.setOnClickListener {
            val code = etPairingCode.text.toString().trim()
            if (code.length == 6) {
                prefs.edit().putString(keyPairingCode, code).apply()
                tvPairingStatus.text = "Eşleştirilen kod: $code"
                Toast.makeText(this, "Kod kaydedildi", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Kod 6 haneli olmalı", Toast.LENGTH_SHORT).show()
            }
        }

        btnGrant.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        btnSendList.setOnClickListener {
            val code = prefs.getString(keyPairingCode, null)
            if (code == null) {
                Toast.makeText(this, "Önce eşleştirme kodunu kaydet", Toast.LENGTH_SHORT).show()
            } else {
                sendInstalledAppsToFirebase(code)
                Toast.makeText(this, "Uygulama listesi gönderildi", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        tvStatus.text = "İzni verdikten sonra listede AppBlocker'ı bulup açman gerekiyor."
    }

    private fun sendInstalledAppsToFirebase(code: String) {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolvedApps = pm.queryIntentActivities(intent, 0)

        val appList = resolvedApps.map { resolveInfo ->
            mapOf(
                "packageName" to resolveInfo.activityInfo.packageName,
                "appName" to resolveInfo.loadLabel(pm).toString()
            )
        }.distinctBy { it["packageName"] }.sortedBy { it["appName"] }

        FirebaseDatabase.getInstance().getReference("pairs/$code/installedApps").setValue(appList)
    }
}