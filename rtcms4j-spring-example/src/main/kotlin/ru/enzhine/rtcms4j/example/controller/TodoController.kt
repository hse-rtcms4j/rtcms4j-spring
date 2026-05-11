package ru.enzhine.rtcms4j.example.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import ru.enzhine.rtcms4j.example.config.props.TodoProperties
import ru.enzhine.rtcms4j.example.service.TodoService

@Controller
class TodoController(
    private val todoService: TodoService,
    private val todoProperties: TodoProperties,
) {
    @GetMapping("/")
    fun home(model: Model): String {
        model.addAttribute("props", todoProperties)
        model.addAttribute("todos", todoService.getAllTodos())
        model.addAttribute("priorities", TodoProperties.TodoPriority.values())
        return "index"
    }

    @PostMapping("/api/todos")
    @ResponseBody
    fun addTodo(
        @RequestParam text: String,
        @RequestParam priority: String,
    ): ResponseEntity<Map<String, Any>> {
        val priorityEnum =
            try {
                TodoProperties.TodoPriority.valueOf(priority)
            } catch (e: IllegalArgumentException) {
                todoProperties.defaultPriority
            }

        val result = todoService.addTodo(text, priorityEnum)
        return when (result) {
            is TodoService.Result.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "success" to true,
                        "message" to todoProperties.successMessage,
                        "todo" to
                            mapOf(
                                "id" to result.value.id,
                                "text" to result.value.text,
                                "completed" to result.value.completed,
                                "priority" to result.value.priority.name,
                            ),
                    ),
                )

            is TodoService.Result.Error ->
                ResponseEntity.badRequest().body(
                    mapOf(
                        "success" to false,
                        "message" to result.message,
                    ),
                )
        }
    }

    @PutMapping("/api/todos/{id}/toggle")
    @ResponseBody
    fun toggleTodo(
        @PathVariable id: Long,
    ): ResponseEntity<Map<String, Any>> {
        val result = todoService.toggleTodo(id)
        return when (result) {
            is TodoService.Result.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "success" to true,
                        "todo" to
                            mapOf(
                                "id" to result.value.id,
                                "completed" to result.value.completed,
                            ),
                    ),
                )

            is TodoService.Result.Error ->
                ResponseEntity.badRequest().body(
                    mapOf(
                        "success" to false,
                        "message" to result.message,
                    ),
                )
        }
    }

    @DeleteMapping("/api/todos/{id}")
    @ResponseBody
    fun deleteTodo(
        @PathVariable id: Long,
    ): ResponseEntity<Map<String, Any>> {
        val result = todoService.deleteTodo(id)
        return when (result) {
            is TodoService.Result.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "success" to true,
                        "message" to "Todo deleted successfully",
                    ),
                )

            is TodoService.Result.Error ->
                ResponseEntity.badRequest().body(
                    mapOf(
                        "success" to false,
                        "message" to result.message,
                    ),
                )
        }
    }
}
