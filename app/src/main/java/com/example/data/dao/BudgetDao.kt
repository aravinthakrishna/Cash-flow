package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BudgetSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_settings WHERE id = 1 LIMIT 1")
    fun getBudgetSetting(): Flow<BudgetSetting?>

    @Query("SELECT * FROM budget_settings WHERE id = 1 LIMIT 1")
    suspend fun getBudgetSettingDirect(): BudgetSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudgetSetting(setting: BudgetSetting)
}
