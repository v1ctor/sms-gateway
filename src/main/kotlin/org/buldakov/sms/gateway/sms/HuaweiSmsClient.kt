package org.buldakov.sms.gateway.sms

import org.buldakov.huawei.modem.api.SmsApi
import org.buldakov.huawei.modem.client.ModemClient
import org.buldakov.huawei.modem.model.Message

class HuaweiSmsClient(modemUrl: String,
                      private val modemUsername: String,
                      private val modemPassword: String): SmsClient {

    private val modemClient = ModemClient(modemUrl)
    private val smsApi = SmsApi(modemClient)

    override fun connect() {
        modemClient.login(modemUsername, modemPassword)
    }

    override fun getSms(): List<Message> = smsApi.getSms(amount = 20)

    override fun deleteSms(indexes: List<Int>) {
        smsApi.deleteSms(indexes)
    }
}