package com.nurflugel.ideaplugins.aws.secretsmanager

import java.io.IOException
import java.io.StringReader
import java.util.*


class PropertiesValidator {

    companion object {
        @Throws(IOException::class)
        fun isProperties(text: String?): Boolean {
            val properties = Properties()
            return try {
                properties.load(StringReader(text))
                val size = properties.size
                println("size = $size")
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
