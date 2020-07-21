package org.optaplanner.core.config.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Basic structure tests.
 */
public class ScanAnnotatedClassesConfigTest {

    private List<String> PACKAGE_INCLUDE_LIST = Collections.singletonList("pkg1");
    private List<String> PACKAGE_EXCLUDE_LIST = Collections.singletonList("pkg2");

    private ScanAnnotatedClassesConfig config;

    @Before
    public void setUp() {
        config = new ScanAnnotatedClassesConfig();
        config.setPackageIncludeList(PACKAGE_INCLUDE_LIST);
        config.setPackageExcludeList(PACKAGE_EXCLUDE_LIST);
    }

    @Test
    public void setPackageIncludeList() {
        List<String> packageIncludeList = Collections.singletonList("otherIncludedPackage");
        config.setPackageIncludeList(packageIncludeList);
        assertThat(config.getPackageIncludeList()).isEqualTo(packageIncludeList);
    }

    @Test
    public void setPackageExcludeList() {
        List<String> packageExcludeList = Collections.singletonList("otherExcludedPackage");
        config.setPackageExcludeList(packageExcludeList);
        assertThat(config.getPackageExcludeList()).isEqualTo(packageExcludeList);
    }

    @Test
    public void getPackageIncludeList() {
        assertThat(config.getPackageIncludeList()).isEqualTo(PACKAGE_INCLUDE_LIST);
    }

    @Test
    public void getPackageExcludeList() {
        assertThat(config.getPackageExcludeList()).isEqualTo(PACKAGE_EXCLUDE_LIST);
    }

    @Test
    public void inherit() {
        List<String> inheritedPackageIncludeList = Collections.singletonList("inherited_pkg1");
        List<String> inheritedPackageExcludeList = Collections.singletonList("inherited_pkg2");
        List<String> expectedPackageIncludeList = new ArrayList<>(PACKAGE_INCLUDE_LIST);
        List<String> expectedPackageExcludeList = new ArrayList<>(PACKAGE_EXCLUDE_LIST);
        expectedPackageIncludeList.addAll(inheritedPackageIncludeList);
        expectedPackageExcludeList.addAll(inheritedPackageExcludeList);

        ScanAnnotatedClassesConfig inheritedConfig = new ScanAnnotatedClassesConfig();
        inheritedConfig.setPackageIncludeList(expectedPackageIncludeList);
        inheritedConfig.setPackageExcludeList(expectedPackageExcludeList);
        assertFields(inheritedConfig, expectedPackageIncludeList, expectedPackageExcludeList);
    }

    @Test
    public void copyConfig() {
        ScanAnnotatedClassesConfig copyConfig = config.copyConfig();
        assertFields(copyConfig, config.getPackageIncludeList(), config.getPackageExcludeList());
    }

    @Test
    public void toStringTest() {
        String expectedValue = "getClass().getSimpleName() (" + PACKAGE_INCLUDE_LIST + ", " + PACKAGE_EXCLUDE_LIST + ")";
        assertThat(expectedValue).isEqualTo(expectedValue);
    }

    private static void assertFields(ScanAnnotatedClassesConfig config, List<String> expectedPackageIncludeList,
            List<String> expectedPackageExcludeList) {
        assertThat(config.getPackageIncludeList()).containsAll(expectedPackageIncludeList);
        assertThat(config.getPackageIncludeList().size()).isEqualTo(expectedPackageExcludeList.size());
        assertThat(config.getPackageExcludeList().size()).isEqualTo(expectedPackageIncludeList.size());
        assertThat(config.getPackageExcludeList()).containsAll(expectedPackageExcludeList);
    }
}
