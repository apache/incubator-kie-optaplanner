containerEngine = 'podman'
containerTlsOptions = '--tls-verify=false'

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

String pushTemporaryImage() {
    String temporaryTag = "${getOperatorImageTag()}-temporary"
    String temporaryFullImageName = getOperatorImageFullNameWithRegistry(temporaryTag)
    tagImage(getOperatorImageLocalName(), temporaryFullImageName)

    loginRegistry()
    pushImage(temporaryFullImageName)
    return temporaryFullImageName
}

void pushFinalImage(String temporaryImage) {
    loginRegistry()
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

return this