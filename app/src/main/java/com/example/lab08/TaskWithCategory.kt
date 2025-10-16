package com.example.lab08

import androidx.room.ColumnInfo

// TaskWithCategory.kt - nuevo archivo
data class TaskWithCategory(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean,
    @ColumnInfo(name = "priority") val priority: Priority,
    @ColumnInfo(name = "category_id") val categoryId: Long?,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "category_color") val categoryColor: Int?
)