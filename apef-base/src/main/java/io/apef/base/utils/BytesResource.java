package io.apef.base.utils;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
public class BytesResource {

    private Bytes resourceUrl;
    private Bytes resourceUrlNoParams;
    private Bytes resourcePath;
    private Bytes resourceName;
    private Bytes resourceNameNoParams;
    private Bytes resourceParams;

    public BytesResource(Bytes resourceUrl) {
        if (resourceUrl != null) {
            this.resourceUrl = resourceUrl;
            this.init();
        }
    }

    public BytesResource(byte[] resourceUrl) {
        if (resourceUrl != null) {
            this.resourceUrl = Bytes.wrap(resourceUrl);
            this.init();
        }
    }

    public BytesResource(ByteBuf resourceUrl) {
        if (resourceUrl != null) {
            this.resourceUrl = Bytes.wrap(resourceUrl);
            this.init();
        }
    }

    public BytesResource(String resourceUrl) {
        if (resourceUrl != null) {
            this.resourceUrl = Bytes.wrap(Bytes.bytesOf(resourceUrl));
            this.init();
        }
    }

    private void init() {
        int index = this.resourceUrl.lastIndexOf('/') + 1;
        if (index > 0) {
            this.resourcePath = this.resourceUrl.left(index);
        }
        if (this.resourceUrl.length() > index) {
            this.resourceName = this.resourceUrl.right(index);
        }
        if (this.resourceName != null) {
            index = this.resourceName.indexOf('?') + 1;
            if (index > 0) {
                if (this.resourceName.length() == 1) {
                    this.resourceName = null;
                } else {
                    this.resourceParams = this.resourceName.right(index);
                    this.resourceNameNoParams = this.resourceName.left(index - 1);
                }
            } else {
                this.resourceNameNoParams = resourceName;
            }
        }
        int pos = this.resourceUrl.indexOf('?');
        if (pos >= 0) {
            this.resourceUrlNoParams = this.resourceUrl.left(pos);
        } else {
            this.resourceUrlNoParams = this.resourceUrl;
        }
    }
}
