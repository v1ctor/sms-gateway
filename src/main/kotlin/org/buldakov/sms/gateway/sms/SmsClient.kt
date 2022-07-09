package org.buldakov.sms.gateway.sms

import org.buldakov.huawei.modem.model.Message

interface SmsClient {

    fun connect()

    fun getSms(): List<Message>

    fun deleteSms(indexes: List<Int>)
}