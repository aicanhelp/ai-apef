package io.apef.base.config.application;

import io.apef.testing.unit.BaseUnitSpec;
import io.apef.base.config.utils.ConfigurationLoader;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Test
public class ApplicationInfoTest extends BaseUnitSpec {

    public void testApplicationInfo() {
        System.setProperty(ConfigurationLoader.CONFIG_ENV_PREFIX, "project");
        System.setProperty("project.name", "TestProject");

        assertNotNull(ApplicationInfo.instance().getBuild());
        assertNotNull(ApplicationInfo.instance().getProject());

        assertEquals(ApplicationInfo.instance().getProject().getName(), "TestProject");

        ApplicationInfo applicationInfo1 = ApplicationInfo.instance();
        ApplicationInfo applicationInfo2 = ApplicationInfo.instance();

        //verify singleton instance
        assertEquals(applicationInfo1.hashCode(), applicationInfo2.hashCode());

        String jsonString = LegacyApplicationInfo.from(applicationInfo1).toJson();

        //Verify the rename the json key
        assertTrue(jsonString.contains("AppBuildNumber"));
    }
}