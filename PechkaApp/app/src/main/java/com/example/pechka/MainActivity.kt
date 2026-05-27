package com.example.pechka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import com.example.pechka.theme.PechkaTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      val density = LocalDensity.current
      val customDensity = remember(density) {
        object : androidx.compose.ui.unit.Density {
          override val density: Float = density.density
          override val fontScale: Float = 1.0f
        }
      }
      CompositionLocalProvider(LocalDensity provides customDensity) {
        PechkaTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() } }
      }
    }
  }
}
