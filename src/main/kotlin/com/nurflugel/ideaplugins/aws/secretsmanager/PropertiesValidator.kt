package com.nurflugel.ideaplugins.aws.secretsmanager

import java.io.IOException
import java.io.StringReader
import java.util.*

/** class to determine if something is a valid properties file or not */
class PropertiesValidator {

    companion object {
        @Throws(IOException::class)
        fun isProperties(text: String?): Boolean {
            if (text == null)
                return false
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
