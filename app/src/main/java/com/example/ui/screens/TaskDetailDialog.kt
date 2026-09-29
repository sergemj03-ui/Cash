package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ProofType
import com.example.data.model.TaskItemEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TechBlue
import kotlinx.coroutines.delay

@Composable
fun TaskDetailDialog(
    task: TaskItemEntity,
    uiState: com.example.ui.viewmodel.CashLinkUiState,
    onClose: () -> Unit,
    onSubmitProof: (String) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) } // 0: Briefing, 1: Interaction, 2: Verification/Success

    // Form states for interaction
    var surveyQ1Answer by remember { mutableStateOf("Vodacom M-Pesa") }
    var surveyQ2Answer by remember { mutableStateOf("Plusieurs fois par semaine") }
    var proofText by remember { mutableStateOf("") }
    var videoSecondsWatched by remember { mutableIntStateOf(0) }
    var isVerifyingAntiFraud by remember { mutableStateOf(false) }

    // Video timer countdown simulation
    if (currentStep == 1 && task.proofType == ProofType.VIDEO_WATCH.name && videoSecondsWatched < 5) {
        LaunchedEffect(videoSecondsWatched) {
            delay(1000)
            videoSecondsWatched += 1
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header: Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.sponsorName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                        Text(
                            text = task.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reward Tag
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldGreen.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rémunération :",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "+${uiState.formatBothCurrencies(task.rewardUsd)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (currentStep) {
                    0 -> {
                        // Step 0: Task Details & Instructions
                        Text(
                            text = "Conditions requises",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.conditions,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Consignes & Instructions",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.instructions,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Anti-Fraud Notice
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Anti-Fraude actif : Vos réponses font l'objet d'un contrôle de cohérence. Ne soumettez que des informations authentiques.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { currentStep = 1 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("start_execution_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Démarrer l'activité maintenant", fontWeight = FontWeight.Bold)
                        }
                    }

                    1 -> {
                        // Step 1: Interactive Execution Based on ProofType
                        if (task.proofType == ProofType.SURVEY_FORM.name) {
                            Text(
                                text = "Questionnaire du sondage",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "1. Quel est votre moyen de transfert d'argent principal ?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            val optionsQ1 = listOf("Vodacom M-Pesa", "Airtel Money", "Orange Money", "Afrimoney")
                            optionsQ1.forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { surveyQ1Answer = option }
                                        .padding(vertical = 2.dp)
                                ) {
                                    RadioButton(
                                        selected = surveyQ1Answer == option,
                                        onClick = { surveyQ1Answer = option }
                                    )
                                    Text(text = option, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "2. À quelle fréquence utilisez-vous ce service ?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            val optionsQ2 = listOf("Tous les jours", "Plusieurs fois par semaine", "Occasionnellement")
                            optionsQ2.forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { surveyQ2Answer = option }
                                        .padding(vertical = 2.dp)
                                ) {
                                    RadioButton(
                                        selected = surveyQ2Answer == option,
                                        onClick = { surveyQ2Answer = option }
                                    )
                                    Text(text = option, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = proofText,
                                onValueChange = { proofText = it },
                                label = { Text("Votre avis / recommandation en quelques mots") },
                                placeholder = { Text("Ex: Les frais sont corrects mais le réseau est parfois lent...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("survey_feedback_input"),
                                shape = RoundedCornerShape(10.dp),
                                minLines = 2
                            )
                        } else if (task.proofType == ProofType.VIDEO_WATCH.name) {
                            Text(
                                text = "Visionnage sponsorisé",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (videoSecondsWatched >= 5) "Vidéo terminée avec succès !" else "Visionnage en cours: $videoSecondsWatched / 5 secondes",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = proofText,
                                onValueChange = { proofText = it },
                                label = { Text("Code de confirmation ou mot clé retenu") },
                                placeholder = { Text("Ex: FINTECH-RDC-2026") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        } else {
                            // Micro-task or App test
                            Text(
                                text = "Preuve de réalisation",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Saisissez les informations demandées par le sponsor pour valider votre tâche :",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = proofText,
                                onValueChange = { proofText = it },
                                label = { Text("Détails / N° transaction / Compte-rendu") },
                                placeholder = { Text("Ex: Reçu vérifié N° 48921-Gombe, test d'inscription effectué...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("task_proof_input"),
                                shape = RoundedCornerShape(10.dp),
                                minLines = 3
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (isVerifyingAntiFraud) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = EmeraldGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Vérification anti-fraude en cours...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    val finalProof = if (task.proofType == ProofType.SURVEY_FORM.name) {
                                        "Opérateur: $surveyQ1Answer | Fréquence: $surveyQ2Answer | Commentaire: $proofText"
                                    } else {
                                        if (proofText.isEmpty()) "Vérification standard validée" else proofText
                                    }
                                    isVerifyingAntiFraud = true
                                    onSubmitProof(finalProof)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("submit_task_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                Text(
                                    text = "Valider et Soumettre",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
