package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.CashLinkDatabase
import com.example.data.repository.CashLinkRepository
import com.example.ui.components.CashLinkTopBar
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EducationDialog
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReferralScreen
import com.example.ui.screens.TaskDetailDialog
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.WithdrawalScreen
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.CashLinkViewModel
import com.example.ui.viewmodel.CashLinkViewModelFactory
import com.example.ui.viewmodel.InfoSheetType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dbScope = CoroutineScope(Dispatchers.IO)
        val database = CashLinkDatabase.getDatabase(applicationContext, dbScope)
        val repository = CashLinkRepository(database.cashLinkDao())
        val factory = CashLinkViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: CashLinkViewModel = viewModel(factory = factory)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                // Display notifications
                LaunchedEffect(uiState.userNotificationMessage) {
                    uiState.userNotificationMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearNotification()
                    }
                }

                // Display errors
                LaunchedEffect(uiState.errorMessage) {
                    uiState.errorMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearError()
                    }
                }

                // Hardware Back Button Handling
                BackHandler(
                    enabled = uiState.selectedTaskForDetail != null ||
                            uiState.activeInfoSheet != InfoSheetType.None ||
                            uiState.isAdminMode ||
                            uiState.currentTab != AppTab.DASHBOARD
                ) {
                    when {
                        uiState.selectedTaskForDetail != null -> viewModel.closeTaskDetail()
                        uiState.activeInfoSheet != InfoSheetType.None -> viewModel.closeInfoSheet()
                        uiState.isAdminMode -> viewModel.toggleAdminMode()
                        uiState.currentTab != AppTab.DASHBOARD -> viewModel.setTab(AppTab.DASHBOARD)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CashLinkTopBar(
                            currentUser = uiState.currentUser,
                            isAdminMode = uiState.isAdminMode,
                            onToggleCurrency = { viewModel.toggleCurrencyPreference() },
                            onToggleLanguage = { viewModel.toggleLanguagePreference() },
                            onToggleAdmin = { viewModel.toggleAdminMode() }
                        )
                    },
                    bottomBar = {
                        if (!uiState.isAdminMode) {
                            CashLinkBottomNav(
                                currentTab = uiState.currentTab,
                                onTabSelected = { viewModel.setTab(it) }
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (uiState.isAdminMode) {
                            AdminScreen(
                                uiState = uiState,
                                onApproveWithdrawal = { viewModel.adminApproveWithdrawal(it) },
                                onRejectWithdrawal = { id, reason -> viewModel.adminRejectWithdrawal(id, reason) },
                                onApproveSubmission = { viewModel.adminApproveSubmission(it) },
                                onRejectSubmission = { id, reason -> viewModel.adminRejectSubmission(id, reason) },
                                onAddNewTask = { viewModel.adminAddNewTask(it) },
                                onToggleFraudFlag = { viewModel.adminToggleFraudFlag(it) },
                                onExitAdmin = { viewModel.toggleAdminMode() }
                            )
                        } else {
                            when (uiState.currentTab) {
                                AppTab.DASHBOARD -> DashboardScreen(
                                    uiState = uiState,
                                    onNavigateToTasks = { viewModel.setTab(AppTab.TASKS) },
                                    onNavigateToWithdraw = { viewModel.setTab(AppTab.WITHDRAW) },
                                    onNavigateToReferral = { viewModel.setTab(AppTab.REFERRAL) },
                                    onTaskClick = { viewModel.openTaskDetail(it) },
                                    onOpenHowItWorks = { viewModel.openInfoSheet(InfoSheetType.HowItWorks) }
                                )
                                AppTab.TASKS -> TasksScreen(
                                    uiState = uiState,
                                    onCategorySelected = { viewModel.setCategory(it) },
                                    onSearchChanged = { viewModel.setSearchQuery(it) },
                                    onStartTaskClick = { viewModel.openTaskDetail(it) }
                                )
                                AppTab.WITHDRAW -> WithdrawalScreen(
                                    uiState = uiState,
                                    onRequestWithdrawal = { amount, method, account, name ->
                                        viewModel.requestWithdrawal(amount, method, account, name)
                                    }
                                )
                                AppTab.REFERRAL -> ReferralScreen(
                                    uiState = uiState,
                                    onClaimBonus = { viewModel.claimReferralBonus() }
                                )
                                AppTab.PROFILE -> ProfileScreen(
                                    uiState = uiState,
                                    onToggleCurrency = { viewModel.toggleCurrencyPreference() },
                                    onToggleLanguage = { viewModel.toggleLanguagePreference() },
                                    onToggleAdmin = { viewModel.toggleAdminMode() },
                                    onOpenInfoSheet = { viewModel.openInfoSheet(it) }
                                )
                            }
                        }

                        // Task Detail / Execution Dialog
                        uiState.selectedTaskForDetail?.let { task ->
                            TaskDetailDialog(
                                task = task,
                                uiState = uiState,
                                onClose = { viewModel.closeTaskDetail() },
                                onSubmitProof = { proof -> viewModel.submitTaskWithProof(proof) }
                            )
                        }

                        // Legal / Info Sheets Dialog
                        if (uiState.activeInfoSheet != InfoSheetType.None) {
                            EducationDialog(
                                sheetType = uiState.activeInfoSheet,
                                onDismiss = { viewModel.closeInfoSheet() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CashLinkBottomNav(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .testTag("cashlink_bottom_navigation")
            .navigationBarsPadding(),
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(AppTab.DASHBOARD, Icons.Filled.Home, Icons.Outlined.Home),
            Triple(AppTab.TASKS, Icons.Filled.AssignmentTurnedIn, Icons.Outlined.AssignmentTurnedIn),
            Triple(AppTab.WITHDRAW, Icons.Filled.Payments, Icons.Outlined.Payments),
            Triple(AppTab.REFERRAL, Icons.Filled.People, Icons.Outlined.People),
            Triple(AppTab.PROFILE, Icons.Filled.Person, Icons.Outlined.Person)
        )

        items.forEach { (tab, filledIcon, outlinedIcon) ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) filledIcon else outlinedIcon,
                        contentDescription = tab.labelFr
                    )
                },
                label = {
                    Text(
                        text = tab.labelFr,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = EmeraldGreen.copy(alpha = 0.2f),
                    selectedIconColor = EmeraldGreen,
                    selectedTextColor = EmeraldGreen
                ),
                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
            )
        }
    }
}
