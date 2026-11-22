package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MyApplicationTheme
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

// Stateful state holder for our theological research engine
class BibleStudyState(context: Context) {
    val prefs = context.getSharedPreferences("belajar_alkitab_prefs", Context.MODE_PRIVATE)

    var inputPassage by mutableStateOf("Yohanes 3:16")
    var isLoading by mutableStateOf(false)
    var isSendingSheet by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var sheetResponseStatus by mutableStateOf<String?>(null)

    // Scripture state
    var parsedReference by mutableStateOf("Yohanes 3:16")
    var scriptureTb by mutableStateOf("Karena begitu besar kasih Allah akan dunia ini, sehingga Ia telah mengaruniakan Anak-Nya yang tunggal, supaya setiap orang yang percaya kepada-Nya tidak binasa, melainkan beroleh hidup yang kekal.")
    var scriptureKjv by mutableStateOf("For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.")

    // Deep analyses
    var analysisResult by mutableStateOf<AnalysisResult?>(
        AnalysisResult(
            timeline = "Kitab Injil Yohanes diperkirakan ditulis antara tahun 85–95 Masehi di kota Efesus oleh Rasul Yohanes, murid yang dikasihi Yesus, pada masa tua setelah pembuangan di pulau Patmos.",
            komparasi_manuskrip = "Ayat ini sangat kokoh dan konsisten di seluruh manuskrip Yunani kuno utama (Codex Sinaiticus, Codex Vaticanus, Codex Alexandrinus, P66, dan P75). Tidak ditemukan variasi tekstual signifikan yang memengaruhi esensi doktrin penyelamatan.",
            sejarah_latar_belakang = "Yesus mengucapkan ayat ini saat berbicara di malam hari dengan Nikodemus, seorang pengajar Israel dan anggota Mahkamah Agama Yahudi (Sanhedrin). Nikodemus mewakili legalisme Yahudi yang kaku yang perlu lahir baru oleh Roh.",
            analogi_modern = "Bayangkan seorang penemu mesin yang luar biasa mengorbankan bagian paling berharga dari dirinya agar ciptaannya yang rusak tidak didaur ulang menjadi sia-sia, melainkan dipulihkan seutuhnya.",
            bahasa_asli_data = listOf(
                OriginalLanguageWord("begitu besar kasih", "so loved", "Agapao (ἀγαπάω)", "G25", "Kasih ilahi yang tulus, aktif, rela berkorban demi masa depan objek yang dikasihi."),
                OriginalLanguageWord("Anak-Nya yang tunggal", "his only begotten Son", "Monogenes (μονογενής)", "G3439", "Satu-satunya yang unik dari jenis-Nya, menegaskan hubungan esensi ilahi yang setara dengan Bapa."),
                OriginalLanguageWord("tidak binasa", "should not perish", "Apollymi (ἀπόλλυμι)", "G622", "Mengalami kehancuran total secara rohani/kekal akibat keterpisahan dari Allah."),
                OriginalLanguageWord("hidup yang kekal", "everlasting life", "Zoe Aionios (ζωὴ αἰώνιος)", "G2222 G166", "Kehidupan rohani ilahi berkulitas tinggi yang dimulai dari sekarang hingga kekekalan.")
            ),
            kristosentris = "Ayat ini adalah jantung Kristologi alkitabiah. Yohanes memperlihatkan bahwa Allah menuntut keadilan, namun mematuhinya secara pribadi melalui penyerahan Kristus di atas salib sebagai persembahan pendamaian murni.",
            cross_reference = "Roma 5:8 (Allah menyatakan kasih-Nya kepada kita ketika kita masih berdosa), 1 Yohanes 4:9 (Kasih Allah dinyatakan dengan mengutus Anak Tunggal-Nya), Efesus 2:8-9 (Diselamatkan oleh anugerah keselamatan melalui iman).",
            apologetika_corner = "Melawan bidaah kuno Gnostisisme yang menolak kemanusiaan sejati Yesus, serta menjawab skeptisisme ateis modern yang menganggap Allah Perjanjian Baru kejam. Yohanes menegaskan motif utama tindakan Allah adalah KASIH yang melampaui akal.",
            renungan_praktis = "Ingatlah bahwa nilai jiwamu di mata Allah sangat tinggi—seharga darah Kristus. Hiduplah hari ini dalam kepastian keselamatan murni, bukan dalam kecemasan perbuatan hukum taurat."
        )
    )

    // Configuration preferences
    var geminiApiKey by mutableStateOf(prefs.getString("gemini_api_key", "") ?: "")
    var googleSheetUrl by mutableStateOf(prefs.getString("google_sheet_url", "") ?: "")
    var spreadsheetUrl by mutableStateOf(prefs.getString("spreadsheet_url", "") ?: "")
    var bibleApiKey by mutableStateOf(prefs.getString("bible_api_key", "vqe_K9Z4-3HV6S2HaMFQM") ?: "vqe_K9Z4-3HV6S2HaMFQM")

    // Database repository
    val offlineRepo = OfflineBibleRepository(context)

    // UI Navigation State
    var selectedTab by mutableIntStateOf(0)
    var showSettingsDialog by mutableStateOf(false)

    fun savePrefs() {
        prefs.edit().apply {
            putString("gemini_api_key", geminiApiKey)
            putString("google_sheet_url", googleSheetUrl)
            putString("spreadsheet_url", spreadsheetUrl)
            putString("bible_api_key", bibleApiKey)
            apply()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val state = remember { BibleStudyState(context) }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    sendToGoogleSheets(context, state, scope)
                },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .testTag("save_to_sheet_fab")
            ) {
                if (state.isSendingSheet) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Simpan ke Google Sheet"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header & Search Inputs Area remains FIXED at the top.
            HeaderSection(state = state, scope = scope)

            Spacer(modifier = Modifier.height(10.dp))

            // Switchable Theology Lens Tabs remain FIXED under the header for ease-of-tab-switching.
            TheologyTabs(state = state)

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Membedah Teks Murni & Konsultasi Teologi IA...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Langkah 1: Mengunduh teks murni. Langkah 2: Proses eksegetis teologis...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                // This whole viewport scrolls under the tabs! The ScriptureSection will scroll up and out of view,
                // giving the user maximum possible vertical space to read the detailed tabs content!
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 0.dp)
                ) {
                    // Main Verse Card - placed inside the scrollable container.
                    ScriptureSection(state = state)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Active Tab content without their own internal scrolling.
                    AnimatedContent(
                        targetState = state.selectedTab,
                        transitionSpec = {
                            fadeIn() + slideInVertically { it / 3 } with fadeOut() + slideOutVertically { it / 3 }
                        },
                        label = "TabContentAnimation"
                    ) { tabIndex ->
                        val result = state.analysisResult
                        if (result == null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tidak Ada Data Bedahan Ayat",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Ketik rentang ayat di atas lalu klik Bedah Ayat.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                when (tabIndex) {
                                    0 -> ContextTab(result)
                                    1 -> LanguageTab(result)
                                    2 -> ChristocentricTab(result)
                                    3 -> ApologeticsTab(result)
                                    4 -> DevotionalTab(result)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp)) // generous bottom padding inside the scrollview
                }
            }
        }
    }

    // Toggleable Settings Dialog
    if (state.showSettingsDialog) {
        SettingsDialog(state = state)
    }

    // Floating notification for google sheet statuses
    state.sheetResponseStatus?.let { msg ->
        LaunchedEffect(msg) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            state.sheetResponseStatus = null
        }
    }

    // Floating notifications for errors
    state.errorMessage?.let { err ->
        AlertDialog(
            onDismissRequest = { state.errorMessage = null },
            confirmButton = {
                TextButton(onClick = { state.errorMessage = null }) {
                    Text("OK")
                }
            },
            title = { Text("Pemberitahuan Sistem", fontWeight = FontWeight.Bold) },
            text = { Text(err) },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) }
        )
    }
}

@Composable
fun HeaderSection(state: BibleStudyState, scope: CoroutineScope) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        shadowElevation = 5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Belajar Alkitab Pro",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Teologi Ortodoks & Eksegesis Sastra Terpercaya",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(
                    onClick = { state.showSettingsDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .testTag("settings_button")
                        .size(40.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Pengaturan",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.inputPassage,
                    onValueChange = { state.inputPassage = it },
                    placeholder = { Text("Matius 28:19 atau Roma 8:28") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bible_input_field"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                )

                Button(
                    enabled = !state.isLoading,
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        triggerDeepAnalysis(context, state, scope)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("analyze_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Bedah",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScriptureSection(state: BibleStudyState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with badge and source
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.09f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AYAT UTAMA (TB & KJV)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Ref: ${state.parsedReference}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
            )

            // Indonesian TB Edition and English KJV stacked in a clean modern way
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "TB (Terjemahan Baru)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.scriptureTb,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "KJV (King James Version)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.scriptureKjv,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 22.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
fun TheologyTabs(state: BibleStudyState) {
    val items = listOf("Konteks", "Bahasa", "Kristus", "Apologetika", "Renungan")
    val icons = listOf(
        Icons.Default.Language,
        Icons.Default.SignLanguage,
        Icons.Default.Favorite,
        Icons.Default.Shield,
        Icons.Default.Lightbulb
    )

    ScrollableTabRow(
        selectedTabIndex = state.selectedTab,
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {},
        indicator = {}, // Hide the legacy underlay line for a modern pill aesthetic
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEachIndexed { index, title ->
            val selected = state.selectedTab == index
            Tab(
                selected = selected,
                onClick = { state.selectedTab = index },
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    )
                    .testTag("tab_$index"),
                text = {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = null,
                            tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = title,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                }
            )
        }
    }
}

// --- Dynamic Content Tab Displays ---

@Composable
fun ContextTab(result: AnalysisResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LensCard(
            title = "Peta Garis Waktu Teologis (Timeline)",
            description = result.timeline,
            icon = Icons.Default.HourglassBottom,
            tint = MaterialTheme.colorScheme.primary
        )

        LensCard(
            title = "Sejarah & Latar Belakang Kebudayaan",
            description = result.sejarah_latar_belakang,
            icon = Icons.Default.HistoryEdu,
            tint = MaterialTheme.colorScheme.secondary
        )

        LensCard(
            title = "Komparasi Kritis Manuskrip Purba",
            description = result.komparasi_manuskrip,
            icon = Icons.Default.MenuBook,
            tint = MaterialTheme.colorScheme.tertiary
        )

        LensCard(
            title = "Visualisasi & Analogi Abad Ke-21",
            description = result.analogi_modern,
            icon = Icons.Default.TrendingUp,
            tint = MaterialTheme.colorScheme.error,
            isItalic = true
        )
    }
}

@Composable
fun LanguageTab(result: AnalysisResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = "Analisis Bahasa Asli (Lexicon & Sastra)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (result.bahasa_asli_data.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Gagal memproses kata asli. Gunakan ayat yang valid.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            result.bahasa_asli_data.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.kataAsli,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.kataTb.ifEmpty { item.kataKjv },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (item.kataTb.isNotEmpty() && item.kataKjv.isNotEmpty()) {
                                        Text(
                                            text = "• En: ${item.kataKjv}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }

                            // Strong's ID Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = item.strongId,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                        )

                        Text(
                            text = "Ulasan Linguistik & Teologis:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            text = item.makna,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChristocentricTab(result: AnalysisResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LensCard(
            title = "Fokus Kristosentris & Janji Penebusan",
            description = result.kristosentris,
            icon = Icons.Default.AllInclusive,
            tint = MaterialTheme.colorScheme.primary
        )

        LensCard(
            title = "Sistem Referensi Silang Ayat Alkitab (Scriptures)",
            description = result.cross_reference,
            icon = Icons.Default.CompareArrows,
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun ApologeticsTab(result: AnalysisResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        LensCard(
            title = "Apologetika Corner (Pertahanan Iman)",
            description = result.apologetika_corner,
            icon = Icons.Default.Shield,
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun DevotionalTab(result: AnalysisResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        LensCard(
            title = "Aplikasi Praktis Sehari-hari (Praktika)",
            description = result.renungan_praktis,
            icon = Icons.Default.SelfImprovement,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun LensCard(
    title: String,
    description: String,
    icon: ImageVector,
    tint: Color,
    isItalic: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tint
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.95f)
            )
        }
    }
}

// --- Toggleable Configuration preference setup Dialog ---

@Composable
fun SettingsDialog(state: BibleStudyState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var apiKeyInput by remember { mutableStateOf(state.geminiApiKey) }
    var webAppUrlInput by remember { mutableStateOf(state.googleSheetUrl) }
    var spreadsheetUrlInput by remember { mutableStateOf(state.spreadsheetUrl) }
    var bibleApiKeyInput by remember { mutableStateOf(state.bibleApiKey) }
    var obscureKey by remember { mutableStateOf(true) }
    var obscureBibleKey by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = { state.showSettingsDialog = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Konfigurasi Sistem",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { state.showSettingsDialog = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                // Gemini API Configuration
                Column {
                    Text(
                        text = "1. Kunci API Gemini (Google AI Studio)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Jika kosong, aplikasi akan menggunakan Kunci Rahasia Sistem agar GRATIS SELAMANYA.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        placeholder = { Text("Masukkan API Key pribadi (Opsional)") },
                        visualTransformation = if (obscureKey) PasswordVisualTransformation() else VisualTransformation.None,
                        trailingIcon = {
                            IconButton(onClick = { obscureKey = !obscureKey }) {
                                Icon(
                                    imageVector = if (obscureKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                               )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("api_key_input")
                    )
                }

                // Apps Script Deployment Configuration
                Column {
                    Text(
                        text = "2. URL Apps Script Pelayan Spreadsheet",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "URL Web App dari Apps Script Anda setelah di-deploy (berakhir dengan '/exec').",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = webAppUrlInput,
                        onValueChange = { webAppUrlInput = it },
                        placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth().testTag("apps_script_url_input")
                    )
                }

                // Spreadsheet Specific URL Key Configuration
                Column {
                    Text(
                        text = "3. URL Google Spreadsheet Sasaran",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Tautan penuh Spreadsheet tempat Anda ingin menyimpan seluruh catatan bedah teologi Anda.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = spreadsheetUrlInput,
                        onValueChange = { spreadsheetUrlInput = it },
                        placeholder = { Text("https://docs.google.com/spreadsheets/d/...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth().testTag("spreadsheet_url_input")
                    )
                }

                // API.Bible Configuration Key Configuration
                Column {
                    Text(
                        text = "4. Kunci API Alkitab (api.bible)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Digunakan untuk mengunduh versi NKJV/KJV Alkitab dari rest.api.bible secara tepercaya.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = bibleApiKeyInput,
                        onValueChange = { bibleApiKeyInput = it },
                        placeholder = { Text("Masukkan API Key Alkitab anda") },
                        visualTransformation = if (obscureBibleKey) PasswordVisualTransformation() else VisualTransformation.None,
                        trailingIcon = {
                            IconButton(onClick = { obscureBibleKey = !obscureBibleKey }) {
                                Icon(
                                    imageVector = if (obscureBibleKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("bible_api_key_input")
                    )
                }

                // Local Database Cache Configuration
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "5. Database Penyimpanan Luring (Offline)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Setiap ayat TB (Terjemahan Baru) dan KJV/NKJV yang pernah dibuka diunduh dan tertanam permanen di database lokal agar instan dibuka (Offline total!).",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                state.offlineRepo.clearCache()
                                Toast.makeText(context, "Database cache lokal dibersihkan!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kosongkan Cache Luring Alkitab", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { state.showSettingsDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            state.geminiApiKey = apiKeyInput.trim()
                            state.googleSheetUrl = webAppUrlInput.trim()
                            state.spreadsheetUrl = spreadsheetUrlInput.trim()
                            state.bibleApiKey = bibleApiKeyInput.trim()
                            state.savePrefs()
                            state.showSettingsDialog = false
                            Toast.makeText(context, "Semua konfigurasi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f).testTag("save_settings_btn")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}


// --- API Call Execution Controllers // --- API Call Execution Controllers (Coordinating steps 1, 2, and Sheets) ---

// Helper function to safely fetch from api.bible
suspend fun fetchFromApiBible(bibleId: String, passageId: String, apiKey: String): String? {
    return try {
        val response = withContext(Dispatchers.IO) {
            NetworkClient.apiBibleService.getPassage(
                bibleId = bibleId,
                passageId = passageId,
                apiKey = apiKey
            )
        }
        val rawText = response.data?.content
        if (rawText != null) {
            // Strip any HTML tags that api.bible sometimes wraps in paragraph/span nodes
            rawText.replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim()
        } else {
            null
        }
    } catch (e: Exception) {
        android.util.Log.e("BibleLens", "Gagal mengunduh dari api.bible ($bibleId): ${e.message}")
        null
    }
}

// Helper function to safely fetch from bolls.life (Bolls API)
suspend fun fetchFromBolls(translation: String, input: String): String? {
    return try {
        val parsed = NetworkClient.parsePassage(input) ?: return null
        val verses = withContext(Dispatchers.IO) {
            NetworkClient.bollsBibleService.getChapter(
                translation = translation,
                book = parsed.bookId,
                chapter = parsed.chapter
            )
        }
        if (verses.isEmpty()) return null
        
        // Filter by verse range if present
        val filtered = if (parsed.startVerse != null) {
            val end = parsed.endVerse ?: parsed.startVerse
            verses.filter { it.verse in parsed.startVerse..end }
        } else {
            verses
        }
        
        if (filtered.isEmpty()) return null
        
        val showVerseNumbers = filtered.size > 1
        filtered.joinToString(" ") { verse ->
            val cleanText = verse.text
                .replace(Regex("<[^>]*>"), " ") // Strip HTML tags
                .replace(Regex("\\s+"), " ")    // Normalize whitespace
                .trim()
            if (showVerseNumbers) "(${verse.verse}) $cleanText" else cleanText
        }.trim()
    } catch (e: Exception) {
        android.util.Log.e("BibleLens", "Gagal mengunduh dari bolls.life ($translation): ${e.message}")
        null
    }
}

fun triggerDeepAnalysis(context: Context, state: BibleStudyState, scope: CoroutineScope) {
    if (state.inputPassage.trim().isEmpty()) {
        state.errorMessage = "Silakan masukkan rentang ayat Alkitab terlebih dahulu! Contoh: Yohanes 3:16."
        return
    }

    state.isLoading = true
    state.errorMessage = null

    scope.launch {
        try {
            val normalizedQuery = state.offlineRepo.normalizeQuery(state.inputPassage)
            val cachedEntry = withContext(Dispatchers.IO) {
                state.offlineRepo.getCache(normalizedQuery)
            }

            // OPTION A: Fully analyzed theological study already exists in local luring (offline) SQLite DB!
            if (cachedEntry != null && !cachedEntry.analysisJson.isNullOrBlank()) {
                val parsedResult = createSafeGson().fromJson(cachedEntry.analysisJson, AnalysisResult::class.java)
                state.parsedReference = cachedEntry.reference
                state.scriptureTb = cachedEntry.textTb
                state.scriptureKjv = cachedEntry.textKjv
                state.analysisResult = parsedResult
                state.selectedTab = 0
                state.isLoading = false
                
                Toast.makeText(
                    context, 
                    "Memuat analisis '${cachedEntry.reference}' dari penyimpanan offline (Instan!).", 
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            // OPTION B: Scripture texts exist in local offset, or we obtain them and analyze with Gemini
            var tbResultText = cachedEntry?.textTb ?: ""
            var kjvResultText = cachedEntry?.textKjv ?: ""
            var parsedReferenceText = cachedEntry?.reference ?: state.inputPassage.trim()

            val usfmPassageId = NetworkClient.toApiBiblePassageId(state.inputPassage)

            // 1a. Fetch TB LAI if not cached (Primary option: Bolls API, Fallback 1: Alkitab Vercel, Fallback 2: API.Bible)
            if (tbResultText.isEmpty()) {
                // Primary: Bolls API
                val bollsTb = fetchFromBolls("TB", state.inputPassage)
                if (!bollsTb.isNullOrBlank()) {
                    tbResultText = bollsTb
                }

                // Fallback 1: Alkitab API Vercel
                if (tbResultText.isEmpty()) {
                    try {
                        val tbResponse = withContext(Dispatchers.IO) {
                            NetworkClient.alkitabService.getTbVerse(state.inputPassage)
                        }
                        if (tbResponse.verses != null && tbResponse.verses.isNotEmpty()) {
                            tbResultText = tbResponse.verses.joinToString(" ") { it.text }
                            parsedReferenceText = tbResponse.passage ?: state.inputPassage
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("BibleLens", "alkitab-api vercel down: ${e.message}")
                    }
                }

                // Fallback 2: api.bible (Indonesian TB)
                if (tbResultText.isEmpty() && state.bibleApiKey.isNotEmpty()) {
                    val fallbackTb = fetchFromApiBible(
                        bibleId = "2dd568ee4ab95aa6-01", // Indonesian TB
                        passageId = usfmPassageId,
                        apiKey = state.bibleApiKey
                    )
                    if (!fallbackTb.isNullOrBlank()) {
                        tbResultText = fallbackTb
                    }
                }
            }

            // 1b. Fetch English KJV if not cached (Primary option: Bolls API, Fallback 1: API.Bible, Fallback 2: bible-api.com)
            if (kjvResultText.isEmpty()) {
                // Primary: Bolls API KJV
                val bollsKjv = fetchFromBolls("KJV", state.inputPassage)
                if (!bollsKjv.isNullOrBlank()) {
                    kjvResultText = bollsKjv
                }

                // Fallback 1: api.bible NKJV/KJV
                if (kjvResultText.isEmpty() && state.bibleApiKey.isNotEmpty()) {
                    val fallbackKjv = fetchFromApiBible(
                        bibleId = "de4e12af7c8c9592-01", // English KJV Authorized
                        passageId = usfmPassageId,
                        apiKey = state.bibleApiKey
                    )
                    if (!fallbackKjv.isNullOrBlank()) {
                        kjvResultText = fallbackKjv
                    }
                }

                // Fallback 2: bible-api.com standard KJV
                if (kjvResultText.isEmpty()) {
                    try {
                        val englishPassageQuery = NetworkClient.normalizeBookToEnglish(state.inputPassage)
                        val kjvResponse = withContext(Dispatchers.IO) {
                            NetworkClient.bibleService.getKjvVerse(englishPassageQuery)
                        }
                        if (kjvResponse.verses.isNotEmpty()) {
                            kjvResultText = kjvResponse.verses.joinToString(" ") { it.text.trim() }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("BibleLens", "KJV generic lookup failed: ${e.message}")
                    }
                }
            }

            // Fallback for scripture texts: if network failed to fetch them, Gemini will output authentic verses
            val promptScriptureSection = if (tbResultText.isEmpty() || kjvResultText.isEmpty()) {
                "Teks ayat '${state.inputPassage}' tidak berhasil diunduh. Tolong isi dan duga secara akurat sesuai Alkitab TB LAI Indonesia dan KJV Inggris resmi."
            } else {
                "Teks TB LAI: $tbResultText \nTeks KJV: $kjvResultText"
            }

            // STEP 2: Initiate Gemini call with severe Reformed theology rules
            val resolvedApiKey = if (state.geminiApiKey.isNotEmpty()) {
                state.geminiApiKey
            } else {
                BuildConfig.GEMINI_API_KEY
            }

            if (resolvedApiKey.trim().isEmpty() || resolvedApiKey == "MY_GEMINI_API_KEY") {
                state.isLoading = false
                state.errorMessage = "Pemberitahuan: Kunci API Gemini belum terintegrasi di server/handphone. Silakan buka Pengaturan (ikon ⚙️ di pojok kanan atas) dan masukkan Kunci API Google AI Studio Anda."
                return@launch
            }

            val prompt = """
                Analisis ayat pilihan secara mendalam: '$parsedReferenceText'
                Data teks sah dari server (jika ada):
                $promptScriptureSection
                
                Lakukan bedah ayat teologis secara ortodoks dan sangat mendalam sesuai struktur yang diwajibkan. Sajikan ulasan secara sistematis, kaya rincian akademis, menggunakan poin-poin panjang dan terstruktur, serta berikan penjelasan teologis penuh makna. Selesaikan 100% respons JSON murni tanpa celah.
            """.trimIndent()

            val systemInstructionText = """
                Anda adalah seorang Senior Teolog Reformed/Injili yang berakar kuat pada Teologi Kovenan dan kedaulatan Allah yang mutlak, Pakar Linguistik Kamus Strong's Lexicon yang kritis, serta Apologet Kristen Protestan murni yang anti-penyesatan (anti-eisegesis, anti-cocoklogi, anti-kemakmuran palsu, atau sekadar khotbah motivasi murahan).
                Tugas utama Anda adalah menyajikan ulasan eksegesis dan eksposisi ayat Alkitab secara ilmiah, bersejarah, gramatikal, dan ortodoks murni tanpa distorsi.
                
                Sajikan seluruh penjelasan Anda secara kaya rincian akademis, sistematis, menggunakan poin-poin penjelasan yang panjang dan mendalam di dalam nilai JSON, meniru gaya pemotongan/pembedahan Injili yang autentik, berbobot, terstruktur rapi, dan mudah diserap namun tetap sangat teologis (kovenantal, salvifik, kristologis).
                
                Anda WAJIB memberikan respons HANYA dalam format JSON murni terstruktur tanpa bungkus markdown ```json ``` jika kemungkinan. Seluruh kolom di bawah ini harus diisi secara komprehensif, sangat padat makna, mendalam, dan tidak disingkat sedikit pun.
                
                Skema struktur JSON wajib:
                {
                  "timeline": "Sajikan tahun penulisan kitab, konteks garis waktu sejarah, kaisar/raja yang berkuasa, pergolakan zaman purba secara berurutan, kronologis, dan terperinci.",
                  "komparasi_manuskrip": "Ulas perbandingan kritis naskah orisinal purba (naskah Laut Mati/DSS, Teks Masoret, Septuaginta, Codex Sinaiticus, Vaticanus, Alexandrinus, dll.) secara teologis dan tekstual. Jelaskan bagaimana variasi tekstual minor justru menegaskan keamanan dan kedaulatan pelestarian teks oleh Allah bagi keyakinan Kristen murni.",
                  "sejarah_latar_belakang": "Informasi luas mengenai penulis asli kitab, jemaat penerima mula-mula, sosiopolitik-budaya, latar belakang kota/daerah, keadaan geografi, pergumulan spiritual jemaat tersebut, serta alasan mendasar dan kontekstual mengapa ayat ini dideklarasikan.",
                  "analogi_modern": "Berikan analogi/perumpamaan abad ke-21 yang relevan dengan pergumulan orang percaya di era digital ini, membumi namun tetap kokoh berdiri di atas kebenaran absolut firman Tuhan tanpa kompromi.",
                  "bahasa_asli_data": [
                    {
                      "kata_tb": "Kata/frase Indonesia versi Terjemahan Baru",
                      "kata_kjv": "Kata/frase Inggris versi King James Version",
                      "kata_asli": "Kata asli Ibrani (PL) / Yunani (PB) disertai transliterasi beraksen lengkap",
                      "strong_id": "Nomor indeks kamus Strong (misalnya H1254 atau G2316)",
                      "makna": "Ulas secara tuntas arti harfiah kata asli, analisis sintaksis/tenses kata, arti sesungguhnya di zaman Alkitabiah, serta implikasi eksegetis teologisnya bagi doktrin iman kita."
                    }
                  ],
                  "kristosentris": "Ulas analisis teologis yang benderang bagaimana seluruh ayat and maknanya memancar dan mengarah kepada karya keselamatan Kristus Yesus, penyaliban-Nya, penebusan dosa umat pilihan-Nya, atau penegasan ke-Tritunggalan Allah (Sola Gratia, Soli Deo Gloria).",
                  "cross_reference": "WAJIB berupa string/teks penjelasan narasi panjang (bukan array []). Sebutkan minimal 3 ayat referensi silang terkait erat, lalu ikat ketiganya dalam sebuah ulasan teologis yang padu menjelaskan hubungan organik antar-ayat tersebut.",
                  "apologetika_corner": "Argumen tangguh pertahanan iman doktrin Protestan ortodoks melawan ajaran bidaah kuno (Arianisme, Pelagianisme, Gnostisisme) maupun ajaran menyimpang modern (teologi kemakmuran, hyper-grace, rasionalisme ekstrem, liberalisme teologis, eisegesis serampangan).",
                  "renungan_praktis": "Aplikasi rohani praktis sehari-hari yang sangat mendalam dan menusuk kalbu. Tuliskan panduan yang membakar kasih akan kekudusan, mendesak pertobatan yang sungguh-sungguh, disiplin rohani, ketaatan, dan kepasrahan penuh kepada kedaulatan Bapa.",
                  "teks_tb_alkitab": "Sajikan secara lengkap dan asli (verbatim/harfiah) teks seluruh ayat/rentang ayat yang diminta dalam versi terjemahan Alkitab Indonesia Terjemahan Baru (TB LAI) resmi.",
                  "teks_kjv_alkitab": "Sajikan secara lengkap dan asli (verbatim/harfiah) teks seluruh ayat/rentang ayat yang diminta dalam versi terjemahan Alkitab bahasa Inggris King James Version (KJV) resmi."
                }
                
                Ketentuan Mutlak: Seluruh nilai diisi dalam string berbahasa Indonesia yang sopan, lugas, menggebu, akademis, dan teologis tinggi. Respons JSON Anda wajib valid penuh.
            """.trimIndent()

            val responseText = withContext(Dispatchers.IO) {
                val request = GeminiRestRequest(
                    contents = listOf(
                        GeminiRestContent(parts = listOf(GeminiRestPart(text = prompt)))
                    ),
                    generationConfig = GeminiRestGenerationConfig(responseMimeType = "application/json"),
                    systemInstruction = GeminiRestContent(
                        parts = listOf(GeminiRestPart(text = systemInstructionText))
                    )
                )

                val modelsToTry = listOf("gemini-3.5-flash", "gemini-2.5-flash", "gemini-1.5-flash")
                var lastException: Exception? = null
                var finalResult: String? = null

                for (modelName in modelsToTry) {
                    var retryCount = 0
                    val maxRetries = 2
                    while (retryCount <= maxRetries) {
                        try {
                            val response = NetworkClient.geminiRestService.generateContent(
                                model = modelName,
                                apiKey = resolvedApiKey,
                                request = request
                            )
                            finalResult = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            if (finalResult != null && finalResult.trim().isNotEmpty()) {
                                lastException = null
                                break
                            }
                        } catch (e: Exception) {
                            lastException = e
                            
                            val errMessage = e.message ?: ""
                            val isTransient = errMessage.contains("503") || errMessage.contains("429") || errMessage.contains("500") || e is java.io.IOException
                            
                            if (isTransient && retryCount < maxRetries) {
                                retryCount++
                                val delayMs = 1500L * retryCount
                                android.util.Log.d("BibleLens", "Retrying model $modelName due to transient error, attempt $retryCount after ${delayMs}ms. Error: $errMessage")
                                kotlinx.coroutines.delay(delayMs)
                            } else {
                                break
                            }
                        }
                    }
                    if (finalResult != null) {
                        break
                    } else {
                        android.util.Log.e("BibleLens", "Model $modelName call failed. Trying next fallback model if available.")
                    }
                }

                if (lastException != null) {
                    throw lastException
                }
                finalResult
            }

            if (responseText != null && responseText.trim().isNotEmpty()) {
                val cleanedJson = cleanJson(responseText)
                val parsedResult = createSafeGson().fromJson(cleanedJson, AnalysisResult::class.java)

                val finalTbText = if (tbResultText.isNotEmpty()) tbResultText else parsedResult.teksTbAlkitab
                val finalKjvText = if (kjvResultText.isNotEmpty()) kjvResultText else parsedResult.teksKjvAlkitab

                // Update UI state
                state.parsedReference = parsedReferenceText
                state.scriptureTb = finalTbText
                state.scriptureKjv = finalKjvText
                state.analysisResult = parsedResult
                state.selectedTab = 0 // jump to Context tab

                // Permanently caching/downloading inside localized SQLite DB!
                withContext(Dispatchers.IO) {
                    state.offlineRepo.saveCache(
                        queryPassage = normalizedQuery,
                        reference = parsedReferenceText,
                        textTb = finalTbText,
                        textKjv = finalKjvText,
                        analysisJson = cleanedJson
                    )
                }

                Toast.makeText(context, "Bedahan untuk '$parsedReferenceText' sukses dianalisis & disimpan offline di database lokal!", Toast.LENGTH_SHORT).show()
            } else {
                state.errorMessage = "AI Studio mengembalikan data kosong. Coba ulangi kembali."
            }

        } catch (e: Exception) {
            state.errorMessage = "Terjadi kegagalan pemrosesan: ${e.localizedMessage ?: e.message}. Pastikan koneksi internet aktif dan Kunci API benar."
        } finally {
            state.isLoading = false
        }
    }
}

class SafeStringDeserializer : com.google.gson.JsonDeserializer<String> {
    override fun deserialize(
        json: com.google.gson.JsonElement,
        typeOfT: java.lang.reflect.Type,
        context: com.google.gson.JsonDeserializationContext
    ): String {
        return when {
            json.isJsonNull -> ""
            json.isJsonArray -> {
                val array = json.asJsonArray
                val list = mutableListOf<String>()
                for (element in array) {
                    if (element.isJsonPrimitive) {
                        list.add(element.asString)
                    } else if (element.isJsonNull) {
                        // ignore
                    } else {
                        list.add(element.toString())
                    }
                }
                list.joinToString(", ")
            }
            json.isJsonObject -> json.toString()
            json.isJsonPrimitive -> json.asString
            else -> ""
        }
    }
}

fun createSafeGson(): Gson {
    return com.google.gson.GsonBuilder()
        .registerTypeAdapter(String::class.java, SafeStringDeserializer())
        .create()
}

// Cleans JSON responses from potential AI Markdown fence code blocks
fun cleanJson(input: String): String {
    var text = input.trim()
    if (text.startsWith("```")) {
        // Strip markdown header segment
        text = text.substringAfter("\n")
        if (text.endsWith("```")) {
            text = text.substringBeforeLast("```")
        }
    }
    return text.trim()
}

// Sends parsed analyses parameters to Google Sheets Web App endpoint
fun sendToGoogleSheets(context: Context, state: BibleStudyState, scope: CoroutineScope) {
    if (state.googleSheetUrl.trim().isEmpty()) {
        state.errorMessage = "Anda belum mengatur URL Google Sheet Apps Script Pelayan! Klik ikon ⚙️ di pojok kanan atas, lalu tempelkan URL Web App Apps Script Anda yang berakhiran '/exec'."
        return
    }

    val result = state.analysisResult
    if (result == null) {
        state.errorMessage = "Belum ada hasil bedahan ayat yang dimuat. Lakukan Bedah Ayat terlebih dahulu sebelum menyimpan data."
        return
    }

    state.isSendingSheet = true

    scope.launch {
        try {
            val timestamp = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val payload = SheetPostPayload(
                sheetUrl = state.spreadsheetUrl.trim(),
                timestamp = timestamp,
                reference = state.parsedReference,
                textTb = state.scriptureTb,
                textKjv = state.scriptureKjv,
                timeline = result.timeline,
                komparasi_manuskrip = result.komparasi_manuskrip,
                sejarah_latar_belakang = result.sejarah_latar_belakang,
                analogi_modern = result.analogi_modern,
                bahasaAsliJson = Gson().toJson(result.bahasa_asli_data),
                kristosentris = result.kristosentris,
                cross_reference = result.cross_reference,
                apologetika_corner = result.apologetika_corner,
                renungan_praktis = result.renungan_praktis
            )

            val response = withContext(Dispatchers.IO) {
                NetworkClient.googleSheetService.postToSheet(state.googleSheetUrl.trim(), payload)
            }

            if (response.isSuccessful) {
                state.sheetResponseStatus = "Analisis ayat ${state.parsedReference} berhasil diekspor dan dicatat di Google Sheets!"
            } else {
                state.errorMessage = "Spreadshet Apps Script mengembalikan kode kesalahan: ${response.code()} - ${response.message()}"
            }
        } catch (e: Exception) {
            state.errorMessage = "Gagal mengirim data ke Google Sheets: ${e.localizedMessage ?: e.message}. Hubungi administrator atau periksa URL Apps Script Anda."
        } finally {
            state.isSendingSheet = false
        }
    }
}
