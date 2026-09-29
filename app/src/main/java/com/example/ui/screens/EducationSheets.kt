package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EmeraldGreen
import com.example.ui.viewmodel.InfoSheetType

@Composable
fun EducationDialog(
    sheetType: InfoSheetType,
    onDismiss: () -> Unit
) {
    if (sheetType == InfoSheetType.None) return

    val (title, content) = when (sheetType) {
        InfoSheetType.HowItWorks -> Pair(
            "Comment ça marche ?",
            "1. LES ORIGINES DES RÉMUNÉRATIONS :\n" +
                    "CashLink RDC collabore avec des entreprises nationales et internationales (banques, opérateurs télécoms, startups, commerces locaux) qui souhaitent tester des produits, recueillir l'avis des consommateurs congolais ou vérifier des informations de terrain.\n\n" +
                    "2. AUCUN GAIN PASSIF NI INVESTISSEMENT :\n" +
                    "CashLink n'est pas un plan d'investissement ni une loterie. Vos gains dépendent exclusivement de vos réalisations effectives et validées.\n\n" +
                    "3. CYCLE DE VALIDATION :\n" +
                    "Après réalisation d'une tâche, celle-ci passe par un contrôle de qualité et anti-fraude. Dès validation, les fonds sont crédités sur votre solde disponible."
        )
        InfoSheetType.PaymentTerms -> Pair(
            "Conditions de Paiement & Retraits",
            "1. SEUILS DE RETRAIT :\n" +
                    "• Mobile Money (M-Pesa, Airtel, Orange, Afrimoney) : Minimum 3.00 USD (ou équivalent en Francs Congolais selon le cours du jour).\n" +
                    "• Carte Bancaire & PayPal : Minimum 10.00 USD.\n\n" +
                    "2. FRAIS DE RÉSEAU :\n" +
                    "Des frais minimes imposés par les réseaux de télécommunication (1% à 2.5%) s'appliquent pour couvrir les coûts d'envoi API.\n\n" +
                    "3. DÉLAIS DE TRAITEMENT :\n" +
                    "Les paiements réels sont exécutés par passerelle sécurisée sous 24 à 48 heures ouvrables après vérification de conformité administrative."
        )
        InfoSheetType.PrivacyPolicy -> Pair(
            "Politique de Confidentialité",
            "CashLink RDC s'engage à protéger la vie privée de ses utilisateurs conformément aux lois sur les télécommunications et la protection des données en RDC.\n\n" +
                    "• Données collectées : Nom, adresse email, numéro de compte Mobile Money (pour les virements), et réponses d'enquêtes pseudonymisées.\n" +
                    "• Finalité : Les réponses aux enquêtes sont compilées de manière anonyme pour les sponsors.\n" +
                    "• Sécurité : Vos données bancaires et numéros de téléphone ne sont jamais revendus à des tiers."
        )
        InfoSheetType.TermsOfService -> Pair(
            "Conditions Générales d'Utilisation",
            "1. ADMISSION :\n" +
                    "L'application est ouverte à toute personne résidant en RDC ou en Afrique, âgée d'au moins 18 ans.\n\n" +
                    "2. POLITIQUE ANTI-FRAUDE STRICTE :\n" +
                    "Sont strictement interdits sous peine de bannissement définitif et confiscation du solde :\n" +
                    "• La création de comptes multiples par la même personne ou sur le même appareil ;\n" +
                    "• L'utilisation de VPN, proxies ou robots d'automatisation ;\n" +
                    "• La soumission de fausses preuves ou de réponses aléatoires dénuées de sens.\n\n" +
                    "3. PARRAINAGE :\n" +
                    "Le parrainage est un bonus d'encouragement loyal. Tout abus ou trafic d'inscriptions factices entraînera la clôture du compte."
        )
        InfoSheetType.SupportContact -> Pair(
            "Support & Assistance Client",
            "Une question sur une tâche ou un retrait ? Notre équipe vous répond du lundi au samedi de 8h à 18h (heure de Kinshasa).\n\n" +
                    "• Email officiel : support@cashlink-rdc.com\n" +
                    "• Téléphone / WhatsApp : +243 81 000 7788\n" +
                    "• Siège : Boulevard du 30 Juin, Gombe, Kinshasa, RDC"
        )
        InfoSheetType.None -> Pair("", "")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = content,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("J'ai compris", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
