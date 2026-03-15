package dto

data class EndpointProperties(
    val enableValidation: Boolean = false,
    val validationPattern: String = "^.*$",
    val defaultBatchSize: Int = 20,
    val defaultCardinalities: List<Int> = listOf(2, 4, 8),
    val allowSender: SenderType = SenderType.USER,
) {
    enum class SenderType {
        USER,
        CLIENT,
        UNKNOWN,
    }
}
