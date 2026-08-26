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

    private var currentStatus: String = "allowed"
    private var blockedPackageNames: Set<String> = emptySet()

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AppBlockerDebug", "Servis bağlandı!")

        // Servis ayarlarını kod içinde manuel olarak zorla ayarla
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.DEFAULT
        info.notificationTimeout = 100
        serviceInfo = info

        Log.d("AppBlockerDebug", "ServiceInfo manuel olarak ayarlandı")

        val database = FirebaseDatabase.getInstance()

        database.getReference("status").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentStatus = snapshot.getValue(String::class.java) ?: "allowed"
                Log.d("AppBlockerDebug", "Durum güncellendi: $currentStatus")
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("AppBlockerDebug", "Durum okuma hatası: ${error.message}")
            }
        })

        database.getReference("blockedApps").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typeIndicator = object : GenericTypeIndicator<List<String>>() {}
                blockedPackageNames = (snapshot.getValue(typeIndicator) ?: emptyList()).toSet()
                Log.d("AppBlockerDebug", "Engellenen uygulamalar: $blockedPackageNames")
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("AppBlockerDebug", "Liste okuma hatası: ${error.message}")
            }
        })
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        Log.d("AppBlockerDebug", "Açılan uygulama: $packageName | durum: $currentStatus | engelli mi: ${blockedPackageNames.contains(packageName)}")

        if (blockedPackageNames.contains(packageName) && currentStatus == "blocked") {
            Log.d("AppBlockerDebug", "ENGELLENDİ: $packageName")
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onInterrupt() {}
}