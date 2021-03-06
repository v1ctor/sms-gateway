package org.buldakov.sms.gateway.util

fun escapeMarkdownText(value: String): String {
    val chars = listOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')

    var result = value
    for (ch in chars) {
        result = result.replace("" + ch, "\\" + ch)
    }
    return result
}
