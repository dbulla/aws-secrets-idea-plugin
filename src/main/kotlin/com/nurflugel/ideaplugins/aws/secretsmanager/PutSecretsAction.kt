package com.nurflugel.ideaplugins.aws.secretsmanager

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest
import com.amazonaws.services.secretsmanager.model.PutSecretValueRequest
import com.amazonaws.services.secretsmanager.model.PutSecretValueResult
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import org.apache.commons.io.FileUtils
import java.io.File

class PutSecretsAction : AnAction("Save AWS secrets files") {

    private val log = Logger.getInstance(FetchSecretsAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {

        val awsRegion = System.getenv("AWS_REGION") ?: "us-west-2"
        val files: Array<VirtualFile>? = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)

        if (files != null) {
            val events = mutableListOf<Event>()
            for (file in files) {

                // get type of thing based on file name - blob, properties, or json?
                val extension = file.extension
                val secretType: SecretType = SecretType.parseValue(extension)
                val baseSecretName = file.nameWithoutExtension

                when (secretType) {
                    SecretType.PROPERTIES -> {
                        val secretAsString = readSecretsFromPropertiesFile(file)
                        writeToAwsSecretsProperties(baseSecretName, awsRegion, secretAsString, events)
                    }
                    SecretType.JSON -> {
                        TODO("Not implemented yet")
                    }
                    else -> {
                        TODO("Not implemented yet")
                    }
                }

//                val formattedText=secretType.format(secretAsString)
                // fetch the secret based on stripped file name (no extensions
                // format the secret as per type - blob, properties or json
                // save to disk, with file name based on file type
            }
        }
    }

    private fun readSecretsFromPropertiesFile(file: VirtualFile): String {
        val canonicalPath = file.canonicalPath !!
        val lines = FileUtils.readLines(File(canonicalPath)) // dgbtodo format nicely
                .joinToString("\n")
        return lines
    }

    private fun writeToAwsSecretsProperties(
            secretName: String,
            awsRegion: String,
            secretAsString: String,
            events: MutableList<Event>
                                           ) {
        val client: AWSSecretsManager = AWSSecretsManagerClientBuilder.standard()
                .withRegion(awsRegion)
                .build()
        val request: PutSecretValueRequest = PutSecretValueRequest()
                .withSecretId(secretName)
                .withSecretString(secretAsString)
        try {
            val result: PutSecretValueResult = client.putSecretValue(request)
            val versionId = result.versionId
            log.info("Wrote version $versionId of secret $secretName")
            val toString = result.toString()
            println("toString = $toString")
            events.add(Event(true, "Put secret $secretName"))
        } catch (e: Exception) {
            log.info("Got an error!", e)
            if (e is ResourceNotFoundException) {
                log.info("Trying to create the secret")
                val createSecretRequest: CreateSecretRequest = CreateSecretRequest()
                    .withName(secretName)
                    .withSecretString(secretAsString)
                try {
                    val secretResult = client.createSecret(createSecretRequest)
                    events.add(Event(true, "Created new secret $secretName"))
                } catch (e: Exception) {
                    log.error("Got an error creating the secret!", e)
                    events.add(Event(false, "Failed to Put/Create secret $secretName"))
                }
            }
        }
    }
}
