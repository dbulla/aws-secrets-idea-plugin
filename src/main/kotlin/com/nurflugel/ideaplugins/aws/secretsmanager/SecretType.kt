package com.nurflugel.ideaplugins.aws.secretsmanager

import com.google.gson.Gson
import java.io.StringReader
import java.util.*

/** What type of format is this secret stored as?  When retrieving, we'd like to store them locally
 * with the right file extension
 */
enum class SecretType(val extension: String?) {
    BLOB(null),
    PROPERTIES("properties"),
    JSON("json"),
    MISSING(null); // couldn't find it

    /** take the text and format as appropriate for the secreet type */
    fun format(secretAsString: String?): String {
        return when {
            secretAsString.isNullOrBlank() -> ""
            this == PROPERTIES             -> formatAsProperties(secretAsString)
            this == JSON                   -> formatAsJson(secretAsString)
            else                           -> secretAsString
        }
    }

    private fun formatAsJson(secretAsString: String): String {
        val gson=Gson()
        // todo do something
        return secretAsString
    }

    fun formatAsProperties(secretAsString: String): String {
        val properties = Properties()
        // create a new reader
        val reader = StringReader(secretAsString)
        properties.load(reader)

        //todo figure out how to align
        val formattedOutput = properties.toList()
            .map { it.first.toString() + " = " + it.second +"\n"}
            .joinToString ( separator=""){ it}

//        return formattedOutput
        return secretAsString
    }

    companion object {
        fun parseValue(extension: String?): SecretType {
            val values: Array<SecretType> = values()
            return values.firstOrNull { it.extension == extension }
                ?: BLOB
        }
    }
}
