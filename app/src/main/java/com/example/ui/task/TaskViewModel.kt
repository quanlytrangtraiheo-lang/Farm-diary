package com.example.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.TaskEntity
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilterOption(val label: String) {
    ALL("Tất cả"),
    TODAY("Hôm nay"),
    THIS_WEEK("Tuần này"),
    TODO("Chưa làm"),
    IN_PROGRESS("Đang làm"),
    DONE("Đã xong")
}

data class TaskUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: TaskFilterOption = TaskFilterOption.ALL
)

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeFilter = MutableStateFlow(TaskFilterOption.ALL)
    val activeFilter: StateFlow<TaskFilterOption> = _activeFilter.asStateFlow()

    val uiState: StateFlow<TaskUiState> = combine(
        taskRepository.allTasks,
        _searchQuery,
        _activeFilter
    ) { tasks, query, filter ->
        val filtered = tasks.filter { task ->
            val matchesQuery = query.isBlank() ||
                task.title.contains(query, ignoreCase = true) ||
                task.referenceId.contains(query, ignoreCase = true) ||
                task.locationInfo.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                TaskFilterOption.ALL -> true
                TaskFilterOption.TODAY -> true // In demo all relevant tasks are today/this week
                TaskFilterOption.THIS_WEEK -> true
                TaskFilterOption.TODO -> task.status == TaskStatus.TODO || task.status == TaskStatus.OVERDUE
                TaskFilterOption.IN_PROGRESS -> task.status == TaskStatus.IN_PROGRESS
                TaskFilterOption.DONE -> task.status == TaskStatus.DONE
            }

            matchesQuery && matchesFilter
        }

        TaskUiState(
            tasks = filtered,
            searchQuery = query,
            activeFilter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: TaskFilterOption) {
        _activeFilter.value = filter
    }

    fun toggleTaskStatus(task: TaskEntity) {
        viewModelScope.launch {
            val nextStatus = when (task.status) {
                TaskStatus.TODO -> TaskStatus.IN_PROGRESS
                TaskStatus.IN_PROGRESS -> TaskStatus.DONE
                TaskStatus.DONE -> TaskStatus.TODO
                TaskStatus.OVERDUE -> TaskStatus.DONE
            }
            taskRepository.updateTaskStatus(task.id, nextStatus)
        }
    }

    fun addTask(
        title: String,
        taskType: TaskType,
        timeLabel: String,
        referenceId: String,
        locationInfo: String,
        priority: TaskPriority,
        note: String
    ) {
        viewModelScope.launch {
            taskRepository.addTask(
                title = title,
                taskType = taskType,
                dueDate = System.currentTimeMillis(),
                timeLabel = timeLabel,
                assignedTo = "Kỹ thuật viên chuồng",
                priority = priority,
                referenceId = referenceId,
                locationInfo = locationInfo,
                note = note
            )
        }
    }
}
