package com.nurflugel.ideaplugins.aws.secretsmanager


object Utils {


    /**
     * We get back all the values in one big string - need to break them up and parse them for properties maps
     */
    private fun parseSecretsString(
        secretName: String,
        secrets: String
    ): Map<String, String> {
        val props = mutableMapOf<String, String>()
        val lines = secrets.split("\n").toTypedArray()
        lines
            .map { obj: String -> obj.trim { it <= ' ' } }
            .filter { line -> line.isNotBlank() } // ignore empty lines
            .filter { line -> !line.startsWith("#") } // remove comments
            .sorted() // in order of the keys
            .forEach { line ->
                val keyValue = line.split("=") // split on the  "=" - all keys MUST have a value
                if (keyValue.size == 2) {
                    addKeyToMap(props, keyValue)
                } else {
                    throw RuntimeException("error parsing secret: " + secretName + " - Invalid number of tokens: " + keyValue.size + " , expected 2 in the text: `" + line + "`")
                }
            }
        return props
    }

    /**
     * Trim the key-value pair of all whitespace and add it to the map.
     */
    private fun addKeyToMap(
        props: MutableMap<String, String>,
        keyValue: List<String>
    ) {
        val key = keyValue[0].trim { it <= ' ' } // remove any leading/trailing whitespace
        val value = keyValue[1].trim { it <= ' ' }
        props[key] = value
    }
}
