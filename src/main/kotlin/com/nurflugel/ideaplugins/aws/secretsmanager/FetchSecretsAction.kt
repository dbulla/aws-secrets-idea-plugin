package com.nurflugel.ideaplugins.aws.secretsmanager

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.Filter
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.nurflugel.ideaplugins.aws.secretsmanager.GetSecretsAction.Companion.fetchAndWriteSecret
import java.io.File
import javax.swing.JOptionPane

class FetchSecretsAction : AnAction("Fetch a bunch of secrets with wildcards") {


    private val log = Logger.getInstance(FetchSecretsAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val awsRegion = System.getenv("AWS_REGION") ?: "us-west-2"
        val files: Array<VirtualFile>? = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        if (files != null) {
            if (files.isNotEmpty()) {
                val virtualFile = files.first()
                val fileSystem: VirtualFileSystem = virtualFile.fileSystem
                val canonicalPath = virtualFile.canonicalPath!!
                val file = File(canonicalPath)
                //todo get a popup to enter text
                val searchPattern: String = JOptionPane.showInputDialog(null, "What's the search string?");
                val secrets = getSecretIdsForWildcards(searchPattern, awsRegion)
                for (secret in secrets) {

                    fetchAndWriteSecret(secret, awsRegion, file.absolutePath, fileSystem)
                }
            } else {
                JOptionPane.showMessageDialog(null, "No file or dir was selected, where do we put the secrets?")
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

        val allSecretNames = mutableListOf<String>()
        try {
            val filter: Filter = Filter()
                .withKey("name")
                .withValues(text)
            val listSecretsRequest = ListSecretsRequest()
                .withFilters(filter)
            var nextToken = parseResultsForSecretNames(client, allSecretNames, null, listSecretsRequest)

            while (nextToken != null) {
                nextToken = parseResultsForSecretNames(client, allSecretNames, nextToken, listSecretsRequest)
            }

        } catch (e: Exception) {
            JOptionPane.showMessageDialog(null, "Error fetching secrets: " + e.message)
            log.error("Error!", e)
            println("Error!" + e.message)
            return mutableListOf()
        }
        return allSecretNames
    }

    private fun parseResultsForSecretNames(
        client: AWSSecretsManager,
        allSecretNames: MutableList<String>,
        nextToken: String?,
        listSecretsRequest: ListSecretsRequest
    ): String? {
        val request = when {
            nextToken != null -> {
                listSecretsRequest.withNextToken(nextToken)
            }
            else -> listSecretsRequest
        }
        val result = client.listSecrets(request)
        val secretNames = result.secretList.map { it.name }
        allSecretNames.addAll(secretNames)
        return result.nextToken
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
