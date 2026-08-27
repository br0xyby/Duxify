package com.br0xy.appblocker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class AppBlockerAccessibilityService : AccessibilityService() {

    private val prefsName = "AppBlockerPrefs"
    private val keyPairingCode = "pairing_code"

    private var currentStatus: String = "allowed"
    private var blockedPackageNames: Set<String> = emptySet()

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AppBlockerDebug", "Servis bağlandı!")

        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.DEFAULT
        info.notificationTimeout = 100
        serviceInfo = info

        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val code = prefs.getString(keyPairingCode, null)

        if (code == null) {
            Log.d("AppBlockerDebug", "Eşleştirme kodu yok, servis pasif kalacak")
            return
        }

        Log.d("AppBlockerDebug", "Kullanılan eşleştirme kodu: $code")

        val database = FirebaseDatabase.getInstance()

        database.getReference("pairs/$code/status").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentStatus = snapshot.getValue(String::class.java) ?: "allowed"
                Log.d("AppBlockerDebug", "Durum güncellendi: $currentStatus")
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.getReference("pairs/$code/blockedApps").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typeIndicator = object : GenericTypeIndicator<List<String>>() {}
                blockedPackageNames = (snapshot.getValue(typeIndicator) ?: emptyList()).toSet()
                Log.d("AppBlockerDebug", "Engellenen uygulamalar: $blockedPackageNames")
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return

        if (blockedPackageNames.contains(packageName) && currentStatus == "blocked") {
            Log.d("AppBlockerDebug", "ENGELLENDİ: $packageName")
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onInterrupt() {}
}
