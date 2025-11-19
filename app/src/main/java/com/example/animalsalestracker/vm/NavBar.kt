package com.example.animalsalestracker.vm

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavBar(
    selected: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavButton(
            label = "Home",
            isSelected = selected == BottomTab.HOME,
            onClick = { onTabSelected(BottomTab.HOME) }
        )
        BottomNavButton(
            label = "Animals",
            isSelected = selected == BottomTab.ANIMALS,
            onClick = { onTabSelected(BottomTab.ANIMALS) }
        )
        BottomNavButton(
            label = "Customers",
            isSelected = selected == BottomTab.CUSTOMERS,
            onClick = { onTabSelected(BottomTab.CUSTOMERS) }
        )
        BottomNavButton(
            label = "Orders",
            isSelected = selected == BottomTab.ORDERS,
            onClick = { onTabSelected(BottomTab.ORDERS) }
        )
        BottomNavButton(
            label = "Summary",
            isSelected = selected == BottomTab.SUMMARY,
            onClick = { onTabSelected(BottomTab.SUMMARY) }
        )
    }
}

@Composable
private fun BottomNavButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = if (isSelected) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    Button(
        onClick = onClick,
        colors = colors,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier
            .height(40.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

enum class BottomTab {
    HOME,
    ANIMALS,
    CUSTOMERS,
    ORDERS,
    SUMMARY
}