package io.apef.base.config.application;

import io.apef.base.utils.ObjectFormatter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;

import java.util.List;

@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class LegacyApplicationInfo {

    /**
     * The OS
     **/
    @JsonProperty("OsName")
    private String osName = null;
    /**
     * The OS Version
     **/
    @JsonProperty("OsVersion")
    private String osVersion = null;
    /**
     * The user region
     **/
    @JsonProperty("UserRegion")
    private String userRegion = null;
    /**
     * The user TimeZone
     **/
    @JsonProperty("UserTimezone")
    private String userTimezone = null;
    /**
     * The Java vendor
     **/
    @JsonProperty("JavaVendor")
    private String javaVendor = null;
    /**
     * The Java vendor's URL
     **/
    @JsonProperty("JavaVendorUrl")
    private String javaVendorUrl = null;
    /**
     * The Java version
     **/
    @JsonProperty("JavaVersion")
    private String javaVersion = null;

    /**
     * The network interfaces
     **/
    @JsonProperty("NetworkInterface")
    private List<NetworkInterfaceInfo> networkInterfaces;

    /**
     * The application name
     **/
    @JsonProperty("AppName")
    private String appName = null;
    /**
     * The application version
     **/
    @JsonProperty("AppVersion")
    private String appVersion = null;
    /**
     * The application artifact
     **/
    @JsonProperty("AppArtifact")
    private String appArtifact = null;
    /**
     * The application's build date
     **/
    @JsonProperty("AppBuildDate")
    private String appBuildDate = null;
    /**
     * The application's build number
     **/
    @JsonProperty("AppBuildNumber")
    private String appBuildNumber = null;

    /**
     * The Status
     **/
    @JsonProperty("Status")
    private String status = "OK";

    private LegacyApplicationInfo(ApplicationInfo applicationInfo) {
        this.networkInterfaces = applicationInfo.getNetworkInterfaces();
        this.osName = applicationInfo.getSystem().getOsName();
        this.osVersion = applicationInfo.getSystem().getOsVersion();
        this.userRegion = applicationInfo.getSystem().getUserRegion();
        this.userTimezone = applicationInfo.getSystem().getUserRegion();
        this.javaVendor = applicationInfo.getSystem().getJavaVendor();
        this.javaVendorUrl = applicationInfo.getSystem().getJavaVendorUrl();
        this.javaVersion = applicationInfo.getSystem().getJavaVersion();
        this.appArtifact = applicationInfo.getProject().getArtifact();
        this.appBuildDate = applicationInfo.getBuild().getDate();
        this.appName = applicationInfo.getProject().getName();
        this.appVersion = applicationInfo.getProject().getVersion();
        this.appBuildNumber = applicationInfo.getBuild().getRevision();
    }

    public String toJson() {
        return ObjectFormatter.toString(this);
    }

    public String toString() {
        return this.toJson();
    }

    public static LegacyApplicationInfo from(ApplicationInfo applicationInfo) {
        return new LegacyApplicationInfo(applicationInfo);
    }
}
