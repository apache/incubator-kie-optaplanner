void execute(def pipelinesCommon) {
    githubscm.findAndStageNotIgnoredFiles('pom.xml')
    githubscm.findAndStageNotIgnoredFiles('antora.yml')
}

return this
