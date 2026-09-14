package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.mtaatok.data.local.MtaaTokDatabase
import com.example.mtaatok.data.local.SessionManager
import com.example.mtaatok.data.repository.MtaaTokRepository
import com.example.mtaatok.ui.auth.AuthScreen
import com.example.mtaatok.ui.main.MainScaffold
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = MtaaTokDatabase.getDatabase(applicationContext)
        val sessionManager = SessionManager(applicationContext)
        val repository = MtaaTokRepository(database.dao(), sessionManager)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    val currentUser by sessionManager.currentUser.collectAsState()

                    if (currentUser != null) {
                        MainScaffold(
                            repository = repository,
                            onLogout = {
                                sessionManager.logout()
                            }
                        )
                    } else {
                        AuthScreen(
                            sessionManager = sessionManager,
                            onAuthSuccess = {
                                // Session is updated in SessionManager, Compose will automatically recompose to MainScaffold
                            }
                        )
                    }
                }
            }
        }
    }
}
