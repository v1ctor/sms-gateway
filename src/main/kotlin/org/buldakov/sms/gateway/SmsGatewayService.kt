package org.buldakov.sms.gateway

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel
import org.buldakov.huawei.modem.client.ModemClient
import org.buldakov.sms.gateway.core.*
import org.buldakov.sms.gateway.db.SmsMessage
import org.buldakov.sms.gateway.db.Subscription
import org.buldakov.sms.gateway.sms.FakeSmsClient
import org.buldakov.sms.gateway.sms.HuaweiSmsClient
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.LinkedBlockingQueue

class SmsGatewayService(
    dbConnection: String,
    modemUrl: String,
    modemUsername: String?,
    modemPassword: String?,
    allowedTelegramUsers: Set<String>,
    telegramAuthToken: String
) {

    private var smsPoller: SmsPoller
    private val subscriptionManager: SubscriptionManager
    private val bot: Bot

    private val messageQueue = LinkedBlockingQueue<MessagePayload>()

    init {
        Database.connect(dbConnection, "org.sqlite.JDBC")
        transaction {
            SchemaUtils.createMissingTablesAndColumns(SmsMessage, Subscription)
        }
        val client = if (modemUsername != null && modemPassword != null) {
            HuaweiSmsClient(modemUrl, modemUsername, modemPassword)
        } else {
            FakeSmsClient()
        }
        client.connect()
        smsPoller = SmsPoller(client, messageQueue)
        val fakeSmsSender = FakeSmsSender(messageQueue)
        subscriptionManager = SubscriptionManager(allowedTelegramUsers)
        bot = bot {
            token = telegramAuthToken
            logLevel = LogLevel.Error
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
                    bot.sendMessage(
                        ChatId.fromId(message.chat.id),
                        text = "You've been removed from the sms subscription."
                    )
                    update.consume()
                }
                command("status") {
                    bot.sendMessage(ChatId.fromId(message.chat.id), "OK")
                    update.consume()
                }
                command("fake") {
                    val text = this.args.joinToString(separator = " ")
                    if (text.isBlank()) {
                        return@command
                    }
                    fakeSmsSender.sendSms("Fake", text)
                    update.consume()
                }
            }
        }
    }

    fun start() {
        val messageRouter = MessageRouter(subscriptionManager, bot, messageQueue)
        messageRouter.start()
        smsPoller.start()
        bot.startPolling()
    }
}