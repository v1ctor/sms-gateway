package org.buldakov.sms.gateway.core

import org.joda.time.DateTime
import java.util.concurrent.BlockingQueue

class FakeSmsSender(private val messageQueue: BlockingQueue<MessagePayload>) {

    fun sendSms(from: String, text: String) {
        val message = MessagePayload(from, text, DateTime.now())
        messageQueue.offer(message)
    }
}