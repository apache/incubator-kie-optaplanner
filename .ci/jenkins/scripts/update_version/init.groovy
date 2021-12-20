void execute(def pipelinesCommon) {
    echo 'Hello from init script'
    if (pipelinesCommon.isRelease() || pipelinesCommon.isCreatePR()) {
        // Verify version is set
        assert pipelinesCommon.getKogitoVersion()
        assert pipelinesCommon.getOptaPlannerVersion()

        if (pipelinesCommon.isRelease()) {
            // Verify if on right release branch
            assert pipelinesCommon.getGitBranch() == util.getReleaseBranchFromVersion(pipelinesCommon.getOptaPlannerVersion())
        }
    }
}

return this
