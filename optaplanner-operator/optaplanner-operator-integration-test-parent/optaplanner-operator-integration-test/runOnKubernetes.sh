#!/bin/bash

# The script setups environment for testing optaplanner-operator in Kubernetes.
#
# Before starting the script make sure that the Kubernetes cluster is running.

readonly BASEDIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" || exit; pwd -P)
readonly KEDA_NAMESPACE=keda
readonly OPTAPLANNER_OPERATOR_NAMESPACE=optaplanner-operator
readonly QUARKUS_CONTAINER_IMAGE_REGISTRY=quay.io
readonly QUARKUS_CONTAINER_IMAGE_GROUP=optaplanner
readonly ARTEMIS_NAMESPACE=artemis-operator

function install_keda() {
  kubectl apply -f "https://github.com/kedacore/keda/releases/download/v$KEDA_VERSION/keda-$KEDA_VERSION.yaml" -n "$KEDA_NAMESPACE"
}

function install_artemis_cloud() {
  local temp_directory=/tmp/artemiscloud
  rm -rf $temp_directory
  mkdir $temp_directory
  local archive_file="$temp_directory/activemq-artemis-operator-$ARTEMIS_CLOUD_VERSION.zip"
  wget -q -O $archive_file "https://github.com/artemiscloud/activemq-artemis-operator/archive/refs/tags/v$ARTEMIS_CLOUD_VERSION.zip"
  unzip -q -d $temp_directory $archive_file
  local DEPLOY_PATH="$temp_directory/activemq-artemis-operator-$ARTEMIS_CLOUD_VERSION/deploy/install"

  kubectl create namespace "$ARTEMIS_NAMESPACE"
  kubectl config set-context --current --namespace=$ARTEMIS_NAMESPACE

  kubectl apply -f $DEPLOY_PATH/010_crd_artemis.yaml
  kubectl apply -f $DEPLOY_PATH/020_crd_artemis_security.yaml
  kubectl apply -f $DEPLOY_PATH/030_crd_artemis_address.yaml
  kubectl apply -f $DEPLOY_PATH/040_crd_artemis_scaledown.yaml
  kubectl apply -f $DEPLOY_PATH/050_service_account.yaml
  kubectl apply -f $DEPLOY_PATH/060_cluster_role.yaml
  local SERVICE_ACCOUNT_NS="$(kubectl get -f $DEPLOY_PATH/050_service_account.yaml -o jsonpath='{.metadata.namespace}')"
  sed "s/namespace:.*/namespace: ${SERVICE_ACCOUNT_NS}/" $DEPLOY_PATH/070_cluster_role_binding.yaml | kubectl apply -f -
  kubectl apply -f $DEPLOY_PATH/080_election_role.yaml
  kubectl apply -f $DEPLOY_PATH/090_election_role_binding.yaml
  kubectl apply -f $DEPLOY_PATH/100_operator_config.yaml
  kubectl apply -f $DEPLOY_PATH/110_operator.yaml
  #  Make artemis-operator watching all awalible namespaces
  sed -e "/WATCH_NAMESPACE/,/- name/ { /WATCH_NAMESPACE/b; /valueFrom:/bx; /- name/b; d; :x s/valueFrom://}" $DEPLOY_PATH/110_operator.yaml | kubectl apply -f -
}

function install_optaplanner_operator() {
  cd "$BASEDIR"/../../../ || exit
  #  Create optaplanner-operator image
  mvn clean -am -pl :optaplanner-operator package -DskipTests -Doperator.image.build -Dquarkus.container-image.registry=$QUARKUS_CONTAINER_IMAGE_REGISTRY -Dquarkus.container-image.group=$QUARKUS_CONTAINER_IMAGE_GROUP -Dquarkus.container-image.tag=""

  local operator_distribution_directory_local="$BASEDIR"/../../optaplanner-operator/target/install
  #  Never pull optaplanner-operator image from remote
  sed -i "s/imagePullPolicy: Always/imagePullPolicy: Never/g" "$operator_distribution_directory_local"/optaplanner-operator.yml

  kubectl create namespace "$OPTAPLANNER_OPERATOR_NAMESPACE"
  kubectl apply -f "$operator_distribution_directory_local/crd-solver.yml" -n "$OPTAPLANNER_OPERATOR_NAMESPACE"
  kubectl apply -f "$operator_distribution_directory_local/optaplanner-operator.yml" -n "$OPTAPLANNER_OPERATOR_NAMESPACE"
}

function build_solver_image() {
  cd "$BASEDIR"/../school-timetabling-parent/ || exit
  mvn clean -am -pl :kubernetes-demo-school-timetabling package  -Dopenshift -Dquarkus.container-image.group=$QUARKUS_CONTAINER_IMAGE_GROUP -Dquarkus.container-image.registry=$QUARKUS_CONTAINER_IMAGE_REGISTRY -Dquarkus.container-image.build=true  -Dquarkus.container-image.tag="" -DskipTests -Dquarkus.container-image.push=false
}

function minikubeSetup() {
  #Allow minikube reuse docker images
  eval "$(minikube -p minikube docker-env)"
}

function setup_operators() {
  build_solver_image
  install_keda
  install_artemis_cloud
  install_optaplanner_operator
}

if [ -z "$1" ] || [ -z "$2" ]; then
    print_help
else
    ARTEMIS_CLOUD_VERSION=$1
    KEDA_VERSION=$2
    minikubeSetup
    setup_operators
fi

function print_help() {
  echo "Usage: ./runOnKubernetes.sh [ARTEMIS_CLOUD_VERSION] [KEDA_VERSION]"
  echo
  echo "Examples:"
  echo "./runOnKubernetes.sh 1.0.7 2.8.0  Configures minikube to share images from docker"
  echo "                                  Installs artemis, keda and optaplanner operators"
  echo "                                  Creates school-timetabling image"
}
