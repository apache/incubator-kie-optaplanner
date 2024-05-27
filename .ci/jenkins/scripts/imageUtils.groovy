/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

containerEngine = 'docker'
containerTlsOptions = ''

void loginRegistry() {
    loginContainerRegistry(getOperatorImageRegistry(), getOperatorImageRegistryCredentials())
}

void loginContainerRegistry(String registry, String credsId) {
    echo "Using credentials ($credsId) to login to registry ($registry)"
    withCredentials([usernamePassword(credentialsId: credsId, usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PWD')]) {
        sh "${containerEngine} login ${containerTlsOptions} -u ${REGISTRY_USER} -p ${REGISTRY_PWD} ${registry}"
    }
}

String getOperatorImageRegistryCredentials() {
    return params.OPERATOR_IMAGE_REGISTRY_CREDENTIALS
}

String getOperatorImageRegistry() {
    return params.OPERATOR_IMAGE_REGISTRY
}

String getImageFullName(String namespace, String image, String tag) {
    return "${namespace}/${image}:$tag"
}

String getImageFullNameWithRegistry(String registry, String namespace, String image, String tag) {
    return "${registry}/${getImageFullName(namespace, image, tag)}"
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
    loginRegistry()
    retry(env.MAX_REGISTRY_RETRIES ?: 1) {
        sh "${containerEngine} push ${containerTlsOptions} ${image}"
    }
}

return this
