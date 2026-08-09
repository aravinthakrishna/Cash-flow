package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey
    val id: Int = 1,
    val darkModeEnabled: Boolean = false,
    val trackingStreakCount: Int = 1,
    val lastActiveDate: String = "" // YYYY-MM-DD
)
