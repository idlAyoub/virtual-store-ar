package com.example.myapplication.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.myapplication.R

/**
 * ARViewActivity - Placeholder for future AR implementation
 * 
 * Current state: Shows product information and AR model resource availability
 * Future enhancement: Integrate ARSceneView for 3D model visualization
 */
class ARViewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_view)

        val arModel = intent.getStringExtra("AR_MODEL") ?: ""
        val productName = intent.getStringExtra("PRODUCT_NAME") ?: "Product"

        findViewById<TextView>(R.id.tvARProductName).text = productName

        Toast.makeText(
            this,
            "AR feature for $productName is coming soon!\nModel: $arModel",
            Toast.LENGTH_LONG
        ).show()

        findViewById<View>(R.id.btnCloseAR).setOnClickListener { finish() }
    }
}

