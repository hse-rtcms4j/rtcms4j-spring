package ru.enzhine.rtcms4j.example.service

import org.springframework.stereotype.Service
import ru.enzhine.rtcms4j.example.config.props.TodoProperties
import ru.enzhine.rtcms4j.example.model.Todo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service
class TodoService(
    private val todoProperties: TodoProperties,
) {
    private val todos = ConcurrentHashMap<Long, Todo>()
    private val idGenerator = AtomicLong(1)

    init {
        addTodo("Buy groceries")
        addTodo("Complete project documentation")
        addTodo("Review pull requests")
    }

    fun getAllTodos(): List<Todo> = todos.values.toList()

    fun addTodo(
        text: String,
        priority: TodoProperties.TodoPriority = todoProperties.defaultPriority,
    ): Result<Todo> =
        when {
            todos.size >= todoProperties.maxTodos ->
                Result.Error(todoProperties.maxTodosErrorMessage.format(todoProperties.maxTodos))
            text.length > todoProperties.maxTodoLength ->
                Result.Error(todoProperties.tooLongErrorMessage.format(todoProperties.maxTodoLength))
            else -> {
                val todo = Todo(idGenerator.getAndIncrement(), text, false, priority)
                todos[todo.id] = todo
                Result.Success(todo)
            }
        }

    fun toggleTodo(id: Long): Result<Todo> {
        val todo = todos[id] ?: return Result.Error("Todo not found")
        val updated = todo.copy(completed = !todo.completed)
        todos[id] = updated
        return Result.Success(updated)
    }

    fun deleteTodo(id: Long): Result<Unit> =
        if (todoProperties.allowDeletion && todos.remove(id) != null) {
            Result.Success(Unit)
        } else if (!todoProperties.allowDeletion) {
            Result.Error("Deletion is currently disabled")
        } else {
            Result.Error("Todo not found")
        }

    sealed class Result<out T> {
        data class Success<T>(
            val value: T,
        ) : Result<T>()

        data class Error(
            val message: String,
        ) : Result<Nothing>()
    }
}
