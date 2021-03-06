package org.buldakov.sms.gateway.core

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import org.buldakov.sms.gateway.util.escapeMarkdownText
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors

class MessageRouter(
    private val subscriptionManager: SubscriptionManager,
    private val bot: Bot,
    private val messageQueue: BlockingQueue<MessagePayload>
) {
    private val executor = Executors.newSingleThreadExecutor()

    private fun waitingOnTheQueue() {
        while (true) {
            val message = messageQueue.take()
            sendMessage(message)
        }
    }

    private fun sendMessage(message: MessagePayload) {
        subscriptionManager.subscriptions().forEach { chatId ->
            bot.sendMessage(ChatId.fromId(chatId), text = messageMarkdown(message), ParseMode.MARKDOWN_V2)
        }
    }

    fun start() {
        executor.submit { waitingOnTheQueue() }
    }

    private fun messageMarkdown(message: MessagePayload): String {
        return "*From:* ${escapeMarkdownText(message.from)}\n\n" +
                escapeMarkdownText(message.text)
    }
}