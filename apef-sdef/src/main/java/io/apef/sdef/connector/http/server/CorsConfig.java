package io.apef.sdef.connector.http.server;

import io.apef.base.utils.ObjectFormatter;
import io.vertx.core.http.HttpMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class CorsConfig {
    @NotNull
    private String allowedOriginPattern="*";

    private List<String> allowedMethods = new ArrayList<>();

    private List<String> allowedHeaders = new ArrayList<>();

    public Set<HttpMethod> allowedMethods() {
        return this.allowedMethods.stream()
                .map(HttpMethod::valueOf).collect(Collectors.toSet());
    }

    public Set<String> allowedHeaders() {
        return this.allowedHeaders.stream().collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return ObjectFormatter.toString(this);
    }
}
