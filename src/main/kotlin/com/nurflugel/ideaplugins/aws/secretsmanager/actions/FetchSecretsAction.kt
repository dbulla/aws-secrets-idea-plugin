package com.nurflugel.ideaplugins.aws.secretsmanager.actions

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.Filter
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE_ARRAY
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.nurflugel.ideaplugins.aws.secretsmanager.Event
import com.nurflugel.ideaplugins.aws.secretsmanager.actions.GetSecretsAction.Companion.fetchAndWriteSecret
import com.nurflugel.ideaplugins.aws.secretsmanager.actions.PutSecretsAction.Companion.showResults
import java.io.File
import javax.swing.JOptionPane

/** This class does a batch fetch of all secrets starting with text supplied via a popup dialog */
class FetchSecretsAction : AnAction("Fetch AWS secrets with search") {

    private val log = Logger.getInstance(FetchSecretsAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val awsRegion = System.getenv("AWS_REGION") ?: "us-west-2"
        val files: Array<VirtualFile>? = e.dataContext.getData(VIRTUAL_FILE_ARRAY)
        if (files != null) {
            if (files.isNotEmpty()) {
                val events = mutableListOf<Event>()
                val virtualFile = files.first()
                val fileSystem: VirtualFileSystem = virtualFile.fileSystem
                val canonicalPath = virtualFile.canonicalPath!!
                val file = File(canonicalPath)
                val searchPattern: String = JOptionPane.showInputDialog(null, "Find secrets starting with what?");
                val secrets = getSecretIdsForWildcards(searchPattern, awsRegion)
                secrets.forEach { fetchAndWriteSecret(it, awsRegion, file.absolutePath, fileSystem, events) }
                showResults(events)

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
                    .withValues(text) // the matching AWS uses is not really specified...
            val listSecretsRequest = ListSecretsRequest()
                    .withFilters(filter)

            // We have to get results in batches, the token tells AWS what the next batch should start at. In this case, the null token means "start at the beginning"
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

    /** From the AWS call, we want to get a nice, clear list of secret IDs, and add it to the list of all results */
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
}
