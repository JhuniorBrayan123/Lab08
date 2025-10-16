package com.example.lab08

import androidx.compose.material3.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import kotlinx.coroutines.launch
import com.example.lab08.ui.theme.Lab08Theme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.vector.ImageVector




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab08Theme {
                val db = Room.databaseBuilder(
                    applicationContext,
                    TaskDatabase::class.java,
                    "task_db"
                ).fallbackToDestructiveMigration().build()


                val taskDao = db.taskDao()
                val categoryDao = db.categoryDao() // OBTENER categoryDao

                val viewModel = TaskViewModel(taskDao, categoryDao) // PASAR AMBOS DAOs


                TaskScreen(viewModel)
            }
        }
    }
}

@Composable
fun PriorityIndicator(priority: Priority, modifier: Modifier = Modifier) {
    val (color, text) = when (priority) {
        Priority.LOW -> Color.Green to "Baja"
        Priority.MEDIUM -> Color.Yellow to "Media"
        Priority.HIGH -> Color.Red to "Alta"
    }

    Box(
        modifier = modifier
            .background(color, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PrioritySelector(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        Priority.entries.forEach { priority ->
            val isSelected = priority == selectedPriority
            val color = when (priority) {
                Priority.LOW -> Color.Green
                Priority.MEDIUM -> Color.Yellow
                Priority.HIGH -> Color.Red
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isSelected) color else color.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .clickable { onPrioritySelected(priority) }
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = Color.Black,
                        shape = CircleShape
                    )
            ) {
                Text(
                    text = when (priority) {
                        Priority.LOW -> "B"
                        Priority.MEDIUM -> "M"
                        Priority.HIGH -> "A"
                    },
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    var newTaskDescription by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    val currentFilter by viewModel.currentFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentSort by viewModel.currentSort.collectAsState()
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var editedDescription by remember { mutableStateOf("") }
    var editedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var editedCategory by remember { mutableStateOf<Long?>(null) }

    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    // ==== DIÁLOGO DE EDICIÓN CORREGIDO ====
    if (taskToEdit != null) {
        taskToEdit?.let { task ->
            EditTaskDialog(
                task = task,
                onDismiss = {
                    taskToEdit = null
                },
                onConfirm = { newDescription, newPriority ->
                    // Actualizar descripción si cambió
                    if (task.description != newDescription) {
                        viewModel.updateTaskDescription(task, newDescription)
                    }
                    // Actualizar prioridad si cambió
                    if (task.priority != newPriority) {
                        viewModel.updateTaskPriority(task, newPriority)
                    }
                    taskToEdit = null
                }
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Barra de búsqueda
        item {
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.searchTasks(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Selector de categorías
        item {
            CategorySelector(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.setSelectedCategory(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Fila para Filtros y Ordenamiento
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filtros
                Column(modifier = Modifier.weight(1f)) {
                    Text("Filtrar:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    FilterSelector(
                        currentFilter = currentFilter,
                        onFilterSelected = { viewModel.setFilter(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Ordenamiento
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ordenar:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    SortSelector(
                        currentSort = currentSort,
                        onSortSelected = { viewModel.setSort(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Estado de búsqueda/filtro
        item {
            Text(
                text = buildString {
                    when {
                        searchQuery.isNotEmpty() -> append("Buscando: '$searchQuery'")
                        currentFilter != TaskViewModel.TaskFilter.ALL -> append("Filtro: ${currentFilter.name}")
                        else -> append("Todas las tareas")
                    }
                    append(" • ")
                    append(when (currentSort) {
                        TaskViewModel.TaskSort.NAME_ASC -> "Orden: A-Z"
                        TaskViewModel.TaskSort.NAME_DESC -> "Orden: Z-A"
                        TaskViewModel.TaskSort.PRIORITY_DESC -> "Orden: Alta prioridad"
                        TaskViewModel.TaskSort.PRIORITY_ASC -> "Orden: Baja prioridad"
                        TaskViewModel.TaskSort.CREATION_DATE_DESC -> "Orden: Recientes"
                        TaskViewModel.TaskSort.CREATION_DATE_ASC -> "Orden: Antiguas"
                        TaskViewModel.TaskSort.COMPLETED_FIRST -> "Orden: Completadas"
                        TaskViewModel.TaskSort.PENDING_FIRST -> "Orden: Pendientes"
                    })
                    append(" • ${tasks.size} tareas")
                },
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Campo de texto para nueva tarea
        item {
            TextField(
                value = newTaskDescription,
                onValueChange = { newTaskDescription = it },
                label = { Text("Nueva tarea") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Selector de prioridad
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item {
            Text("Prioridad:", fontWeight = FontWeight.Bold)
        }
        item {
            PrioritySelector(
                selectedPriority = selectedPriority,
                onPrioritySelected = { selectedPriority = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Botón para agregar tarea
        item {
            Button(
                onClick = {
                    if (newTaskDescription.isNotEmpty()) {
                        viewModel.addTask(newTaskDescription, selectedPriority)
                        newTaskDescription = ""
                        selectedPriority = Priority.MEDIUM
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Agregar tarea")
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // LISTA DE TAREAS CON MANEJO DE VACÍO
        if (tasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            searchQuery.isNotEmpty() -> "No se encontraron tareas con '$searchQuery'"
                            currentFilter != TaskViewModel.TaskFilter.ALL -> "No hay tareas con este filtro"
                            else -> "No hay tareas. ¡Agrega una nueva!"
                        },
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            // Lista de tareas
            items(tasks) { task ->
                TaskItem(
                    task = task,
                    onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                    onUpdatePriority = { newPriority ->
                        viewModel.updateTaskPriority(task, newPriority)
                    },
                    onDeleteTask = {
                        viewModel.deleteTask(task)
                    },
                    onEditTask = { taskToEditParam, newDescription, newPriority ->
                        // Abrir diálogo de edición
                        taskToEdit = taskToEditParam
                        editedDescription = taskToEditParam.description
                        editedPriority = taskToEditParam.priority
                        editedCategory = taskToEditParam.categoryId
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Botón para eliminar todas
        item {
            Button(
                onClick = { viewModel.deleteAllTasks() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar todas las tareas")
            }
        }
    }
}
@Composable
fun TaskItem(
    task: Task,
    onToggleCompletion: () -> Unit,
    onUpdatePriority: (Priority) -> Unit,
    onDeleteTask: () -> Unit,  // <- FALTABA ESTA COMA
    onEditTask: (Task, String, Priority) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Descripción y prioridad
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Spacer(modifier = Modifier.height(4.dp))
                PriorityIndicator(priority = task.priority)
            }

            // Controles de la tarea
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Selector de prioridad rápido
                PrioritySelector(
                    selectedPriority = task.priority,
                    onPrioritySelected = onUpdatePriority,
                    modifier = Modifier.width(120.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Botón de editar (NUEVO)
                Button(
                    onClick = { onEditTask(task, task.description, task.priority) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("E", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón de completado
                Button(
                    onClick = onToggleCompletion,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.isCompleted) Color.Green else Color.Gray
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Text(if (task.isCompleted) "✓" else "○")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón de eliminar
                Button(
                    onClick = onDeleteTask,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("X", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onConfirm: (String, Priority) -> Unit
) {
    var editedDescription by remember { mutableStateOf(task.description) }
    var editedPriority by remember { mutableStateOf(task.priority) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar tarea") },
        text = {
            Column {
                TextField(
                    value = editedDescription,
                    onValueChange = { editedDescription = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Prioridad:", fontWeight = FontWeight.Bold)
                PrioritySelector(
                    selectedPriority = editedPriority,
                    onPrioritySelected = { editedPriority = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(editedDescription, editedPriority) },
                enabled = editedDescription.isNotEmpty()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
// En MainActivity.kt - agregar este composable
@Composable
fun FilterSelector(
    currentFilter: TaskViewModel.TaskFilter,
    onFilterSelected: (TaskViewModel.TaskFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = when (currentFilter) {
                    TaskViewModel.TaskFilter.ALL -> "Todas las tareas"
                    TaskViewModel.TaskFilter.COMPLETED -> "Completadas"
                    TaskViewModel.TaskFilter.PENDING -> "Pendientes"
                    TaskViewModel.TaskFilter.HIGH_PRIORITY -> "Alta prioridad"
                    TaskViewModel.TaskFilter.MEDIUM_PRIORITY -> "Media prioridad"
                    TaskViewModel.TaskFilter.LOW_PRIORITY -> "Baja prioridad"
                }
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Filtros"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todas las tareas") },
                onClick = {
                    onFilterSelected(TaskViewModel.TaskFilter.ALL)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Completadas") },
                onClick = {
                    onFilterSelected(TaskViewModel.TaskFilter.COMPLETED)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Pendientes") },
                onClick = {
                    onFilterSelected(TaskViewModel.TaskFilter.PENDING)
                    expanded = false
                }
            )
            Divider()
            DropdownMenuItem(
                text = { Text("Alta prioridad") },
                onClick = {
                    onFilterSelected(TaskViewModel.TaskFilter.HIGH_PRIORITY)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Media prioridad") },
                onClick = {
                    onFilterSelected(TaskViewModel.TaskFilter.MEDIUM_PRIORITY)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Baja prioridad") },
                onClick = {
                    onFilterSelected(TaskViewModel.TaskFilter.LOW_PRIORITY)
                    expanded = false
                }
            )
        }
    }
}
// En MainActivity.kt - agregar este composable
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(searchQuery) }

    TextField(
        value = text,
        onValueChange = {
            text = it
            onSearchQueryChange(it)
        },
        placeholder = { Text("Buscar tareas...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar"
            )
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = {
                    text = ""
                    onSearchQueryChange("")
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Limpiar búsqueda"
                    )
                }
            }
        },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}
// En MainActivity.kt - agregar este composable
@Composable
fun SortSelector(
    currentSort: TaskViewModel.TaskSort,
    onSortSelected: (TaskViewModel.TaskSort) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = when (currentSort) {
                    TaskViewModel.TaskSort.NAME_ASC -> "Nombre (A-Z)"
                    TaskViewModel.TaskSort.NAME_DESC -> "Nombre (Z-A)"
                    TaskViewModel.TaskSort.PRIORITY_DESC -> "Prioridad (alta a baja)"
                    TaskViewModel.TaskSort.PRIORITY_ASC -> "Prioridad (baja a alta)"
                    TaskViewModel.TaskSort.CREATION_DATE_DESC -> "Más recientes"
                    TaskViewModel.TaskSort.CREATION_DATE_ASC -> "Más antiguas"
                    TaskViewModel.TaskSort.COMPLETED_FIRST -> "Completadas primero"
                    TaskViewModel.TaskSort.PENDING_FIRST -> "Pendientes primero"
                }
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Ordenar"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Orden por nombre
            DropdownMenuItem(
                text = { Text("Nombre (A-Z)") },
                onClick = {
                    onSortSelected(TaskViewModel.TaskSort.NAME_ASC)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Nombre (Z-A)") },
                onClick = {
                    onSortSelected(TaskViewModel.TaskSort.NAME_DESC)
                    expanded = false
                }
            )

            Divider()

            // Orden por prioridad
            DropdownMenuItem(
                text = { Text("Prioridad (alta a baja)") },
                onClick = {
                    onSortSelected(TaskViewModel.TaskSort.PRIORITY_DESC)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Prioridad (baja a alta)") },
                onClick = {
                    onSortSelected(TaskViewModel.TaskSort.PRIORITY_ASC)
                    expanded = false
                }
            )

            Divider()

            // Orden por fecha
            DropdownMenuItem(
                text = { Text("Más recientes") },
                onClick = {
                    onSortSelected(TaskViewModel.TaskSort.CREATION_DATE_DESC)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Más antiguas") },
                onClick = {
                    onSortSelected(TaskViewModel.TaskSort.CREATION_DATE_ASC)
                    expanded = false
                }
            )

            Divider()

            // Orden por estado
            DropdownMenuItem(
                text = { Text("Completadas primero") },
                onClick = {
                    onSortSelected(TaskViewModel.TaskSort.COMPLETED_FIRST)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Pendientes primero") },
                onClick = {
                    onSortSelected(TaskViewModel.TaskSort.PENDING_FIRST)
                    expanded = false
                }
            )
        }
    }
}
// En MainActivity.kt - agregar composables
@Composable
fun CategoryChip(
    category: Category?,
    isSelected: Boolean,
    onCategorySelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryName = category?.name ?: "Sin categoría"
    val categoryColor = category?.color ?: 0xFF757575.toInt()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(categoryColor) else Color(categoryColor).copy(alpha = 0.2f),
        border = if (isSelected) BorderStroke(2.dp, Color(categoryColor)) else null,
        modifier = modifier
            .clickable {
                onCategorySelected(category?.id)
            }
    ) {
        Text(
            text = categoryName,
            color = if (isSelected) Color.White else Color(categoryColor),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun CategorySelector(
    categories: List<Category>,
    selectedCategory: Long?,
    onCategorySelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Categorías:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Chip para "Todas las categorías"
            item {
                CategoryChip(
                    category = null,
                    isSelected = selectedCategory == null,
                    onCategorySelected = onCategorySelected
                )
            }
            // Chips para cada categoría
            items(categories) { category ->
                CategoryChip(
                    category = category,
                    isSelected = selectedCategory == category.id,
                    onCategorySelected = onCategorySelected
                )
            }
        }
    }
}