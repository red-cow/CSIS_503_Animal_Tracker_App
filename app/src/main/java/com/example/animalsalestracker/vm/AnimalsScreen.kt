package com.example.animalsalestracker.vm

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.animalsalestracker.data.Animal   // adjust if yours differs
import com.example.animalsalestracker.data.AppDatabase
import com.example.animalsalestracker.data.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimalsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember(context) { AppDatabase.get(context) }

    val animalDao = db.animalDao()
    val customerDao = db.customerDao()

    // Flow → State
    val animals by animalDao.getAll().collectAsState(initial = emptyList())
    val customers by customerDao.getAll().collectAsState(initial = emptyList())

    var selected by remember { mutableStateOf<Animal?>(null) }

    LaunchedEffect(animals) {
        selected = selected?.let { sel -> animals.find { it.animalId == sel.animalId } }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // LEFT: single column list (< 50% width)
        AnimalsGallery(
            animals = animals,
            onAnimalClick = { a -> selected = a },
            modifier = Modifier
                .weight(0.45f)   // less than half
                .fillMaxHeight()
        )

        // RIGHT: form (remaining width)
        AnimalForm(
            selected = selected,
            customers = customers,
            onSave = { name, type, dob, weight, buyerId ->
                // Insert if nothing selected; otherwise update the selected row
                if (selected == null) {
                    // create
                    val newId = withContext(Dispatchers.IO) {

                        animalDao.upsert(Animal(name=name, type=type, dob=dob, weight=weight, buyerId=buyerId))
                    }
                    // set selection to the newly inserted animal
                    selected = Animal(newId, name, type, dob, weight, buyerId)
                } else {
                    // update
                    withContext(Dispatchers.IO) {
                        animalDao.update( Animal(
                            animalId = selected!!.animalId,
                            name = name,
                            type = type,
                            dob = dob,
                            weight = weight//,
                            //buyerId = buyerId
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
/** --- Extracted gallery: single-column list --- */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimalsGallery(
    animals: List<Animal>,
    onAnimalClick: (Animal) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 12.dp)
        ) {
            items(animals, key = { it.animalId }) { a ->
                AnimalCard(a, onClick = onAnimalClick)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimalCard(a: Animal, onClick: (Animal) -> Unit) {
    val age = calculateAgeFromDob(a.dob)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(a) }
    ){
        Column(Modifier.padding(12.dp)) {
            Text(
                text = a.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = a.type,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text =  "Age: ${age ?: "Unknown"}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun DateOfBirthField(dob: String, onDateSelected: (String) -> Unit) {
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
        label = { Text("D.O.B") },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Pick date")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateAgeFromDob(dobString: String?): Int? {
    if (dobString.isNullOrBlank()) return null
    return try {
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val birthDate = LocalDate.parse(dobString, formatter)
        val today = LocalDate.now()
        Period.between(birthDate, today).years
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalForm(
    selected: Animal?,
    customers: List<Customer>,
    onSave: suspend (name: String, type: String, dob: String, weight: Double, buyerIdText: Long?) -> Unit,
    onAfterSaveRefresh: () -> Unit,
    onNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }

   // var weight by remember { mutableDoubleStateOf(0.0) }
    var weightText by remember { mutableStateOf("")}

    var buyerId by remember { mutableStateOf<Long?>(null) }      // blank means NULL
    var expanded by remember { mutableStateOf(false) }

    // compute current label from id
    val buyerName: String = remember(buyerId, customers) {
        customers.firstOrNull { it.customerId == buyerId }?.name ?: ""
    }

    // Populate fields when selection changes
    LaunchedEffect(selected?.animalId) {
        if (selected == null) {
            name = ""
            type = ""
            dob = ""
            weightText = ""
            buyerId = null
        } else {
            name = selected.name
            type = selected.type
            dob = selected.dob
            weightText = selected.weight.toString()
            buyerId = selected.buyerId//?.toString() ?: ""
        }
    }

    val scope = rememberCoroutineScope()
    val canSave = name.isNotBlank() && type.isNotBlank() && dob.isNotBlank() && weightText.toDoubleOrNull() != null

    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (selected == null) "New Animal" else "Editing ${selected.name}",
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
            value = type,
            onValueChange = { type = it },
            label = { Text("Type") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        DateOfBirthField(dob = dob, onDateSelected = { dob = it })

        OutlinedTextField(
            value = weightText,
            onValueChange = { newText ->
                val cleaned = newText.replace(Regex("[^\\d.]"), "")
                if (cleaned.count { it == '.' } <= 1) {
                    // Parse and enforce non-negative rule
                    val parsed = cleaned.toDoubleOrNull()
                    if (parsed == null || parsed >= 0.0) {
                        weightText = cleaned
                    }
                }
            },
            label = { Text("Weight: lb") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = buyerName.ifBlank { "None" },
                onValueChange = {}, // read-only; selection via menu
                label = { Text("Buyer (optional)") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // Option: None
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        buyerId = null
                        expanded = false
                    }
                )
                // All customers by name
                customers.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c.name) },
                        onClick = {
                            buyerId = c.customerId     // ← save id
                            expanded = false
                        }
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(200.dp)) {
            Button(
                enabled = canSave,
                onClick = {
                    scope.launch {
                        val weight = weightText.toDoubleOrNull() ?: 0.0  // ← parse here
                        onSave(name.trim(), type.trim(), dob.trim(), weight, buyerId)
                        onAfterSaveRefresh()
                    }
                }
            ) { Text(if (selected == null) "Create" else "Save") }

            OutlinedButton(onClick = onNew) { Text("New") }
        }
    }
}