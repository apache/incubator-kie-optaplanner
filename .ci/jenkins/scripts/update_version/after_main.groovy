void execute(def pipelinesCommon) {
    pipelinesCommon.getDefaultMavenCommand().withProperty('full').skipTests(params.SKIP_TESTS).run('clean install')
    if (pipelinesCommon.isRelease()) {
        updateAntoraYaml(pipelinesCommon)
    }
}

void updateAntoraYaml(def pipelinesCommon) {
    if (pipelinesCommon.isNotTestingBuild()) {
        echo 'updateAntoraYaml'
        sh './build/release/update_antora_yml.sh'
    } else {
        echo 'No updateAntoraYaml due to testing build'
    }
}

return this
