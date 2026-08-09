package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LoggedChecklistEvent
import com.example.data.model.RecurringItem
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringItemDao {
    @Query("SELECT * FROM recurring_items ORDER BY id ASC")
    fun getAllRecurringItems(): Flow<List<RecurringItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringItem(item: RecurringItem): Long

    @Update
    suspend fun updateRecurringItem(item: RecurringItem)

    @Delete
    suspend fun deleteRecurringItem(item: RecurringItem)

    @Query("SELECT * FROM logged_checklist_events WHERE dateString = :dateString")
    fun getLoggedEventsForDate(dateString: String): Flow<List<LoggedChecklistEvent>>

    @Query("SELECT * FROM logged_checklist_events WHERE checklistId = :checklistId AND dateString = :dateString LIMIT 1")
    suspend fun getLoggedEvent(checklistId: Long, dateString: String): LoggedChecklistEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logChecklistEvent(event: LoggedChecklistEvent): Long

    @Query("DELETE FROM logged_checklist_events WHERE checklistId = :checklistId AND dateString = :dateString")
    suspend fun deleteLoggedEvent(checklistId: Long, dateString: String)

    @Query("DELETE FROM logged_checklist_events WHERE id = :id")
    suspend fun deleteLoggedEventById(id: Long)

    @Query("DELETE FROM recurring_items")
    suspend fun deleteAllRecurringItems()

    @Query("DELETE FROM logged_checklist_events")
    suspend fun deleteAllLoggedEvents()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllItems(items: List<RecurringItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEvents(events: List<LoggedChecklistEvent>)
}
