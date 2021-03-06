package org.buldakov.sms.gateway

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import org.buldakov.huawei.modem.client.ModemClient
import org.buldakov.sms.gateway.core.*
import org.buldakov.sms.gateway.db.SmsMessage
import org.buldakov.sms.gateway.db.Subscription
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
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
    val dbConnection by parser.option(
        ArgType.String,
        shortName = "d",
        fullName = "db_connection",
        description = "DB connection string"
    ).required()
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


    Database.connect(dbConnection, "org.sqlite.JDBC")
    transaction {
        SchemaUtils.createMissingTablesAndColumns(SmsMessage, Subscription)
    }

    val client = ModemClient(modemUrl)
    client.login(username, password)

    val messageQueue = LinkedBlockingQueue<MessagePayload>()
    val smsPoller = SmsPoller(client, messageQueue)
    val fakeSmsSender = FakeSmsSender(messageQueue)


    val subscriptionManager = SubscriptionManager(setOf("handspringer"))
    val bot = bot {
        token = authToken
        logLevel = LogLevel.All()
        dispatch {
            command("join") {
                val telegramUsername = message.chat.username ?: return@command
                subscriptionManager.join(telegramUsername, message.chat.id)
                bot.sendMessage(ChatId.fromId(message.chat.id), text = "You've been added to the sms subscription.")
                update.consume()
            }
            command("leave") {
                val telegramUsername = message.chat.username ?: return@command
                subscriptionManager.leave(telegramUsername, message.chat.id)
                bot.sendMessage(ChatId.fromId(message.chat.id), text = "You've been removed from the sms subscription.")
                update.consume()
            }
            command("status") {
                update.consume()
            }
            text {
                fakeSmsSender.sendSms("Fake", text)
                update.consume()
            }
        }
    }
    val messageRouter = MessageRouter(subscriptionManager, bot, messageQueue)
    messageRouter.start()
    smsPoller.start()
    bot.startPolling()

}