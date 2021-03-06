package org.buldakov.sms.gateway.core

import org.buldakov.huawei.modem.api.SmsApi
import org.buldakov.huawei.modem.client.ModemClient
import org.buldakov.huawei.modem.model.Message
import org.buldakov.sms.gateway.db.SmsMessage
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors

class SmsPoller(
    client: ModemClient,
    private val messageQueue: BlockingQueue<MessagePayload>,
    private val interval: Long = 1000
) {

    private val log = LoggerFactory.getLogger(SmsPoller::class.java)

    private val executor = Executors.newSingleThreadExecutor()
    private val stopped = false
    private val smsApi = SmsApi(client)

    private fun startPolling() {
        while (!stopped) {
            val sms = smsApi.getSms(amount = 20)
            sms.forEach { processMessage(it) }
            log.info("$sms")
            if (sms.isNotEmpty()) {
                smsApi.deleteSms(sms.map { it.index })
            }
            Thread.sleep(interval)
        }
    }

    private fun processMessage(message: Message) {
        val now = DateTime.now()
        transaction {
            SmsMessage.insert {
                it[content] = message.content
                it[from] = message.phone
                it[date] = message.date.toDateTime().millis
                it[created] = now.millis
            }
            messageQueue.offer(MessagePayload(message.phone, message.content, now))
        }
    }

    fun start() {
        executor.submit { startPolling() }
    }
}