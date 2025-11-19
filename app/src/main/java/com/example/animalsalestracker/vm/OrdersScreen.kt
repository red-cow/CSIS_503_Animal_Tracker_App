package com.example.animalsalestracker.vm
import android.app.DatePickerDialog
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.animalsalestracker.data.Animal
import com.example.animalsalestracker.data.AppDatabase
import com.example.animalsalestracker.data.Customer
import com.example.animalsalestracker.data.Order
import com.example.animalsalestracker.data.OrderDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.collections.forEach

@Composable
fun OrdersScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember(context) { AppDatabase.get(context) }

    val animalDao = db.animalDao()
    val customerDao = db.customerDao()
    val orderDao = db.orderDao()

    // Flow → State
    val animals by animalDao.getAll().collectAsState(initial = emptyList())
    val animalsWithNoOrderAttached by animalDao.getAnimalsWithoutOrders().collectAsState(initial = emptyList())
    val customers by customerDao.getAll().collectAsState(initial = emptyList())
    val orders by orderDao.getAll().collectAsState(initial = emptyList())

    var selected by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(orders) {
        selected = selected?.let { sel -> orders.find { it.id == sel.id } }
    }

    val animalsForForm = remember(animalsWithNoOrderAttached, selected) {
        if (selected?.animalId != null) {
            val selectedAnimal = animals.find { it.animalId == selected!!.animalId }
            if (selectedAnimal != null) {
                animalsWithNoOrderAttached + selectedAnimal
            } else {
                animalsWithNoOrderAttached
            }
        } else {
            animalsWithNoOrderAttached
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // LEFT: single column list (< 50% width)
        OrderGallery(
            orders = orders,
            customers = customers,
            animals = animals,
            onOrderClick = { o -> selected = o },
            modifier = Modifier
                .weight(0.45f)   // less than half
                .fillMaxHeight()
        )
        // RIGHT: form (remaining width)
        OrderForm(
            selected = selected,
            customers = customers,
            animals = animalsForForm,
            onSave = { animalId, customerId, deposit, payment, readyDate, purchaseComplete ->
                // Insert if nothing selected; otherwise update the selected row
                if (selected == null) {
                    // create
                    val newId = withContext(Dispatchers.IO) {

                        orderDao.upsert(Order(animalId=animalId, customerId =customerId, deposit = deposit, payment=payment, readyDate = readyDate, purchaseComplete = purchaseComplete))
                    }
                    // set selection to the newly inserted animal
                    selected = Order(newId, animalId, customerId, deposit, payment, readyDate, purchaseComplete)
                } else {
                    // update
                    withContext(Dispatchers.IO) {
                        orderDao.update( Order(
                            id = selected!!.id,
                            animalId = selected!!.animalId,
                            customerId = selected!!.customerId,
                            deposit = deposit,
                            payment = payment,
                            readyDate = readyDate,
                            purchaseComplete = purchaseComplete
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


    }}

@Composable
fun OrderGallery(
    orders: List<Order>,
    customers: List<Customer>,
    animals: List<Animal>,
    onOrderClick: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    // make quick lookup maps (faster than scanning list in every row)
    val customerMap = remember(customers) {
        customers.associateBy { it.customerId }
    }
    val animalMap = remember(animals) {
        animals.associateBy { it.animalId }
    }
    Column(modifier = modifier) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 12.dp)
        ) {
            items(orders, key = { it.id }) { o ->
                val customer = o.customerId?.let { customerMap[it] }
                val animal = o.animalId?.let { animalMap[it] }
                OrderCard(o, customer,animal, onClick = onOrderClick)
            }
        }
    }
}


@Composable
fun OrderCard(o: Order, customer: Customer?, animal: Animal?, onClick: (Order) -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(o) }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${customer?.email}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${animal?.type}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Deposit: $${o.deposit}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${if(o.purchaseComplete){"Complete"}else{"In Process"}}",
                style = MaterialTheme.typography.bodyMedium//,
                //textAlign = TextAlign.Center,          // center the text
                //modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
@Composable
fun ReadyDateField(dob: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formatted =
                    "%02d/%02d/%04d".format(selectedMonth + 1, selectedDay, selectedYear)
                onDateSelected(formatted)
                showDialog = false
            },
            year,
            month,
            day
        ).show()
        showDialog = false
    }

    OutlinedTextField(
        value = dob,
        onValueChange = {},
        label = { Text("Pick up Date") },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Pick date")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderForm(
    selected: Order?,
    animals: List<Animal>,
    customers: List<Customer>,
    onSave: suspend (
        animalId: Long?,
        customerId: Long?,
        deposit: Double,
        payment: Double,
        readyDate: String,
        purchaseComplete: Boolean
    ) -> Unit,
    onAfterSaveRefresh: () -> Unit,
    onNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    var depositText by remember { mutableStateOf("") }
    var paymentText by remember { mutableStateOf("") }
    var readyDate by remember { mutableStateOf("") }
    var purchaseComplete by remember { mutableStateOf(false) }

    var customerId by remember { mutableStateOf<Long?>(null) }
    var animalId by remember { mutableStateOf<Long?>(null) }
    var expandedBuyer by remember { mutableStateOf(false) }
    var expandedAnimal by remember { mutableStateOf(false) }

    val buyerName: String = remember(customerId, customers) {
        customers.firstOrNull { it.customerId == customerId }?.name ?: ""
    }

    val animalName: String = remember(animalId, animals) {
        animals.firstOrNull { it.animalId == animalId }?.name ?: ""
    }

    // Populate fields when selection changes
    LaunchedEffect(selected?.id) {
        if (selected == null) {
            animalId = null
            customerId = null
            depositText = ""
            paymentText = ""
            readyDate = ""
            purchaseComplete = false
        } else {
            animalId = selected.animalId
            customerId = selected.customerId
            depositText = selected.deposit.toString()
            paymentText = selected.payment.toString()
            readyDate = selected.readyDate
            purchaseComplete = selected.purchaseComplete
        }
    }

    val scope = rememberCoroutineScope()
    val canSave = animalId != null && customerId != null  // <- simpler, more forgiving

    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (selected == null) "New Order" else "Edit Order #${selected.id}",
            style = MaterialTheme.typography.titleLarge
        )

        // Animal dropdown
        ExposedDropdownMenuBox(
            expanded = expandedAnimal,
            onExpandedChange = { expandedAnimal = !expandedAnimal },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = animalName.ifBlank { "None" },
                onValueChange = {},
                label = { Text("Animal") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedAnimal) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedAnimal,
                onDismissRequest = { expandedAnimal = false }
            ) {
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        animalId = null
                        expandedAnimal = false
                    }
                )
                animals.forEach { a ->
                    DropdownMenuItem(
                        text = { Text(a.name) },
                        onClick = {
                            animalId = a.animalId
                            expandedAnimal = false
                        }
                    )
                }
            }
        }

        // Buyer dropdown
        ExposedDropdownMenuBox(
            expanded = expandedBuyer,
            onExpandedChange = { expandedBuyer = !expandedBuyer },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = buyerName.ifBlank { "None" },
                onValueChange = {},
                label = { Text("Buyer") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedBuyer) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedBuyer,
                onDismissRequest = { expandedBuyer = false }
            ) {
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        customerId = null
                        expandedBuyer = false
                    }
                )
                customers.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c.name) },
                        onClick = {
                            customerId = c.customerId
                            expandedBuyer = false
                        }
                    )
                }
            }
        }

        // Deposit
        OutlinedTextField(
            value = depositText,
            onValueChange = { newText ->
                val cleaned = newText.replace(Regex("[^\\d.]"), "")
                if (cleaned.count { it == '.' } <= 1) {
                    val parsed = cleaned.toDoubleOrNull()
                    if (parsed == null || parsed >= 0.0) {
                        depositText = cleaned
                    }
                }
            },
            label = { Text("Deposit") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        // Payment
        OutlinedTextField(
            value = paymentText,
            onValueChange = { newText ->
                val cleaned = newText.replace(Regex("[^\\d.]"), "")
                if (cleaned.count { it == '.' } <= 1) {
                    val parsed = cleaned.toDoubleOrNull()
                    if (parsed == null || parsed >= 0.0) {
                        paymentText = cleaned
                    }
                }
            },
            label = { Text("Payment") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        ReadyDateField(dob = readyDate, onDateSelected = { readyDate = it })

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = purchaseComplete,
                onCheckedChange = { checked -> purchaseComplete = checked }
            )
            Text(text = "Purchase completed?")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(200.dp)
        ) {
            Button(
                enabled = canSave,
                onClick = {
                    scope.launch {
                        val deposit = depositText.toDoubleOrNull() ?: 0.0
                        val payment = paymentText.toDoubleOrNull() ?: 0.0
                        onSave(
                            animalId,
                            customerId,
                            deposit,
                            payment,
                            readyDate.trim(),
                            purchaseComplete
                        )
                        onAfterSaveRefresh()
                    }
                }
            ) { Text(if (selected == null) "Create" else "Save") }

            OutlinedButton(onClick = onNew) { Text("New") }
        }
    }
}