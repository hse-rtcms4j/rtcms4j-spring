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

    override fun extractVersionFromValuesString(values: String): Pair<String, String> {
        val objectNode = objectMapper.readTree(values) as ObjectNode
        val versionNode = objectNode.remove("version")
        return versionNode.asText() to objectNode.toString()
    }
}
