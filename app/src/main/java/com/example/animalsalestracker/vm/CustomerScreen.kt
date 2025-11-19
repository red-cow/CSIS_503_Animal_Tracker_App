package com.example.animalsalestracker.vm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.animalsalestracker.data.AppDatabase

import com.example.animalsalestracker.data.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CustomerScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember(context) { AppDatabase.get(context) }

    val animalDao = db.animalDao()
    val customerDao = db.customerDao()
    // Flow → State
    val animals by animalDao.getAll().collectAsState(initial = emptyList())
    val customers by customerDao.getAll().collectAsState(initial = emptyList())

    var selected by remember { mutableStateOf<Customer?>(null) }

    LaunchedEffect(customers) {
        selected = selected?.let { sel -> customers.find { it.customerId == sel.customerId } }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // LEFT: single column list (< 50% width)
        CustomerGallery(
            customers = customers,
            onCustomerClick = { c -> selected = c },
            modifier = Modifier
                .weight(0.45f)   // less than half
                .fillMaxHeight()
        )

        // RIGHT: form (remaining width)
        CustomerForm(
            selected = selected,
            customers = customers,
            onSave = {name, email, phone ->
                // Insert if nothing selected; otherwise update the selected row
                if (selected == null) {
                    // create
                    val newId = withContext(Dispatchers.IO) {

                        customerDao.upsert(Customer(name=name, email=email, phone=phone))
                    }
                    // set selection to the newly inserted animal
                    selected = Customer(newId, name, email, phone)
                } else {
                    // update
                    withContext(Dispatchers.IO) {
                        customerDao.update( Customer(
                            customerId = selected!!.customerId,
                            name = name,
                            email = email,
                            phone = phone
                        ))
                    }
                }
            },
            onAfterSaveRefresh = {

            },
            onNew = {
                selected = null

            },
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
                .padding(start = 12.dp)
        )
    }
}

@Composable
fun CustomerGallery(
    customers: List<Customer>,
    onCustomerClick: (Customer) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 12.dp)
        ) {
            items(customers, key = { it.customerId }) { c ->
                CustomerCard(c, onClick = onCustomerClick)
            }
        }
    }
}


@Composable
fun CustomerCard(c: Customer, onClick: (Customer) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(c) }
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = c.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = c.email,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = c.phone,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun CustomerForm(
    selected: Customer?,
    customers: List<Customer>,
    onSave: suspend (name: String, email: String, phone: String) -> Unit,
    onAfterSaveRefresh: () -> Unit,
    onNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // compute current label from id
//    val buyerName: String = remember(buyerId, customers) {
//        customers.firstOrNull { it.customerId == buyerId }?.name ?: ""
//    }

    // Populate fields when selection changes
    LaunchedEffect(selected?.customerId) {
        if (selected == null) {
            name = ""
            email = ""
            phone = ""
        } else {
            name = selected.name
            email = selected.email
            phone = selected.phone
        }
    }

    val scope = rememberCoroutineScope()
    val canSave = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank()

    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (selected == null) "New Customer" else "Edit Customer #${selected.customerId}",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(200.dp)) {
            Button(
                enabled = canSave,
                onClick = {
                    scope.launch {
                        onSave(name.trim(), email.trim(), phone.trim())
                        onAfterSaveRefresh()
                    }
                }
            ) { Text(if (selected == null) "Create" else "Save") }

            OutlinedButton(onClick = onNew) { Text("New") }
        }
    }
}