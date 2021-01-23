package org.buldakov.sms.gateway

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.logging.LogLevel
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import org.buldakov.huawei.modem.client.ModemClient
import org.buldakov.huawei.modem.model.Message
import org.buldakov.sms.gateway.core.MessageRouter
import org.buldakov.sms.gateway.core.SessionManager
import org.buldakov.sms.gateway.core.SmsPoller
import org.slf4j.LoggerFactory
import java.util.concurrent.LinkedBlockingQueue

private val log = LoggerFactory.getLogger("main")

fun main(args: Array<String>) {
    val parser = ArgParser("nfng-bot")
    val authToken by parser.option(
        ArgType.String,
        shortName = "t",
        fullName = "token",
        description = "Telegram Bot Token"
    )
        .required()
//    val dbConnection by parser.option(
//        ArgType.String,
//        shortName = "d",
//        fullName = "db_connection",
//        description = "DB connection string"
//    ).required()
    val modemUrl by parser.option(
        ArgType.String,
        shortName = "m",
        fullName = "modem_url",
        description = "Modem url for the HTTP API"
    ).default("http://192.168.8.1")
    val username by parser.option(
        ArgType.String,
        shortName = "u",
        fullName = "username",
        description = "Huawei Modem HTTP API username."
    ).required()
    val password by parser.option(
        ArgType.String,
        shortName = "p",
        fullName = "password",
        description = "Huawei Modem HTTP API password."
    ).required()
    parser.parse(args)


    val client = ModemClient(modemUrl)
    client.login(username, password)

    val messageQueue = LinkedBlockingQueue<Message>()
    val poller = SmsPoller(client, messageQueue)


    val sessionManager = SessionManager()
    val bot = bot {
        token = authToken
        logLevel = LogLevel.Error
        dispatch {
            command("join") {
                val telegramUsername = message.chat.username ?: return@command
                if (telegramUsername != "handspringer") {
                    return@command
                }
                sessionManager.join(telegramUsername, message.chat.id)
                bot.sendMessage(message.chat.id, text = "Joined")
            }
            command("leave") {
                bot.sendMessage(message.chat.id, text = "Left")
            }
        }
    }
    val messageRouter = MessageRouter(sessionManager, bot, messageQueue)
    messageRouter.start()
    poller.start()
    bot.startPolling()

}