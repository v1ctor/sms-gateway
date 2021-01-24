package org.buldakov.sms.gateway.core

import com.github.kotlintelegrambot.Bot
import org.buldakov.huawei.modem.model.Message
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

class MessageRouter(
    private val sessionManager: SessionManager,
    private val bot: Bot,
    private val messageQueue: BlockingQueue<Message>
) {
    private val executor = Executors.newSingleThreadExecutor()

    private fun waitingOnTheQueue() {
        while (true) {
            val message = messageQueue.take()
            sendMessage(message)
        }
    }

    private fun sendMessage(message: Message) {
        sessionManager.sessions.forEach { (_, chatId) ->
            bot.sendMessage(chatId, text = message.content)
        }
    }

    fun start() {
        executor.submit { waitingOnTheQueue() }
    }
}