package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.core.configureAppCheck
import com.example.ui.GreenBiteApp
import com.example.ui.theme.GreenBiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before rendering UI or creating ViewModels.
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            GreenBiteTheme {
                GreenBiteApp()
            }
        }
    }
}
