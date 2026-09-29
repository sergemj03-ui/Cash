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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.data.model.WithdrawalEntity
import com.example.data.model.WithdrawalStatus
import com.example.ui.theme.AfrimoneyBlue
import com.example.ui.theme.AirtelRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MpesaRed
import com.example.ui.theme.OrangeBrand
import com.example.ui.theme.TechBlue
import com.example.ui.theme.VisaBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WithdrawalScreen(
    uiState: com.example.ui.viewmodel.CashLinkUiState,
    onRequestWithdrawal: (Double, PaymentMethod, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.MPESA) }
    var withdrawalAmountStr by remember { mutableStateOf("5.0") }
    var recipientAccount by remember { mutableStateOf("0812345678") }
    var recipientName by remember { mutableStateOf(uiState.currentUser.name) }
    var inputError by remember { mutableStateOf<String?>(null) }

    val amountRequested = withdrawalAmountStr.toDoubleOrNull() ?: 0.0
    val minThreshold = selectedMethod.minWithdrawalUsd
    val feeUsd = (amountRequested * (selectedMethod.feePercent / 100.0)) + selectedMethod.fixedFeeUsd
    val netReceivedUsd = (amountRequested - feeUsd).coerceAtLeast(0.0)
    val netReceivedCdf = netReceivedUsd * uiState.platformConfig.usdToCdfRate

    val canWithdraw = amountRequested >= minThreshold &&
            amountRequested <= uiState.currentUser.balanceUsd &&
            recipientAccount.length >= 8 &&
            !uiState.currentUser.isFraudFlagged

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance & Minimum Notice Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Solde disponible au retrait",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = uiState.formatCurrency(uiState.currentUser.balanceUsd),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldGreen
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "Min: ${minThreshold.toInt()} $ USD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (uiState.currentUser.balanceUsd < minThreshold) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Solde inférieur au seuil de ${minThreshold.toInt()} $. Effectuez plus de tâches pour débloquer votre retrait.",
                                fontSize = 11.sp,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }
        }

        // Payment Methods Selector
        item {
            Text(
                text = "Sélectionnez le moyen de paiement",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(PaymentMethod.values()) { method ->
                    val isSelected = selectedMethod == method
                    val brandColor = getMethodBrandColor(method)

                    Surface(
                        modifier = Modifier
                            .width(135.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedMethod = method
                                recipientAccount = method.exampleNumber
                            }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) brandColor else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) brandColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(brandColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getMethodIcon(method),
                                    contentDescription = method.title,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = method.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "Frais: ${method.feePercent}%",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Payout Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Coordonnées de retrait (${selectedMethod.providerName})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = selectedMethod.prefixHelp,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Account / Phone Input
                    OutlinedTextField(
                        value = recipientAccount,
                        onValueChange = { recipientAccount = it },
                        label = { Text(selectedMethod.accountPlaceholderFr) },
                        placeholder = { Text(selectedMethod.exampleNumber) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdrawal_account_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Beneficiary Name
                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text("Nom complet du titulaire") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdrawal_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Amount USD Input
                    OutlinedTextField(
                        value = withdrawalAmountStr,
                        onValueChange = { withdrawalAmountStr = it },
                        label = { Text("Montant à retirer ($ USD)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdrawal_amount_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick amount chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(3.0, 5.0, 10.0, 20.0).forEach { amt ->
                            val isMax = amt > uiState.currentUser.balanceUsd
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { withdrawalAmountStr = amt.toString() }
                            ) {
                                Text(
                                    text = "${amt.toInt()} $",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isMax) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Calculation Breakdown
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Montant brut :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "%.2f $ USD".format(amountRequested), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Taux de change appliqué :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "1 $ = ${uiState.platformConfig.usdToCdfRate.toInt()} FC", fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Frais de transfert réseau :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "- %.2f $ USD".format(feeUsd), fontSize = 12.sp, color = Color(0xFFEF4444))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Montant net reçu :", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "%.2f $ USD".format(netReceivedUsd),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldGreen
                                )
                                Text(
                                    text = "≈ %,d FC".format(netReceivedCdf.toInt()).replace(',', ' '),
                                    fontSize = 11.sp,
                                    color = GoldAccent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Important Security & Legitimacy Disclaimer
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Traitement sécurisé : Les paiements réels sont exécutés par passerelle de paiement partenaire (M-Pesa, Airtel, Orange) après vérification administrative sous 24h à 48h.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            onRequestWithdrawal(amountRequested, selectedMethod, recipientAccount, recipientName)
                        },
                        enabled = canWithdraw && !uiState.isSubmittingAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_withdrawal_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (uiState.isSubmittingAction) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Demander le retrait",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Withdrawals History
        item {
            Text(
                text = "Historique des demandes de retrait",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (uiState.withdrawals.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Aucune demande de retrait effectuée",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.withdrawals) { withdrawal ->
                WithdrawalHistoryCard(withdrawal = withdrawal)
            }
        }
    }
}

@Composable
fun WithdrawalHistoryCard(withdrawal: WithdrawalEntity) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH)
    val dateStr = dateFormat.format(Date(withdrawal.requestedAt))

    val (statusLabel, statusColor, statusIcon) = when (withdrawal.status) {
        WithdrawalStatus.PENDING.name -> Triple("En attente", Color(0xFFF59E0B), Icons.Default.HourglassBottom)
        WithdrawalStatus.PROCESSING.name -> Triple("En traitement", TechBlue, Icons.Default.HourglassBottom)
        WithdrawalStatus.PAID.name -> Triple("Payé", EmeraldGreen, Icons.Default.CheckCircle)
        else -> Triple("Refusé", Color(0xFFEF4444), Icons.Default.Close)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = withdrawal.paymentMethodCode,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${withdrawal.recipientAccount}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Montant net : %.2f $ (%,d FC)".format(
                        withdrawal.netReceivedUsd,
                        withdrawal.netReceivedCdf.toInt()
                    ).replace(',', ' '),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldGreen
                )
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (withdrawal.transactionRef.isNotEmpty() && withdrawal.transactionRef != "PENDING_GATEWAY_DISPATCH") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Réf: ${withdrawal.transactionRef}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (withdrawal.rejectionReason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Motif: ${withdrawal.rejectionReason}",
                    fontSize = 11.sp,
                    color = Color(0xFFEF4444)
                )
            }
        }
    }
}

fun getMethodBrandColor(method: PaymentMethod): Color {
    return when (method) {
        PaymentMethod.MPESA -> MpesaRed
        PaymentMethod.AIRTEL_MONEY -> AirtelRed
        PaymentMethod.ORANGE_MONEY -> OrangeBrand
        PaymentMethod.AFRIMONEY -> AfrimoneyBlue
        PaymentMethod.VISA_MASTERCARD -> VisaBlue
        PaymentMethod.PAYPAL -> TechBlue
    }
}

fun getMethodIcon(method: PaymentMethod): ImageVector {
    return when (method) {
        PaymentMethod.MPESA, PaymentMethod.AIRTEL_MONEY, PaymentMethod.ORANGE_MONEY, PaymentMethod.AFRIMONEY -> Icons.Default.PhoneAndroid
        PaymentMethod.VISA_MASTERCARD -> Icons.Default.CreditCard
        PaymentMethod.PAYPAL -> Icons.Default.AccountBalance
    }
}
