package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.repository.BreedingRepository
import com.example.data.repository.PigRepositoryImpl
import com.example.data.repository.SyncRepository
import com.example.data.repository.TaskRepository
import com.example.domain.repository.PigRepository
import com.example.sync.SyncManager
import com.example.ui.MainApp
import com.example.ui.herd.HerdViewModel
import com.example.ui.home.HomeViewModel
import com.example.ui.pig.PigListViewModel
import com.example.ui.pig.PigViewModel
import com.example.ui.report.ReportViewModel
import com.example.ui.settings.SettingsViewModel
import com.example.ui.task.TaskViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Khởi tạo kênh thông báo và đặt lịch quét cảnh báo tồn kho thức ăn
        com.example.notification.FeedStockNotificationHelper.createNotificationChannel(applicationContext)
        com.example.notification.FeedStockNotificationHelper.scheduleDailyFeedCheckAlarm(applicationContext)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        val database = AppDatabase.getDatabase(applicationContext)

        // Seed data verification on first launch
        lifecycleScope.launch(Dispatchers.IO) {
            val count = database.pigDao().getPigByTagCode("Heo nái NY-089")
            val invCount = database.inventoryDao().countItems()
            val masterCount = database.masterItemDao().countMasterItems()
            if (count == null || invCount == 0 || masterCount == 0) {
                AppDatabase.populateInitialFarmData(database)
            }
        }

        val syncManager = SyncManager(database)

        val pigRepository: PigRepository = PigRepositoryImpl(database.pigDao())
        val breedingRepository = BreedingRepository(
            database.breedingCycleDao(),
            database.pigDao(),
            database.eventDao(),
            database.taskDao()
        )
        val taskRepository = TaskRepository(database.taskDao())
        val syncRepository = SyncRepository(database.syncLogDao(), syncManager)
        val systemParameterRepository = com.example.data.repository.SystemParameterRepositoryImpl(database.systemParameterDao())
        val inventoryRepository = com.example.data.repository.InventoryRepositoryImpl(database.inventoryDao())
        val masterInventoryRepository = com.example.data.repository.MasterInventoryRepositoryImpl(
            database.masterItemDao(),
            database.receiptDao(),
            database.issueDao(),
            database.inventoryEntityDao()
        )
        val staffRepository = com.example.data.repository.StaffRepositoryImpl(database.staffDao())

        val farmRuleEngine = com.example.domain.rule.FarmRuleEngine(
            pigDao = database.pigDao(),
            taskDao = database.taskDao(),
            eventDao = database.eventDao(),
            breedingCycleDao = database.breedingCycleDao()
        )

        val homeViewModel = HomeViewModel(pigRepository, taskRepository, syncRepository, inventoryRepository, systemParameterRepository)
        val herdViewModel = HerdViewModel(pigRepository, breedingRepository, taskRepository)
        val pigViewModel = PigListViewModel(pigRepository, farmRuleEngine, systemParameterRepository)
        val taskViewModel = TaskViewModel(taskRepository)
        val reportViewModel = ReportViewModel(pigRepository, breedingRepository)
        val settingsViewModel = SettingsViewModel(syncRepository, systemParameterRepository)
        val inventoryViewModel = com.example.ui.inventory.InventoryViewModel(inventoryRepository, systemParameterRepository, pigRepository)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(
                        homeViewModel = homeViewModel,
                        herdViewModel = herdViewModel,
                        pigViewModel = pigViewModel,
                        taskViewModel = taskViewModel,
                        reportViewModel = reportViewModel,
                        settingsViewModel = settingsViewModel,
                        inventoryViewModel = inventoryViewModel
                    )
                }
            }
        }
    }
}
