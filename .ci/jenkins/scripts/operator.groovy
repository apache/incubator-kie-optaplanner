containerEngine = 'podman'
containerTlsOptions = '--tls-verify=false'

void loginRegistry() {
    loginContainerRegistry(getOperatorImageRegistry(), getOperatorImageRegistryCredentials())
}

String getOperatorImageRegistryCredentials() {
    return params[constructKey('REGISTRY_CREDENTIALS')]
}

String getOperatorImageRegistry() {
    return params[operatorImageParamKey('registry')]
}

String getOperatorImageNamespace() {
    return params[operatorImageParamKey('namespace')]
}

String getOperatorImageName() {
    return params[operatorImageParamKey('name')]
}

String getOperatorImageTag() {
    return params[operatorImageParamKey('tag')] ?: getProjectVersion()
}

String getTemporaryImage() {
    return params[operatorImageParamKey('temporary_image')]
}

String operatorImageParamKey(String suffix) {
    return "operator.image.$suffix"
}

String getOperatorImageLocalName() {
    return "${getOperatorImageNamespace()}/${env.OPERATOR_IMAGE_NAME}:${getProjectVersion()}"
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
    pushImage(temporaryFullImageName)
    return temporaryFullImageName
}

void pushFinalImage() {
    String temporaryImageName = getTemporaryImage()

    loginRegistry()
    operator.pullImage(temporaryImageName)

    String finalImageName = getOperatorImageFullNameWithRegistry()
    tagImage(temporaryImageName, finalImageName)
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