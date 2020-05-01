package io.apef.base.config.application;


import io.apef.base.config.utils.ConfigurationLoader;
import lombok.Data;

import java.util.List;

@Data
public class ApplicationInfo {
    private final static String APPLICATION_INF_FILE = "vex-application-inf.xml";
    private ApisConfig apis = new ApisConfig();
    private BuildInfo build = new BuildInfo();
    private ProjectInfo project = new ProjectInfo();
    private SystemInfo system = new SystemInfo();
    private List<NetworkInterfaceInfo> networkInterfaces = NetworkInterfaceInfo.NetworkInterfaces();

    public static ApplicationInfo instance() {
        return InstanceHolder.applicationInfo;
    }

    private static class InstanceHolder {
        private final static ApplicationInfo applicationInfo = init();

        private static ApplicationInfo init() {
            ApplicationInfo applicationInfo = ConfigurationLoader.loadConfiguration(
                    ApplicationInfo.class,
                    APPLICATION_INF_FILE);
            return applicationInfo;
        }

    }
}
