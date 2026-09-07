package ru.gbzlat.authentication

import at.favre.lib.crypto.bcrypt.BCrypt

private const val COST = 12

fun hashPassword(raw: String): String = BCrypt.withDefaults().hashToString(COST, raw.toCharArray())

fun verifyPassword(raw: String, hash: String): Boolean =
    BCrypt.verifyer().verify(raw.toCharArray(), hash).verified

/** Import files may carry either plaintext or hashes restored from a backup. */
fun hashIfPlaintext(value: String): String =
    if (value.startsWith("$2")) value else hashPassword(value)
