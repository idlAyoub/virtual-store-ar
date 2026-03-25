package com.example.myapplication.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

import com.example.myapplication.R
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

        // --- Back Button ---
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // --- Language Dialog Setup ---
        val tvLanguageSubtitle = findViewById<TextView>(R.id.tvLanguageSubtitle)
        val currentLang = prefs.getString("app_language", "en") ?: "en"
        val languageMap = mapOf("en" to "English", "fr" to "French", "ar" to "Arabic")
        tvLanguageSubtitle.text = languageMap[currentLang] ?: "English"

        findViewById<View>(R.id.btnLanguage).setOnClickListener {
            val options = arrayOf("English", "French", "Arabic")
            val langCodes = arrayOf("en", "fr", "ar")
            var checkedItem = langCodes.indexOf(currentLang).takeIf { it >= 0 } ?: 0

            AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                    checkedItem = which
                    val selectedCode = langCodes[which]
                    
                    prefs.edit().putString("app_language", selectedCode).apply()
                    tvLanguageSubtitle.text = options[which]
                    
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(selectedCode))
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // --- Dark Mode Toggle ---
        val switchDarkMode = findViewById<SwitchMaterial>(R.id.switchDarkMode)
        switchDarkMode.isChecked = prefs.getBoolean("dark_mode_enabled", false)
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode_enabled", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // --- About Dialog Setup ---
        findViewById<View>(R.id.btnAbout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("VirtualStore v1.0")
                .setMessage("Smart Shopping AR is an innovative AR-powered shopping experience designed to make buying smarter and more interactive.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

}
