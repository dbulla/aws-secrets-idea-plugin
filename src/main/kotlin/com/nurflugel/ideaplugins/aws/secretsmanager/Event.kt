package com.nurflugel.ideaplugins.aws.secretsmanager


/** Representation of an interaction with AWS, so we can show a nice summary dialog afterwards. */
data class Event(
        /** Did the event succeed or fail? */
        val isSuccess: Boolean,
        /** Whatever message we wan to show */
        val text: String
                ) {

    override fun toString(): String {
        return "     Success? $isSuccess: $text"
    }

}
