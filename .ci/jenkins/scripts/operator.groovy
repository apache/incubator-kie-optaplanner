containerEngine = 'docker'
containerTlsOptions = ''//'--tls-verify=false'

void loginRegistry() {
    loginContainerRegistry(getOperatorImageRegistry(), getOperatorImageRegistryCredentials())
}

String getOperatorImageRegistryCredentials() {
    return params.OPERATOR_IMAGE_REGISTRY_CREDENTIALS
}

String getOperatorImageRegistry() {
    return params.OPERATOR_IMAGE_REGISTRY
}

String getOperatorImageNamespace() {
    return params.OPERATOR_IMAGE_NAMESPACE
}

String getOperatorImageName() {
    return env.OPERATOR_IMAGE_NAME
}

String getOperatorImageTag() {
    return params.OPERATOR_IMAGE_TAG ?: getProjectVersion() ?: 'latest'
}

String getProjectVersion() {
    return params.PROJECT_VERSION
}

String getOperatorImageLocalName() {
    return "${getOperatorImageName()}:latest"
}

String getOperatorImageFullName(String tag = null) {
    String resolvedTag = tag ?: getOperatorImageTag()
    return "${getOperatorImageNamespace()}/${getOperatorImageName()}:$resolvedTag"
}

String getOperatorImageFullNameWithRegistry(String tag = null) {
    String operatorImageFullName = getOperatorImageFullName(tag)
    return "${getOperatorImageRegistry()}/${operatorImageFullName}"
}

String pushTemporaryImage(String temporaryImageTag) {
    String temporaryFullImageName = getOperatorImageFullNameWithRegistry(temporaryImageTag)
    tagImage(getOperatorImageLocalName(), temporaryFullImageName)

    loginRegistry()
    pushImage(temporaryFullImageName)
    return temporaryFullImageName
}

String getTemporaryImageTag() {
    return "${getOperatorImageTag()}-temporary"
}

void pushFinalImage(String temporaryImageTag) {
    loginRegistry()
    String temporaryImage = getOperatorImageFullNameWithRegistry(temporaryImageTag)
    pullImage(temporaryImage)

    String finalImageName = getOperatorImageFullNameWithRegistry()
    tagImage(temporaryImage, finalImageName)
    pushImage(finalImageName)
    String finalImageLatestName = getOperatorImageFullNameWithRegistry('latest')
    tagImage(finalImageName, finalImageLatestName)
    pushImage(finalImageLatestName)
}

void pullImage(String image) {
    retry(env.MAX_REGISTRY_RETRIES ?: 1) {
        sh "${containerEngine} pull ${containerTlsOptions} ${image}"
    }
}

void tagImage(String oldImage, String newImage) {
    sh "${containerEngine} tag ${oldImage} ${newImage}"
}

void pushImage(String image) {
    retry(env.MAX_REGISTRY_RETRIES ?: 1) {
        sh "${containerEngine} push ${containerTlsOptions} ${image}"
    }
}

void loginContainerRegistry(String registry, String credsId) {
    withCredentials([usernamePassword(credentialsId: credsId, usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PWD')]) {
        sh "${containerEngine} login ${containerTlsOptions} -u ${REGISTRY_USER} -p ${REGISTRY_PWD} ${registry}"
    }
}

boolean removeTemporaryQuayTag(String temporaryTag) {
    String namespace = getOperatorImageNamespace()
    String image = getOperatorImageName()
    echo "Removing a temporary image tag ${namespace}/${image}:${temporaryTag}"
    try {
        def output = 'false'
        withCredentials([usernamePassword(credentialsId: env.CLOUD_IMAGE_REGISTRY_CREDENTIALS_NIGHTLY, usernameVariable: 'QUAY_USER', passwordVariable: 'QUAY_TOKEN')]) {
            output = sh(returnStdout: true, script: "curl -H 'Content-Type: application/json' -H 'Authorization: Bearer ${QUAY_TOKEN}' -X DELETE https://quay.io/api/v1/repository/${namespace}/${repository}/tag/${temporaryTag} | jq '.success'").trim()
        }
        return output == 'true'
    } catch (err) {
        echo "[ERROR] Cannot remove a temporary image tag quay.io/${namespace}/${repository}:${temporaryTag}."
    }
}

return this