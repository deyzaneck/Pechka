package com.example.pechka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pechka.theme.*

@Composable
fun OnboardingScreen(onSaveName: (String, String) -> Unit) {
    var firstNameInput by remember { mutableStateOf("") }
    var lastNameInput by remember { mutableStateOf("") }
    val lastNameFocus = remember { androidx.compose.ui.focus.FocusRequester() }
    val firstNameFocus = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(Unit) { lastNameFocus.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PrimaryContainerGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = "Хлебный Путь",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ХЛЕБНЫЙ ПУТЬ",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryGreen
        )

        Text(
            text = "Приложение водителя-экспедитора",
            fontSize = 14.sp,
            color = SecondaryGray,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, OutlineVariant, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "РЕГИСТРАЦИЯ ВОДИТЕЛЯ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryGray
                )

                OutlinedTextField(
                    value = lastNameInput,
                    onValueChange = { lastNameInput = it },
                    label = { Text("Фамилия") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onNext = { firstNameFocus.requestFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(lastNameFocus),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = OutlineGray
                    )
                )

                OutlinedTextField(
                    value = firstNameInput,
                    onValueChange = { firstNameInput = it },
                    label = { Text("Имя") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(firstNameFocus),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = OutlineGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val isButtonEnabled = firstNameInput.isNotBlank() && lastNameInput.isNotBlank()
        Button(
            onClick = {
                if (isButtonEnabled) {
                    onSaveName(firstNameInput.trim(), lastNameInput.trim())
                }
            },
            enabled = isButtonEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryContainerGreen,
                disabledContainerColor = SecondaryGray.copy(alpha = 0.3f),
                contentColor = Color.White,
                disabledContentColor = SecondaryGray
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "НАЧАТЬ РАБОТУ",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
