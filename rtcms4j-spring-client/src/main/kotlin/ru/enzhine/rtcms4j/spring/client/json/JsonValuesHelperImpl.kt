package ru.enzhine.rtcms4j.spring.client.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

class JsonValuesHelperImpl(
    private val objectMapper: ObjectMapper,
) : JsonValuesHelper {
    override fun generateValues(
        obj: Any,
        version: String,
    ): String {
        val objectNode = objectMapper.valueToTree<ObjectNode>(obj)

        addVersionField(objectNode, version)

        return objectNode.toPrettyString()
    }

    private fun addVersionField(
        objectNode: ObjectNode,
        version: String,
    ) {
        if (!objectNode.has("version")) {
            objectNode.put("version", version)
        }
    }

    override fun excludeVersionFromValuesString(values: String): String {
        val objectNode = objectMapper.readTree(values) as ObjectNode
        objectNode.remove("version")
        return objectNode.toString()
    }
}
