@Library('jenkins-pipeline-shared-libraries')_

import org.kie.jenkins.MavenCommand

changeAuthor = env.ghprbPullAuthorLogin ?: CHANGE_AUTHOR
changeBranch = env.ghprbSourceBranch ?: CHANGE_BRANCH
changeTarget = env.ghprbTargetBranch ?: CHANGE_TARGET

optaplannerRepo = 'optaplanner'
quickstartsRepo = 'optaplanner-quickstarts'
kogitoRuntimesRepo = 'kogito-runtimes'
quarkusRepo = 'quarkus'

pipeline {
    agent {
        label 'kie-rhel7 && kie-mem16g'
    }
    tools {
        maven 'kie-maven-3.6.2'
        jdk 'kie-jdk11'
    }
    options {
        timestamps()
        timeout(time: getTimeoutValue(), unit: 'MINUTES')
    }
    environment {
        MAVEN_OPTS = '-Xms1024m -Xmx4g'
    }
    stages {
        stage('Initialize') {
            steps {
                script {
                    mailer.buildLogScriptPR()

                    checkoutRuntimesRepo()
                    checkoutOptaplannerRepo()
                    dir(quickstartsRepo) {
                        // If the PR to OptaPlanner targets the 'main' branch, we assume the branch 'development' for quickstarts.
                        String quickstartsChangeTarget = changeTarget == 'main' ? 'development' : getOptaplannerTargetBranch()
                        githubscm.checkoutIfExists(quickstartsRepo, changeAuthor, changeBranch, 'kiegroup', quickstartsChangeTarget, true)
                    }
                }
            }
        }
        stage('Build quarkus') {
            when {
                expression { return getQuarkusBranch() }
            }
            steps {
                script {
                    checkoutQuarkusRepo()
                    getMavenCommand(quarkusRepo, false)
                        .withProperty('quickly')
                        .run('clean install')
                }
            }
        }
        stage('Build Kogito Runtimes skipping tests') {
            steps {
                script {
                    getMavenCommand(kogitoRuntimesRepo)
                        .skipTests(true)
                        .withProperty('skipITs', true)
                        .run('clean install')
                }
            }
        }
        stage('Build OptaPlanner') {
            steps {
                script {
                    mvnCmd = getMavenCommand(optaplannerRepo, true, true)
                        .withProperty('full')
                    if (isNormalPRCheck() && isSonarCloudEnabled()) {
                        mvnCmd.withProfiles(['run-code-coverage'])
                    }
                    mvnCmd.run('clean install')
                }
            }
        }
        stage('Analyze OptaPlanner by SonarCloud') {
            when {
                expression { isNormalPRCheck() && isSonarCloudEnabled() }
            }
            steps {
                script {
                    withCredentials([string(credentialsId: 'SONARCLOUD_TOKEN', variable: 'SONARCLOUD_TOKEN')]) {
                        getMavenCommand(optaplannerRepo)
                                .withOptions(['-e', '-nsu'])
                                .withProperty('sonar.projectKey', 'org.optaplanner:optaplanner')
                                .withProfiles(['sonarcloud-analysis'])
                                .run('validate')
                    }
                }
            }
        }
        stage('Build OptaPlanner Quickstarts') {
            steps {
                script {
                    getMavenCommand(quickstartsRepo, true, true)
                        .run('clean install')
                }
            }
        }
    }
    post {
        always {
            sh '$WORKSPACE/trace.sh'
            junit '**/target/surefire-reports/**/*.xml, **/target/failsafe-reports/**/*.xml'
        }
        failure {
            script {
                mailer.sendEmail_failedPR()
            }
        }
        unstable {
            script {
                mailer.sendEmail_unstablePR()
            }
        }
        fixed {
            script {
                mailer.sendEmail_fixedPR()
            }
        }
        cleanup {
            script {
                // Clean also docker in case of usage of testcontainers lib
                util.cleanNode('docker')
            }
        }
    }
}

void checkoutRuntimesRepo() {
    dir(kogitoRuntimesRepo) {
        githubscm.checkoutIfExists(kogitoRuntimesRepo, changeAuthor, changeBranch, 'kiegroup', getKogitoTargetBranch(), true)
    }
}

void checkoutOptaplannerRepo() {
    dir(optaplannerRepo) {
        githubscm.checkoutIfExists(optaplannerRepo, changeAuthor, changeBranch, 'kiegroup', getOptaplannerTargetBranch(), true)
    }
}

void checkoutQuarkusRepo() {
    dir(quarkusRepo) {
        checkout(githubscm.resolveRepository(quarkusRepo, 'quarkusio', getQuarkusBranch(), false))
    }
}

String getKogitoTargetBranch() {
    return getTargetBranch(isUpstreamKogitoProject() ? 0 : -7)
}

String getOptaplannerTargetBranch() {
    return getTargetBranch(isUpstreamKogitoProject() ? 7 : 0)
}

String getTargetBranch(Integer addToMajor) {
    String targetBranch = changeTarget
    String [] versionSplit = targetBranch.split("\\.")
    if (versionSplit.length == 3
        && versionSplit[0].isNumber()
        && versionSplit[1].isNumber()
        && versionSplit[2] == 'x') {
        targetBranch = "${Integer.parseInt(versionSplit[0]) + addToMajor}.${versionSplit[1]}.x"
    } else {
        echo "Cannot parse changeTarget as release branch so going further with current value: ${changeTarget}"
        }
    return targetBranch
}

MavenCommand getMavenCommand(String directory, boolean addQuarkusVersion=true, boolean canNative = false) {
    mvnCmd = new MavenCommand(this, ['-fae'])
                .withSettingsXmlId('kogito_release_settings')
                .withSnapshotsDisabledInSettings()
                .withProperty('java.net.preferIPv4Stack', true)
                .inDirectory(directory)
    if (addQuarkusVersion && getQuarkusBranch()) {
        mvnCmd.withProperty('version.io.quarkus', '999-SNAPSHOT')
    }
    if (canNative && isNative()) {
        mvnCmd.withProfiles(['native'])
            .withProperty('quarkus.native.container-build', true)
            .withProperty('quarkus.native.container-runtime', 'docker')
            .withProperty('quarkus.profile', 'native') // Added due to https://github.com/quarkusio/quarkus/issues/13341
    }
    return mvnCmd
}

String getQuarkusBranch() {
    return env['QUARKUS_BRANCH']
}

boolean isNative() {
    return env['NATIVE'] && env['NATIVE'].toBoolean()
}

boolean isDownstreamJob() {
    return env['DOWNSTREAM_BUILD'] && env['DOWNSTREAM_BUILD'].toBoolean()
}

String getUpstreamTriggerProject() {
    return env['UPSTREAM_TRIGGER_PROJECT']
}

boolean isNormalPRCheck() {
    return !(isDownstreamJob() || getQuarkusBranch() || isNative())
}

boolean isSonarCloudEnabled() {
    return env['ENABLE_SONARCLOUD'] && env['ENABLE_SONARCLOUD'].toBoolean()
}

boolean isUpstreamKogitoProject() {
    return getUpstreamTriggerProject() && getUpstreamTriggerProject().startsWith('kogito')
}

boolean isUpstreamOptaplannerProject() {
    return getUpstreamTriggerProject() && getUpstreamTriggerProject() == 'optaplanner'
}

Integer getTimeoutValue() {
    return isNative() ? 240 : 120
}
