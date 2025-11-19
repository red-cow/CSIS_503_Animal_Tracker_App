package com.example.animalsalestracker

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.animalsalestracker.vm.AnimalsScreen
import com.example.animalsalestracker.ui.theme.AnimalSalesTrackerTheme
import com.example.animalsalestracker.vm.BottomNavBar
import com.example.animalsalestracker.vm.BottomTab
import com.example.animalsalestracker.vm.CustomerScreen
import com.example.animalsalestracker.vm.HomeScreen
import com.example.animalsalestracker.vm.OrdersScreen
import com.example.animalsalestracker.vm.SummaryScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AnimalSalesTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFF9C4)
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            BottomNavBar(
                selected = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        when (selectedTab) {
            BottomTab.HOME -> HomeScreen(modifier = Modifier.padding(innerPadding))
            BottomTab.ANIMALS -> AnimalsScreen(modifier = Modifier.padding(innerPadding))
            BottomTab.CUSTOMERS -> CustomerScreen(modifier = Modifier.padding(innerPadding))
            BottomTab.ORDERS -> OrdersScreen(modifier = Modifier.padding(innerPadding))
            BottomTab.SUMMARY -> SummaryScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}
