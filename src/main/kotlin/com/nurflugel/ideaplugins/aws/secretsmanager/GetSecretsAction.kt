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
import com.intellij.openapi.vfs.VirtualFileSystem
import com.nurflugel.ideaplugins.aws.secretsmanager.SecretType.*
import org.apache.commons.io.FileUtils
import java.io.File
import javax.swing.JOptionPane

/** This class gets a secret by name */
class GetSecretsAction : AnAction("Get AWS secrets for selected files") {

    override fun actionPerformed(e: AnActionEvent) {

        val files: Array<VirtualFile>? = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)

        if (files != null) {
            val events = mutableListOf<Event>()
            for (file in files) {
                // get type of thing based on file name - blob, properties, or json?
                fetchAndWriteSecret(awsRegion, file, events)
                // fetch the secret based on stripped file name (no extensions
                // format the secret as per type - blob, properties or json
                // save to disk, with file name based on file type
            }
            val fileSystem = files[0].fileSystem
            fileSystem.refresh(false)
            JOptionPane.showMessageDialog(null, "Results: \n" + events.joinToString(separator = "\n") { it.toString() })
        } else {
            JOptionPane.showMessageDialog(null, "No files selected")
        }
    }


    companion object {
        val awsRegion = System.getenv("AWS_REGION") ?: "us-west-2"

        private val client: AWSSecretsManager = AWSSecretsManagerClientBuilder.standard()
                .withRegion(awsRegion)
                .build()

        fun fetchAndWriteSecret(
                awsRegion: String,
                file: VirtualFile,
                events: MutableList<Event>
                               ) {
            return fetchAndWriteSecret(
                    file.nameWithoutExtension,
                    awsRegion,
                    file.parent?.canonicalPath !!,
                    file.fileSystem,
                    events
                                      )
        }

        fun fetchAndWriteSecret(
                secretName: String,
                awsRegion: String,
                dirPath: String,
                fileSystem: VirtualFileSystem,
                events: MutableList<Event>
                               ) {
            val secretAsString = getSecretAsString(secretName, awsRegion, events)

            val isJson = JsonValidator.isJson(secretAsString)
            val isProperties = PropertiesValidator.isProperties(secretAsString)

            //todo keypair type
            val secretType = when {
                isJson                 -> JSON
                isProperties           -> PROPERTIES
                secretAsString == null -> MISSING
                else                   -> BLOB
            }
            if (secretType != MISSING) {
                val formattedText = secretType.format(secretAsString)

                val lines = formattedText.split("\n")
                val filePath = secretName + "." + secretType.extension

                val theFile = File(dirPath, filePath)
                log.info("Writing secret $secretName to ${theFile.absolutePath}")
                // todo add option to format based on file type?
                FileUtils.writeLines(theFile, lines)
                // refresh GUI
                fileSystem.refresh(false)
            }
        }

        /**
         * For the given secret ID, get the properties
         */
        private fun getSecretAsString(secretId: String, awsRegion: String, events: MutableList<Event>): String? {
            val secretString = try {

                val getSecretValueRequest: GetSecretValueRequest = GetSecretValueRequest()
                        .withSecretId(secretId)
                val getSecretValueResult: GetSecretValueResult = client.getSecretValue(getSecretValueRequest)
                events.add(Event(true, "Fetched secret for $secretId"))
                getSecretValueResult.secretString
            } catch (e: Exception) {
                events.add(Event(false, "Couldn't fetch secret for $secretId"))
                null
            }
            return secretString
        }

        private val log = Logger.getInstance(FetchSecretsAction::class.java)
    }
}
