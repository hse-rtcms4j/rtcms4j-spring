package ru.enzhine.rtcms4j.example.model

import ru.enzhine.rtcms4j.example.config.props.TodoProperties

data class Todo(
    val id: Long,
    val text: String,
    val completed: Boolean = false,
    val priority: TodoProperties.TodoPriority = TodoProperties.TodoPriority.MEDIUM,
)
