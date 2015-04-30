package org.optaplanner.core.config.score.director;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirectorFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Config class which holds the configuration of the Drools KJAR {@link ReleaseId} (Maven GAV).
 * <p/>
 * Provides a builder method to build a {@link DroolsScoreDirectorFactory} directly from a config instance.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
@XStreamAlias("scoreKJarReleaseId")
public class ScoreKJarReleaseIdConfig {

	@XStreamAlias("groupId")
	private String groupId;
	
	@XStreamAlias("artifactId")
	private String artifactId;
	
	@XStreamAlias("version")
	private String version;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	// ************************************************************************
    // Builder methods
    // ************************************************************************
	public DroolsScoreDirectorFactory build() {
		KieServices kieServices = KieServices.Factory.get();
		ReleaseId releaseId = kieServices.newReleaseId(getGroupId(), getArtifactId(), getVersion());
		KieContainer kieContainer = kieServices.newKieContainer(releaseId);
		KieBase kieBase = kieContainer.getKieBase();
		return new DroolsScoreDirectorFactory(kieBase);
	}
	
}
