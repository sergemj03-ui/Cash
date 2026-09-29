package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.PlatformConfigEntity
import com.example.data.model.ProofType
import com.example.data.model.TaskCategory
import com.example.data.model.TaskDifficulty
import com.example.data.model.TaskItemEntity
import com.example.data.model.TaskSubmissionEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.UserEntity
import com.example.data.model.WithdrawalEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TaskItemEntity::class,
        TaskSubmissionEntity::class,
        WithdrawalEntity::class,
        TransactionEntity::class,
        PlatformConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CashLinkDatabase : RoomDatabase() {

    abstract fun cashLinkDao(): CashLinkDao

    companion object {
        @Volatile
        private var INSTANCE: CashLinkDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CashLinkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CashLinkDatabase::class.java,
                    "cashlink_rdc_database"
                )
                    .addCallback(CashLinkDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class CashLinkDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.cashLinkDao())
                }
            }
        }

        suspend fun populateDatabase(dao: CashLinkDao) {
            // Seed User
            val initialUser = UserEntity(
                id = "user_rdc_001",
                name = "Serge Mukendi",
                email = "serge.mukendi@example.cd",
                phone = "+243 81 234 5678",
                country = "RDC",
                level = 2,
                xp = 420,
                nextLevelXp = 800,
                balanceUsd = 8.50,
                totalEarnedUsd = 24.20,
                earningsTodayUsd = 1.80,
                earningsThisWeekUsd = 5.20,
                earningsThisMonthUsd = 18.50,
                completedTasksCount = 14,
                referralCode = "CASHLINK-RD77",
                referralCount = 3,
                referralBonusAvailableUsd = 1.50,
                currencyPreference = "USD",
                languagePreference = "fr"
            )
            dao.insertUser(initialUser)

            // Seed Platform Config
            dao.insertPlatformConfig(
                PlatformConfigEntity(
                    usdToCdfRate = 2800.0,
                    minWithdrawalUsd = 3.0,
                    referralBonusRefereeThresholdTasks = 1,
                    referralBonusAmountUsd = 0.50,
                    totalPlatformPayoutsUsd = 14250.0,
                    totalRevenueGeneratedUsd = 38920.0
                )
            )

            // Seed Tasks
            val seedTasks = listOf(
                TaskItemEntity(
                    id = "task_survey_01",
                    title = "Sondage Habitudes Mobile Money en RDC",
                    description = "Donnez votre avis sur vos transferts et recharges d'unités via M-Pesa, Airtel Money et Orange Money à Kinshasa.",
                    category = TaskCategory.SURVEY.name,
                    rewardUsd = 0.85,
                    estimatedMinutes = 6,
                    totalSlots = 500,
                    remainingSlots = 142,
                    conditions = "Résider en RDC, avoir au moins 18 ans, utiliser un compte Mobile Money actif.",
                    instructions = "Répondez honnêtement aux 5 questions. Vos réponses aident les opérateurs à améliorer l'accès aux services financiers.",
                    proofType = ProofType.SURVEY_FORM.name,
                    sponsorName = "Observatoire FinTech Congo",
                    difficulty = TaskDifficulty.FACILE.name,
                    isFeatured = true
                ),
                TaskItemEntity(
                    id = "task_survey_02",
                    title = "Enquête Réseau Internet & Forfaits 4G/5G",
                    description = "Évaluez la qualité de connexion et le coût des forfaits data dans votre commune (Gombe, Lemba, Bandalungwa, etc.).",
                    category = TaskCategory.SURVEY.name,
                    rewardUsd = 0.65,
                    estimatedMinutes = 5,
                    totalSlots = 350,
                    remainingSlots = 88,
                    conditions = "Posséder un smartphone avec connexion données mobiles en RDC.",
                    instructions = "Indiquez votre commune et notez la fluidité des appels et du streaming selon votre opérateur.",
                    proofType = ProofType.SURVEY_FORM.name,
                    sponsorName = "Telecom Insights Africa",
                    difficulty = TaskDifficulty.FACILE.name
                ),
                TaskItemEntity(
                    id = "task_micro_01",
                    title = "Vérification Adresse Commerce Local à Kinshasa",
                    description = "Validez l'existence et les horaires d'un point de vente partenaire pour la mise à jour de la cartographie locale.",
                    category = TaskCategory.MICRO_TASK.name,
                    rewardUsd = 0.50,
                    estimatedMinutes = 4,
                    totalSlots = 200,
                    remainingSlots = 54,
                    conditions = "Photo claire de la devanture ou nom exact du commerce vérifié sur place.",
                    instructions = "Confirmez le nom de l'enseigne, l'adresse visible et cochez les moyens de paiement acceptés.",
                    proofType = ProofType.SCREENSHOT_UPLOAD.name,
                    sponsorName = "Kinshasa Digital Map",
                    difficulty = TaskDifficulty.FACILE.name
                ),
                TaskItemEntity(
                    id = "task_micro_02",
                    title = "Transcription de Reçu Commercial en Franc Congolais",
                    description = "Saisissez les montants et le numéro fiscal (NIF) d'un ticket de caisse pour notre moteur d'analyse comptable.",
                    category = TaskCategory.MICRO_TASK.name,
                    rewardUsd = 0.40,
                    estimatedMinutes = 3,
                    totalSlots = 400,
                    remainingSlots = 210,
                    conditions = "Attention à l'exactitude des chiffres en CDF.",
                    instructions = "Vérifiez que le total HT et TTC correspond parfaitement au montant scanné.",
                    proofType = ProofType.SCREENSHOT_UPLOAD.name,
                    sponsorName = "ComptaCongo Solutions",
                    difficulty = TaskDifficulty.FACILE.name
                ),
                TaskItemEntity(
                    id = "task_app_01",
                    title = "Test Bêta : Application Livraison Repas Kinshasa",
                    description = "Téléchargez l'application partenaire, créez un compte test et parcourez le menu de 3 restaurants locaux.",
                    category = TaskCategory.APP_TEST.name,
                    rewardUsd = 1.75,
                    estimatedMinutes = 12,
                    totalSlots = 150,
                    remainingSlots = 29,
                    conditions = "Smartphone Android 9+, connexion stable.",
                    instructions = "Naviguez dans l'application, essayez d'ajouter un plat au panier et partagez votre avis sur la rapidité de chargement.",
                    proofType = ProofType.APP_DOWNLOAD_TEST.name,
                    sponsorName = "KinExpress Delivery",
                    difficulty = TaskDifficulty.MOYEN.name,
                    isFeatured = true
                ),
                TaskItemEntity(
                    id = "task_app_02",
                    title = "Audit Ergonomie Site Web Agence Immobilière",
                    description = "Testez la recherche de logements locatifs à Lubumbashi et Kolwezi sur le nouveau portail immobilier.",
                    category = TaskCategory.APP_TEST.name,
                    rewardUsd = 1.25,
                    estimatedMinutes = 9,
                    totalSlots = 120,
                    remainingSlots = 43,
                    conditions = "Navigateur Chrome ou Firefox mis à jour.",
                    instructions = "Faites une simulation de recherche avec filtre de prix et signalez tout éventuel problème d'affichage.",
                    proofType = ProofType.LINK_VISIT.name,
                    sponsorName = "Katanga Immo Pro",
                    difficulty = TaskDifficulty.MOYEN.name
                ),
                TaskItemEntity(
                    id = "task_video_01",
                    title = "Capsule Vidéo : Lancement Nouvelle Carte Prépayée",
                    description = "Visionnez la présentation vidéo de 40 secondes et répondez à la question de validation pour débloquer votre gain.",
                    category = TaskCategory.VIDEO_SPONSOR.name,
                    rewardUsd = 0.30,
                    estimatedMinutes = 2,
                    totalSlots = 1000,
                    remainingSlots = 632,
                    conditions = "Regarder la vidéo en entier sans avancer.",
                    instructions = "Regardez le spot publicitaire jusqu'au bout pour obtenir le code de validation.",
                    proofType = ProofType.VIDEO_WATCH.name,
                    sponsorName = "Banque Commerciale Partenaire",
                    difficulty = TaskDifficulty.FACILE.name
                ),
                TaskItemEntity(
                    id = "task_mission_01",
                    title = "Client Mystère : Test Retrait Kiosque Mobile Money",
                    description = "Effectuez une simulation ou un retrait réel dans un point agréé et évaluez l'accueil et la disponibilité des coupures.",
                    category = TaskCategory.MISSION.name,
                    rewardUsd = 3.00,
                    estimatedMinutes = 15,
                    totalSlots = 50,
                    remainingSlots = 12,
                    conditions = "Niveau 2 requis (Utilisateur Actif). Compte-rendu détaillé exigé.",
                    instructions = "Notez le temps d'attente, l'affichage des tarifs réglementaires et la clarté du reçu délivré par l'agent.",
                    proofType = ProofType.SCREENSHOT_UPLOAD.name,
                    sponsorName = "Audit & Qualité Services RDC",
                    difficulty = TaskDifficulty.AVANCE.name,
                    isFeatured = true
                )
            )
            dao.insertTasks(seedTasks)

            // Seed Initial Transactions
            val seedTransactions = listOf(
                TransactionEntity(
                    id = "tx_01",
                    userId = "user_rdc_001",
                    title = "Sondage Habitudes Mobile Money",
                    type = TransactionType.TASK_REWARD.name,
                    amountUsd = 0.85,
                    isCredit = true,
                    timestamp = System.currentTimeMillis() - 86400000L * 2
                ),
                TransactionEntity(
                    id = "tx_02",
                    userId = "user_rdc_001",
                    title = "Bonus Parrainage (Filleul: Patrick K.)",
                    type = TransactionType.REFERRAL_BONUS.name,
                    amountUsd = 0.50,
                    isCredit = true,
                    timestamp = System.currentTimeMillis() - 86400000L
                ),
                TransactionEntity(
                    id = "tx_03",
                    userId = "user_rdc_001",
                    title = "Test Bêta KinExpress Delivery",
                    type = TransactionType.TASK_REWARD.name,
                    amountUsd = 1.75,
                    isCredit = true,
                    timestamp = System.currentTimeMillis() - 3600000L * 5
                )
            )
            dao.insertTransactions(seedTransactions)
        }
    }
}
