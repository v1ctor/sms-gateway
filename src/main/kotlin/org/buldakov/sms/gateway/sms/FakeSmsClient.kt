package org.buldakov.sms.gateway.sms

import org.buldakov.huawei.modem.model.Message

class FakeSmsClient : SmsClient {
    override fun connect() {}

    override fun getSms(): List<Message> = listOf()

    override fun deleteSms(indexes: List<Int>) {}
}