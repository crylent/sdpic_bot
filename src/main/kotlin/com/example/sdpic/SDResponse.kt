package com.example.sdpic

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema

@JsonSerializableSchema
data class SDResponse(
    @JsonProperty("images") val images: List<String>,
    @JsonProperty("parameters") val parameters: Any,
    @JsonProperty("info") val info: String
)