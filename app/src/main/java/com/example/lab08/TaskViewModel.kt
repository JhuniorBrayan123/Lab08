package com.example.lab08

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class TaskViewModel(private val taskDao: TaskDao,
                    private val categoryDao: CategoryDao) : ViewModel() {

    // Estado existente...
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks


    // NUEVO: Estado para el filtro actual
    private val _currentFilter = MutableStateFlow<TaskFilter>(TaskFilter.ALL)
    val currentFilter: StateFlow<TaskFilter> = _currentFilter
    // NUEVO: Estado para búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    private val _currentSort = MutableStateFlow<TaskSort>(TaskSort.CREATION_DATE_DESC)
    val currentSort: StateFlow<TaskSort> = _currentSort

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _selectedCategory = MutableStateFlow<Long?>(null)
    val selectedCategory: StateFlow<Long?> = _selectedCategory

    // NUEVO: Inicializar categorías
    init {
        viewModelScope.launch {
            _categories.value = categoryDao.getAllCategories()
            // Crear categorías por defecto si no existen
            if (_categories.value.isEmpty()) {
                createDefaultCategories()
            }
        }
    }
    // NUEVO: Crear categorías por defecto
    private suspend fun createDefaultCategories() {
        val defaultCategories = listOf(
            Category(name = "Trabajo", color = 0xFF2196F3.toInt()),
            Category(name = "Personal", color = 0xFF4CAF50.toInt()),
            Category(name = "Estudio", color = 0xFFFF9800.toInt()),
            Category(name = "Hogar", color = 0xFF9C27B0.toInt())
        )
        defaultCategories.forEach { categoryDao.insertCategory(it) }
        _categories.value = categoryDao.getAllCategories()
    }
    // NUEVO: Funciones para categorías
    fun setSelectedCategory(categoryId: Long?) {
        _selectedCategory.value = categoryId
        applyCurrentSortAndFilter()
    }

    fun addCategory(name: String, color: Int) {
        viewModelScope.launch {
            val newCategory = Category(name = name, color = color)
            categoryDao.insertCategory(newCategory)
            _categories.value = categoryDao.getAllCategories()
        }
    }

    // NUEVO: Enum para tipos de ordenamiento
    enum class TaskSort {
        NAME_ASC, NAME_DESC,
        PRIORITY_DESC, PRIORITY_ASC,
        CREATION_DATE_DESC, CREATION_DATE_ASC,
        COMPLETED_FIRST, PENDING_FIRST
    }

    // NUEVO: Función para cambiar ordenamiento
    fun setSort(sort: TaskSort) {
        _currentSort.value = sort
        applyCurrentSortAndFilter()
    }

    // NUEVO: Aplicar ordenamiento y filtro
    // NUEVO: Aplicar ordenamiento y filtro
    private fun applyCurrentSortAndFilter() {
        viewModelScope.launch {
            var filteredTasks = when (_currentFilter.value) {
                TaskFilter.ALL -> taskDao.getAllTasks()                    // dao → taskDao
                TaskFilter.COMPLETED -> taskDao.getCompletedTasks()        // dao → taskDao
                TaskFilter.PENDING -> taskDao.getPendingTasks()           // dao → taskDao
                TaskFilter.HIGH_PRIORITY -> taskDao.getHighPriorityTasks() // dao → taskDao
                TaskFilter.MEDIUM_PRIORITY -> taskDao.getMediumPriorityTasks() // dao → taskDao
                TaskFilter.LOW_PRIORITY -> taskDao.getLowPriorityTasks()   // dao → taskDao
            }

            // Aplicar filtro de categoría
            val categoryId = _selectedCategory.value
            if (categoryId != null) {
                filteredTasks = filteredTasks.filter { it.categoryId == categoryId }
            }

            // Aplicar búsqueda si existe
            val query = _searchQuery.value
            if (query.isNotEmpty()) {
                val searchResults = taskDao.searchTasks("%$query%")        // dao → taskDao
                // Combinar con filtros existentes
                filteredTasks = filteredTasks.filter { task ->
                    searchResults.any { it.id == task.id }
                }
            }

            _tasks.value = when (_currentSort.value) {
                TaskSort.NAME_ASC -> filteredTasks.sortedBy { it.description }
                TaskSort.NAME_DESC -> filteredTasks.sortedByDescending { it.description }
                TaskSort.PRIORITY_DESC -> filteredTasks.sortedByDescending { it.priority }
                TaskSort.PRIORITY_ASC -> filteredTasks.sortedBy { it.priority }
                TaskSort.CREATION_DATE_DESC -> filteredTasks.sortedByDescending { it.id }
                TaskSort.CREATION_DATE_ASC -> filteredTasks.sortedBy { it.id }
                TaskSort.COMPLETED_FIRST -> filteredTasks.sortedByDescending { it.isCompleted }
                TaskSort.PENDING_FIRST -> filteredTasks.sortedBy { it.isCompleted }
            }
        }
    }

    // NUEVO: Función para buscar
    fun searchTasks(query: String) {
        _searchQuery.value = query
        applyCurrentSortAndFilter()
        viewModelScope.launch {
            if (query.isNotEmpty()) {
                _tasks.value = taskDao.searchTasks("%$query%")
            } else {
                // Si no hay query, aplicar el filtro actual
                setFilter(_currentFilter.value)
            }
        }
    }
    // NUEVO: Función para cambiar filtro
    fun setFilter(filter: TaskFilter) {
        _currentFilter.value = filter
        applyCurrentSortAndFilter()
        viewModelScope.launch {
            val query = _searchQuery.value
            _tasks.value = if (query.isNotEmpty()) {
                taskDao.searchTasks("%$query%")
            } else {
                when (filter) {
                    TaskFilter.ALL -> taskDao.getAllTasks()
                    TaskFilter.COMPLETED -> taskDao.getCompletedTasks()
                    TaskFilter.PENDING -> taskDao.getPendingTasks()
                    TaskFilter.HIGH_PRIORITY -> taskDao.getHighPriorityTasks()
                    TaskFilter.MEDIUM_PRIORITY -> taskDao.getMediumPriorityTasks()
                    TaskFilter.LOW_PRIORITY -> taskDao.getLowPriorityTasks()
                }
            }
        }
    }
    // NUEVO: Enum para los tipos de filtro
    enum class TaskFilter {
        ALL, COMPLETED, PENDING, HIGH_PRIORITY, MEDIUM_PRIORITY, LOW_PRIORITY
    }
    // Función existente para agregar tareas - MODIFICAR
    fun addTask(description: String, priority: Priority = Priority.MEDIUM) {
        val newTask = Task(description = description, priority = priority)

        viewModelScope.launch {
            taskDao.insertTask(newTask)
            applyCurrentSortAndFilter()
        }
    }

    // Función existente para toggle - se mantiene igual
    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            taskDao.updateTask(updatedTask)
            applyCurrentSortAndFilter()
        }
    }

    // NUEVA: Función para cambiar prioridad de una tarea
    fun updateTaskPriority(task: Task, newPriority: Priority) {
        viewModelScope.launch {
            val updatedTask = task.copy(priority = newPriority)
            taskDao.updateTask(updatedTask)
            applyCurrentSortAndFilter()
        }
    }

    // NUEVA: Función para eliminar tarea individual (preparamos para el paso 3)
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
            applyCurrentSortAndFilter()
        }
    }

    // Función existente para eliminar todas
    fun deleteAllTasks() {
        viewModelScope.launch {
            taskDao.deleteAllTasks()
            applyCurrentSortAndFilter()
        }
    }
    // En TaskViewModel.kt - agregar función
    fun updateTaskDescription(task: Task, newDescription: String) {
        viewModelScope.launch {
            val updatedTask = task.copy(description = newDescription)
            taskDao.updateTask(updatedTask)
            applyCurrentSortAndFilter()
        }
    }

    // Función para actualizar tarea completa
    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task)
            applyCurrentSortAndFilter()
        }
    }

    init {
        viewModelScope.launch {
            // Cargar tareas
            applyCurrentSortAndFilter()

            // Cargar categorías (ya lo tienes)
            _categories.value = categoryDao.getAllCategories()
            if (_categories.value.isEmpty()) {
                createDefaultCategories()
            }
        }
    }
}