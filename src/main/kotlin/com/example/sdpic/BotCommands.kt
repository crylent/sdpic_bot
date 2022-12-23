package com.example.sdpic

enum class BotCommands(val cmd: String) {
    START("/start"),
    SET_PROMPT("✅ Set Prompt"),
    SET_NEGATIVE_PROMPT("❌ Set Negative Prompt"),
    CREATE("✏ CREATE")
}