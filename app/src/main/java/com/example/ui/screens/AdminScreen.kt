package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ProofType
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItemEntity
import com.example.data.model.TaskSubmissionEntity
import com.example.data.model.UserEntity
import com.example.data.model.WithdrawalEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TechBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    uiState: com.example.ui.viewmodel.CashLinkUiState,
    onApproveWithdrawal: (String) -> Unit,
    onRejectWithdrawal: (String, String) -> Unit,
    onApproveSubmission: (String) -> Unit,
    onRejectSubmission: (String, String) -> Unit,
    onAddNewTask: (TaskItemEntity) -> Unit,
    onToggleFraudFlag: (String) -> Unit,
    onExitAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var adminTab by remember { mutableIntStateOf(0) } // 0: Stats, 1: Retraits, 2: Tâches soumises, 3: Utilisateurs
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Admin Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Espace Administration",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Supervision financière & anti-fraude",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(
                onClick = onExitAdmin,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Quitter", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Admin Tabs
        ScrollableTabRow(
            selectedTabIndex = adminTab,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = adminTab == 0,
                onClick = { adminTab = 0 },
                text = { Text("Statistiques", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = adminTab == 1,
                onClick = { adminTab = 1 },
                text = {
                    Text(
                        "Retraits (${uiState.adminPendingWithdrawals.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
            Tab(
                selected = adminTab == 2,
                onClick = { adminTab = 2 },
                text = {
                    Text(
                        "Soumissions (${uiState.adminPendingSubmissions.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
            Tab(
                selected = adminTab == 3,
                onClick = { adminTab = 3 },
                text = { Text("Utilisateurs & Fraude", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (adminTab) {
            0 -> AdminStatsTab(uiState = uiState, onOpenAddTask = { showAddTaskDialog = true })
            1 -> AdminWithdrawalsTab(
                uiState = uiState,
                onApprove = onApproveWithdrawal,
                onReject = onRejectWithdrawal
            )
            2 -> AdminSubmissionsTab(
                uiState = uiState,
                onApprove = onApproveSubmission,
                onReject = onRejectSubmission
            )
            3 -> AdminUsersTab(
                uiState = uiState,
                onToggleFraud = onToggleFraudFlag
            )
        }
    }

    if (showAddTaskDialog) {
        AdminAddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { task ->
                onAddNewTask(task)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun AdminStatsTab(
    uiState: com.example.ui.viewmodel.CashLinkUiState,
    onOpenAddTask: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatCard(
                    title = "Revenus Plateforme",
                    value = "%.2f $".format(uiState.platformConfig.totalRevenueGeneratedUsd),
                    icon = Icons.Default.TrendingUp,
                    tint = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Total Retraits Payés",
                    value = "%.2f $".format(uiState.platformConfig.totalPlatformPayoutsUsd),
                    icon = Icons.Default.MonetizationOn,
                    tint = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatCard(
                    title = "Campagnes Actives",
                    value = "${uiState.tasks.size}",
                    icon = Icons.Default.Poll,
                    tint = TechBlue,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Sponsors Vérifiés",
                    value = "${uiState.platformConfig.activeSponsorsCount}",
                    icon = Icons.Default.Security,
                    tint = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Actions Administrateur",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenAddTask,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("admin_add_task_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Publier une nouvelle tâche rémunérée", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Passerelles de paiement configurées",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• M-Pesa API : Connectée (En direct RDC)\n" +
                                "• Airtel Money API : Connectée\n" +
                                "• Orange Money API : Connectée\n" +
                                "• Afrimoney API : Connectée\n" +
                                "• Taux fixe configuré : 1 USD = ${uiState.platformConfig.usdToCdfRate.toInt()} CDF",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun AdminWithdrawalsTab(
    uiState: com.example.ui.viewmodel.CashLinkUiState,
    onApprove: (String) -> Unit,
    onReject: (String, String) -> Unit
) {
    if (uiState.adminPendingWithdrawals.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Aucun retrait en attente !",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.adminPendingWithdrawals) { wth ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${wth.paymentMethodCode} • ${wth.recipientAccount}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Net: %.2f $ (%,d FC)".format(wth.netReceivedUsd, wth.netReceivedCdf.toInt()).replace(',', ' '),
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldGreen,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = "Bénéficiaire : ${wth.recipientName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onApprove(wth.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("admin_approve_wth_${wth.id}"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                Text("Approuver (Payer)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onReject(wth.id, "Numéro de compte non conforme ou rejet passerelle") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("admin_reject_wth_${wth.id}"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Rejeter & Rembourser", fontSize = 11.sp, color = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSubmissionsTab(
    uiState: com.example.ui.viewmodel.CashLinkUiState,
    onApprove: (String) -> Unit,
    onReject: (String, String) -> Unit
) {
    if (uiState.adminPendingSubmissions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Toutes les soumissions sont traitées",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.adminPendingSubmissions) { sub ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = sub.taskTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "+${uiState.formatCurrency(sub.rewardUsd)}",
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Preuve utilisateur: ${sub.userProofInput}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onApprove(sub.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                Text("Valider & Créditer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { onReject(sub.id, "Preuve incomplète ou non conforme") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Refuser", fontSize = 11.sp, color = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUsersTab(
    uiState: com.example.ui.viewmodel.CashLinkUiState,
    onToggleFraud: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(uiState.allUsers) { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${user.email} • Solde: ${uiState.formatCurrency(user.balanceUsd)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (user.isFraudFlagged) {
                            Text(
                                text = "⚠️ COMPTE BLOQUÉ POUR FRAUDE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }

                    Button(
                        onClick = { onToggleFraud(user.id) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (user.isFraudFlagged) EmeraldGreen else Color(0xFFEF4444)
                        ),
                        modifier = Modifier.testTag("admin_toggle_fraud_${user.id}")
                    ) {
                        Text(
                            text = if (user.isFraudFlagged) "Débloquer" else "Bloquer",
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (TaskItemEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var sponsorName by remember { mutableStateOf("") }
    var rewardStr by remember { mutableStateOf("1.00") }
    var slotsStr by remember { mutableStateOf("100") }
    var category by remember { mutableStateOf(TaskCategory.SURVEY) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Créer une nouvelle tâche",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre de la tâche") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = sponsorName,
                    onValueChange = { sponsorName = it },
                    label = { Text("Nom de l'entreprise sponsor") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rewardStr,
                        onValueChange = { rewardStr = it },
                        label = { Text("Gain (USD)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = slotsStr,
                        onValueChange = { slotsStr = it },
                        label = { Text("Places") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val reward = rewardStr.toDoubleOrNull() ?: 0.50
                        val slots = slotsStr.toIntOrNull() ?: 50
                        val newTask = TaskItemEntity(
                            id = "task_" + UUID.randomUUID().toString().take(8),
                            title = if (title.isEmpty()) "Nouvelle Mission Entreprise" else title,
                            description = if (description.isEmpty()) "Mission rémunérée sponsorisée" else description,
                            category = category.name,
                            rewardUsd = reward,
                            estimatedMinutes = 5,
                            totalSlots = slots,
                            remainingSlots = slots,
                            conditions = "Résider en Afrique subsaharienne",
                            instructions = "Complétez l'activité selon les consignes",
                            proofType = ProofType.SCREENSHOT_UPLOAD.name,
                            sponsorName = if (sponsorName.isEmpty()) "Partenaire CashLink" else sponsorName
                        )
                        onAddTask(newTask)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Publier la mission", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
