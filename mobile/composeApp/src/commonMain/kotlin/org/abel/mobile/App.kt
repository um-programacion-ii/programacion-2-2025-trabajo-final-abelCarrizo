package org.abel.mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.abel.mobile.ui.navigation.AppNavigation
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        AppNavigation()
    }
}