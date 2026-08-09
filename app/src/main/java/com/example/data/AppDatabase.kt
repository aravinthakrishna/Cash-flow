package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BudgetDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.PreferencesDao
import com.example.data.dao.RecurringItemDao
import com.example.data.model.BudgetSetting
import com.example.data.model.Expense
import com.example.data.model.LoggedChecklistEvent
import com.example.data.model.RecurringItem
import com.example.data.model.UserPreferenceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Expense::class,
        RecurringItem::class,
        LoggedChecklistEvent::class,
        BudgetSetting::class,
        UserPreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun recurringItemDao(): RecurringItemDao
    abstract fun budgetDao(): BudgetDao
    abstract fun preferencesDao(): PreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "money_manager_db"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: AppDatabase) {
                // Populate default budget
                db.budgetDao().setBudgetSetting(BudgetSetting(id = 1, monthlyBudget = 15000.0))

                // Populate default preferences
                db.preferencesDao().setUserPreferences(
                    UserPreferenceEntity(
                        id = 1,
                        darkModeEnabled = false,
                        trackingStreakCount = 1,
                        lastActiveDate = ""
                    )
                )

                // Populate default recurring items
                val defaultItems = listOf(
                    RecurringItem(name = "Breakfast", amount = 50.0, category = "Food", iconKey = "breakfast"),
                    RecurringItem(name = "Tea / Coffee", amount = 20.0, category = "Food", iconKey = "tea"),
                    RecurringItem(name = "Bus / Auto Pass", amount = 40.0, category = "Transport", iconKey = "bus"),
                    RecurringItem(name = "Lunch", amount = 120.0, category = "Food", iconKey = "lunch"),
                    RecurringItem(name = "Evening Snacks", amount = 30.0, category = "Food", iconKey = "snacks"),
                    RecurringItem(name = "Dinner", amount = 150.0, category = "Food", iconKey = "dinner")
                )
                for (item in defaultItems) {
                    db.recurringItemDao().insertRecurringItem(item)
                }
            }
        }
    }
}
