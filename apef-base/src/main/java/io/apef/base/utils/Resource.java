package io.apef.base.utils;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

@Getter
@Accessors(fluent = true)
public class Resource {
    private String resourceUrl;
    private String resourcePath;
    private String resourceName;
    private String resourceParams;

    public Resource(String resourceUrl) {
        this.resourceUrl = resourceUrl;
        if (!StringUtils.isEmpty(this.resourceUrl)) {
            int index = StringUtils.lastIndexOf(this.resourceUrl, "/") + 1;
            if (index > 0) {
                this.resourcePath = StringUtils.substring(this.resourceUrl, 0, index);
            }
            if (this.resourceUrl.length() > index) {
                this.resourceName = StringUtils.substring(this.resourceUrl, index);
            }
            if (this.resourceName != null) {
                index = StringUtils.indexOf(this.resourceName, "?") + 1;
                if (index > 0) {
                    if (this.resourceName.length() == 1) {
                        this.resourceName = null;
                    } else
                        this.resourceParams = StringUtils.substring(this.resourceName, index);
                }
            }
        }
    }

}
