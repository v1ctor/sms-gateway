package org.buldakov.sms.gateway.db

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object Subscription : IdTable<Long>() {
    override val id: Column<EntityID<Long>> = long("telegramChatId").entityId()
}