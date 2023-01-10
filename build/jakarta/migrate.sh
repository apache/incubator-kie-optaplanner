#!/bin/bash

script_dir_path=$(cd "$(dirname "${BASH_SOURCE[0]}")" || exit; pwd -P)

mvn_cmd="mvn ${BUILD_MVN_OPTS:-}"
optaplanner_file="${script_dir_path}/optaplanner-quarkus3.yaml"

# Install artifacts locally.
${mvn_cmd} clean install -Dquickly

# Run the recipe.
${mvn_cmd} org.openrewrite.maven:rewrite-maven-plugin:4.38.2:run \
  -Drewrite.configLocation="${optaplanner_file}" \
  -Drewrite.recipeArtifactCoordinates=org.optaplanner:optaplanner-migration:8.33.0-SNAPSHOT \
  -Drewrite.exclusions=optaplanner-operator/** \
  -Drewrite.activeRecipes=org.optaplanner.openrewrite.Quarkus3 \
  -Dfull \
  -Dquickly \

# Commit the changes.
git status
git add -u
git commit -m "Migrate to Quarkus 3 and Jakarta packages"