package com.tcprivatelimited.hitlerai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.tcprivatelimited.hitlerai.firebase.configureAppCheck
import com.tcprivatelimited.hitlerai.ui.WorkspaceShell
import com.tcprivatelimited.hitlerai.ui.screens.AuthScreen
import com.tcprivatelimited.hitlerai.ui.theme.HitlerAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before UI, ViewModels, or Firebase calls.
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            AppRoot()
        }
    }
}

@Composable
private fun AppRoot() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        HitlerAiTheme(backgroundPreset = com.tcprivatelimited.hitlerai.ui.theme.WorkspaceBackgroundPreset.MIDNIGHT_NEBULA) {
            AuthScreen(
                onAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                }
            )
        }
    } else {
        val uid = currentUser!!.uid
        key(uid) {
            WorkspaceShell(
                currentUserId = uid,
                onSignedOut = {
                    currentUser = null
                }
            )
        }
    }
}
