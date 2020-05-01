package io.apef.base.config.application;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class SystemInfo {
    private String osName = null;
    private String osVersion = null;
    private String userRegion = null;
    private String userTimezone = null;
    private String javaVendor = null;
    private String javaVendorUrl = null;
    private String javaVersion = null;

    public SystemInfo() {
        this.osName = System.getProperty("os.name");
        this.osVersion = System.getProperty("os.version");
        this.userRegion = System.getProperty("user.region");
        this.userTimezone = System.getProperty("user.timezone");
        this.javaVendor = System.getProperty("java.vendor");
        this.javaVendorUrl = System.getProperty("java.vendor.url");
        this.javaVersion = System.getProperty("java.version");
    }
}
