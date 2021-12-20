void execute(def pipelinesCommon) {
    maven.mvnVersionsSet(pipelinesCommon.getDefaultMavenCommand(), pipelinesCommon.getOptaPlannerVersion(), !pipelinesCommon.isRelease())
    maven.mvnSetVersionProperty(pipelinesCommon.getDefaultMavenCommand(), 'version.org.kie.kogito', pipelinesCommon.getKogitoVersion())
}

return this
