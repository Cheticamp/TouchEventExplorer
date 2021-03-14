package com.example.toucheventexplorer.utility

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.annotation.Nonnull

internal object Util {
    fun hash(@Nonnull toHash: String) =
        try {
            val digest = MessageDigest.getInstance("SHA-1")
            val array = digest.digest(toHash.toByteArray())
            StringBuilder().apply {
                array.forEach {
                    append(Integer.toHexString(it.toInt() and 0xFF))
                }
            }.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        }
}