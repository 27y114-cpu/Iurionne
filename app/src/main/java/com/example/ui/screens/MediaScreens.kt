package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.models.*
import com.example.ui.MediaViewModel
import com.example.ui.SortOrder
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.UUID

@Composable
fun MediaAppNavigationContainer(viewModel: MediaViewModel) {
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Observe backup status messages as simple Toast triggers
    val backupStatus by viewModel.backupStatus.collectAsStateWithLifecycle()
    LaunchedEffect(backupStatus) {
        backupStatus?.let {
            val msg = when (it) {
                "RESTORE_SUCCESS" -> "Backup restaurado com sucesso!"
                "RESTORE_ERROR" -> "Descriptografia ou formato de backup inválido!"
                "RESTORE_AUTH_FAILED" -> "Erro de autenticação na restauração!"
                "PROFILE_NOT_FOUND" -> "Perfil de usuário não encontrado!"
                else -> it
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentPage) {
            "LOGIN" -> LoginScreen(viewModel)
            "REGISTER" -> RegisterScreen(viewModel)
            "MAIN" -> HomeScreenContainer(viewModel)
        }
    }
}

// ==========================================
// 1. TELA DE LOGIN
// ==========================================
@Composable
fun LoginScreen(viewModel: MediaViewModel) {
    val username by viewModel.usernameQuery.collectAsStateWithLifecycle()
    val password by viewModel.passwordQuery.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showRestoreDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Elegant Icon / Title
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = "MEDIA CRYPT",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.testTag("app_logo_title")
            )

            Text(
                text = "Gerenciador Pessoal Offline Criptografado",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Form Cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { viewModel.usernameQuery.value = it },
                        label = { Text("Usuário") },
                        singleLine = true,
                        placeholder = { Text("Nome do perfil") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.passwordQuery.value = it },
                        label = { Text("Senha") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) }
                    )

                    if (authState == "ERROR_LOGIN") {
                        Text(
                            text = "Usuário ou senha inválidos!",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (authState == "REGISTER_SUCCESS") {
                        Text(
                            text = "Perfil registrado! Faça o login.",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.handleLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ENTRAR", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo("REGISTER") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("go_register_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("REGISTRAR NOVO PERFIL", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Restore DB button
            TextButton(
                onClick = { showRestoreDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("go_restore_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RESTAURAR BANCO DE DADOS",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    // Modal dialogue to input backup strings directly from external clips
    if (showRestoreDialog) {
        var restoreUser by remember { mutableStateOf("") }
        var restorePass by remember { mutableStateOf("") }
        var backupText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showRestoreDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Restaurar Banco Offline",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Valide as credenciais do perfil para descriptografar os dados do backup.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    OutlinedTextField(
                        value = restoreUser,
                        onValueChange = { restoreUser = it },
                        label = { Text("Usuário") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = restorePass,
                        onValueChange = { restorePass = it },
                        label = { Text("Senha") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = backupText,
                        onValueChange = { backupText = it },
                        label = { Text("Conteúdo do Backup (JSON)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        placeholder = { Text("Cole o texto JSON do backup exportado...") }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clip = clipboardManager.getText()
                                if (clip != null) {
                                    backupText = clip.text
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Colar Clip")
                        }

                        Button(
                            onClick = {
                                if (restoreUser.isNotBlank() && restorePass.isNotBlank() && backupText.isNotBlank()) {
                                    viewModel.restoreBackupFromLoginScreen(backupText, restorePass, restoreUser)
                                    showRestoreDialog = false
                                } else {
                                    Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Importar")
                        }
                    }

                    TextButton(
                        onClick = { showRestoreDialog = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. TELA DE REGISTRO
// ==========================================
@Composable
fun RegisterScreen(viewModel: MediaViewModel) {
    val username by viewModel.usernameQuery.collectAsStateWithLifecycle()
    val password by viewModel.passwordQuery.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = "NOVO PERFIL",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Crie seu perfil pessoal offline. Suas credenciais geram uma chave mestre criptográfica única.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { viewModel.usernameQuery.value = it },
                        label = { Text("Nome de Usuário") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.passwordQuery.value = it },
                        label = { Text("Defina uma Senha") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) }
                    )

                    if (authState == "ERROR_REGISTER") {
                        Text(
                            text = "Erro: Este perfil correspondente já existe!",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (authState == "ERROR_FIELD_EMPTY") {
                        Text(
                            text = "Erro: Usuário e Senha são obrigatórios!",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = { viewModel.handleRegister() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("REGISTRAR", fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { viewModel.navigateTo("LOGIN") },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Voltar para o Login", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. HOME CONTAINER COM BOTTOM NAVIGATION
// ==========================================
enum class HomeTab { DASHBOARD, SERIES, FILMES, ANIMES, LIVROS, HQS, BACKUP }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContainer(viewModel: MediaViewModel) {
    var activeTab by remember { mutableStateOf(HomeTab.DASHBOARD) }
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    var selectedWorkDetail by remember { mutableStateOf<Work?>(null) }
    var isEditModeActive by remember { mutableStateOf(false) }

    // Direct detail linking using viewModel state
    val viewDetailState by viewModel.selectedWork.collectAsStateWithLifecycle()
    LaunchedEffect(viewDetailState) {
        selectedWorkDetail = viewDetailState
        if (viewDetailState == null) {
            isEditModeActive = false
        }
    }

    Scaffold(
        bottomBar = {
            if (selectedWorkDetail == null && !isEditModeActive) {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    tonalElevation = 8.dp
                ) {
                    val tabs = listTabs()
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = activeTab == tab.first,
                            onClick = { activeTab = tab.first },
                            icon = { Icon(tab.third, contentDescription = null) },
                            label = { 
                                Text(
                                    text = tab.second, 
                                    maxLines = 1, 
                                    fontSize = 10.sp, 
                                    overflow = TextOverflow.Ellipsis
                                ) 
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isEditModeActive) {
                // Edit/Add media details view overlay
                AddEditWorkScreen(
                    viewModel = viewModel,
                    editingWork = selectedWorkDetail,
                    onBack = {
                        isEditModeActive = false
                        viewModel.selectWork(selectedWorkDetail) // Stay in detail or return
                    }
                )
            } else if (selectedWorkDetail != null) {
                // Interactive detail review view
                WorkDetailsScreen(
                    work = selectedWorkDetail!!,
                    onBack = { 
                        viewModel.selectWork(null)
                        selectedWorkDetail = null 
                    },
                    onEdit = { isEditModeActive = true },
                    onDelete = {
                        viewModel.deleteMediaWork(selectedWorkDetail!!.id)
                        viewModel.selectWork(null)
                        selectedWorkDetail = null
                    },
                    onUpdateState = { updated ->
                        viewModel.saveMediaWork(updated)
                    }
                )
            } else {
                // Main functional categories layout
                when (activeTab) {
                    HomeTab.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onWorkSelect = { w -> viewModel.selectWork(w) },
                        onNavigateTab = { t -> activeTab = t }
                    )
                    HomeTab.SERIES -> CategoryListScreen(
                        viewModel = viewModel,
                        category = WorkType.SERIES,
                        onWorkSelect = { w -> viewModel.selectWork(w) },
                        onAddClick = { isEditModeActive = true }
                    )
                    HomeTab.FILMES -> CategoryListScreen(
                        viewModel = viewModel,
                        category = WorkType.FILMES,
                        onWorkSelect = { w -> viewModel.selectWork(w) },
                        onAddClick = { isEditModeActive = true }
                    )
                    HomeTab.ANIMES -> CategoryListScreen(
                        viewModel = viewModel,
                        category = WorkType.ANIMES,
                        onWorkSelect = { w -> viewModel.selectWork(w) },
                        onAddClick = { isEditModeActive = true }
                    )
                    HomeTab.LIVROS -> CategoryListScreen(
                        viewModel = viewModel,
                        category = WorkType.LIVROS,
                        onWorkSelect = { w -> viewModel.selectWork(w) },
                        onAddClick = { isEditModeActive = true }
                    )
                    HomeTab.HQS -> CategoryListScreen(
                        viewModel = viewModel,
                        category = WorkType.HQS,
                        onWorkSelect = { w -> viewModel.selectWork(w) },
                        onAddClick = { isEditModeActive = true }
                    )
                    HomeTab.BACKUP -> BackupSettingsScreen(viewModel)
                }
            }
        }
    }
}

private fun listTabs() = listOf(
    Triple(HomeTab.DASHBOARD, "Início", Icons.Default.Dashboard),
    Triple(HomeTab.SERIES, "Séries", Icons.Default.Tv),
    Triple(HomeTab.FILMES, "Filmes", Icons.Default.Movie),
    Triple(HomeTab.ANIMES, "Animes", Icons.Default.AutoAwesome),
    Triple(HomeTab.LIVROS, "Livros", Icons.Default.MenuBook),
    Triple(HomeTab.HQS, "HQs", Icons.Default.ChromeReaderMode),
    Triple(HomeTab.BACKUP, "Ajustes", Icons.Default.Settings)
)

// ==========================================
// 4. LOTE DE DETALHES DAS OBRAS (DASHBOARD)
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: MediaViewModel,
    onWorkSelect: (Work) -> Unit,
    onNavigateTab: (HomeTab) -> Unit
) {
    val worksList by viewModel.works.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Aggregate statistics in memory instantly
    val totalWorks = worksList.size
    val totalFavorite = worksList.count { it.favorite }
    
    // Total watched/read counts
    var totalEpisodesWatched = 0
    var totalChaptersRead = 0
    var totalMoviesWatched = 0
    
    worksList.forEach { work ->
        when (work.type) {
            WorkType.SERIES, WorkType.ANIMES -> {
                work.seriesData?.seasons?.flatMap { it.episodes }?.forEach { ep ->
                    if (ep.watched) {
                        totalEpisodesWatched += 1
                        if (ep.watchCount > 1) {
                            totalEpisodesWatched += (ep.watchCount - 1)
                        }
                    }
                }
            }
            WorkType.FILMES -> {
                work.movieData?.movies?.forEach { movie ->
                    if (movie.watched) {
                        totalMoviesWatched += 1
                        if (movie.watchCount > 1) {
                            totalMoviesWatched += (movie.watchCount - 1)
                        }
                    }
                }
            }
            WorkType.LIVROS -> {
                work.bookData?.volumes?.flatMap { it.chapters }?.forEach { ch ->
                    if (ch.read) {
                        totalChaptersRead += 1
                        if (ch.readCount > 1) {
                            totalChaptersRead += (ch.readCount - 1)
                        }
                    }
                }
            }
            WorkType.HQS -> {
                work.hqData?.volumes?.flatMap { it.chapters }?.forEach { ch ->
                    if (ch.read) {
                        totalChaptersRead += 1
                        if (ch.readCount > 1) {
                            totalChaptersRead += (ch.readCount - 1)
                        }
                    }
                }
            }
        }
    }

    // Average user rating
    val ratedWorks = worksList.filter { it.userRating > 0 }
    val generalAverage = if (ratedWorks.isEmpty()) 0.0 else ratedWorks.map { it.userRating }.average()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Header Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Circular Avatar
                val username = activeProfile?.username ?: "Usuário"
                val initials = if (username.length >= 2) username.take(2).uppercase() else username.take(1).uppercase()
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp
                    )
                }

                Column {
                    Text(
                        text = "Dashboard",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Olá, $username!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            IconButton(
                onClick = { viewModel.handleLogout() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        // STATS DASHBOARD GRID
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    value = "$totalWorks",
                    label = "Obras",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "$totalFavorite",
                    label = "Favoritos",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    value = String.format("%.1f ★", generalAverage),
                    label = "Média",
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    hasOutline = true,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "$totalEpisodesWatched",
                    label = "Episódios",
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    hasOutline = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    value = "$totalMoviesWatched",
                    label = "Filmes",
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    hasOutline = true,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "$totalChaptersRead",
                    label = "Capítulos",
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    hasOutline = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // CONTINUAR ASSISTINDO (TV / Series / Anime in progress)
        val watchingWorks = worksList.filter { 
            (it.type == WorkType.SERIES || it.type == WorkType.ANIMES || it.type == WorkType.FILMES) && 
            (it.userStatus == UserStatus.ASSISTINDO || it.userStatus == UserStatus.REASSISTINDO)
        }
        if (watchingWorks.isNotEmpty()) {
            DashboardSectionRow(title = "Continuar assistindo") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    watchingWorks.take(3).forEach { work ->
                        ProgressWorkRow(work = work, onClick = { onWorkSelect(work) })
                    }
                }
            }
        }

        // CONTINUAR LENDO (Books / HQs in progress)
        val readingWorks = worksList.filter { 
            (it.type == WorkType.LIVROS || it.type == WorkType.HQS) && 
            (it.userStatus == UserStatus.LENDO || it.userStatus == UserStatus.RELENDO)
        }
        if (readingWorks.isNotEmpty()) {
            DashboardSectionRow(title = "Continuar lendo") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    readingWorks.take(3).forEach { work ->
                        ProgressWorkRow(work = work, onClick = { onWorkSelect(work) })
                    }
                }
            }
        }

        // POPULAR FAVORITES
        val favorites = worksList.filter { it.favorite }
        if (favorites.isNotEmpty()) {
            DashboardSectionRow(title = "Seus Favoritos") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(favorites) { work ->
                        MediaThumbnailCard(work = work, onClick = { onWorkSelect(work) })
                    }
                }
            }
        }

        // RECENTLY UPDATED
        val recentlyUpdatedWorks = worksList.sortedByDescending { it.updatedAt }
        if (recentlyUpdatedWorks.isNotEmpty()) {
            DashboardSectionRow(title = "Últimas atualizações") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentlyUpdatedWorks.take(5).forEach { work ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onWorkSelect(work) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = work.getPinnedTitle(),
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${work.type.name} • ${work.userStatus.name}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (work.favorite) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(end = 4.dp)
                                    )
                                }
                                Text(
                                    text = "${work.userRating} ★",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // EMPTY STATE BOX
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "Sua biblioteca está vazia",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Use as abas abaixo para carregar novas séries, filmes, livros, HQs ou mangás no seu acervo criptografado offline.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardSectionRow(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            letterSpacing = 0.5.sp
        )
        content()
    }
}

@Composable
fun StatBox(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    hasOutline: Boolean = false
) {
    Card(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(24.dp),
        border = if (hasOutline) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)) else null,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = contentColor
            )
        }
    }
}

@Composable
fun ProgressWorkRow(work: Work, onClick: () -> Unit) {
    val typeLabel = when (work.type) {
        WorkType.SERIES -> "Série"
        WorkType.ANIMES -> "Anime"
        WorkType.FILMES -> "Filme"
        WorkType.LIVROS -> "Livro"
        WorkType.HQS -> work.hqData?.subtype ?: "HQ"
    }
    
    var totalCount = 0
    var completedCount = 0
    
    when (work.type) {
        WorkType.SERIES, WorkType.ANIMES -> {
            totalCount = work.seriesData?.seasons?.flatMap { it.episodes }?.size ?: 0
            completedCount = work.seriesData?.seasons?.flatMap { it.episodes }?.count { it.watched } ?: 0
        }
        WorkType.FILMES -> {
            totalCount = work.movieData?.movies?.size ?: 0
            completedCount = work.movieData?.movies?.count { it.watched } ?: 0
        }
        WorkType.LIVROS -> {
            totalCount = work.bookData?.volumes?.flatMap { it.chapters }?.size ?: 0
            completedCount = work.bookData?.volumes?.flatMap { it.chapters }?.count { it.read } ?: 0
        }
        WorkType.HQS -> {
            totalCount = work.hqData?.volumes?.flatMap { it.chapters }?.size ?: 0
            completedCount = work.hqData?.volumes?.flatMap { it.chapters }?.count { it.read } ?: 0
        }
    }
    
    val pct = if (totalCount > 0) completedCount.toFloat() / totalCount else 0.0f
    val progressText = if (totalCount > 0) {
        when (work.type) {
            WorkType.SERIES, WorkType.ANIMES -> "Ep $completedCount/$totalCount"
            WorkType.FILMES -> "$completedCount/$totalCount Filmes"
            WorkType.LIVROS, WorkType.HQS -> "Cap $completedCount/$totalCount"
        }
    } else {
        "Sem episódios/capítulos"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp, 52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (work.type) {
                WorkType.SERIES, WorkType.ANIMES -> Icons.Default.Tv
                WorkType.FILMES -> Icons.Default.Movie
                WorkType.LIVROS -> Icons.Default.Book
                WorkType.HQS -> Icons.Default.Book
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = work.getPinnedTitle(),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$typeLabel • $progressText",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            LinearProgressIndicator(
                progress = pct,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }

        if (work.userRating > 0) {
            Text(
                text = "★ ${work.userRating}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MediaThumbnailCard(work: Work, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(170.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Category tag
                Text(
                    text = work.type.name,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Middle Title
                Text(
                    text = work.getPinnedTitle(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                    modifier = Modifier.weight(1f)
                )

                // InfoFooter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = work.userStatus.name.lowercase().capitalize(),
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "${work.userRating}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (work.favorite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(14.dp)
                )
            }
        }
    }
}

// ==========================================
// 5. LISTAGEM DE CATEGORIA UNITÁRIA
// ==========================================
@Composable
fun CategoryListScreen(
    viewModel: MediaViewModel,
    category: WorkType,
    onWorkSelect: (Work) -> Unit,
    onAddClick: () -> Unit
) {
    val filteredWorks by viewModel.filteredWorks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeSort by viewModel.selectedSortOrder.collectAsStateWithLifecycle()
    val activeFilterStatus by viewModel.selectedStatus.collectAsStateWithLifecycle()
    val onlyFavorites by viewModel.onlyFavorites.collectAsStateWithLifecycle()

    // Bind current category dynamically to the viewModel combiner on view mounting
    LaunchedEffect(category) {
        viewModel.selectedCategory.value = category
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Pre-create basic model of appropriate type, then edit
                    val emptyWork = Work(
                        id = UUID.randomUUID().toString(),
                        type = category,
                        titles = listOf(Title("Português", "", true)),
                        createdAt = System.currentTimeMillis()
                    )
                    viewModel.selectWork(emptyWork)
                    onAddClick()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Title
            Text(
                text = when (category) {
                    WorkType.SERIES -> "Séries de TV"
                    WorkType.FILMES -> "Filmes e Franquias"
                    WorkType.ANIMES -> "Animes"
                    WorkType.LIVROS -> "Livros & Web Novels"
                    WorkType.HQS -> "HQs & Mangás"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Pesquisar nesta categoria...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_bar"),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                }
            )

            // Quick Filter Tabs row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorites filter toggle chip
                FilterChip(
                    selected = onlyFavorites,
                    onClick = { viewModel.onlyFavorites.value = !onlyFavorites },
                    label = { Text("Favoritos") },
                    leadingIcon = {
                        if (onlyFavorites) {
                            Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                        } else {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                )

                // Sorting chip cycle
                AssistChip(
                    onClick = {
                        val nextSort = when (activeSort) {
                            SortOrder.RECENTES -> SortOrder.NOME
                            SortOrder.NOME -> SortOrder.AVALIACAO
                            SortOrder.AVALIACAO -> SortOrder.FAVORITOS
                            SortOrder.FAVORITOS -> SortOrder.RECENTES
                        }
                        viewModel.selectedSortOrder.value = nextSort
                    },
                    label = { Text("Ordenação: ${activeSort.name}") },
                    leadingIcon = { Icon(Icons.Default.Sort, contentDescription = null) }
                )

                // UserStatus cycle chip
                AssistChip(
                    onClick = {
                        val nextStatus = when (activeFilterStatus) {
                            null -> UserStatus.PLANEJANDO
                            UserStatus.PLANEJANDO -> UserStatus.LENDO
                            UserStatus.LENDO -> UserStatus.ASSISTINDO
                            UserStatus.ASSISTINDO -> UserStatus.CONCLUIDO
                            UserStatus.CONCLUIDO -> null
                            else -> null
                        }
                        viewModel.selectedStatus.value = nextStatus
                    },
                    label = { Text(activeFilterStatus?.name ?: "Status: Todos") },
                    leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) }
                )
            }

            // Category list result representation
            if (filteredWorks.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredWorks) { work ->
                        WorkRowCard(work = work, onClick = { onWorkSelect(work) })
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Nenhum resultado encontrado.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkRowCard(work: Work, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = work.getPinnedTitle(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = work.status.name.lowercase().capitalize().replace("_", " "),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    Text(
                        text = work.userStatus.name,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (work.favorite) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorito",
                        tint = Color.Red,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "${work.userRating} ★",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

// ==========================================
// 6. DETALHES DE UMA OBRA SERECIONADA
// ==========================================
@Composable
fun WorkDetailsScreen(
    work: Work,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateState: (Work) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back, edit row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Title info banner
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = work.type.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (work.favorite) {
                    Icon(Icons.Default.Favorite, contentDescription = "Favorito", tint = Color.Red, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = work.getPinnedTitle(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Other titles
            work.titles.filter { !it.pinned }.forEach { alt ->
                Text(
                    text = "${alt.language}: ${alt.title}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Divider()

        // Status / Dates info card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Condição da Obra:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(work.status.name.replace("_", " "), color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Seu Progresso:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(work.userStatus.name, color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Publicação:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(work.publicationDate.ifBlank { "Não informado" }, fontSize = 13.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sua Avaliação fixa:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("${work.userRating} / 5 ★", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Avaliação Calculada (Média):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(String.format("%.2f / 5 ★", work.calculatedRating), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, fontSize = 13.sp)
                }
            }
        }

        // Description
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("DESCRIÇÃO / ANOTAÇÕES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(
                text = work.description.ifBlank { "Sem descrição." },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
        }

        // LINKS BLOCK
        if (work.links.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("LINKS RÁPIDOS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                work.links.forEach { link ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Link copiado: ${link.url}", Toast.LENGTH_SHORT).show()
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(link.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${link.type} • ${link.language}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar link", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Divider()

        // SUB-ELEMENT PROGRESS TRACKER ACTIONS (Série/Anime: Episódios, Livros: Capítulos, etc.)
        Text("CONTROLE DE PROGRESSO DETALHADO", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

        when (work.type) {
            WorkType.SERIES, WorkType.ANIMES -> {
                val series = work.seriesData ?: SeriesData()
                if (series.seasons.isEmpty()) {
                    Text("Nenhuma temporada criada. Edite para gerar a estrutura.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                } else {
                    series.seasons.forEach { season ->
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Temporada ${season.number}: ${season.title}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            if (season.description.isNotBlank()) {
                                Text(season.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            }
                            
                            season.episodes.forEach { ep ->
                                EpisodeRow(episode = ep, onUpdate = { updatedEp ->
                                    // Update nested list
                                    val newSeasons = series.seasons.map { s ->
                                        if (s.number == season.number) {
                                            s.copy(episodes = s.episodes.map { e ->
                                                if (e.number == ep.number) updatedEp else e
                                            })
                                        } else s
                                    }
                                    onUpdateState(work.copy(seriesData = SeriesData(newSeasons)))
                                })
                            }
                        }
                    }
                }
            }

            WorkType.FILMES -> {
                val movieData = work.movieData ?: MovieData()
                if (movieData.movies.isEmpty()) {
                    Text("Nenhum filme cadastrado nesta saga.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                } else {
                    if (movieData.franchise.isNotBlank()) {
                        Text("Franquia / Saga: ${movieData.franchise}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    movieData.movies.forEach { movieItem ->
                        MovieRow(movie = movieItem, onUpdate = { updatedMovie ->
                            val newMovies = movieData.movies.map { m ->
                                if (m.number == movieItem.number) updatedMovie else m
                            }
                            onUpdateState(work.copy(movieData = MovieData(movieData.franchise, newMovies)))
                        })
                    }
                }
            }

            WorkType.LIVROS -> {
                val bookData = work.bookData ?: BookData()
                if (bookData.volumes.isEmpty()) {
                    Text("Nenhum volume cadastrado.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                } else {
                    if (bookData.seriesName.isNotBlank()) {
                        Text("Série: ${bookData.seriesName}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    bookData.volumes.forEach { volume ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Volume ${volume.volumeNumber}: ${volume.title} ${if (volume.partName.isNotBlank()) "(${volume.partName})" else ""}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            volume.chapters.forEach { chapter ->
                                BookChapterRow(chapter = chapter, onUpdate = { updatedCh ->
                                    val newVolumes = bookData.volumes.map { v ->
                                        if (v.volumeNumber == volume.volumeNumber) {
                                            v.copy(chapters = v.chapters.map { c ->
                                                if (c.number == chapter.number) updatedCh else c
                                            })
                                        } else v
                                    }
                                    onUpdateState(work.copy(bookData = BookData(bookData.seriesName, newVolumes)))
                                })
                            }
                        }
                    }
                }
            }

            WorkType.HQS -> {
                val hqData = work.hqData ?: HqData()
                if (hqData.volumes.isEmpty()) {
                    Text("Nenhum volume cadastrado nesta HQ/Mangá.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                } else {
                    Text("${hqData.subtype}: ${hqData.seriesName.ifBlank { work.getPinnedTitle() }}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    hqData.volumes.forEach { volume ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Volume ${volume.volumeNumber}: ${volume.title}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            volume.chapters.forEach { chapter ->
                                HqChapterRow(chapter = chapter, onUpdate = { updatedCh ->
                                    val newVolumes = hqData.volumes.map { v ->
                                        if (v.volumeNumber == volume.volumeNumber) {
                                            v.copy(chapters = v.chapters.map { c ->
                                                if (c.number == chapter.number) updatedCh else c
                                            })
                                        } else v
                                    }
                                    onUpdateState(work.copy(hqData = HqData(hqData.seriesName, hqData.subtype, newVolumes)))
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EpisodeRow(episode: Episode, onUpdate: (Episode) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(
                    checked = episode.watched,
                    onCheckedChange = { onUpdate(episode.copy(watched = it, watchCount = if (it) 1 else 0)) }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text("Ep. ${episode.number}: ${episode.title}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    if (episode.watchCount > 1) {
                        Text("Assistido ${episode.watchCount} vezes", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Quick Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val nextCount = if (episode.watched) episode.watchCount + 1 else 1
                    onUpdate(episode.copy(watched = true, watchCount = nextCount))
                }) {
                    Icon(Icons.Default.Repeat, contentDescription = "Reassistir", modifier = Modifier.size(16.dp))
                }

                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= episode.rating) Icons.Default.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (i <= episode.rating) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    val nextRating = if (episode.rating == i) 0 else i
                                    onUpdate(episode.copy(rating = nextRating))
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MovieRow(movie: MovieItem, onUpdate: (MovieItem) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(
                    checked = movie.watched,
                    onCheckedChange = { onUpdate(movie.copy(watched = it, watchCount = if (it) 1 else 0)) }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text("Nº ${movie.number}: ${movie.title}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    if (movie.watchCount > 1) {
                        Text("Assistido ${movie.watchCount} vezes", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val nextCount = if (movie.watched) movie.watchCount + 1 else 1
                    onUpdate(movie.copy(watched = true, watchCount = nextCount))
                }) {
                    Icon(Icons.Default.Repeat, contentDescription = "Reassistir", modifier = Modifier.size(16.dp))
                }

                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= movie.rating) Icons.Default.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (i <= movie.rating) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    val nextRating = if (movie.rating == i) 0 else i
                                    onUpdate(movie.copy(rating = nextRating))
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookChapterRow(chapter: BookChapter, onUpdate: (BookChapter) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(
                    checked = chapter.read,
                    onCheckedChange = { onUpdate(chapter.copy(read = it, readCount = if (it) 1 else 0)) }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text("Cap. ${chapter.number}: ${chapter.title}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    if (chapter.readCount > 1) {
                        Text("Lido ${chapter.readCount} vezes", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val nextCount = if (chapter.read) chapter.readCount + 1 else 1
                    onUpdate(chapter.copy(read = true, readCount = nextCount))
                }) {
                    Icon(Icons.Default.Repeat, contentDescription = "Reler", modifier = Modifier.size(16.dp))
                }

                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= chapter.rating) Icons.Default.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (i <= chapter.rating) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    val nextRating = if (chapter.rating == i) 0 else i
                                    onUpdate(chapter.copy(rating = nextRating))
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HqChapterRow(chapter: HqChapter, onUpdate: (HqChapter) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(
                    checked = chapter.read,
                    onCheckedChange = { onUpdate(chapter.copy(read = it, readCount = if (it) 1 else 0)) }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text("Cap. ${chapter.number}: ${chapter.title}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    if (chapter.readCount > 1) {
                        Text("Lido ${chapter.readCount} vezes", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val nextCount = if (chapter.read) chapter.readCount + 1 else 1
                    onUpdate(chapter.copy(read = true, readCount = nextCount))
                }) {
                    Icon(Icons.Default.Repeat, contentDescription = "Reler", modifier = Modifier.size(16.dp))
                }

                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= chapter.rating) Icons.Default.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (i <= chapter.rating) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    val nextRating = if (chapter.rating == i) 0 else i
                                    onUpdate(chapter.copy(rating = nextRating))
                                }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. FORMULÁRIO DE ADIÇÃO OU EDIÇÃO DE OBRAS
// ==========================================
@Composable
fun AddEditWorkScreen(
    viewModel: MediaViewModel,
    editingWork: Work?,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Initialize structures from editing or default state
    var description by remember { mutableStateOf(editingWork?.description ?: "") }
    var publicationDate by remember { mutableStateOf(editingWork?.publicationDate ?: "") }
    var workStatus by remember { mutableStateOf(editingWork?.status ?: WorkStatus.EM_ANDAMENTO) }
    var userStatus by remember { mutableStateOf(editingWork?.userStatus ?: UserStatus.PLANEJANDO) }
    var userRating by remember { mutableStateOf(editingWork?.userRating ?: 3) }
    var favorite by remember { mutableStateOf(editingWork?.favorite ?: false) }

    // List of added titles
    val titlesList = remember { mutableStateListOf<Title>().apply { addAll(editingWork?.titles ?: listOf(Title("Português", "", true))) } }
    var newTitleLang by remember { mutableStateOf("") }
    var newTitleText by remember { mutableStateOf("") }

    // Links builder lists
    val linksList = remember { mutableStateListOf<Link>().apply { addAll(editingWork?.links ?: emptyList()) } }
    var newLinkLabel by remember { mutableStateOf("") }
    var newLinkUrl by remember { mutableStateOf("") }
    var newLinkLang by remember { mutableStateOf("Português") }
    var newLinkType by remember { mutableStateOf("Streaming") }

    // Sub structures
    var episodesCountToGen by remember { mutableStateOf("12") }
    var chaptersCountToGen by remember { mutableStateOf("20") }
    var seriesDataState by remember { mutableStateOf(editingWork?.seriesData ?: SeriesData()) }
    var movieDataState by remember { mutableStateOf(editingWork?.movieData ?: MovieData()) }
    var bookDataState by remember { mutableStateOf(editingWork?.bookData ?: BookData()) }
    var hqDataState by remember { mutableStateOf(editingWork?.hqData ?: HqData()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Navigation header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Fechar")
            }

            Button(
                onClick = {
                    val cleanTitles = titlesList.filter { it.title.isNotBlank() }
                    if (cleanTitles.isEmpty()) {
                        Toast.makeText(context, "Defina pelo menos um título!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    val saveable = Work(
                        id = editingWork?.id ?: UUID.randomUUID().toString(),
                        type = editingWork?.type ?: WorkType.SERIES,
                        coverPath = null,
                        description = description,
                        publicationDate = publicationDate,
                        status = workStatus,
                        userStatus = userStatus,
                        userRating = userRating,
                        favorite = favorite,
                        createdAt = editingWork?.createdAt ?: System.currentTimeMillis(),
                        titles = cleanTitles,
                        links = linksList.toList(),
                        seriesData = if (editingWork?.type == WorkType.SERIES || editingWork?.type == WorkType.ANIMES) seriesDataState else null,
                        movieData = if (editingWork?.type == WorkType.FILMES) movieDataState else null,
                        bookData = if (editingWork?.type == WorkType.LIVROS) bookDataState else null,
                        hqData = if (editingWork?.type == WorkType.HQS) hqDataState else null
                    )
                    
                    viewModel.saveMediaWork(saveable)
                    Toast.makeText(context, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SALVAR OBRA", fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(
            text = if (editingWork?.description != "") "Editar Cadastro" else "Novo Cadastro",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Divider()

        // 1. TITLES MANAGER
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("TÍTULOS DA OBRA (Suporta múltiplos idiomas)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                
                // Active titles list
                titlesList.forEachIndexed { idx, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                if (item.pinned) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Fixado", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)).padding(horizontal = 4.dp))
                                }
                            }
                            Text("Idioma: ${item.language}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }

                        Row {
                            if (!item.pinned) {
                                IconButton(onClick = {
                                    // Remove pin from all others, pin this
                                    val remapped = titlesList.map { it.copy(pinned = false) }
                                    titlesList.clear()
                                    titlesList.addAll(remapped)
                                    titlesList[idx] = item.copy(pinned = true)
                                }) {
                                    Icon(Icons.Default.PushPin, contentDescription = "Pin", modifier = Modifier.size(16.dp))
                                }
                            }
                            IconButton(onClick = {
                                if (titlesList.size > 1) {
                                    titlesList.removeAt(idx)
                                    if (item.pinned) {
                                        titlesList[0] = titlesList[0].copy(pinned = true)
                                    }
                                } else {
                                    Toast.makeText(context, "Deixe pelo menos um título!", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Add form
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTitleLang,
                        onValueChange = { newTitleLang = it },
                        label = { Text("Idioma") },
                        placeholder = { Text("Ex: Japonês") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newTitleText,
                        onValueChange = { newTitleText = it },
                        label = { Text("Título") },
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                }

                Button(
                    onClick = {
                        if (newTitleLang.isNotBlank() && newTitleText.isNotBlank()) {
                            titlesList.add(Title(newTitleLang, newTitleText, titlesList.isEmpty()))
                            newTitleLang = ""
                            newTitleText = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("+ Adicionar Título")
                }
            }
        }

        // 2. META FIELDS Form
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Anotações e Descrição") },
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp),
            placeholder = { Text("Adicione um resumo, sinopse, observações ou metas pessoais...") }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = publicationDate,
                onValueChange = { publicationDate = it },
                label = { Text("Data de Lançamento") },
                placeholder = { Text("Ex: 15/02/2026") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            // Rating Selector
            Column(modifier = Modifier.weight(1f)) {
                Text("Avaliação Fixa Opcional", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= userRating) Icons.Default.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (i <= userRating) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { userRating = i }
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = favorite, onCheckedChange = { favorite = it })
            Spacer(modifier = Modifier.width(8.dp))
            Text("Marcar como Favorito (Estrela especial)", fontWeight = FontWeight.Bold)
        }

        // STATUS SPIN DROPDOWNS
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("SELEÇÃO DE STATUS", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Status da obra:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                            items(WorkStatus.values()) { st ->
                                FilterChip(
                                    selected = workStatus == st,
                                    onClick = { workStatus = st },
                                    label = { Text(st.name.replace("_", " "), fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Seu status de progresso:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                            items(UserStatus.values()) { st ->
                                FilterChip(
                                    selected = userStatus == st,
                                    onClick = { userStatus = st },
                                    label = { Text(st.name, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. STRUCTURE BUILDER (Séries vs Filmes vs Livros etc)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("GERADOR AUTOMÁTICO DE PROGRESSO", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                
                when (editingWork?.type ?: WorkType.SERIES) {
                    WorkType.SERIES, WorkType.ANIMES -> {
                        Text("Gere temporadas e episódios rapidamente para começar a rastrear.", fontSize = 12.sp)
                        OutlinedTextField(
                            value = episodesCountToGen,
                            onValueChange = { episodesCountToGen = it },
                            label = { Text("Número de Episódios na 1ª Temporada") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val count = episodesCountToGen.toIntOrNull() ?: 12
                                val list = (1..count).map {
                                    Episode(number = it, title = "Episódio $it")
                                }
                                val generatedSeason = Season(
                                    number = 1,
                                    title = "Temporada Principal",
                                    description = "Temporada gerada automaticamente",
                                    episodes = list
                                )
                                seriesDataState = SeriesData(seasons = listOf(generatedSeason))
                                Toast.makeText(context, "Estrutura de $count episódios criada com sucesso!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Gerar 1ª Temporada com Episódios")
                        }
                    }

                    WorkType.FILMES -> {
                        Text("Configure filmes se for parte de uma saga, franquia, ou sequência.", fontSize = 12.sp)
                        var franchiseText by remember { mutableStateOf(movieDataState.franchise) }
                        OutlinedTextField(
                            value = franchiseText,
                            onValueChange = { 
                                franchiseText = it
                                movieDataState = movieDataState.copy(franchise = it)
                            },
                            label = { Text("Nome da Franquia / Saga") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row {
                            Button(
                                onClick = {
                                    val currentSize = movieDataState.movies.size
                                    val newMovieItem = MovieItem(
                                        number = currentSize + 1,
                                        title = "Parte ${currentSize + 1}"
                                    )
                                    movieDataState = movieDataState.copy(
                                        movies = movieDataState.movies + newMovieItem
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("+ Adicionar Filme à Franquia")
                            }
                        }
                    }

                    WorkType.LIVROS -> {
                        Text("Configure volumes e capítulos para livros ou light novels.", fontSize = 12.sp)
                        OutlinedTextField(
                            value = chaptersCountToGen,
                            onValueChange = { chaptersCountToGen = it },
                            label = { Text("Quantidade de Capítulos no Volume 1") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val count = chaptersCountToGen.toIntOrNull() ?: 20
                                val list = (1..count).map {
                                    BookChapter(number = it, title = "Capítulo $it")
                                }
                                val volume = BookVolume(
                                    volumeNumber = 1,
                                    title = "Volume Principal",
                                    chapters = list
                                )
                                bookDataState = BookData(seriesName = "", volumes = listOf(volume))
                                Toast.makeText(context, "Volume gerado com $count capítulos!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Gerar Volume 1 com Capítulos")
                        }
                    }

                    WorkType.HQS -> {
                        Text("Adicione volumes e capítulos de mangás, comics ou webtoon.", fontSize = 12.sp)
                        OutlinedTextField(
                            value = chaptersCountToGen,
                            onValueChange = { chaptersCountToGen = it },
                            label = { Text("Quantidade de Capítulos") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val count = chaptersCountToGen.toIntOrNull() ?: 10
                                val list = (1..count).map {
                                    HqChapter(number = it, title = "Capítulo $it")
                                }
                                val vol = HqVolume(
                                    volumeNumber = 1,
                                    title = "Volume 1",
                                    chapters = list
                                )
                                hqDataState = HqData(seriesName = "", subtype = "HQ", volumes = listOf(vol))
                                Toast.makeText(context, "$count capítulos gerados no Volume 1!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Gerar Volume 1 com Capítulos")
                        }
                    }
                }
            }
        }

        // 4. LINKS EDITOR
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("LINKS DE LEITURA / STREAMING / COMPRA", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                
                linksList.forEachIndexed { i, link ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(link.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${link.type} • ${link.url}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        IconButton(onClick = { linksList.removeAt(i) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                OutlinedTextField(
                    value = newLinkLabel,
                    onValueChange = { newLinkLabel = it },
                    label = { Text("Rótulo do Link") },
                    placeholder = { Text("Ex: Assistir na Netflix") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = newLinkUrl,
                    onValueChange = { newLinkUrl = it },
                    label = { Text("URL de Acesso") },
                    placeholder = { Text("Ex: https://netflix.com/...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Selectors row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newLinkLang,
                        onValueChange = { newLinkLang = it },
                        label = { Text("Idioma") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newLinkType,
                        onValueChange = { newLinkType = it },
                        label = { Text("Tipo") },
                        placeholder = { Text("Streaming / Leitura") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Button(
                    onClick = {
                        if (newLinkLabel.isNotBlank() && newLinkUrl.isNotBlank()) {
                            linksList.add(Link(newLinkLang, newLinkLabel, newLinkUrl, newLinkType))
                            newLinkLabel = ""
                            newLinkUrl = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("+ Adicionar Link")
                }
            }
        }
    }
}

// ==========================================
// 8. TELA DE BACKUP E AJUSTES DE PERFIL
// ==========================================
@Composable
fun BackupSettingsScreen(viewModel: MediaViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var backupTextOutput by remember { mutableStateOf("") }
    var restoreTextInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Simple title
        Text(
            text = "Configurações e Backup",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PERFIL ATIVO", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Nome de Usuário: ${activeProfile?.username ?: "Offline"}")
                Text("Registrado em: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(activeProfile?.createdAt ?: 0))}")
                
                Divider()
                
                Button(
                    onClick = { viewModel.handleLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SAIR DO PERFIL (Desativará criptografia na memória)")
                }
            }
        }

        // EXPORT BANCO
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("EXPORTAR BANCO CRIPTOGRAFADO (Backup)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Gere uma cópia segura criptografada de todas as suas séries, filmes, anime, e anotações. Esta cópia é protegida por AES-GCM.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
                )

                Button(
                    onClick = {
                        val out = viewModel.generateBackupString()
                        if (out != null) {
                            backupTextOutput = out
                            clipboardManager.setText(AnnotatedString(out))
                            Toast.makeText(context, "Backup copiado para a Área de Transferência!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Não há dados para exportar!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gerar e Copiar Texto do Backup")
                }

                if (backupTextOutput.isNotBlank()) {
                    OutlinedTextField(
                        value = backupTextOutput,
                        onValueChange = {},
                        label = { Text("Conteúdo do Backup") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            }
        }

        // IMPORT BANCO
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("IMPORTAR BANCO DE DADOS (Restaurar)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Substitua ou restaure a biblioteca offline colando o texto do backup gerado.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
                )

                OutlinedTextField(
                    value = restoreTextInput,
                    onValueChange = { restoreTextInput = it },
                    label = { Text("Cole o JSON de Backup") },
                    placeholder = { Text("Cole o texto gerado aqui...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val clip = clipboardManager.getText()
                            if (clip != null) {
                                restoreTextInput = clip.text
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Colar do Clip")
                    }

                    Button(
                        onClick = {
                            if (restoreTextInput.isNotBlank()) {
                                viewModel.restoreBackupFromString(restoreTextInput)
                                restoreTextInput = ""
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Iniciar Importação")
                    }
                }
            }
        }
    }
}
