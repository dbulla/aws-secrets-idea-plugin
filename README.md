# aws-secrets-idea-plugin
AWS Secrets management plugin for JetBrains products.

## Installation
When this is published, you'll be able to access it via the JetBrains plugin portal.  Until then, you can download the plugin
[here](awssecretsmanagerplugin-0.0.1.zip) (alternately, you can clone the repo and run the `buildPlugin` task, then find the zip file in
build/distributions), open the IDEA plugins section, click on the little gear icon, and select "Install from disk" - 
then select the file you just downloaded.

## Usage
To use this, you MUST first have run "gimme-aws-creds" to talk to AWS and deal with secrets. 

Secrets stored as JSON will be written to disk with the format `secretname.json`, and secrets in properties format stored as `secretname.properties`.  The name of the secret in AWS for this cas would be `secretname`.

### Batch fetching
Batch fetching is nice because you can fetch all secrets matching a pattern.
 - First, select a dir you want the secrets to go into
 - From the `File` menu, select `Fetch AWS Secrets`.
 - A popup dialog will show, enter the starting text (i.e., if you wanted to fetch `dougs-dev`, `dougs-qa`,`dougs-stg` you would enter `dougs-`).  Wildcards are not supported
 - If you have access, the secrets will be downloaded to the selected dir, and added to the `.gitignore` in that dir.  If `.gitignore` does not exist, it will be created. (I have a todo to make that optional)
 - A status summary will show telling success/failure for each secret attempted

### Fetch selected secrets
This will fetch secrets based on the selected files.
 - First, select the file(s) you want to fetch secrets for.  If you select a file like `secretname-dev.properties`, the plugin will fetch the secret `secretname-dev`.
 - From the `File` menu, select `Fetch secrets for file(s)`.
 - If you have access, the secrets will be downloaded to the selected dir, and added to the `.gitignore` in that dir.  If `.gitignore` does not exist, it will be created. (I have a todo for that)
 - A status summary will show telling success/failure for each secret attempted

### Create/save selected secrets
Creating a new secret or saving an existing secret are identical
 - First, select the file(s) you want to save or create secrets for.  If you select a file like `secretname-dev.properties`, the plugin will save the secret as `secretnamedev`.
 - From the `File` menu, select `Fetch secrets for file(s)`.
 - If you have access, the secrets will be saved into AWS.
 - A status summary will show telling success/failure for each secret attempted

I tend to create a `secrets` dir in the project I'm working in, and deal with the secrets there.  Because they're added to the `.gitignore` file, we don't have to worry about checking them in.


## Todos
 - Make the `.gitignore` entry optional -  not urgent, cause it's harmless if you're not using Git
 - More robust file type checking, so anything not JSON or properties is  stored as a text blob

