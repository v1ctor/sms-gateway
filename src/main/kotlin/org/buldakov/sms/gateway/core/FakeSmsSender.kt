package org.buldakov.sms.gateway.core

import java.util.concurrent.BlockingQueue

class FakeSmsSender(private val messageQueue: BlockingQueue<MessagePayload>) {

    fun sendSms(from: String, text: String) {
        val message = MessagePayload(from, text)
        messageQueue.offer(message)
    }
}