package com.br0xy.appblocker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class TargetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_target)

        val btnGrant = findViewById<Button>(R.id.btnGrantPermission)
        val btnSendList = findViewById<Button>(R.id.btnSendAppList)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        btnGrant.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        btnSendList.setOnClickListener {
            sendInstalledAppsToFirebase()
            Toast.makeText(this, "Uygulama listesi gönderildi", Toast.LENGTH_SHORT).show()
        }

        tvStatus.text = "İzni verdikten sonra listede AppBlocker'ı bulup açman gerekiyor."
    }

    private fun sendInstalledAppsToFirebase() {
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

        FirebaseDatabase.getInstance().getReference("installedApps").setValue(appList)
    }
}
