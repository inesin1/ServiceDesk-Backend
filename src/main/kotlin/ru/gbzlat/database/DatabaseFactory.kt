package ru.gbzlat.database

import org.ktorm.database.Database

class DatabaseFactory() {
    companion object {
        fun createConnection(
            hostname: String,
            port: String,
            db: String,
            username: String,
            password: String
        ): Database {
            val jdbcUrl = "jdbc:mysql://$hostname:$port/$db?allowPublicKeyRetrieval=true&useSSL=false"
            return Database.connect(
                url = jdbcUrl,
                user = username,
                password = password
            )
        }
    }

/*    init {
        val jdbcUrl = "jdbc:mysql://$hostname:$port/$db?allowPublicKeyRetrieval=true&useSSL=false"
        database = Database.connect(
            url = jdbcUrl,
            user = username,
            password = password
        )
    }*/
}