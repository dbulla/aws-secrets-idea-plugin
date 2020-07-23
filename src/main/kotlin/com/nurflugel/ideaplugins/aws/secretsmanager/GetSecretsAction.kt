package com.nurflugel.ideaplugins.aws.secretsmanager

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import org.apache.commons.io.FileUtils
import java.io.File

class GetSecretsAction : AnAction("Get AWS secrets for selected files") {

    override fun actionPerformed(e: AnActionEvent) {

        val awsRegion = System.getenv("AWS_REGION") ?: "us-west-2"
        val files: Array<VirtualFile>? = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)

        if (files != null) {
            for (file in files) {

                // get type of thing based on file name - blob, properties, or json?
                val extension = file.extension
                val secretType: SecretType = SecretType.parseValue(extension)
                val baseSecretName = file.nameWithoutExtension
                fetchAndWriteSecret(baseSecretName, awsRegion, secretType, file.canonicalPath!!)
                // fetch the secret based on stripped file name (no extensions
                // format the secret as per type - blob, properties or json
                // save to disk, with file name based on file type
            }
            val fileSystem = files[0].fileSystem
            fileSystem.refresh(false)
        }
    }


    companion object {
        fun fetchAndWriteSecret(
            baseSecretName: String,
            awsRegion: String,
            secretType: SecretType,
            filepath: String
        ) {
            val secretAsString = getSecretAsString(baseSecretName, awsRegion)
            val formattedText = secretType.format(secretAsString)
            val lines = formattedText.split("\n")
            log.info("Writing secret $baseSecretName to $filepath")
            val file = File(filepath)
            val absolutePath = file.absolutePath
            println("absolutePath = ${absolutePath}")
            FileUtils.writeLines(file, lines)
        }

        /**
         * For the given secret ID, get the properties
         */
        fun getSecretAsString(secretId: String, awsRegion: String): String? {
            val client: AWSSecretsManager = AWSSecretsManagerClientBuilder.standard()
                .withRegion(awsRegion)
                .build()
            val getSecretValueRequest: GetSecretValueRequest = GetSecretValueRequest()
                .withSecretId(secretId)
            val getSecretValueResult: GetSecretValueResult = client.getSecretValue(getSecretValueRequest)
            return getSecretValueResult.secretString
        }

        private val log = Logger.getInstance(FetchSecretsAction::class.java)
    }


}
