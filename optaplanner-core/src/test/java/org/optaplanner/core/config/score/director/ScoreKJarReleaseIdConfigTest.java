package org.optaplanner.core.config.score.director;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.drools.compiler.CommonTestMethodBase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.io.Resource;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirectorFactory;

public class ScoreKJarReleaseIdConfigTest extends CommonTestMethodBase {
	
	private static final String KMODULE_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<kmodule xmlns=\"http://jboss.org/kie/6.0.0/kmodule\">"
			+ "</kmodule>";
	
	private static KieServices kieServices;
	
	//Simple rule that doesn't really do anything. Just for test.
	private static String drlOne = "package org.optaplanner.test;\n " +
		
				"import org.optaplanner.core.api.score.buildin.simple.SimpleScoreHolder;\n" +
				"import java.lang.String;\n" +
				
				"global SimpleScoreHolder scoreHolder;\n" +
				
				"rule \"SimpleConstraintRule\"\n" + 
				"when\n" +
					"$s:String(this==\"MY_CODE\")\n" + 
				"then\n" +
					"scoreHolder.addConstraintMatch(kcontext, -1);\n" +
				"end\n";
		
	private static String groupId = "org.optaplanner.rules.test";
	private static String artifactId = "test-rules";
	private static String version = "1.0.0";
	private static ReleaseId releaseId;
	
	@BeforeClass
	public static void setupTest() {
		kieServices = KieServices.Factory.get();
		releaseId  = kieServices.newReleaseId(groupId, artifactId, version);
		Resource drlOneResource = kieServices.getResources().newReaderResource(new StringReader(drlOne));
		drlOneResource.setTargetPath("rules.drl");
		createAndDeployJar(kieServices, KMODULE_CONTENT, releaseId, drlOneResource);
	}
	
	@Test
	public void testKJarAvailable() {
		
		ScoreKJarReleaseIdConfig config = new ScoreKJarReleaseIdConfig();
		config.setGroupId(groupId);
		config.setArtifactId(artifactId);
		config.setVersion(version);
		DroolsScoreDirectorFactory scoreDirectorFactory = config.build();
		
		KieBase kieBase = scoreDirectorFactory.getKieBase();
		List<String> ruleNames = new ArrayList<String>();
		Collection<KiePackage> kiePackages = kieBase.getKiePackages();
		for(KiePackage nextKiePackage: kiePackages) {
			Collection<Rule> rules = nextKiePackage.getRules();
			for(Rule nextRule: rules) {
				ruleNames.add(nextRule.getName());
			}
		}
		assertEquals("Expected only 1 rule in KieBase.", 1, ruleNames.size());
		assertEquals("Expected rule with this specific name in KieBase.", "SimpleConstraintRule", ruleNames.get(0));
	}
	
	@Test(expected=RuntimeException.class)
	public void testKJarNotAvailable() {
		ScoreKJarReleaseIdConfig config = new ScoreKJarReleaseIdConfig();
		config.setGroupId(groupId);
		config.setArtifactId(artifactId);
		//Use a non-existing version.
		config.setVersion("1.0.1");
		
		DroolsScoreDirectorFactory scoreDirectorFactory = config.build();
	}

}
