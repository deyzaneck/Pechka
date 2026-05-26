package com.example.pechka

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechka.ui.DeliveryViewModel
import com.example.pechka.ui.RoundingScreen
import com.example.pechka.ui.RouteScreen
import com.example.pechka.theme.*
import com.example.pechka.ui.screens.*

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("pechka_prefs", Context.MODE_PRIVATE) }
    
    var firstName by remember { mutableStateOf(sharedPrefs.getString("first_name", "") ?: "") }
    var lastName by remember { mutableStateOf(sharedPrefs.getString("last_name", "") ?: "") }
    
    val isRegistered = firstName.isNotBlank() && lastName.isNotBlank()

    if (!isRegistered) {
        OnboardingScreen(
            onSaveName = { first, last ->
                sharedPrefs.edit()
                    .putString("first_name", first)
                    .putString("last_name", last)
                    .apply()
                firstName = first
                lastName = last
            }
        )
    } else {
        AppContentScreen(firstName = firstName, lastName = lastName)
    }
}

@Composable
fun AppContentScreen(firstName: String, lastName: String) {
    val repository = PechkaApplication.instance.repository
    val viewModel: DeliveryViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
        DeliveryViewModel(repository)
    }

    val selectedTab = rememberSaveable { mutableStateOf("orders") }

    Scaffold(
        bottomBar = {
            Surface(
                color = SurfaceWhite,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = OutlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NavBarItem(
                            icon = Icons.Default.ShoppingCart,
                            label = "Заказы",
                            isSelected = selectedTab.value == "orders",
                            onClick = { selectedTab.value = "orders" },
                            modifier = Modifier.weight(1f)
                        )
                        NavBarItem(
                            icon = Icons.Default.Route,
                            label = "Рейс",
                            isSelected = selectedTab.value == "route",
                            onClick = { selectedTab.value = "route" },
                            modifier = Modifier.weight(1f)
                        )
                        NavBarItem(
                            icon = Icons.Default.History,
                            label = "История",
                            isSelected = selectedTab.value == "history",
                            onClick = { selectedTab.value = "history" },
                            modifier = Modifier.weight(1f)
                        )
                        NavBarItem(
                            icon = Icons.Default.Person,
                            label = "Профиль",
                            isSelected = selectedTab.value == "profile",
                            onClick = { selectedTab.value = "profile" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Full isolation: only the active tab is composed
            when (selectedTab.value) {
                "orders" -> OrdersTabScreen(viewModel = viewModel)
                "route" -> {
                    val isRoundingActive by viewModel.isRoundingActive.collectAsState(initial = false)
                    if (isRoundingActive) {
                        RoundingScreen(viewModel = viewModel)
                    } else {
                        RouteScreen(viewModel = viewModel)
                    }
                }
                "history" -> HistoryTabScreen(viewModel = viewModel)
                "profile" -> ProfileTabScreen(
                    viewModel = viewModel,
                    firstName = firstName,
                    lastName = lastName
                )
            }
        }
    }
}

@Composable
fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) SecondaryContainerGray else Color.Transparent
    val contentColor = if (isSelected) PrimaryGreen else SecondaryGray

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
        }
    }
}
