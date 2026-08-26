package com.br0xy.appblocker

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class ControllerActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var tvStatus: TextView
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    private val packageNames = mutableListOf<String>()
    private val displayNames = mutableListOf<String>()
    private var currentBlockedApps: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller)

        database = FirebaseDatabase.getInstance()
        tvStatus = findViewById(R.id.tvStatus)
        listView = findViewById(R.id.lvApps)

        val btnBlock = findViewById<Button>(R.id.btnBlock)
        val btnAllow = findViewById<Button>(R.id.btnAllow)
        val btnSaveApps = findViewById<Button>(R.id.btnSaveApps)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, displayNames)
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        val statusRef = database.getReference("status")
        statusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(String::class.java) ?: "belirsiz"
                tvStatus.text = "Durum: $value"
            }
            override fun onCancelled(error: DatabaseError) {
                tvStatus.text = "Durum: hata (${error.message})"
            }
        })

        btnBlock.setOnClickListener { statusRef.setValue("blocked") }
        btnAllow.setOnClickListener { statusRef.setValue("allowed") }

        database.getReference("installedApps").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                packageNames.clear()
                displayNames.clear()
                for (child in snapshot.children) {
                    val pkg = child.child("packageName").getValue(String::class.java) ?: continue
                    val name = child.child("appName").getValue(String::class.java) ?: pkg
                    packageNames.add(pkg)
                    displayNames.add("$name\n$pkg")
                }
                adapter.notifyDataSetChanged()
                applyCheckedState()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.getReference("blockedApps").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typeIndicator = object : GenericTypeIndicator<List<String>>() {}
                currentBlockedApps = (snapshot.getValue(typeIndicator) ?: emptyList()).toSet()
                applyCheckedState()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        btnSaveApps.setOnClickListener {
            val selected = mutableListOf<String>()
            for (i in packageNames.indices) {
                if (listView.isItemChecked(i)) {
                    selected.add(packageNames[i])
                }
            }
            database.getReference("blockedApps").setValue(selected)
            Toast.makeText(this, "Seçim kaydedildi (${selected.size} uygulama)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyCheckedState() {
        for (i in packageNames.indices) {
            listView.setItemChecked(i, currentBlockedApps.contains(packageNames[i]))
        }
    }
}