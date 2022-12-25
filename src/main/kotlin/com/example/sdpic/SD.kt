package com.example.sdpic

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Base64

@Component
class SD(
    @Value("\${sd.api.txt2img}") val txt2img: String,
    @Value("\${sd.host}") private val host: String,
    @Value("\${sd.port}") private val port: String
) {
    private val http = HttpClient.newHttpClient()

    private fun getUri(apiPath: String) = URI("http://${host}:${port}${apiPath}")

    fun createImage(input: SDInput, apiPath: String): ByteArrayInputStream {
        val request = jsonMapper().writeValueAsString(input)
        val httpRequest = HttpRequest.newBuilder()
            .uri(getUri(apiPath))
            .POST(HttpRequest.BodyPublishers.ofString(request))
            .build()
        val httpResponse = http.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body()
        val response = ObjectMapper().readValue<SDResponse>(httpResponse)
        return ByteArrayInputStream(
            Base64.getDecoder().decode(response.images[0])
        )
    }
}