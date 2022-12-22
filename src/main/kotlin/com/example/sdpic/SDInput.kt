package com.example.sdpic

import com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema

@JsonSerializableSchema
data class SDInput(
    var prompt: String = "",
    var negativePrompt: String = "",
    var steps: Int = 30,
    var cfg_scale: Float = 5f
)
