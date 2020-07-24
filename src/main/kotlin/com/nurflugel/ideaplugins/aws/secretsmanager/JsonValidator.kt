package com.nurflugel.ideaplugins.aws.secretsmanager

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonToken.*
import com.google.gson.stream.MalformedJsonException
import java.io.IOException
import java.io.Reader
import java.io.StringReader


class JsonValidator {

    companion object {

        @Throws(IOException::class)
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

        @Throws(IOException::class)
        private fun isJson(reader: Reader): Boolean {
            return isJson(JsonReader(reader))
        }


        @Throws(IOException::class)
        fun isJson(json: String?): Boolean {
            return isJson(StringReader(json))
        }
    }
}
