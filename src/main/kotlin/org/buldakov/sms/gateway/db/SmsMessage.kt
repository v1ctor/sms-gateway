package org.buldakov.sms.gateway.db

import org.jetbrains.exposed.dao.id.IntIdTable

object SmsMessage : IntIdTable() {
    var from = varchar("from", 50)
    var content = text("content")
    val date = long("date")
}