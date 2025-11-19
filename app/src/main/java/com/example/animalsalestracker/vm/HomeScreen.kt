package com.example.animalsalestracker.vm

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val currentHour = LocalTime.now().hour
    val greeting = if (currentHour < 12){
        "Good morning"
    } else {
        "Good afternoon"
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            //.background(Color(0xFFFFF9C4)) // Light yellow background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Animal Sales Tracker",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = greeting,
            fontSize = 18.sp
        )
    }
}