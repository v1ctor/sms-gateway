package org.buldakov.sms.gateway.core

import org.joda.time.DateTime

data class MessagePayload(val from: String, val text: String, val received: DateTime)