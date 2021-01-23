package org.buldakov.sms.gateway.core

import java.util.concurrent.ConcurrentHashMap

class SessionManager {

    val sessions = ConcurrentHashMap<String, Long>()

    fun join(username: String, chatId: Long) {
        sessions[username] = chatId
    }

    fun leave(username: String) {
        sessions.remove(username)
    }
}