package org.buldakov.sms.gateway

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required

fun main(args: Array<String>) {
    val parser = ArgParser("sms-gateway")
    val authToken by parser.option(
        ArgType.String,
        shortName = "t",
        fullName = "token",
        description = "Telegram Bot Token"
    )
        .required()
    val dbConnection by parser.option(
        ArgType.String,
        shortName = "d",
        fullName = "db_connection",
        description = "DB connection string"
    ).required()
    val modemUrl by parser.option(
        ArgType.String,
        shortName = "m",
        fullName = "modem_url",
        description = "Modem url for the HTTP API"
    ).default("http://192.168.8.1")
    val username by parser.option(
        ArgType.String,
        shortName = "u",
        fullName = "username",
        description = "Huawei Modem HTTP API username."
    ).required()
    val password by parser.option(
        ArgType.String,
        shortName = "p",
        fullName = "password",
        description = "Huawei Modem HTTP API password."
    ).required()
    val users by parser.option(
        ArgType.String,
        shortName = "a",
        fullName = "allowed_users",
        description = "List of allowed users to receive updates"
    )
    parser.parse(args)

    val allowedUsers = users
        ?.split(",")
        ?.map { it.trim().lowercase() }
        ?.toHashSet() ?: hashSetOf()

    val service = SmsGatewayService(dbConnection, modemUrl, username, password, allowedUsers, authToken)
    service.start()
}