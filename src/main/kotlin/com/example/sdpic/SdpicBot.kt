package com.example.sdpic

import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.io.InputStream

@Service
final class SdpicBot(
    @Autowired private val sd: SD,
    @Autowired private val db: Database,
    @Value("\${telegram.botName}") private val botName: String,
    @Value("\${telegram.token}") private val token: String
    ) : TelegramLongPollingBot() {

    init {
        val api = TelegramBotsApi(DefaultBotSession::class.java)
        api.registerBot(this)
    }

    override fun getBotToken() = token
    override fun getBotUsername() = botName

    private var status = BotStatus.EXCEPT_COMMAND

    companion object {
        const val PROMPT_MAX_LENGTH = 1024
    }

    override fun onUpdateReceived(update: Update) {
        runBlocking {
            if (update.hasMessage()) {
                val message = update.message
                val chatId = message.chatId
                val responseText = if (message.hasText()) {
                    when (status) {
                        BotStatus.EXCEPT_COMMAND -> when (message.text) {
                            BotCommands.START.cmd -> {
                                status = BotStatus.EXCEPT_COMMAND
                                if (db.newUser(chatId)) "Welcome to SDpic bot!"
                                else "Welcome again!"
                            }
                            BotCommands.SET_PROMPT.cmd -> {
                                status = BotStatus.EXCEPT_PROMPT
                                "Now input the prompt for Stable Diffusion. You can use (_single_), ((_double_)) and even (((_triple_))) parentheses to amplify some part of input."
                            }
                            BotCommands.SET_NEGATIVE_PROMPT.cmd -> {
                                status = BotStatus.EXCEPT_NEGATIVE_PROMPT
                                "Now input the negative prompt for Stable Diffusion. You can use (_single_), ((_double_)) and even (((_triple_))) parentheses to amplify some part of input."
                            }
                           BotCommands.CREATE.cmd -> {
                                status = BotStatus.WAIT_FOR_RESULT
                                @Suppress("DeferredResultUnused")
                                async { createImage(chatId) }
                                "Please wait..."
                            }
                            else -> null
                        }
                        BotStatus.EXCEPT_PROMPT -> {
                            if (message.text.length <= PROMPT_MAX_LENGTH) {
                                status = BotStatus.EXCEPT_COMMAND
                                db.updateUserInput(chatId, InputType.PROMPT, message.text)
                                "*Prompt:* ${message.text}"
                            }
                            else "Please shorten the input and try again"
                        }
                        BotStatus.EXCEPT_NEGATIVE_PROMPT -> {
                            if (message.text.length <= PROMPT_MAX_LENGTH) {
                                status = BotStatus.EXCEPT_COMMAND
                                db.updateUserInput(chatId, InputType.NEGATIVE_PROMPT, message.text)
                                "*Negative Prompt:* ${message.text}"
                            }
                            else "Please shorten the input and try again"
                        }
                        BotStatus.WAIT_FOR_RESULT -> null
                    }
                } else null
                if (responseText != null) {
                    sendNotification(chatId, responseText)
                }
            }
        }
    }

    private suspend fun createImage(chatId: Long) {
        val img = sd.createImage(db.getUserInput(chatId), sd.txt2img)
        status = BotStatus.EXCEPT_COMMAND
        sendImage(chatId, img)
    }

    private fun sendNotification(chatId: Long, responseText: String) {
        SendMessage(chatId.toString(), responseText).apply {
            enableMarkdown(true)
            replyMarkup = makeReplyMarkup()
            execute(this)
        }
    }

    private fun sendImage(chatId: Long, responseImage: InputStream) {
        SendPhoto(chatId.toString(), InputFile(responseImage, "sdpic.png")).apply {
            replyMarkup = makeReplyMarkup()
            execute(this)
        }
    }

    private val replyMarkup = ReplyKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(BotCommands.SET_PROMPT.cmd, BotCommands.SET_NEGATIVE_PROMPT.cmd),
            listOf(BotCommands.CREATE.cmd)
        ).map { rowButtons ->
            val row = KeyboardRow()
            rowButtons.forEach { rowButton -> row.add(rowButton) }
            row
        }
        oneTimeKeyboard = true
    }

    private fun makeReplyMarkup() =
        if (status == BotStatus.EXCEPT_COMMAND) replyMarkup
        else null
}