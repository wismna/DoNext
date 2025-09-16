package com.wismna.geoffroy.donext.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wismna.geoffroy.donext.presentation.screen.MainScreen
import com.wismna.geoffroy.donext.presentation.ui.theme.DoNextTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoNextTheme { MainScreen() }
        }
    }
}