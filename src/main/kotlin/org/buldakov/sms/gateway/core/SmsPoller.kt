package org.buldakov.sms.gateway.core

import org.buldakov.huawei.modem.api.SmsApi
import org.buldakov.huawei.modem.client.ModemClient
import org.buldakov.huawei.modem.model.Message
import org.slf4j.LoggerFactory
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors

class SmsPoller(
    client: ModemClient,
    private val messageQueue: BlockingQueue<Message>,
    private val interval: Long = 1000
) {

    private val log = LoggerFactory.getLogger(SmsPoller::class.java)

    private val executor = Executors.newSingleThreadExecutor()
    private val stopped = false
    private val smsApi = SmsApi(client)

    private fun startPolling() {
        while (!stopped) {
            val sms = smsApi.getSms(amount = 20)
            sms.forEach { messageQueue.offer(it) }
            log.info("$sms")
            Thread.sleep(interval)
        }
    }

    fun start() {
        executor.submit { startPolling() }
    }
}