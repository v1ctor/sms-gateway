package org.buldakov.sms.gateway.core

import org.buldakov.sms.gateway.db.Subscription
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class SubscriptionManager(private val allowedUsernames: Set<String>) {

    fun join(username: String, chatId: Long): Boolean {
        if (!checkAuth(username)) {
            return false
        }
        transaction {
            Subscription.insert {
                it[id] = chatId
            }
        }
        return true
    }

    fun leave(username: String, chatId: Long): Boolean {
        if (!checkAuth(username)) {
            return false
        }
        transaction {
            Subscription.deleteWhere { Subscription.id eq chatId }
        }
        return true
    }

    fun subscriptions(): Set<Long> {
        return transaction {
            Subscription.selectAll().map {
                it[Subscription.id].value
            }
        }.toHashSet()
    }


    private fun checkAuth(username: String) = allowedUsernames.contains(username.trim().lowercase())
}