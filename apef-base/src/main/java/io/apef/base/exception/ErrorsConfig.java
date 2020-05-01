package io.apef.base.exception;

import io.apef.base.utils.ObjectFormatter;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ErrorsConfig {
    private String configFile;

    public ErrorsConfig(){}

    public ErrorsConfig(String configFile){
        this.configFile=configFile;
    }

    public String toString() {
        return ObjectFormatter.toString(this);
    }
}
