package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logged_checklist_events")
data class LoggedChecklistEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val checklistId: Long,
    val dateString: String, // Format: YYYY-MM-DD
    val expenseId: Long
)
