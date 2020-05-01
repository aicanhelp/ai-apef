package io.apef.base.config.application;

import lombok.Data;

@Data
public class ApisConfig {
    private String httpServer = "default";
    private String restContext = "/appStatus";
}
