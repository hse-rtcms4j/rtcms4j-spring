package ru.enzhine.rtcms4j.example.config.props

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.annotation.RemoteConfiguration

@RemoteConfiguration(version = "1.0.0")
@Component
class TodoProperties(
    @field:JsonPropertyDescription("Maximum number of todos allowed")
    val maxTodos: Int = 10,
    @field:JsonPropertyDescription("Maximum length of todo text")
    val maxTodoLength: Int = 50,
    @field:JsonPropertyDescription("Whether to show completion checkboxes")
    val showCompletionCheckboxes: Boolean = true,
    @field:JsonPropertyDescription("Whether to allow todo deletion")
    val allowDeletion: Boolean = true,
    @field:JsonPropertyDescription("Message shown when adding todo")
    val successMessage: String = "Todo added successfully!",
    @field:JsonPropertyDescription("Message shown when max todos reached")
    val maxTodosErrorMessage: String = "Maximum limit of %d todos reached!",
    @field:JsonPropertyDescription("Message shown when todo text too long")
    val tooLongErrorMessage: String = "Todo text cannot exceed %d characters!",
    @field:JsonPropertyDescription("Title of the application")
    val appTitle: String = "Real-time Controlled Todo App",
    @field:JsonPropertyDescription("Header text to display")
    val headerText: String = "My Dynamic Todo List",
    @field:JsonPropertyDescription("Color theme (light/dark)")
    val colorTheme: ColorTheme = ColorTheme.LIGHT,
    @field:JsonPropertyDescription("Whether to auto-refresh the list")
    val autoRefresh: Boolean = false,
    @field:JsonPropertyDescription("Refresh interval in seconds (if auto-refresh enabled)")
    val refreshIntervalSeconds: Int = 5,
    @field:JsonPropertyDescription("Default todo priority")
    val defaultPriority: TodoPriority = TodoPriority.MEDIUM,
    @field:JsonPropertyDescription("Whether to show priority badges")
    val showPriorityBadges: Boolean = true,
) {
    enum class TodoPriority {
        LOW,
        MEDIUM,
        HIGH,
    }

    enum class ColorTheme {
        LIGHT,
        DARK,
    }
}
