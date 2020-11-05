import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

class JenkinsfilePromote extends JenkinsPipelineSpecification {

	def '[Jenkinsfile.promote] test load script' () {
		when:
			def Jenkinsfile = loadPipelineScriptForTest('Jenkinsfile.promote')
		then:
			Jenkinsfile != null
	}
}
