package com.example.lab08

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<Task>

    @Insert
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)  // Asegúrate de tener esta línea

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
    
    @Query("SELECT * FROM tasks WHERE is_completed = 1")
    suspend fun getCompletedTasks(): List<Task>
    
    @Query("SELECT * FROM tasks WHERE is_completed = 0")
    suspend fun getPendingTasks(): List<Task>
    
    @Query("SELECT * FROM tasks WHERE priority = 'HIGH'")
    suspend fun getHighPriorityTasks(): List<Task>
    
    @Query("SELECT * FROM tasks WHERE priority = 'MEDIUM'")
    suspend fun getMediumPriorityTasks(): List<Task>
    
    @Query("SELECT * FROM tasks WHERE priority = 'LOW'")
    suspend fun getLowPriorityTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE description LIKE :query")
    suspend fun searchTasks(query: String): List<Task>
    // NUEVAS: Consultas por categoría
    @Query("SELECT * FROM tasks WHERE category_id = :categoryId")
    suspend fun getTasksByCategory(categoryId: Long): List<Task>

    @Query("SELECT * FROM tasks WHERE category_id IS NULL")
    suspend fun getTasksWithoutCategory(): List<Task>

    // Consulta con JOIN para obtener tareas con información de categoría
    @Query("""
        SELECT tasks.id, tasks.description, tasks.is_completed, tasks.priority, 
               tasks.category_id, categories.name as category_name, categories.color as category_color 
        FROM tasks 
        LEFT JOIN categories ON tasks.category_id = categories.id
    """)
    suspend fun getTasksWithCategoryInfo(): List<TaskWithCategory>

}

