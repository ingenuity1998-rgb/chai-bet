package com.chaibet.app

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = AppPreferences(this)

        setupViews()
        renderStatsAndLedger()
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        renderStatsAndLedger()
    }

    private fun setupViews() {
        val edtBuddyName = findViewById<EditText>(R.id.edtBuddyName)
        val edtBuddyUpi = findViewById<EditText>(R.id.edtBuddyUpi)
        val btnSave = findViewById<Button>(R.id.btnSaveBuddy)
        val btnShare = findViewById<Button>(R.id.btnShareWhatsapp)
        val btnTestOverlay = findViewById<Button>(R.id.btnTestOverlay)
        val btnSimulateWin = findViewById<Button>(R.id.btnSimulateWin)
        val rgSnacks = findViewById<RadioGroup>(R.id.rgSnackTiers)

        edtBuddyName.setText(prefs.buddyName)
        edtBuddyUpi.setText(prefs.buddyUpi)

        // Select active snack tier
        when (prefs.snackPrice) {
            25 -> findViewById<RadioButton>(R.id.rbTier25)?.isChecked = true
            50 -> findViewById<RadioButton>(R.id.rbTier50)?.isChecked = true
            100 -> findViewById<RadioButton>(R.id.rbTier100)?.isChecked = true
            250 -> findViewById<RadioButton>(R.id.rbTier250)?.isChecked = true
        }

        rgSnacks.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbTier25 -> { prefs.snackPrice = 25; prefs.snackName = "Cutting Chai" }
                R.id.rbTier50 -> { prefs.snackPrice = 50; prefs.snackName = "Chai + Samosa" }
                R.id.rbTier100 -> { prefs.snackPrice = 100; prefs.snackName = "Cold Brew" }
                R.id.rbTier250 -> { prefs.snackPrice = 250; prefs.snackName = "Biryani Pot" }
            }
            Toast.makeText(this, "Bet tier: ${prefs.snackName} (₹${prefs.snackPrice})", Toast.LENGTH_SHORT).show()
        }

        btnSave.setOnClickListener {
            prefs.buddyName = edtBuddyName.text.toString().trim()
            prefs.buddyUpi = edtBuddyUpi.text.toString().trim()
            Toast.makeText(this, "Buddy details saved!", Toast.LENGTH_SHORT).show()
        }

        btnShare.setOnClickListener {
            val shareText = "Bro, I bet you a cutting chai you can't stay off reels until 6 PM today! Download Chai Bet with code pavan98: if you slip, you buy my chai! ☕🥟 https://chaibet.app/r/pavan98"
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, "Challenge a Buddy on WhatsApp"))
        }

        // Test Interlock Overlay immediately
        btnTestOverlay.setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                val intent = Intent(this, OverlayService::class.java).apply {
                    putExtra("EXTRA_PACKAGE_NAME", "Instagram Demo")
                }
                startService(intent)
            } else {
                Toast.makeText(this, "Please grant Overlay permission first!", Toast.LENGTH_LONG).show()
            }
        }

        // Simulate a friend buying you a chai
        btnSimulateWin.setOnClickListener {
            val friends = listOf("Vikram Rao", "Sneha Reddy")
            val chosen = friends.random()
            prefs.recordChaiWon(chosen, 25)
            renderStatsAndLedger()
            Toast.makeText(this, "🎉 Free Chai Won! $chosen slipped on Reels (+₹20 to you)!", Toast.LENGTH_LONG).show()
        }
    }

    private fun renderStatsAndLedger() {
        val txtChaisWon = findViewById<TextView>(R.id.txtStatChaisWon)
        val txtChaisGiven = findViewById<TextView>(R.id.txtStatChaisGiven)
        val ledgerContainer = findViewById<LinearLayout>(R.id.ledgerItemsContainer)

        txtChaisWon?.text = "${prefs.totalChaisWon} Cups (₹${prefs.totalChaisWon * 20})"
        txtChaisGiven?.text = "${prefs.totalChaisGiven} Cups (₹${prefs.totalChaisGiven * 20})"

        ledgerContainer?.removeAllViews()
        val entries = prefs.getLedgerEntries()

        for (item in entries) {
            val row = TextView(this).apply {
                text = "${item.time} • ${item.event} -> Buddy: +₹${item.friendCut} | App: +₹${item.appCut}"
                setTextColor(resources.getColor(R.color.text_white, null))
                textSize = 12f
                setPadding(0, 8, 0, 8)
            }
            ledgerContainer?.addView(row)
        }
    }

    private fun checkPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle("Permission Needed: Draw Over Other Apps")
                .setMessage("Chai Bet requires the Overlay permission to display the friendly Chai Bet card over Instagram.")
                .setPositiveButton("Grant") { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            showProminentAccessibilityDisclosureIfNeeded()
        }
    }

    private fun showProminentAccessibilityDisclosureIfNeeded() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.prominent_disclosure_title))
            .setMessage(getString(R.string.prominent_disclosure_body))
            .setPositiveButton("Enable in Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Later", null)
            .show()
    }
}
