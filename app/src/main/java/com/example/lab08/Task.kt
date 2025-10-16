package com.example.lab08

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "priority") val priority: Priority = Priority.MEDIUM,
    @ColumnInfo(name = "category_id") val categoryId: Long? = null // Nueva relación

)

enum class Priority {
    LOW,    // Baja prioridad
    MEDIUM, // Prioridad media
    HIGH    // Alta prioridad
}