package com.example.animalsalestracker.vm

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.animalsalestracker.data.AppDatabase

@Composable
fun SummaryScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val db = remember(context) { AppDatabase.get(context) }
    val dao = db.orderDao()

    // collect the flow → state (0 as initial)
    val openOrders by dao.totalOpenOrders().collectAsState(initial = 0)
    val closedOrder by dao.totalCloseOrders().collectAsState(initial = 0)
    val totalDownpayment by dao.totalDownpayment().collectAsState(initial = 0)
    val totalPayment by dao.totalPayment().collectAsState(initial = 0)
    val animalsSold by dao.animalsSold().collectAsState(initial = 0)
    val animalsAvailable by dao.animalsAvailable().collectAsState(initial = 0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Text("Dashboard", style = MaterialTheme.typography.headlineLarge.copy(textDecoration = TextDecoration.Underline),textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth() )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Orders", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth() )
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(
                    width = 2.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Open orders: $openOrders", style = MaterialTheme.typography.labelLarge)
                Text("Closed orders: $closedOrder", style = MaterialTheme.typography.labelLarge)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)){
            Text("Revenue", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth() )
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(
                    width = 2.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween){
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ){
                    Text("Total Deposits Made: $$totalDownpayment", style = MaterialTheme.typography.labelLarge)
                    Text("Total Payments Made: $$totalPayment", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)){
            Text("Animals", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth() )
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(
                    width = 2.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween){
                Text("Animals Available: $animalsAvailable", style = MaterialTheme.typography.labelLarge)
                Text("Animals Sold: $animalsSold", style = MaterialTheme.typography.labelLarge)
            }
        }


    }
}