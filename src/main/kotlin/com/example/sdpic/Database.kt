package com.example.sdpic

import com.microsoft.sqlserver.jdbc.SQLServerException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.sql.DriverManager

@Component
class Database(
    @Value("\${db.user}") private val user: String,
    @Value("\${db.password}") private val pass: String,
    @Value("\${db.dbname}") private val name: String,
    @Value("\${db.port}") private val port: String,
    @Value("\${db.type}") private val type: String
) {
    private val jdbcUrl = "jdbc:$type://localhost:$port;databaseName=$name;trustServerCertificate=true"
    private val connection = DriverManager.getConnection(jdbcUrl, user, pass)

    fun newUser(chatId: Long): Boolean {
        val query = connection.prepareStatement(
            "INSERT INTO bot.UserData (ChatID) VALUES ($chatId)"
        )
        return try {
            query.executeUpdate()
            true
        }
        catch (_: SQLServerException) { false }
    }

    fun getUserInput(chatId: Long): SDInput {
        val query = connection.prepareStatement(
            "SELECT Prompt, NegativePrompt FROM bot.UserData WHERE ChatID = $chatId"
        )
        val result = query.executeQuery().apply { next() }
        return SDInput(
            result.getString("Prompt"),
            result.getString("NegativePrompt")
        )
    }

    fun updateUserInput(chatId: Long, type: InputType, input: String) {
        val query = connection.prepareStatement(
            "UPDATE bot.UserData SET ${type.columnName} = '${input.replace("'", "")}' WHERE ChatID = $chatId"
        )
        query.executeUpdate()
    }
}