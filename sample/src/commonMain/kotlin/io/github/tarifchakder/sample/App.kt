package io.github.tarifchakder.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.tarifchakder.networkobserver.networkObserverAsState
import io.github.tarifchakder.networkobserver.networkTypeAsState


@Composable
fun App() {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            val status by networkObserverAsState()
            Text("Status: $status")

            val networkType by networkTypeAsState()
            Text("Network type: $networkType")
        }
    }
}