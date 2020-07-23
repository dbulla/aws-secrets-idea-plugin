package com.nurflugel.ideaplugins.aws.secretsmanager

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.*
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.nurflugel.ideaplugins.aws.secretsmanager.GetSecretsAction.Companion.fetchAndWriteSecret
import com.nurflugel.ideaplugins.aws.secretsmanager.SecretType.*
import java.io.File

class FetchSecretsAction : AnAction("Fetch a bunch of secrets with wildcards") {


    private val log = Logger.getInstance(FetchSecretsAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val awsRegion = System.getenv("AWS_REGION") ?: "us-west-2"
        val files: Array<VirtualFile>? = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        if(files !=null) {
            val virtualFile = files.first()
            if (virtualFile != null) {
                val canonicalPath = virtualFile.canonicalPath
                //todo get a popup to enter text
                val secrets = getSecretIdsForWildcards("cc-order", awsRegion)
                for (secret in secrets) {
                    val file = File(canonicalPath)
                    val parentDir = if (file.isDirectory) {
                        file
                    } else {
                        file.parentFile
                    }
                    val filepath = secret + "." + PROPERTIES.extension
                    val secretFile = File(parentDir, filepath)
                    fetchAndWriteSecret(secret, awsRegion, PROPERTIES, secretFile.absolutePath)
                }

            }
        }

    }


    /**
     * For the given secret ID, get the properties
     */
    private fun getSecretIdsForWildcards(text: String, awsRegion: String): List<String> {
        val client: AWSSecretsManager = AWSSecretsManagerClientBuilder.standard()
            .withRegion(awsRegion)
            .build()
        // $ aws secretsmanager list-secrets --filters '[{"Key":"description", "Values":["conducts"]}]' --query "SecretList[*].{SecretName:Name,Description:Description}"

        val filter: Filter = Filter().withKey("name")
            .withValues(text)
        val listSecretsResult: ListSecretsResult = try {
            val listSecretsRequest: ListSecretsRequest = ListSecretsRequest()
                .withFilters(filter)
            client.listSecrets(listSecretsRequest)
        } catch (e: Exception) {
            log.error("Error!", e)
            return mutableListOf()
        }
        listSecretsResult.secretList.forEach { println("secret name is  = ${it}") }
        val secretNames = listSecretsResult.secretList
            .map { it.name }

        for (secretName in secretNames) {
            println("secretName = ${secretName}")
        }
        return secretNames


    }


    /**
     * For the given secret ID, get the properties
     */
    private fun getSecretAsString(secretId: String, awsRegion: String): String? {
        val client: AWSSecretsManager = AWSSecretsManagerClientBuilder.standard()
            .withRegion(awsRegion)
            .build()
        val getSecretValueRequest: GetSecretValueRequest = GetSecretValueRequest()
            .withSecretId(secretId)
        val getSecretValueResult: GetSecretValueResult = client.getSecretValue(getSecretValueRequest)
        return getSecretValueResult.secretString
    }


}
