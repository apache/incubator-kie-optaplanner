import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

class JenkinsfileDeploy extends JenkinsPipelineSpecification {
	
	def '[Jenkinsfile.deploy] test load script' () {
		when:
			def Jenkinsfile = loadPipelineScriptForTest('Jenkinsfile.promote')
		then:
			Jenkinsfile != null
	}
}
