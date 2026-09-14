package com.example.powerusagelog

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class Feeder(
    val id: Int,
    val name: String,
    val description: String,
    val power: Double,
    val pf: Double,
    val usage: Double,
    val used: Boolean = true
)

data class Reading(
    val feederId: Int,
    val feederName: String,
    val date: String,
    val time: String,
    val power: Double,
    val pf: Double,
    val usage: Double,
    val notes: String
)

private val defaultFeeders = listOf(
    Feeder(1, "Feeder 1", "Production Line 1", 125.6, .96, 362.4),
    Feeder(2, "Feeder 2", "Production Line 2", 98.4, .92, 281.7),
    Feeder(3, "Feeder 3", "Compressors", 72.8, .95, 198.3),
    Feeder(4, "Feeder 4", "HVAC", 56.3, .90, 147.6),
    Feeder(5, "Feeder 5", "Lighting", 18.7, .98, 96.1),
    Feeder(6, "Feeder 6", "Water Pump", 44.9, .94, 123.2),
    Feeder(7, "Feeder 7", "Workshop", 32.5, .91, 88.7),
    Feeder(8, "Feeder 8", "Office Building", 28.1, .93, 76.5),
    Feeder(9, "Feeder 9", "Warehouse", 41.2, .96, 112.4),
    Feeder(10, "Feeder 10", "Spare / Other", 0.0, 0.0, 0.0, false)
)

private val Navy = Color(0xFF172033)
private val Blue = Color(0xFF1976D2)
private val Green = Color(0xFF2E9D63)
private val LightBg = Color(0xFFF5F7FA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PowerUsageLogApp(applicationContext) }
    }
}

@Composable
fun PowerUsageLogApp(context: Context) {
    var screen by remember { mutableStateOf("dashboard") }
    var selected by remember { mutableStateOf(defaultFeeders.first()) }
    val store = remember { ReadingStore(context) }
    var readings by remember { mutableStateOf(store.load()) }
    var showSuccess by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Blue,
            background = LightBg,
            surface = Color.White
        )
    ) {
        when (screen) {
            "dashboard" -> DashboardScreen(
                feeders = defaultFeeders,
                onFeeder = { selected = it; screen = "detail" },
                onLogs = { screen = "logs" }
            )
            "detail" -> DetailScreen(
                feeder = selected,
                onBack = { screen = "dashboard" },
                onSave = { reading ->
                    store.add(reading)
                    readings = store.load()
                    showSuccess = true
                },
                showSuccess = showSuccess,
                onDashboard = { showSuccess = false; screen = "dashboard" }
            )
            "logs" -> LogsScreen(
                readings = readings,
                onBack = { screen = "dashboard" }
            )
        }
    }
}

@Composable
fun Header(title: String, subtitle: String = "", back: (() -> Unit)? = null) {
    Surface(color = Navy) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (back != null) {
                IconButton(onClick = back) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
            } else {
                Icon(Icons.Default.Bolt, null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                if (subtitle.isNotBlank()) Text(subtitle, color = Color(0xFFD6DEEA), fontSize = 12.sp)
            }
            Icon(Icons.Default.MoreVert, null, tint = Color.White)
        }
    }
}

@Composable
fun InfoStrip() {
    val now = Date()
    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(now)
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            InfoItem(Icons.Default.CalendarMonth, "Date", date)
            InfoItem(Icons.Default.Schedule, "Time", time)
            InfoItem(Icons.Default.Person, "Recorded by", "Operator")
            InfoItem(Icons.Default.Factory, "Plant / Area", "Main Plant")
        }
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(min = 70.dp)) {
        Icon(icon, null, tint = Blue, modifier = Modifier.size(20.dp))
        Text(label, fontSize = 9.sp, color = Color.Gray)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DashboardScreen(
    feeders: List<Feeder>,
    onFeeder: (Feeder) -> Unit,
    onLogs: () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(true, { }, icon = { Icon(Icons.Default.Dashboard, null) }, label = { Text("Dashboard") })
                NavigationBarItem(false, onLogs, icon = { Icon(Icons.Default.Description, null) }, label = { Text("View Logs") })
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().background(LightBg).padding(padding)) {
            Header("Power Usage Log", "Plant Electrical Monitoring")
            InfoStrip()

            Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Total Usage", "1,482.6 kWh", Icons.Default.Bolt, Modifier.weight(1f))
                StatCard("Average PF", "0.93", Icons.Default.Speed, Modifier.weight(1f))
                StatCard("Current Power", "298.4 kW", Icons.Default.ElectricBolt, Modifier.weight(1f))
                StatCard("Feeders", "10", Icons.Default.Warning, Modifier.weight(1f))
            }

            Row(
                Modifier.fillMaxWidth().padding(16.dp, 18.dp, 16.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Feeders", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Tap a feeder to enter / update readings", fontSize = 12.sp, color = Color.Gray)
                }
                AssistChip(onClick = {}, label = { Text("All feeders ready", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = Green) })
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                items(feeders) { feeder ->
                    FeederRow(feeder, onFeeder)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier
) {
    Card(modifier = modifier.height(86.dp), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(9.dp)) {
            Icon(icon, null, tint = Blue, modifier = Modifier.size(19.dp))
            Text(title, fontSize = 9.sp, color = Color.Gray)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FeederRow(feeder: Feeder, onClick: (Feeder) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(feeder) },
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(
                    if (feeder.used) Blue else Color.Gray
                ),
                contentAlignment = Alignment.Center
            ) {
                Text("${feeder.id}", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(feeder.name, fontWeight = FontWeight.Bold)
                Text(feeder.description, fontSize = 11.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(72.dp)) {
                Text("Power", fontSize = 9.sp, color = Color.Gray)
                Text("${feeder.power} kW", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(48.dp)) {
                Text("PF", fontSize = 9.sp, color = Color.Gray)
                Text(String.format(Locale.US, "%.2f", feeder.pf), fontSize = 12.sp)
            }
            Icon(
                if (feeder.used) Icons.Default.CheckCircle else Icons.Default.RemoveCircle,
                null,
                tint = if (feeder.used) Green else Color.Gray,
                modifier = Modifier.padding(start = 6.dp)
            )
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

@Composable
fun DetailScreen(
    feeder: Feeder,
    onBack: () -> Unit,
    onSave: (Reading) -> Unit,
    showSuccess: Boolean,
    onDashboard: () -> Unit
) {
    var power by remember(feeder.id) { mutableStateOf(feeder.power.toString()) }
    var pf by remember(feeder.id) { mutableStateOf(feeder.pf.toString()) }
    var usage by remember(feeder.id) { mutableStateOf(feeder.usage.toString()) }
    var notes by remember { mutableStateOf("") }

    val now = Date()
    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(now)
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)

    val powerValue = power.toDoubleOrNull()
    val pfValue = pf.toDoubleOrNull()
    val usageValue = usage.toDoubleOrNull()

    val powerError = powerValue == null || powerValue < 0
    val pfError = pfValue == null || pfValue < 0 || pfValue > 1
    val usageError = usageValue == null || usageValue < 0
    val valid = !powerError && !pfError && !usageError

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(true, onDashboard, icon = { Icon(Icons.Default.Dashboard, null) }, label = { Text("Dashboard") })
                NavigationBarItem(false, {}, icon = { Icon(Icons.Default.Edit, null) }, label = { Text("Reading") })
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().background(LightBg).padding(padding)) {
            Header("${feeder.name} – Enter Readings", back = onBack)

            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(48.dp).clip(CircleShape).background(Blue),
                                contentAlignment = Alignment.Center
                            ) { Text("${feeder.id}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(feeder.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(feeder.description, color = Color.Gray)
                            }
                            AssistChip(onClick = {}, label = { Text(if (feeder.used) "OK" else "Not used") })
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryTile("Current Power", "${feeder.power} kW", Modifier.weight(1f))
                        SummaryTile("Power Factor", String.format(Locale.US, "%.2f", feeder.pf), Modifier.weight(1f))
                        SummaryTile("Total Usage", "${feeder.usage} kWh", Modifier.weight(1f))
                    }
                }

                item { ReadOnlyField("Date", date, Icons.Default.CalendarMonth) }
                item { ReadOnlyField("Time", time, Icons.Default.Schedule) }

                item {
                    NumberField(
                        label = "Power (kW)",
                        value = power,
                        onValue = { power = it },
                        error = powerError,
                        errorText = if (powerValue == null) "Enter a number." else "Power must be a positive number.",
                        icon = Icons.Default.Bolt
                    )
                }

                item {
                    NumberField(
                        label = "Power Factor",
                        value = pf,
                        onValue = { pf = it },
                        error = pfError,
                        errorText = "Power factor must be between 0.00 and 1.00.",
                        icon = Icons.Default.Speed
                    )
                }

                item {
                    NumberField(
                        label = "Total Usage (kWh)",
                        value = usage,
                        onValue = { usage = it },
                        error = usageError,
                        errorText = "Total usage must be zero or greater.",
                        icon = Icons.Default.ElectricBolt
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Notes (Optional)") },
                        placeholder = { Text("e.g. normal operation") },
                        leadingIcon = { Icon(Icons.Default.Notes, null) },
                        minLines = 2
                    )
                }

                item {
                    if (showSuccess) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F6EC))
                        ) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = Green)
                                Column(Modifier.padding(start = 10.dp)) {
                                    Text("Reading saved successfully!", fontWeight = FontWeight.Bold)
                                    Text("$date $time", fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                if (valid) {
                                    onSave(
                                        Reading(
                                            feeder.id, feeder.name, date, time,
                                            powerValue!!, pfValue!!, usageValue!!, notes
                                        )
                                    )
                                }
                            },
                            enabled = valid,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save Reading", fontSize = 16.sp)
                        }
                    }
                }

                if (showSuccess) {
                    item {
                        Button(
                            onClick = onDashboard,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Back to Dashboard") }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryTile(label: String, value: String, modifier: Modifier) {
    Card(modifier = modifier.height(76.dp), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(8.dp)) {
            Text(label, fontSize = 9.sp, color = Color.Gray)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReadOnlyField(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        trailingIcon = { Icon(Icons.Default.ChevronRight, null) }
    )
}

@Composable
fun NumberField(
    label: String,
    value: String,
    onValue: (String) -> Unit,
    error: Boolean,
    errorText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = error,
        supportingText = if (error) ({ Text(errorText) }) else null,
        singleLine = true
    )
}

@Composable
fun LogsScreen(readings: List<Reading>, onBack: () -> Unit) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(false, onBack, icon = { Icon(Icons.Default.Dashboard, null) }, label = { Text("Dashboard") })
                NavigationBarItem(true, {}, icon = { Icon(Icons.Default.Description, null) }, label = { Text("View Logs") })
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().background(LightBg).padding(padding)) {
            Header("View Logs", "Saved readings", back = onBack)
            if (readings.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No readings have been saved yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(readings.reversed()) { r ->
                        Card(shape = RoundedCornerShape(14.dp)) {
                            Column(Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(r.feederName, fontWeight = FontWeight.Bold)
                                        Text("${r.date} ${r.time}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Icon(Icons.Default.CheckCircle, null, tint = Green)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Power: ${r.power} kW    PF: ${String.format(Locale.US, "%.2f", r.pf)}    Usage: ${r.usage} kWh")
                                if (r.notes.isNotBlank()) Text("Notes: ${r.notes}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

class ReadingStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("power_usage_log", Context.MODE_PRIVATE)
    private val key = "readings"

    fun load(): List<Reading> {
        val raw = prefs.getString(key, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val result = mutableListOf<Reading>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            result += Reading(
                o.getInt("feederId"),
                o.getString("feederName"),
                o.getString("date"),
                o.getString("time"),
                o.getDouble("power"),
                o.getDouble("pf"),
                o.getDouble("usage"),
                o.optString("notes", "")
            )
        }
        return result
    }

    fun add(reading: Reading) {
        val arr = JSONArray()
        load().forEach { r ->
            arr.put(JSONObject().apply {
                put("feederId", r.feederId)
                put("feederName", r.feederName)
                put("date", r.date)
                put("time", r.time)
                put("power", r.power)
                put("pf", r.pf)
                put("usage", r.usage)
                put("notes", r.notes)
            })
        }
        arr.put(JSONObject().apply {
            put("feederId", reading.feederId)
            put("feederName", reading.feederName)
            put("date", reading.date)
            put("time", reading.time)
            put("power", reading.power)
            put("pf", reading.pf)
            put("usage", reading.usage)
            put("notes", reading.notes)
        })
        prefs.edit().putString(key, arr.toString()).apply()
    }
}
