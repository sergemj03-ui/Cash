package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentMethod(
    val code: String,
    val title: String,
    val providerName: String,
    val minWithdrawalUsd: Double,
    val feePercent: Double,
    val fixedFeeUsd: Double,
    val accountPlaceholderFr: String,
    val exampleNumber: String,
    val prefixHelp: String
) {
    MPESA(
        code = "MPESA",
        title = "Vodacom M-Pesa",
        providerName = "Vodacom RDC",
        minWithdrawalUsd = 3.0,
        feePercent = 1.5,
        fixedFeeUsd = 0.10,
        accountPlaceholderFr = "Numéro M-Pesa (ex: 081XXXXXXX)",
        exampleNumber = "0812345678",
        prefixHelp = "Préfixes Vodacom: 081, 082, 083"
    ),
    AIRTEL_MONEY(
        code = "AIRTEL",
        title = "Airtel Money",
        providerName = "Airtel RDC",
        minWithdrawalUsd = 3.0,
        feePercent = 1.5,
        fixedFeeUsd = 0.10,
        accountPlaceholderFr = "Numéro Airtel (ex: 099XXXXXXX)",
        exampleNumber = "0998765432",
        prefixHelp = "Préfixes Airtel: 099, 097, 098"
    ),
    ORANGE_MONEY(
        code = "ORANGE",
        title = "Orange Money",
        providerName = "Orange RDC",
        minWithdrawalUsd = 3.0,
        feePercent = 1.5,
        fixedFeeUsd = 0.10,
        accountPlaceholderFr = "Numéro Orange (ex: 085XXXXXXX)",
        exampleNumber = "0851122334",
        prefixHelp = "Préfixes Orange: 084, 085, 089"
    ),
    AFRIMONEY(
        code = "AFRIMONEY",
        title = "Afrimoney",
        providerName = "Africell RDC",
        minWithdrawalUsd = 3.0,
        feePercent = 1.0,
        fixedFeeUsd = 0.05,
        accountPlaceholderFr = "Numéro Africell (ex: 090XXXXXXX)",
        exampleNumber = "0901234567",
        prefixHelp = "Préfixes Africell: 090, 091"
    ),
    VISA_MASTERCARD(
        code = "CARD",
        title = "Carte Bancaire (Visa/Mastercard)",
        providerName = "Réseau Bancaire UBA / Rawbank / Equity BCDC",
        minWithdrawalUsd = 10.0,
        feePercent = 2.5,
        fixedFeeUsd = 0.50,
        accountPlaceholderFr = "Numéro de carte à 16 chiffres",
        exampleNumber = "4152 **** **** 9012",
        prefixHelp = "Cartes prépayées ou débit acceptées"
    ),
    PAYPAL(
        code = "PAYPAL",
        title = "PayPal International",
        providerName = "PayPal Inc.",
        minWithdrawalUsd = 10.0,
        feePercent = 3.0,
        fixedFeeUsd = 0.35,
        accountPlaceholderFr = "Adresse Email PayPal",
        exampleNumber = "exemple@gmail.com",
        prefixHelp = "Compte PayPal vérifié requis"
    )
}

enum class WithdrawalStatus(val labelFr: String, val labelEn: String) {
    PENDING("En attente", "Pending"),
    PROCESSING("En traitement", "Processing"),
    PAID("Payé", "Paid"),
    REJECTED("Refusé", "Declined")
}

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amountUsd: Double,
    val exchangeRateUsed: Double = 2800.0,
    val amountCdf: Double,
    val feeUsd: Double,
    val netReceivedUsd: Double,
    val netReceivedCdf: Double,
    val paymentMethodCode: String,
    val recipientAccount: String,
    val recipientName: String,
    val requestedAt: Long = System.currentTimeMillis(),
    val status: String = WithdrawalStatus.PENDING.name,
    val transactionRef: String = "",
    val rejectionReason: String = "",
    val gatewayResponse: String = "Passerelle en attente d'exécution API"
)
