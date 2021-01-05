import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

class JenkinsfileDeploy extends JenkinsPipelineSpecification {

    def Jenkinsfile = null

    void setup() {
        Jenkinsfile = loadPipelineScriptForTest('Jenkinsfile.deploy')
        explicitlyMockPipelineVariable('githubscm')
    }

	def '[Jenkinsfile.deploy] getFallbackBranch: no PR_TARGET_BRANCH' () {
		setup:
            Jenkinsfile.getBinding().setVariable('params', ['PR_TARGET_BRANCH' : ''])
		when:
			def branch = Jenkinsfile.getFallbackBranch('repo')
		then:
            branch == 'master'
	}

	def '[Jenkinsfile.deploy] getFallbackBranch: PR branch exists' () {
		setup:
            Jenkinsfile.getBinding().setVariable('params', ['PR_TARGET_BRANCH' : 'target-branch'])
            getPipelineMock('githubscm.getRepositoryScm')('repo', 'kiegroup', 'target-branch') >> 'branch'
		when:
			def branch = Jenkinsfile.getFallbackBranch('repo')
		then:
            branch == 'target-branch'
	}

	def '[Jenkinsfile.deploy] getFallbackBranch: PR branch doesn\'t exist' () {
		setup:
            Jenkinsfile.getBinding().setVariable('params', ['PR_TARGET_BRANCH' : 'target-branch'])
            getPipelineMock('githubscm.getRepositoryScm')('repo', 'kiegroup', 'target-branch') >> null
		when:
			def branch = Jenkinsfile.getFallbackBranch('repo')
		then:
            branch == 'master'
	}
}
