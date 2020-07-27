package com.nurflugel.ideaplugins.aws.secretsmanager

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonToken.*
import com.google.gson.stream.MalformedJsonException
import java.io.Reader
import java.io.StringReader

/** Class to figure out if this is valid JSON or not */
class JsonValidator {

    companion object {

        /** Is this JSON? */
        private fun isJson(jsonReader: JsonReader): Boolean {
            return try {
                var token: JsonToken?
                loop@ while (jsonReader.peek().also { token = it } !== END_DOCUMENT && token != null) {
                    when (token) {
                        BEGIN_ARRAY -> jsonReader.beginArray()
                        END_ARRAY -> jsonReader.endArray()
                        BEGIN_OBJECT -> jsonReader.beginObject()
                        END_OBJECT -> jsonReader.endObject()
                        NAME -> jsonReader.nextName()
                        STRING, NUMBER, BOOLEAN, NULL -> jsonReader.skipValue()
                        END_DOCUMENT -> break@loop
                        else -> throw AssertionError(token)
                    }
                }
                true
            } catch (ignored: MalformedJsonException) {
                false
            }
        }

        private fun isJson(reader: Reader): Boolean {
            return isJson(JsonReader(reader))
        }

        fun isJson(json: String?): Boolean {
            return json != null && isJson(StringReader(json))
        }
    }
}
