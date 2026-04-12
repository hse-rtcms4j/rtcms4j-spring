package ru.enzhine.rtcms4j.example.config.props

import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.annotation.RemoteConfiguration

@RemoteConfiguration(version = "1.0.0")
@Component
class OmniProps(
    val stringVal: String = "str",
    val stringList: List<String> = listOf("str1"),
    val intVal: Int = 42,
    val intList: List<Int> = listOf(1, 2, 3),
    val longVal: Long = 100L,
    val longList: List<Long> = listOf(1L, 2L, 3L),
    val floatVal: Float = 3.14f,
    val floatList: List<Float> = listOf(1.5f, 2.5f),
    val doubleVal: Double = 2.718,
    val doubleList: List<Double> = listOf(1.618, 3.14159),
    val booleanVal: Boolean = true,
    val booleanList: List<Boolean> = listOf(true, false),
    val enumVal: EnumValue = EnumValue.GRASS,
    val enumList: List<EnumValue> = listOf(EnumValue.GRASS, EnumValue.DIRT),
) {
    enum class EnumValue {
        GRASS,
        DIRT,
    }
}
